package com.example.chatapp.dto;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.io.Serializable;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UserUpdateDTO implements Serializable {
    private String email;

    private String displayName;

    private String gender;

    private String dateOfBirth;

    private String username;
}
