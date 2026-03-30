package com.subtrak.dto.response;

import com.subtrak.entity.User;

import java.math.BigDecimal;

public record UserResponse(
        String id,
        String name,
        String email,
        String locale,
        String displayCurrency,
        BigDecimal salary,
        String salaryCurrency,
        BigDecimal budgetLimitPercent
) {
    public static UserResponse from(User user) {
        return new UserResponse(
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.getLocale(),
                user.getDisplayCurrency(),
                user.getSalary(),
                user.getSalaryCurrency(),
                user.getBudgetLimitPercent()
        );
    }
}
