package com.example.chatapp.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Builder
@AllArgsConstructor
@Data
public class ReadByReceiver {
    private String messageId;
    private String oldMessageId;
    private String roomId;
    private UserProfileDto readByUser;
    private String readAt;
}
