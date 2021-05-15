package com.daoninhthai.chatapp.service;

import com.daoninhthai.chatapp.dto.ChatMessageDto;
import com.daoninhthai.chatapp.dto.SearchRequest;
import com.daoninhthai.chatapp.dto.SearchResponse;
import com.daoninhthai.chatapp.entity.ChatMessage;
import com.daoninhthai.chatapp.repository.ChatMessageRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.stream.Collectors;

@Service
public class MessageSearchService {

    private static final Logger logger = LoggerFactory.getLogger(MessageSearchService.class);

    @Autowired
    private ChatMessageRepository chatMessageRepository;

    @Autowired
    private MessageService messageService;

    public SearchResponse searchMessages(String keyword, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("timestamp").descending());
        Page<ChatMessage> results = chatMessageRepository.searchByKeyword(keyword, pageable);
        return buildSearchResponse(results, page);
    }

    public SearchResponse searchInRoom(String keyword, Long roomId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("timestamp").descending());
        Page<ChatMessage> results = chatMessageRepository.searchByKeywordInRoom(keyword, roomId, pageable);
        return buildSearchResponse(results, page);
    }

    public SearchResponse searchByUser(String keyword, Long userId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("timestamp").descending());
        Page<ChatMessage> results = chatMessageRepository.searchByKeywordAndUser(keyword, userId, pageable);
        return buildSearchResponse(results, page);
    }

    public SearchResponse searchByDateRange(String keyword, LocalDateTime from, LocalDateTime to,
                                             int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("timestamp").descending());
        Page<ChatMessage> results = chatMessageRepository.searchByKeywordAndDateRange(keyword, from, to, pageable);
        return buildSearchResponse(results, page);
    }

    public SearchResponse advancedSearch(SearchRequest request) {
        Pageable pageable = PageRequest.of(request.getPage(), request.getSize(),
                Sort.by("timestamp").descending());

        Page<ChatMessage> results;

        if (request.getRoomId() != null && request.getFromDate() != null && request.getToDate() != null) {
            results = chatMessageRepository.searchByKeywordInRoomAndDateRange(
                    request.getQuery(), request.getRoomId(),
                    request.getFromDate(), request.getToDate(), pageable);
        } else if (request.getRoomId() != null) {
            results = chatMessageRepository.searchByKeywordInRoom(
                    request.getQuery(), request.getRoomId(), pageable);
        } else if (request.getUserId() != null) {
            results = chatMessageRepository.searchByKeywordAndUser(
                    request.getQuery(), request.getUserId(), pageable);
        } else if (request.getFromDate() != null && request.getToDate() != null) {
            results = chatMessageRepository.searchByKeywordAndDateRange(
                    request.getQuery(), request.getFromDate(), request.getToDate(), pageable);
        } else {
            results = chatMessageRepository.searchByKeyword(request.getQuery(), pageable);
        }

        return buildSearchResponse(results, request.getPage());
    }

    private SearchResponse buildSearchResponse(Page<ChatMessage> results, int page) {
        return SearchResponse.builder()
                .messages(results.getContent().stream()
                        .map(messageService::convertToDto)
                        .collect(Collectors.toList()))
                .totalResults(results.getTotalElements())
                .page(page)
                .totalPages(results.getTotalPages())
                .hasNext(results.hasNext())
                .hasPrevious(results.hasPrevious())
                .build();
    }
}
