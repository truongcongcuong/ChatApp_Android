package com.example.chatapp.dto;

import com.example.chatapp.enumvalue.MessageType;

import java.io.Serializable;

import lombok.Data;

@Data
public class MessageSendToServer implements Serializable {
    private String roomId;
    private MessageType type;
    private String content;
}
