package com.example.chatapp.dto;

import com.example.chatapp.enumvalue.MessageType;

import java.io.Serializable;
import java.util.List;

import lombok.Data;

@Data
public class MessageSendToServer implements Serializable {
    private String roomId;
    private MessageType type;
    private String content;
    private String replyId;
    private List<MyMedia> media;
}
