package com.example.chatapp.dto;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class UserSignUpDTO implements Serializable {
    @SerializedName("id")
    private String id;
    @SerializedName("displayName")
    private String displayName;
    @SerializedName("password")
    private String password;
    @SerializedName("phoneNumber")
    private String phoneNumber;
    @SerializedName("email")
    private String email;

}
