package com.daoninhthai.chatapp.controller;

import com.daoninhthai.chatapp.dto.SearchRequest;
import com.daoninhthai.chatapp.dto.SearchResponse;
import com.daoninhthai.chatapp.service.MessageSearchService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/search")
public class SearchController {

    @Autowired
    private MessageSearchService messageSearchService;

    @GetMapping("/messages")
    public ResponseEntity<SearchResponse> searchMessages(
            @RequestParam("q") String query,
            @RequestParam(value = "roomId", required = false) Long roomId,
            @RequestParam(value = "userId", required = false) Long userId,
            @RequestParam(value = "from", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
            @RequestParam(value = "to", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "20") int size) {

        if (query == null || query.trim().isEmpty()) {
            return ResponseEntity.badRequest().build();
        }

        SearchRequest searchRequest = SearchRequest.builder()
                .query(query.trim())
                .roomId(roomId)
                .userId(userId)
                .fromDate(from)
                .toDate(to)
                .page(page)
                .size(Math.min(size, 100))
                .build();

        SearchResponse response = messageSearchService.advancedSearch(searchRequest);
        return ResponseEntity.ok(response);
    }
}
