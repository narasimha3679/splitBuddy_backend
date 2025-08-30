package com.splitbuddy.splitbuddy.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class FriendRequestDto {
    @NotNull(message = "Sender ID is required")
    private Long senderId;

    private String message;

    @NotNull(message = "Receiver ID is required")
    private Long receiverId;
}
