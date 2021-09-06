package com.example.chatapp.dto;

import java.io.Serializable;
import java.util.Set;

import lombok.Data;

@Data
public class InboxDto implements Serializable {
    private String id;
    private RoomDTO room;
    private MessageDto lastMessage;
    private Set<ReadByDto> lastMessageReadBy;
    private Long countNewMessage;
}
