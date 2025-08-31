package com.splitbuddy.splitbuddy.dto.response;

import com.splitbuddy.splitbuddy.models.FriendRequestStatus;
import lombok.Data;

@Data
public class FriendRequestResponse {
    private Long id;
    private String senderName;
    private String senderEmail;
    private String createdAt;
    private FriendRequestStatus status;
    private UserResponse sender;
    private UserResponse receiver;
}
