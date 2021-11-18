package com.example.chatapp.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.io.Serializable;

import lombok.Data;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class FriendDeleteDto implements Serializable {
    private String userId;
    private String friendId;
}
