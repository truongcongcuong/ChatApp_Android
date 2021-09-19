package com.example.chatapp.entity;


import java.io.Serializable;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(of = "type")
public class Reaction implements Serializable {
    private String reactByUserId;
    private String type;
}
