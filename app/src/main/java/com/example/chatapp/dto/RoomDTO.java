package com.example.chatapp.dto;

import com.example.chatapp.enumvalue.RoomType;

import java.io.Serializable;
import java.util.Set;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@AllArgsConstructor
@Builder
public class RoomDTO implements Serializable {
    private String id;
    private String name;
    private String imageUrl;
    private RoomType type;
    private UserProfileDto to;
    private String createAt;
    private Set<MemberDto> members;
    private String createByUserId;
}
