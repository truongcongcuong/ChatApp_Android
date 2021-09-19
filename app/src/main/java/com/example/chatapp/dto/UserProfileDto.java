package com.example.chatapp.dto;

import java.io.Serializable;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(of = "id")
public class UserProfileDto implements Serializable {
    private String id;
    private String displayName;
    private String imageUrl;
    private String onlineStatus;
    private String lastOnline;

}
