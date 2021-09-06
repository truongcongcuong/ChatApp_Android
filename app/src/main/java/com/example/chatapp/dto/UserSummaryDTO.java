package com.example.chatapp.dto;



import java.io.Serializable;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class UserSummaryDTO implements Serializable {
    private String id;
    private String username;
    private String displayName;
    private String imageUrl;
    private String roles;
    private String accessToken;

    @Override
    public String toString() {
        return "{" +
                "id='" + id + '\'' +
                ", username='" + username + '\'' +
                ", displayName='" + displayName + '\'' +
                ", imageUrl='" + imageUrl + '\'' +
                ", roles='" + roles + '\'' +
                ", accessToken='" + accessToken + '\'' +
                '}';
    }
}
