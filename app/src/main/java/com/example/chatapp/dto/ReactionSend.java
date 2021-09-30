package com.example.chatapp.dto;

import com.example.chatapp.enumvalue.ReactionType;

import lombok.Data;

@Data
public class ReactionSend {
    private String messageId;
    private String roomId;
    private String userId;
    private ReactionType type;
}
