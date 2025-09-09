package com.telephone.inventory.management.controller;

import com.common.models.model.PhoneRecordElastic;
import com.telephone.inventory.management.model.SearchRequest;
import com.telephone.inventory.management.service.SearchService;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(SearchController.class)
class SearchControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private SearchService searchService;

    @MockitoBean
    private ElasticsearchOperations elasticsearchOperations;

//    @Test
    void search_withDefaultParams_appliesDefaults() throws Exception {
        when(searchService.searchRecord(any())).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/search/query")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        ArgumentCaptor<SearchRequest> captor = ArgumentCaptor.forClass(SearchRequest.class);
        verify(searchService, times(1)).searchRecord(captor.capture());

        SearchRequest req = captor.getValue();
        assertThat(req.getPage()).isEqualTo(0);
        assertThat(req.getSize()).isEqualTo(10);
        assertThat(req.getSortOrder()).isEqualTo("desc");
        assertThat(req.getSortBy()).isEqualTo("eventTime");
    }

//    @Test
    void search_withCustomParams_passesThemCorrectly() throws Exception {
        PhoneRecordElastic mockRecord = new PhoneRecordElastic();
        when(searchService.searchRecord(any())).thenReturn(List.of(mockRecord));

        mockMvc.perform(get("/search/query")
                        .param("country", "IN")
                        .param("state", "KA")
                        .param("prefix", "91")
                        .param("status", "AVAILABLE")
                        .param("type", "MOBILE")
                        .param("page", "2")
                        .param("size", "5")
                        .param("sortOrder", "asc")
                        .param("sortBy", "country")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        ArgumentCaptor<SearchRequest> captor = ArgumentCaptor.forClass(SearchRequest.class);
        verify(searchService).searchRecord(captor.capture());

        SearchRequest req = captor.getValue();
        assertThat(req.getCountry()).isEqualTo("IN");
        assertThat(req.getState()).isEqualTo("KA");
        assertThat(req.getPrefix()).isEqualTo("91");
        assertThat(req.getStatus()).isEqualTo("AVAILABLE");
        assertThat(req.getType()).isEqualTo("MOBILE");
        assertThat(req.getPage()).isEqualTo(2);
        assertThat(req.getSize()).isEqualTo(5);
        assertThat(req.getSortOrder()).isEqualTo("asc");
        assertThat(req.getSortBy()).isEqualTo("country");
    }

//    @Test
    void search_withInvalidParams_appliesFallbacks() throws Exception {
        when(searchService.searchRecord(any())).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/search/query")
                        .param("page", "-5")
                        .param("size", "0")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        ArgumentCaptor<SearchRequest> captor = ArgumentCaptor.forClass(SearchRequest.class);
        verify(searchService).searchRecord(captor.capture());

        SearchRequest req = captor.getValue();
        assertThat(req.getPage()).isEqualTo(0);  // fallback
        assertThat(req.getSize()).isEqualTo(10); // fallback
    }
}

