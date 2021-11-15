package com.daoninhthai.chatapp.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ThreadResponse {

    private ChatMessageDto parentMessage;

    private List<ChatMessageDto> replies;

    private int replyCount;

    private LocalDateTime lastReplyAt;
}
