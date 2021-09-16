package com.example.chatapp.entity;

import com.example.chatapp.cons.CroppedDrawable;

import java.io.Serializable;
import java.util.List;

import lombok.Data;

@Data
public class MemberInRoom implements Serializable {
    private String id;
    private CroppedDrawable drawable;
}
