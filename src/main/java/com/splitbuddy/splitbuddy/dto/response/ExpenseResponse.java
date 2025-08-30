package com.splitbuddy.splitbuddy.dto.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import com.splitbuddy.splitbuddy.models.ExpenseParticipant.ParticipantSource;

import lombok.Data;

@Data
public class ExpenseResponse {
    private Long id;
    private String title;
    private String description;
    private BigDecimal amount;
    private String currency;
    private String category;
    private LocalDateTime paidAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Long paidBy;
    private List<ParticipantResponse> participants;

    @Data
    public static class ParticipantResponse {
        private Long userId;
        private String userName;
        private BigDecimal amount;
        private ParticipantSource source;
        private Long sourceId;
        private boolean isActive;
    }
}
