package com.telephone.inventory.management.controller;

import com.common.models.model.PhoneRecordElastic;
import com.telephone.inventory.management.model.SearchRequest;
import com.telephone.inventory.management.service.SearchService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/search")
public class SearchController {

    @Autowired
    private ElasticsearchOperations elasticsearchOperations;

    @Autowired
    private SearchService searchService;

    private static final Logger log = LoggerFactory.getLogger(SearchController.class);

    @GetMapping("/query")
    public List<PhoneRecordElastic> search(@ModelAttribute SearchRequest request) {
        log.info("Search request received: {}", request);
        // Pagination defaults
        request.setPage(Math.max(request.getPage(), 0));
        request.setSize(request.getSize() > 0 ? request.getSize() : 10);

        // Sorting defaults
        request.setSortOrder(request.getSortOrder() != null ? request.getSortOrder() : "desc");
        request.setSortBy(request.getSortBy() != null ? request.getSortBy() : "eventTime");

        return searchService.searchRecord(request);
    }
}
