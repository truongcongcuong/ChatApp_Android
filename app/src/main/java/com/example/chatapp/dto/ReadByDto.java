package com.example.chatapp.dto;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.io.Serializable;
import java.util.Date;

import lombok.Data;

@Data
public class ReadByDto implements Serializable {
    private UserProfileDto readByUser;
    private String readAt;
}
