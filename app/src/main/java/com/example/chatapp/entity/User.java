package com.example.chatapp.entity;

import java.io.Serializable;
import java.util.Date;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
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

    @Override
    public String toString() {
        return "{" +
                "id='" + id + '\'' +
                ", displayName='" + displayName + '\'' +
                ", birthday=" + birthday +
                ", password='" + password + '\'' +
                ", gender='" + gender + '\'' +
                ", phoneNumber='" + phoneNumber + '\'' +
                ", email='" + email + '\'' +
                ", verificationCode='" + verificationCode + '\'' +
                ", imageUrl='" + imageUrl + '\'' +
                ", roles='" + roles + '\'' +
                '}';
    }
}
