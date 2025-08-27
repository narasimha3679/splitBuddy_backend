package com.splitbuddy.splitbuddy.dto.request;

import java.util.UUID;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class FriendRequestDto {

    private UUID senderId;
    private UUID receiverId;

}
