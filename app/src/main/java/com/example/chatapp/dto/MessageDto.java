package com.example.chatapp.dto;

import com.example.chatapp.entity.Reaction;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonFormat;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

import lombok.Data;

@Data
public class MessageDto implements Serializable {
    private String id;
    private UserProfileDto sender;
    private String createAt;
    private String type;
    private String content;
    private Boolean pin;
    private Boolean deleted;
    private String status;
    private List<Reaction> reactions;
}
