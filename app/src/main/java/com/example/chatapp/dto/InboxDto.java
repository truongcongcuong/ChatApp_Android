package com.example.chatapp.dto;

import java.io.Serializable;

import lombok.Data;

@Data
public class InboxDto implements Serializable {
    private String id;
    private RoomDTO room;
    private MessageDto lastMessage;
    private long countNewMessage;
}
