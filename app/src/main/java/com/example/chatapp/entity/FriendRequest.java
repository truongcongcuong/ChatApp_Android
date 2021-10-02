package com.example.chatapp.entity;

import com.example.chatapp.dto.UserProfileDto;

import java.io.Serializable;

import lombok.Data;

@Data
public class FriendRequest implements Serializable {
    private UserProfileDto from;
    private String createAt;
}
