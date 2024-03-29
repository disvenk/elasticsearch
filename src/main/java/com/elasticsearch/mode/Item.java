package com.elasticsearch.mode;

import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.*;

@Document(indexName = "item",type = "docs")
//@Setting(settingPath = "/settings/itemSetting.json")
//@Mapping(mappingPath = "/mappings/itemMapping.json")
public class Item {
    @Id
    @Field(index = false,store = true,type = FieldType.Long)
    private Long id;
    @Field( index = true,store = true,type = FieldType.Text ,analyzer = "ik_max_word",searchAnalyzer = "ik_max_word")
    private String title; //标题
    @Field(index = true,store = true,type = FieldType.Text,analyzer = "ik_max_word",searchAnalyzer = "ik_max_word")
    private String category;// 分类
    @Field(index = true,store = true,type = FieldType.Text,analyzer = "ik_max_word",searchAnalyzer = "ik_max_word")
    private String brand; // 品牌
    @Field(index = false,store = true,type = FieldType.Double)
    private Double price; // 价格
    @Field(index = false,store = true,type = FieldType.Keyword )
    private String images; // 图片地址

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getBrand() {
        return brand;
    }

    public void setBrand(String brand) {
        this.brand = brand;
    }

    public Double getPrice() {
        return price;
    }

    public void setPrice(Double price) {
        this.price = price;
    }

    public String getImages() {
        return images;
    }

    public void setImages(String images) {
        this.images = images;
    }


    @Override
    public String toString() {
        return "Item{" +
                "id=" + id +
                ", title='" + title + '\'' +
                ", category='" + category + '\'' +
                ", brand='" + brand + '\'' +
                ", price=" + price +
                ", images='" + images + '\'' +
                '}';
    }
}
