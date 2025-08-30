package com.splitbuddy.splitbuddy.dto.response;

import java.math.BigDecimal;

import lombok.Data;

@Data
public class SettlementResponse {
    private Long id;
    private Long fromUserId;
    private String fromUserName;
    private Long toUserId;
    private String toUserName;
    private BigDecimal amount;
}
