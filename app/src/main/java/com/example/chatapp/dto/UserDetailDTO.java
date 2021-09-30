package com.example.chatapp.dto;

import com.example.chatapp.enumvalue.OnlineStatus;
import com.fasterxml.jackson.annotation.JsonIgnore;

import java.io.Serializable;

import lombok.Data;

@Data
public class UserDetailDTO implements Serializable {
    private String id;
    private String username;
    private String displayName;
    private String gender;

    private String dateOfBirth;

    private String phoneNumber;
    private String email;
    private String imageUrl;

    private String createAt;

    @JsonIgnore
    private boolean block;
    @JsonIgnore
    private boolean enable;
    @JsonIgnore
    private String roles;

    private OnlineStatus onlineStatus;
    private String lastOnline;
}
