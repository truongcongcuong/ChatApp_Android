package com.example.chatapp.entity;

import java.io.Serializable;
import java.util.Date;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class User implements Serializable {

    private String id;
    private String displayName;
    private Date birthday;
    private String password;
    private String gender;
    private String phoneNumber;
    private String email;
    private String verificationCode;
    private String imageUrl;
    private String roles;
}
