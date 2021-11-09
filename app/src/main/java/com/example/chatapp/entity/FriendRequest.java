package com.example.chatapp.entity;

import com.example.chatapp.dto.UserProfileDto;

import java.io.Serializable;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import lombok.AccessLevel;
import lombok.Data;
import lombok.Getter;

@Data
public class FriendRequest implements Serializable, Comparable<FriendRequest> {
    private String id;
    private UserProfileDto from;
    private UserProfileDto to;
    private String createAt;

    @Getter(AccessLevel.NONE)
    private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    @Override
    public int compareTo(FriendRequest o) {
        if (createAt != null && o != null && o.getCreateAt() != null) {
            try {
                Date thisDate = dateFormat.parse(createAt);
                Date thatDate = dateFormat.parse(o.getCreateAt());
                if (thatDate != null)
                    return thatDate.compareTo(thisDate);
            } catch (ParseException e) {

            }
        }
        return 0;
    }
}
