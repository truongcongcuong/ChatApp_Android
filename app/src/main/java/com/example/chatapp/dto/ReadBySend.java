package com.example.chatapp.dto;

import java.util.Date;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Builder
@AllArgsConstructor
@Data
public class ReadBySend {
    private String messageId;
    private String roomId;
    private String userId;
    private Date readAt;
}
