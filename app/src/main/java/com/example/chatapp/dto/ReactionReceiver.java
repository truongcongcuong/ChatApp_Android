package com.example.chatapp.dto;

import com.example.chatapp.enumvalue.ReactionType;

import java.io.Serializable;

import lombok.Data;

@Data
public class ReactionReceiver implements Serializable {
    private String messageId;
    private String roomId;
    private UserProfileDto reactByUser;
    private ReactionType type;
}
