package com.splitbuddy.splitbuddy.dto.response;

import java.math.BigDecimal;

import lombok.Data;

@Data
public class FriendBalanceResponse {
    private Long friendId;
    private String friendName;
    private BigDecimal balance;
}
