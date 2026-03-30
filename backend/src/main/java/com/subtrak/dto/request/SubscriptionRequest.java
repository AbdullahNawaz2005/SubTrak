package com.subtrak.dto.request;

import com.subtrak.entity.BillingCycle;
import com.subtrak.validation.NotPastDate;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.time.LocalDate;

public class SubscriptionRequest {

    @NotBlank
    @Size(max = 100)
    public String name;

    @NotNull
    @Positive
    @DecimalMax(value = "999999.99")
    public BigDecimal amount;

    @NotBlank
    @Pattern(regexp = "^[A-Z]{3}$", message = "Currency must be 3 uppercase letters")
    public String currency;

    @NotNull
    public BillingCycle billingCycle;

    @NotNull
    @NotPastDate
    public LocalDate nextRenewalDate;

    public String categoryId;

    @Size(max = 500)
    public String notes;
}
