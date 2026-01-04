package com.splitbuddy.splitbuddy.dto.request;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import com.splitbuddy.splitbuddy.models.ExpenseParticipant.ParticipantSource;

import lombok.Data;

/**
 * Request DTO for updating an existing expense.
 * All fields are optional - only non-null fields will be updated.
 */
@Data
public class UpdateExpenseRequest {
    private String title;
    private String description;
    private BigDecimal amount;
    private String currency;
    private String category;
    private LocalDateTime paidAt;
    private Long paidBy;
    private List<ParticipantRequest> participants;

    @Data
    public static class ParticipantRequest {
        private Long userId;
        private BigDecimal amount;
        private ParticipantSource source;
        private Long sourceId;
    }
}
