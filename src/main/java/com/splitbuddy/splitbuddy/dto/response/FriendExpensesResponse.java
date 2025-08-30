package com.splitbuddy.splitbuddy.dto.response;

import java.math.BigDecimal;
import java.util.List;
import lombok.Data;

@Data
public class FriendExpensesResponse {
    private Long friendId;
    private String friendName;
    private BigDecimal totalOwedToFriend; // Amount user owes to friend
    private BigDecimal totalOwedByFriend; // Amount friend owes to user
    private BigDecimal netBalance; // totalOwedByFriend - totalOwedToFriend (positive means friend owes user)
    private List<ExpenseResponse> sharedExpenses;
}
