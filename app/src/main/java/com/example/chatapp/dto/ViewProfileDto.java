package com.example.chatapp.dto;

import com.example.chatapp.enumvalue.FriendStatus;
import com.example.chatapp.enumvalue.OnlineStatus;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ViewProfileDto {
    private String id;
    private String username;
    private String displayName;
    private String gender;
    private String dateOfBirth;
    private String phoneNumber;
    private String email;
    private String imageUrl;
    private OnlineStatus onlineStatus;
    private String lastOnline;
    private FriendStatus friendStatus;
}
