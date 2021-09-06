package com.example.chatapp.dto;

import java.io.Serializable;
import java.util.Date;

import lombok.Data;

@Data
public class UserProfileDto implements Serializable {
    private String id;
    private String displayName;
    private String imageUrl;
    private String onlineStatus;
    private String lastOnline;

}
