package com.example.chatapp.entity;

import com.example.chatapp.dto.MyMedia;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UserReport implements Serializable {
    private String id;
    private String fromId;
    private String toId;
    private Date createAt;
    private String content;
    private String messageId;
    private boolean seen;
    private List<MyMedia> media;
}
