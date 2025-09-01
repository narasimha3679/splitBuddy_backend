package com.splitbuddy.splitbuddy.dto.response;

import java.math.BigDecimal;
import lombok.Data;

@Data
public class GroupBalanceResponse {
    private Long groupId;
    private String groupName;
    private Long userId; // Optional: only included when getting all balances for a group
    private String userName; // Optional: only included when getting all balances for a group
    private BigDecimal balance; // Positive means user is owed money by the group, negative means user owes the
                                // group
}
