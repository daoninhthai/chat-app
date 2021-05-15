package com.daoninhthai.chatapp.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SearchRequest {

    private String query;

    private Long roomId;

    private Long userId;

    private LocalDateTime fromDate;

    private LocalDateTime toDate;

    private int page = 0;

    private int size = 20;
}
