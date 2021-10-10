package com.example.chatapp.dto;

import java.io.Serializable;

import lombok.Data;

@Data
public class FriendRequestSentDto implements Serializable {
    private UserProfileDto to;

    private String createAt;
}
