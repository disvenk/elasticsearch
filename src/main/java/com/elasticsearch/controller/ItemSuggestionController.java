package com.elasticsearch.controller;


import com.elasticsearch.indexDao.ItemRepository;
import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.suggest.Suggest;
import org.elasticsearch.search.suggest.SuggestBuilder;
import org.elasticsearch.search.suggest.completion.CompletionSuggestion;
import org.elasticsearch.search.suggest.completion.CompletionSuggestionBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.elasticsearch.core.ElasticsearchTemplate;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Pattern;

@RestController
@RequestMapping("itemSuggest")
public class ItemSuggestionController {

    @Autowired
    ElasticsearchTemplate elasticsearchTemplate;

    @RequestMapping("suggest")
    public Set<String> suggest(String text){
        String index = "item";
        String type = "docs";
//        QueryBuilder queryBuilder = QueryBuilders.matchAllQuery();
        QueryBuilder queryBuilder = QueryBuilders.termsQuery("title",text);
        //String text = "苹";
        String field = "title";

        if(checkLetter(text)) {
    	    field = "title.keyword_pinyin";
        } else if(checkChinese(text)) {
    	    field = "title";
        } else {
    	    field = "title.keyword_pinyin";
        }

        Set<String> results = getSuggestWord(index, type, field, text, queryBuilder);
        //结果为空且是拼音，可以尝试拼音首字母提示
        if(results.size() == 0 && checkLetter(text)) {
            field = "title.keyword_first_py";
            results = getSuggestWord(index, type, field, text, queryBuilder);
        }

        for (String result : results) {
            System.out.println(result);
        }

        return results;
    }

    /**
     * Description:提示词，支持中文、拼音、首字母等(注意要去掉_source信息)
     *
     * 1、检测搜索词是中文还是拼音
     * 2、若是中文，直接按照name字段提示
     * 3、若是拼音（拼音+汉字），先按照name.keyword_pinyin获取，若是无结果按照首字母name.keyword_first_py获取
     *
     * SearchRequestBuilder的size要设置为0，否则显示hits结果
     * searchRequestBuilder.setSize(0);
     *
     * _source 由于磁盘读取和网络传输开销，可以影响性能的大小，为了节省一些网络开销，请从_source 使用源过滤中过滤掉不必要的字段以最小化 _source大小
     * 可以采用过滤的形式，也可以直接不显示_source
     * 1、searchRequestBuilder.setFetchSource("name", null);     过滤形式
     * 2、searchRequestBuilder.setFetchSource(false)   直接不显示_source
     *
     * @author wangweidong
     * CreateTime: 2018年6月28日 下午2:39:47
     *
     * @param index
     * @param type
     * @param field
     * @param text
     * @return
     */
    public  Set<String> getSuggestWord(String index, String type, String field, String text, QueryBuilder queryBuilder) {
        //过滤相同的提示词，Es5.2版本不支持过滤掉重复的建议，故需自己对ES返回做去重处理，Es6.1以上版本可以通过skip_duplicates字段处理，skip_duplicates表示是否应过滤掉重复的建议（默认为false）
        Set<String> results = new TreeSet<String>();
        CompletionSuggestionBuilder suggestionBuilder = new CompletionSuggestionBuilder(field);
        suggestionBuilder.text(text);
        suggestionBuilder.size(100);//返回100条数据

        SuggestBuilder suggestBuilder = new SuggestBuilder();
        suggestBuilder.addSuggestion("my-suggest-1", suggestionBuilder);

        SearchRequestBuilder searchRequestBuilder = elasticsearchTemplate.getClient().prepareSearch(index).setTypes(type);
        //searchRequestBuilder.setExplain(true);
        searchRequestBuilder.setFrom(0).setSize(10);
        searchRequestBuilder.setQuery(queryBuilder);
        searchRequestBuilder.suggest(suggestBuilder);
        searchRequestBuilder.setFetchSource(true);
           	searchRequestBuilder.setFetchSource(new String[]{"id","title"},null);

        SearchResponse resp = searchRequestBuilder.execute().actionGet();
        SearchHit[] hits = resp.getHits().getHits();

        for(SearchHit hit :hits){
            Long id = Long.parseLong(hit.getSource().get("id").toString()) ;
            String name = hit.getSource().get("title").toString();
            System.out.println("id："+id+"----"+"name："+name);
        }
        Suggest sugg = resp.getSuggest();
        CompletionSuggestion suggestion = sugg.getSuggestion("my-suggest-1");
        List<CompletionSuggestion.Entry> list = suggestion.getEntries();
        for (int i = 0; i < list.size(); i++) {
            List<? extends Suggest.Suggestion.Entry.Option> options = list.get(i).getOptions();
            for (Suggest.Suggestion.Entry.Option op : options) {
                results.add(op.getText().toString());
            }
        }
        return results;
    }

    /**
     * 只包含字母
     * @return 验证成功返回true，验证失败返回false
     */
    public static boolean checkLetter(String cardNum) {
        String regex = "^[A-Za-z]+$";
        return Pattern.matches(regex, cardNum);
    }

    /**
     * 验证中文
     * @param chinese 中文字符
     * @return 验证成功返回true，验证失败返回false
     */
    public static boolean checkChinese(String chinese) {
        String regex = "^[\u4E00-\u9FA5]+$";
        return Pattern.matches(regex,chinese);
    }
}
