package com.example.chatapp.dto;

import lombok.Data;

@Data
public class ReactionSend {
    private String messageId;
    private String roomId;
    private String userId;
    private String type;
}
