package com.example.chatapp.dto;

import com.example.chatapp.enumvalue.ReactionType;

import lombok.Data;

@Data
public class ReactionReceiver {
    private String messageId;
    private String roomId;
    private UserProfileDto reactByUser;
    private ReactionType type;
}
