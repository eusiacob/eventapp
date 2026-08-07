package com.example.eventapp.dto;

import com.example.eventapp.model.User;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserAdminDto {

    private User user;

    private long reviewCount;

    public UserAdminDto(User user, long reviewCount) {
        this.user = user;
        this.reviewCount = reviewCount;
    }

}
