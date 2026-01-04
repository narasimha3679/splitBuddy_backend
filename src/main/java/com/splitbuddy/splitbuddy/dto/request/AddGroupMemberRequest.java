package com.splitbuddy.splitbuddy.dto.request;

import java.util.List;

import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

/**
 * Request DTO for adding members to an existing group.
 */
@Data
public class AddGroupMemberRequest {
    @NotEmpty(message = "Member IDs cannot be empty")
    private List<String> memberIds;
}
