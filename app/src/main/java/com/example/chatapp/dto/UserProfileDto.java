package com.example.chatapp.dto;

import com.example.chatapp.enumvalue.FriendStatus;
import com.example.chatapp.enumvalue.OnlineStatus;

import java.io.Serializable;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(of = "id")
public class UserProfileDto implements Serializable {
    private String id;
    private String displayName;
    private String imageUrl;
    private OnlineStatus onlineStatus;
    private String lastOnline;
    private FriendStatus friendStatus;
    private String phoneNumber;

}
