package com.splitbuddy.splitbuddy.dto.response;

import java.util.UUID;

import com.splitbuddy.splitbuddy.models.FriendRequestStatus;
import com.splitbuddy.splitbuddy.models.User;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PendingFriendRequestsResponseDto {

    private UUID id;
    private FriendRequestStatus status;
    private User sender;

}
