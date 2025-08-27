package com.splitbuddy.splitbuddy.models;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Getter
@Setter
@NoArgsConstructor
public class Bill {
    @Id
    @GeneratedValue
    private UUID id;

    private String description;
    private BigDecimal totalAmount;
    private LocalDateTime createdAt = LocalDateTime.now();

    @ManyToOne
    @JoinColumn(name = "paid_by")
    private User paidBy;

//    @ManyToOne
//    @JoinColumn(name = "group_id")
//   // private Group group;
//
//    @OneToMany(mappedBy = "bill", cascade = CascadeType.ALL)
//    private List<BillShare> shares;
}