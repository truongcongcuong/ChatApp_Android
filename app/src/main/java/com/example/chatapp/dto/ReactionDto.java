package com.example.chatapp.dto;


import java.io.Serializable;

import lombok.Data;

@Data
public class ReactionDto implements Serializable {
    private UserProfileDto reactByUser;
    private String type;
}
