package com.example.chatapp.dto;

import lombok.Data;

@Data
public class ReactionReceiver {
    private String messageId;
    private String roomId;
    private UserProfileDto reactByUser;
    private String type;
}
