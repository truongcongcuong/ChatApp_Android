package com.example.chatapp.dto;

import java.io.Serializable;

import lombok.Data;

@Data
public class MessageSendToServer implements Serializable {
    private String roomId;
    private String type;
    private String content;
}
