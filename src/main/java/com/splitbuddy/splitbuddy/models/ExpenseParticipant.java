package com.splitbuddy.splitbuddy.models;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import com.fasterxml.jackson.annotation.JsonBackReference;

import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "expense_participants")
@Getter
@Setter
@NoArgsConstructor
public class ExpenseParticipant {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "expense_id", nullable = false)
    @JsonBackReference("expense-participants")
    private Expense expense;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(precision = 10, scale = 2)
    private BigDecimal amount;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private ParticipantSource source;

    @Column(name = "source_id")
    private Long sourceId;

    @Column(nullable = false)
    private boolean isActive = true;

    @Column(name = "is_paid", nullable = false)
    private boolean isPaid = false;

    @Column(name = "paid_at")
    private Instant paidAt;

    public enum ParticipantSource {
        FRIEND, // Participant added as a friend
        GROUP // Participant added as a group member
    }
}
