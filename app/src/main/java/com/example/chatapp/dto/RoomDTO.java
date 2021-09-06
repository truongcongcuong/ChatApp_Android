package com.example.chatapp.dto;

import java.io.Serializable;
import java.util.Set;

import lombok.Data;

@Data
public class RoomDTO implements Serializable {
    private String id;
    private String name;
    private String imageUrl;
    private String type;
    private UserProfileDto to;
    private String createAt;
    private Set<MemberDto> members;
    private UserProfileDto createByUser;
}
