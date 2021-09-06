package com.example.chatapp.entity;


import java.io.Serializable;

import lombok.Data;

@Data
public class Reaction implements Serializable {
    private String reactByUserId;
    private String type;
}
