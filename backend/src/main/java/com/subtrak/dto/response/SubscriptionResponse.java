package com.subtrak.dto.response;

import com.subtrak.entity.BillingCycle;
import com.subtrak.entity.Subscription;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public record SubscriptionResponse(
        String id,
        String name,
        BigDecimal amount,
        String currency,
        BillingCycle billingCycle,
        LocalDate nextRenewalDate,
        boolean active,
        String notes,
        CategoryResponse category,
        LocalDateTime createdAt
) {
    public static SubscriptionResponse from(Subscription s) {
        return new SubscriptionResponse(
                s.getId(),
                s.getName(),
                s.getAmount(),
                s.getCurrency(),
                s.getBillingCycle(),
                s.getNextRenewalDate(),
                s.isActive(),
                s.getNotes(),
                s.getCategory() != null ? CategoryResponse.from(s.getCategory()) : null,
                s.getCreatedAt()
        );
    }
}
