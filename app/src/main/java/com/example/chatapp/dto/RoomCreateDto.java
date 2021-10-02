package com.example.chatapp.dto;

import com.example.chatapp.enumvalue.RoomType;

import java.util.Date;
import java.util.Set;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor
public class RoomCreateDto {
    private String id;
    private String name;
    private Date createAt;
    private RoomType type;
    private Set<MemberCreateDto> members;
    private String createByUserId;
    private String imageUrl;
}
