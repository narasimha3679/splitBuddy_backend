package com.splitbuddy.splitbuddy.dto.request;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import com.splitbuddy.splitbuddy.models.ExpenseParticipant.ParticipantSource;

import lombok.Data;

@Data
public class CreateExpenseRequest {
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
