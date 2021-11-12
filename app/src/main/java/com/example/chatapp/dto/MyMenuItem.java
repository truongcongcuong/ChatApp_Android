package com.example.chatapp.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@AllArgsConstructor
@Builder
public class MyMenuItem {
    private String key;
    private String name;
    private int imageResource;
}
