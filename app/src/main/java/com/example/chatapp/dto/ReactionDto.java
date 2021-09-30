package com.example.chatapp.dto;


import com.example.chatapp.enumvalue.ReactionType;

import java.io.Serializable;

import lombok.Data;

@Data
public class ReactionDto implements Serializable {
    private UserProfileDto reactByUser;
    private ReactionType type;
}
