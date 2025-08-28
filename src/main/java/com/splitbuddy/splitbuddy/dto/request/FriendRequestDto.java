package com.splitbuddy.splitbuddy.dto.request;

import java.util.UUID;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class FriendRequestDto {

    @NotNull(message = "Sender ID is required")
    private UUID senderId;

    @NotNull(message = "Receiver ID is required")
    private UUID receiverId;
}
