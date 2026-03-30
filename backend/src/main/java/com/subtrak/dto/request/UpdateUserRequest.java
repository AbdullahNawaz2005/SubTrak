package com.subtrak.dto.request;

import java.math.BigDecimal;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateUserRequest {
    private String displayCurrency;
    private BigDecimal salary;
    private String salaryCurrency;
    private BigDecimal budgetLimitPercent;
    private String locale;
}
