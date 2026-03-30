package com.subtrak.service;

import com.subtrak.dto.request.SubscriptionRequest;
import com.subtrak.dto.response.DashboardResponse;
import com.subtrak.dto.response.DashboardResponse.CategorySummary;
import com.subtrak.dto.response.SubscriptionResponse;
import com.subtrak.entity.Category;
import com.subtrak.entity.Subscription;
import com.subtrak.entity.User;
import com.subtrak.exception.ResourceNotFoundException;
import com.subtrak.repository.CategoryRepository;
import com.subtrak.repository.SubscriptionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class SubscriptionService {

    private final SubscriptionRepository subscriptionRepository;
    private final CategoryRepository categoryRepository;
    private final CurrencyService currencyService;

    public List<SubscriptionResponse> getAll(User user) {
        return subscriptionRepository.findByUserAndActiveTrue(user).stream()
                .map(SubscriptionResponse::from)
                .toList();
    }

    @Transactional
    public SubscriptionResponse create(User user, SubscriptionRequest req) {
        Category category = null;
        if (req.categoryId != null) {
            category = categoryRepository.findByIdAndUser(req.categoryId, user).orElse(null);
        }

        Subscription subscription = Subscription.builder()
                .user(user)
                .category(category)
                .name(sanitize(req.name))
                .amount(req.amount)
                .currency(sanitize(req.currency))
                .billingCycle(req.billingCycle)
                .nextRenewalDate(req.nextRenewalDate)
                .notes(sanitize(req.notes))
                .build();

        return SubscriptionResponse.from(subscriptionRepository.save(subscription));
    }

    @Transactional
    public SubscriptionResponse update(User user, String id, SubscriptionRequest req) {
        Subscription subscription = subscriptionRepository.findByIdAndUser(id, user)
                .orElseThrow(() -> new ResourceNotFoundException("Subscription not found"));

        Category category = null;
        if (req.categoryId != null) {
            category = categoryRepository.findByIdAndUser(req.categoryId, user).orElse(null);
        }

        subscription.setCategory(category);
        subscription.setName(sanitize(req.name));
        subscription.setAmount(req.amount);
        subscription.setCurrency(sanitize(req.currency));
        subscription.setBillingCycle(req.billingCycle);
        subscription.setNextRenewalDate(req.nextRenewalDate);
        subscription.setNotes(sanitize(req.notes));

        return SubscriptionResponse.from(subscriptionRepository.save(subscription));
    }

    @Transactional
    public void delete(User user, String id) {
        Subscription subscription = subscriptionRepository.findByIdAndUser(id, user)
                .orElseThrow(() -> new ResourceNotFoundException("Subscription not found"));
        subscription.setActive(false);
        subscriptionRepository.save(subscription);
    }

    public List<SubscriptionResponse> getUpcoming(User user, int days) {
        LocalDate start = LocalDate.now();
        LocalDate end = start.plusDays(days);
        return subscriptionRepository.findByUserAndActiveTrueAndNextRenewalDateBetween(user, start, end)
                .stream()
                .map(SubscriptionResponse::from)
                .toList();
    }

    public DashboardResponse getDashboard(User user) {
        List<Subscription> activeSubscriptions = subscriptionRepository.findByUserAndActiveTrue(user);

        BigDecimal totalMonthlyCostInUSD = activeSubscriptions.stream()
                .map(s -> {
                    BigDecimal monthlyCost = toMonthlyCost(s);
                    try {
                        return currencyService.toUSD(monthlyCost, s.getCurrency());
                    } catch (Exception e) {
                        log.warn("Failed to convert {} {} to USD, using raw amount", monthlyCost, s.getCurrency());
                        return monthlyCost;
                    }
                })
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalMonthlyInDisplayCurrency;
        String displayCurrency = user.getDisplayCurrency();
        try {
            totalMonthlyInDisplayCurrency = currencyService.convert(totalMonthlyCostInUSD, "USD", displayCurrency);
        } catch (Exception e) {
            log.warn("Failed to convert to display currency {}, using USD", displayCurrency);
            totalMonthlyInDisplayCurrency = totalMonthlyCostInUSD;
            displayCurrency = "USD";
        }

        BigDecimal budgetUsedPercent = BigDecimal.ZERO;
        if (user.getSalary() != null && user.getSalary().compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal salaryInUSD;
            try {
                salaryInUSD = currencyService.toUSD(user.getSalary(), user.getSalaryCurrency());
            } catch (Exception e) {
                log.warn("Failed to convert salary to USD, using raw amount");
                salaryInUSD = user.getSalary();
            }
            if (salaryInUSD.compareTo(BigDecimal.ZERO) > 0) {
                budgetUsedPercent = totalMonthlyCostInUSD
                        .multiply(BigDecimal.valueOf(100))
                        .divide(salaryInUSD, 2, RoundingMode.HALF_UP);
            }
        }

        Map<String, List<Subscription>> grouped = activeSubscriptions.stream()
                .collect(Collectors.groupingBy(s -> 
                        s.getCategory() != null ? s.getCategory().getName() : "Uncategorized"));

        List<CategorySummary> byCategory = grouped.entrySet().stream()
                .map(entry -> {
                    BigDecimal monthlyCost = entry.getValue().stream()
                            .map(this::toMonthlyCost)
                            .reduce(BigDecimal.ZERO, BigDecimal::add);
                    String color = entry.getValue().stream()
                            .filter(s -> s.getCategory() != null)
                            .map(s -> s.getCategory().getColor())
                            .findFirst()
                            .orElse("#6b7280");
                    return new CategorySummary(entry.getKey(), color, monthlyCost);
                })
                .toList();

        List<SubscriptionResponse> upcomingRenewals = getUpcoming(user, 30);

        return new DashboardResponse(
                totalMonthlyCostInUSD,
                activeSubscriptions.size(),
                budgetUsedPercent,
                byCategory,
                upcomingRenewals,
                displayCurrency,
                totalMonthlyInDisplayCurrency
        );
    }

    private BigDecimal toMonthlyCost(Subscription s) {
        BigDecimal amount = s.getAmount();
        return switch (s.getBillingCycle()) {
            case WEEKLY -> amount.multiply(BigDecimal.valueOf(52)).divide(BigDecimal.valueOf(12), 2, RoundingMode.HALF_UP);
            case MONTHLY -> amount;
            case QUARTERLY -> amount.divide(BigDecimal.valueOf(3), 2, RoundingMode.HALF_UP);
            case YEARLY -> amount.divide(BigDecimal.valueOf(12), 2, RoundingMode.HALF_UP);
        };
    }

    private String sanitize(String input) {
        return input != null ? input.trim() : null;
    }
}
