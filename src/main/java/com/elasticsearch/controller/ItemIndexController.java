package com.elasticsearch.controller;

import com.elasticsearch.indexDao.ItemRepository;
import com.elasticsearch.mode.Item;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.sort.SortBuilders;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.data.elasticsearch.core.ElasticsearchTemplate;
import org.springframework.data.elasticsearch.core.SearchResultMapper;
import org.springframework.data.elasticsearch.core.aggregation.AggregatedPage;
import org.springframework.data.elasticsearch.core.aggregation.impl.AggregatedPageImpl;
import org.springframework.data.elasticsearch.core.query.NativeSearchQuery;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.UUID;

@RestController
@RequestMapping("item")
public class ItemIndexController {

    @Autowired
    ItemRepository itemRepository;

    @Autowired
    ElasticsearchTemplate elasticsearchTemplate;

    //新增一个对象
    @RequestMapping("/save")
    public String save(Long id,String title,String cate,String brand,double price){

            Item item = new Item();
            item.setId(id);
            item.setTitle(title);
            item.setCategory(cate);
            item.setBrand(brand);
            item.setPrice(price);
            itemRepository.save(item);


        return "success";
    }

    //按价格排序，查询所有的
    @RequestMapping("findAll")
    public List<Item> findAll(){
        Iterable<Item> price = this.itemRepository.findAll(Sort.by("price").ascending());
        List<Item> copy = new ArrayList<Item>();
        for (Item item:price){
            copy.add(item);
            System.out.println(item);
        }
        return copy;
    }


    @RequestMapping("findByPriceBetween")
    public List<Item> findByPriceBetween(double price1,double price2){
        List<Item> list = this.itemRepository.findByPriceBetween(price1, price2);
        for (Item item : list) {
            System.out.println(item);
        }
        return list;
    }

    @RequestMapping("matchQuery")
    public List<Item> findByPriceBetween(String field,String key,String key2){
        // 构建查询条件
        NativeSearchQueryBuilder queryBuilder = new NativeSearchQueryBuilder();
        // 添加基本分词查询
        queryBuilder.withQuery(QueryBuilders.matchPhrasePrefixQuery(field, key));
        queryBuilder.withSort(SortBuilders.fieldSort("").order(SortOrder.DESC));
        // 搜索，获取结果
        Page<Item> items = this.itemRepository.search(queryBuilder.build());
        // 总条数
        long total = items.getTotalElements();
        System.out.println("total = " + total);
        for (Item item : items) {
            System.out.println(item);
        }

        return items.getContent();
    }


    //功能更强大，除了匹配字符串以外，还可以匹配int/long/double/float/....
    @RequestMapping("termQuery")
    public List<Item> termQuery(String field,String key){
        // 构建查询条件
        NativeSearchQueryBuilder queryBuilder = new NativeSearchQueryBuilder();
        // 添加基本分词查询
        queryBuilder.withQuery(QueryBuilders.termQuery(field, key));
        // 搜索，获取结果
        Page<Item> items = this.itemRepository.search(queryBuilder.build());
        // 总条数
        long total = items.getTotalElements();
        System.out.println("total = " + total);
        for (Item item : items) {
            System.out.println(item);
        }

        return items.getContent();
    }

    //布尔查询
    /*
    * must: 文档必须完全匹配条件
    *should: should下面会带一个以上的条件，至少满足一个条件，这个文档就符合should
    *must_not: 文档必须不匹配条件
    * */
    @RequestMapping("booleanQuery")
    public List<Item> booleanQuery(){
        NativeSearchQueryBuilder builder = new NativeSearchQueryBuilder();

        builder.withQuery(
                QueryBuilders.boolQuery().must(QueryBuilders.matchQuery("title","荣耀3C"))
                        .must(QueryBuilders.matchQuery("brand","华为"))
        );

        // 查找
        Page<Item> page = this.itemRepository.search(builder.build());
        for(Item item:page){
            System.out.println(item);
        }
        return page.getContent();
    }

    //模糊查询
    @RequestMapping("fuzzyQuery")
    public List<Item> fuzzyQuery(String key){
        NativeSearchQueryBuilder builder = new NativeSearchQueryBuilder();
        builder.withQuery(QueryBuilders.fuzzyQuery("title",key));
        Page<Item> page = this.itemRepository.search(builder.build());
        for(Item item:page){
            System.out.println(item);
        }
        return page.getContent();
    }

    //分页查询
    @RequestMapping("searchByPage")
    public List<Item> searchByPage(String key){
        // 构建查询条件
        NativeSearchQueryBuilder queryBuilder = new NativeSearchQueryBuilder();
        // 添加基本分词查询
        queryBuilder.withQuery(QueryBuilders.termQuery("category", key));
        // 分页：
        int page = 0;
        int size = 1;
        queryBuilder.withPageable(PageRequest.of(page,size));

        // 搜索，获取结果
        Page<Item> items = this.itemRepository.search(queryBuilder.build());
        // 总条数
        long total = items.getTotalElements();
        System.out.println("总条数 = " + total);
        // 总页数
        System.out.println("总页数 = " + items.getTotalPages());
        // 当前页
        System.out.println("当前页：" + items.getNumber());
        // 每页大小
        System.out.println("每页大小：" + items.getSize());

        for (Item item : items) {
            System.out.println(item);
        }
        return items.getContent();
    }

    //高亮查询
    @RequestMapping("highLigthQuery")
    public List<Item> highLigthQuery(String field, String searchMessage){
        NativeSearchQuery searchQuery = new NativeSearchQueryBuilder()
                .withQuery(QueryBuilders.termQuery(field, searchMessage))
                .withHighlightFields(new HighlightBuilder.Field(field)//添加需要高亮的字段
                        .preTags("<em color='red'>")//可添加多个标签
                        .postTags("</em>"))
              //  .withHighlightFields(new HighlightBuilder.Field("brand")
              //          .preTags("<em color='bule'>")
             //           .postTags("</em>"))
                .build();
        Page<Item> page = elasticsearchTemplate.queryForPage(searchQuery, Item.class, new SearchResultMapper() {

            @Override
            public <T> AggregatedPage<T> mapResults(SearchResponse response, Class<T> clazz, Pageable pageable) {
                ArrayList<Item> items = new ArrayList<Item>();
                SearchHits hits = response.getHits();
                for (SearchHit searchHit : hits) {
                    if (hits.getHits().length <= 0) {
                        return null;
                    }
                    Item item = new Item();
                    String highLightMessage = searchHit.getHighlightFields().get(field).fragments()[0].toString();
                    item.setId(Long.parseLong(searchHit.getId()));
                    item.setTitle(String.valueOf(searchHit.getSource().get("title")));
                    item.setCategory(String.valueOf(searchHit.getSource().get("category")));
                    item.setBrand(String.valueOf(searchHit.getSource().get("brand")));
                    item.setPrice(Double.valueOf((Double) searchHit.getSource().get("price")));
                    // 反射调用set方法将高亮内容设置进去
                    try {
                        String setMethodName = parSetName(field);
                        Class<? extends Item> itemClazz = item.getClass();
                        Method setMethod = itemClazz.getMethod(setMethodName, String.class);
                        setMethod.invoke(item, highLightMessage);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    items.add(item);
                }
                if (items.size() > 0) {
                    return  new AggregatedPageImpl<T>((List<T>) items);
                }
                return null;
            }
        });
        List<Item> items = page.getContent();
        return items;
    }

    private static String parSetName(String fieldName) {
        if (null == fieldName || "".equals(fieldName)) {
            return null;
        }
        int startIndex = 0;
        if (fieldName.charAt(0) == '_')
            startIndex = 1;
        return "set" + fieldName.substring(startIndex, startIndex + 1).toUpperCase()
                + fieldName.substring(startIndex + 1);
    }

}
