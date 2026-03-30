package com.subtrak.dto.response;

import java.math.BigDecimal;
import java.util.List;

public record DashboardResponse(
        BigDecimal totalMonthlyCost,
        int activeSubscriptions,
        BigDecimal budgetUsedPercent,
        List<CategorySummary> byCategory,
        List<SubscriptionResponse> upcomingRenewals,
        String displayCurrency,
        BigDecimal totalMonthlyInDisplayCurrency
) {
    public record CategorySummary(
            String categoryName,
            String color,
            BigDecimal monthlyCost
    ) {}
}
