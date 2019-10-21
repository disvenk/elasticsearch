package com.elasticsearch.controller;

import com.elasticsearch.indexDao.CompanyRepository;
import com.elasticsearch.mode.Company;
import com.elasticsearch.utils.ChineseToPinYinUtil;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.index.query.DisMaxQueryBuilder;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.TermQueryBuilder;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.elasticsearch.core.ElasticsearchTemplate;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.TimeUnit;

@RestController
@RequestMapping("company")
public class CompanyIndexController {

    @Autowired
    private CompanyRepository companyRepository;

    @Autowired
    ElasticsearchTemplate elasticsearchTemplate;

    //索引库名称
    private static final String INDEX = "company";
    //文档类型
    private static final String TYPE = "com";

    @RequestMapping("save")
    public String save(Long id,String name){
        Company company = new Company();
        company.setId(id);
        company.setName(name);
        companyRepository.save(company);
        return "success";
    }

        @RequestMapping("autoCompelet")
    public  Set<String> autoCompelet(String text){
        SearchResponse list = getList(text, 1, 10);
        SearchHit[] hits = list.getHits().getHits();
        Set<String> words = new TreeSet<>();
        for(SearchHit hit :hits){
            Long id = Long.parseLong(hit.getSource().get("id").toString()) ;
            String name = hit.getSource().get("name").toString();
            System.out.println("id:"+id+"----"+"name"+name);
            words.add(name);
        }
        return words;
    }

    public SearchResponse getList(String words, int pageNo, int pageSize){
        // 这个sourcebuilder就类似于查询语句中最外层的部分。包括查询分页的起始，
        // 查询语句的核心，查询结果的排序，查询结果截取部分返回等一系列配置
        SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();
        // 结果开始处
        sourceBuilder.from((pageNo-1)*pageSize);
        // 查询结果终止处
        sourceBuilder.size(pageSize);
        // 查询的等待时间
        sourceBuilder.timeout(new TimeValue(60, TimeUnit.SECONDS));
        //执行查询
        sourceBuilder.query(chineseAndPinYinSearch(words));
        System.out.println(sourceBuilder);
        //指定索引库和类型
        SearchRequest searchRequest = new SearchRequest(INDEX);
        searchRequest.types(TYPE);
        searchRequest.source(sourceBuilder);
        try {
            return elasticsearchTemplate.getClient().search(searchRequest).get();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    //中文、拼音混合搜索
    private QueryBuilder chineseAndPinYinSearch(String words){

        //使用dis_max直接取多个query中，分数最高的那一个query的分数即可
        DisMaxQueryBuilder disMaxQueryBuilder= QueryBuilders.disMaxQuery();

        /**
         * 纯中文搜索，不做拼音转换,采用edge_ngram分词(优先级最高)
         * 权重* 5
         */
        QueryBuilder normSearchBuilder=QueryBuilders.matchQuery("name.ngram",words).analyzer("ngramSearchAnalyzer").boost(5f);

        /**
         * 拼音简写搜索
         * 1、分析key，转换为简写  case:  南京东路==>njdl，南京dl==>njdl，njdl==>njdl
         * 2、搜索匹配，必须完整匹配简写词干
         * 3、如果有中文前缀，则排序优先
         * 权重*1
         */
        String firstChar = ChineseToPinYinUtil.ToFirstChar(words);
        TermQueryBuilder pingYinSampleQueryBuilder = QueryBuilders.termQuery("name.SPY", firstChar);

        /**
         * 拼音简写包含匹配，如 njdl可以查出 "城市公牛 南京东路店"，虽然非南京东路开头
         * 权重*0.8
         */
        QueryBuilder  pingYinSampleContainQueryBuilder=null;
        if(firstChar.length()>1){
            pingYinSampleContainQueryBuilder=QueryBuilders.wildcardQuery("name.SPY", "*"+firstChar+"*").boost(0.8f);
        }

        /**
         * 拼音全拼搜索
         * 1、分析key，获取拼音词干   case :  南京东路==>[nan,jing,dong,lu]，南京donglu==>[nan,jing,dong,lu]
         * 2、搜索查询，必须匹配所有拼音词，如南京东路，则nan,jing,dong,lu四个词干必须完全匹配
         * 3、如果有中文前缀，则排序优先
         * 权重*1
         */
        QueryBuilder pingYinFullQueryBuilder=null;
        if(words.length()>1){
            pingYinFullQueryBuilder=QueryBuilders.matchPhraseQuery("name.FPY", words).analyzer("pinyiFullSearchAnalyzer");
        }


        /**
         * 完整包含关键字查询(优先级最低，只有以上四种方式查询无结果时才考虑）
         * 权重*0.8
         */
        QueryBuilder containSearchBuilder=QueryBuilders.matchQuery("name", words).analyzer("ikSearchAnalyzer").minimumShouldMatch("100%");

        disMaxQueryBuilder
                .add(normSearchBuilder)
                .add(pingYinSampleQueryBuilder)
                .add(containSearchBuilder);

        //以下两个对性能有一定的影响，故作此判定，单个字符不执行此类搜索
        if(pingYinFullQueryBuilder!=null){
            disMaxQueryBuilder.add(pingYinFullQueryBuilder);
        }
        if(pingYinSampleContainQueryBuilder!=null){
            disMaxQueryBuilder.add(pingYinSampleContainQueryBuilder);
        }

        return disMaxQueryBuilder;
    }

}
