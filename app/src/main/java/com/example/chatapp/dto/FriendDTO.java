package com.example.chatapp.dto;

import java.io.Serializable;

import lombok.Data;


@Data
public class FriendDTO implements Serializable {
    private UserProfileDto friend;
    private String createAt;
}
