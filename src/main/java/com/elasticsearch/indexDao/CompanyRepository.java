package com.elasticsearch.indexDao;

import com.elasticsearch.mode.Company;
import com.elasticsearch.mode.Item;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

import java.util.List;

public interface CompanyRepository extends ElasticsearchRepository<Company,Long> {

}
