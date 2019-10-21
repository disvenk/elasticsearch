package com.elasticsearch.mode;

import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.*;

@Document(indexName = "company",type = "com")
@Setting(settingPath = "/settings/companySetting.json")
@Mapping(mappingPath = "/mappings/companyMapping.json")
public class Company {
    @Id
    @Field(index = false,store = true,type = FieldType.Long)
    private Long id;
    @Field( index = true,store = true,type = FieldType.Text ,analyzer = "ik_max_word",searchAnalyzer = "ik_max_word")
    private String name; //名称

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }


    @Override
    public String toString() {
        return "Item{" +
                "id=" + id +
                ", name='" + name + '\'' +
                '}';
    }
}
