package com.example.chatapp.dto;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.io.Serializable;
import java.util.Date;

import lombok.Data;


@Data
public class FriendDTO implements Serializable {
    private UserProfileDto friend;

    private String createAt;
}
