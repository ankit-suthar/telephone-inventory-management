package com.telephone.inventory.management.service;

import co.elastic.clients.elasticsearch._types.query_dsl.BoolQuery;
import com.common.models.model.PhoneRecordElastic;
import com.telephone.inventory.management.model.SearchRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.elasticsearch.client.elc.NativeQuery;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.query.Query;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SearchService {

    @Autowired
    private ElasticsearchOperations elasticsearchOperations;

    private static final Logger log = LoggerFactory.getLogger(SearchService.class);

    public List<PhoneRecordElastic> searchRecord(SearchRequest request) {
        BoolQuery.Builder boolQuery = new BoolQuery.Builder();

        if (request.getCountry() != null) {
            log.info("Country {}", request.getCountry());
            boolQuery.must(q -> q.term(t -> t.field("country").value(request.getCountry().trim().toLowerCase())));
        }

        if (request.getState() != null) {
            log.info("State {}", request.getState());
            boolQuery.must(q -> q.term(t -> t.field("state").value(request.getState().trim().toLowerCase())));
        }

        if (request.getType() != null) {
            log.info("type {}", request.getType());
            boolQuery.must(q -> q.term(t -> t.field("type").value(request.getType().trim().toLowerCase())));
        }

        if (request.getStatus() != null) {
            log.info("status {}", request.getStatus());
            boolQuery.must(q -> q.term(t -> t.field("status").value(request.getStatus().trim())));
        }

        if (request.getPrefix() != null) {
            String finalPrefix = "+" + request.getPrefix();
            log.info("e164Number {}", finalPrefix);
            boolQuery.must(q -> q.prefix(p -> p.field("e164Number").value(finalPrefix.trim())));
        }

        Sort sortObj = Sort.by(
                request.getSortOrder().equalsIgnoreCase("desc")
                        ? Sort.Direction.DESC
                        : Sort.Direction.ASC,
                request.getSortBy());

        Pageable pageable = PageRequest.of(request.getPage(), request.getSize(), sortObj);

        Query query = NativeQuery.builder()
                .withQuery(q -> q.bool(boolQuery.build()))
                .withPageable(pageable)
                .build();

        return elasticsearchOperations
                .search(query, PhoneRecordElastic.class)
                .stream()
                .map(SearchHit::getContent)
                .toList();
    }
}
