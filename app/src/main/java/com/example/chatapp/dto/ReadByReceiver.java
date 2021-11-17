package com.example.chatapp.dto;

import java.io.Serializable;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Builder
@AllArgsConstructor
@Data
public class ReadByReceiver implements Serializable {
    private String messageId;
    private String oldMessageId;
    private String roomId;
    private UserProfileDto readByUser;
    private String readAt;
}
