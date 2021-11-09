package com.example.chatapp.dto;

import java.io.Serializable;

import lombok.Data;

@Data
public class PhoneBookFriendDTO implements Serializable {
    private String name;
    private String phone;
    private UserProfileDto user;
}
