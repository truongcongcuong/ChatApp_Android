package com.example.chatapp.dto;

import com.example.chatapp.entity.Reaction;
import com.example.chatapp.enumvalue.MessageStatus;
import com.example.chatapp.enumvalue.MessageType;

import java.io.Serializable;
import java.util.List;
import java.util.Set;

import lombok.Data;

@Data
public class MessageDto implements Serializable {
    private String id;
    private String roomId;
    private UserProfileDto sender;
    private String createAt;
    private MessageType type;
    private String content;
    private boolean pin;
    private boolean deleted;
    private MessageStatus status;
    private List<Reaction> reactions;
    private Set<ReadByDto> readbyes;
    private MessageDto reply;
}
