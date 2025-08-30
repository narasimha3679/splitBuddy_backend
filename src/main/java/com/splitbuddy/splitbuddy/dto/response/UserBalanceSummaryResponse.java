package com.splitbuddy.splitbuddy.dto.response;

import java.math.BigDecimal;
import lombok.Data;

@Data
public class UserBalanceSummaryResponse {
    private Long userId;
    private String userName;
    private BigDecimal totalOwed; // Amount others owe to this user
    private BigDecimal totalOwes; // Amount this user owes to others
    private BigDecimal netBalance; // totalOwed - totalOwes (positive means net owed to user)
}
