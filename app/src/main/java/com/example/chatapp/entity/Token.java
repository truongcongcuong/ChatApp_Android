package com.example.chatapp.entity;



import lombok.Data;
@Data
public class Token {
    private int id;

    private String accessToken;

    private String refreshToken;

}
