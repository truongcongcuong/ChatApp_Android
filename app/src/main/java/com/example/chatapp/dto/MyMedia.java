package com.example.chatapp.dto;

import com.example.chatapp.enumvalue.MediaType;
import com.fasterxml.jackson.annotation.JsonInclude;

import java.io.Serializable;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class MyMedia implements Serializable {
    private String url;
    private MediaType type;
    private String name;
    private long size;
}
