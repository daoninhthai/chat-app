package com.daoninhthai.chatapp.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TypingEvent {

    private String username;
    private Long roomId;
    private boolean isTyping;
}
