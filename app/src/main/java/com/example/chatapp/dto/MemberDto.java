package com.example.chatapp.dto;

import java.io.Serializable;

import lombok.Data;

@Data
public class MemberDto implements Serializable {
    private UserProfileDto user;
    private UserProfileDto addByUser;
    private String addTime;
    private boolean isAdmin;
}
