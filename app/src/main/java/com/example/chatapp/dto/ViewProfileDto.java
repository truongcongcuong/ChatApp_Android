package com.example.chatapp.dto;

import com.example.chatapp.enumvalue.FriendStatus;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ViewProfileDto {
    private UserProfileDto user;
    private FriendStatus friendStatus;
}
