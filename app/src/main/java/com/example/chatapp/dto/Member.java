package com.example.chatapp.dto;

import java.io.Serializable;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@EqualsAndHashCode(of = "userId")
public class Member implements Serializable {
    private String userId;
    private String addByUserId;
    private String addTime;
    private boolean isAdmin;
}
