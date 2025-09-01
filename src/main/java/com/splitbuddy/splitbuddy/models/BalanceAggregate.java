package com.splitbuddy.splitbuddy.models;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "balance_aggregates", uniqueConstraints = {
        @UniqueConstraint(columnNames = { "user1_id", "user2_id", "balance_type" }),
        @UniqueConstraint(columnNames = { "user_id", "group_id", "balance_type" })
})
@Getter
@Setter
@NoArgsConstructor
public class BalanceAggregate {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user1_id")
    private User user1;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user2_id")
    private User user2;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "group_id")
    private Group group;

    @Enumerated(EnumType.STRING)
    @Column(name = "balance_type", nullable = false)
    private BalanceType balanceType;

    @Column(precision = 10, scale = 2, nullable = false)
    private BigDecimal balance = BigDecimal.ZERO;

    @Column(nullable = false)
    private LocalDateTime lastUpdated = LocalDateTime.now();

    @Column(nullable = false)
    private Long lastExpenseId;

    public enum BalanceType {
        FRIEND_TO_FRIEND, // Balance between two specific friends
        USER_TO_GROUP // Balance between a user and a group
    }

    // Constructor for friend-to-friend balance
    public BalanceAggregate(User user1, User user2, BigDecimal balance, Long lastExpenseId) {
        this.user1 = user1;
        this.user2 = user2;
        this.balance = balance;
        this.balanceType = BalanceType.FRIEND_TO_FRIEND;
        this.lastExpenseId = lastExpenseId;
        this.lastUpdated = LocalDateTime.now();
    }

    // Constructor for user-to-group balance
    public BalanceAggregate(User user, Group group, BigDecimal balance, Long lastExpenseId) {
        this.user = user;
        this.group = group;
        this.balance = balance;
        this.balanceType = BalanceType.USER_TO_GROUP;
        this.lastExpenseId = lastExpenseId;
        this.lastUpdated = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        lastUpdated = LocalDateTime.now();
    }
}
