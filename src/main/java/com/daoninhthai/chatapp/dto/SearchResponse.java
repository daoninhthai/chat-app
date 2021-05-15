package com.daoninhthai.chatapp.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SearchResponse {

    private List<ChatMessageDto> messages;

    private long totalResults;

    private int page;

    private int totalPages;

    private boolean hasNext;

    private boolean hasPrevious;
}
