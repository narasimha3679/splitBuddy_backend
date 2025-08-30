package com.splitbuddy.splitbuddy.dto.response;

import java.time.LocalDateTime;

import lombok.Data;

@Data
public class PendingFriendRequestsResponseDto {
    private Long id;
    private String senderName;
    private String senderEmail;
    private LocalDateTime createdAt;
}
