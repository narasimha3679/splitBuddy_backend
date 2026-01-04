package com.splitbuddy.splitbuddy.dto.request;

import lombok.Data;

/**
 * Request DTO for updating payment status of an expense participant.
 */
@Data
public class UpdatePaymentStatusRequest {
    private boolean isPaid;
}
