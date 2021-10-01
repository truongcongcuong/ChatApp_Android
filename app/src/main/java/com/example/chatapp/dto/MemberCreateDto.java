package com.example.chatapp.dto;

import java.io.Serializable;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor
public class MemberCreateDto implements Serializable {
    private String userId;
    private String addByUserId;
    private String addTime;
    private boolean isAdmin;
}
