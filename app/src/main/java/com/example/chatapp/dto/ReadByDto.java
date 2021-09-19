package com.example.chatapp.dto;

import java.io.Serializable;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(of = "readByUser")
public class ReadByDto implements Serializable {
    private UserProfileDto readByUser;
    private String readAt;
}
