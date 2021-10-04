package com.example.chatapp.dto;

import java.io.Serializable;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(of = "user")
public class MemberDto implements Serializable {
    private UserProfileDto user;
    private UserProfileDto addByUser;
    private String addTime;
    private boolean isAdmin;
}
