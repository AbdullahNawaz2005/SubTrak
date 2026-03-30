package com.subtrak.controller;

import com.subtrak.service.CurrencyService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/currency")
@RequiredArgsConstructor
public class CurrencyController {

    private final CurrencyService currencyService;

    @GetMapping("/rates")
    public ResponseEntity<Map<String, BigDecimal>> getRates() {
        return ResponseEntity.ok(currencyService.getAllRates());
    }

    @GetMapping("/convert")
    public ResponseEntity<ConversionResponse> convert(
            @RequestParam BigDecimal amount,
            @RequestParam String from,
            @RequestParam String to
    ) {
        BigDecimal converted = currencyService.convert(amount, from, to);
        BigDecimal rate = currencyService.convert(BigDecimal.ONE, from, to);
        return ResponseEntity.ok(new ConversionResponse(from, to, amount, converted, rate));
    }

    @GetMapping("/supported")
    public ResponseEntity<List<String>> getSupported() {
        return ResponseEntity.ok(currencyService.getAllRates().keySet().stream().sorted().toList());
    }

    public record ConversionResponse(
            String from,
            String to,
            BigDecimal amount,
            BigDecimal converted,
            BigDecimal rate
    ) {}
}
