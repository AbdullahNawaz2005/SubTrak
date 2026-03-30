package com.subtrak.service;

import com.subtrak.exception.ResourceNotFoundException;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
@Slf4j
public class CurrencyService {

    private final RestTemplate restTemplate;
    private final String apiKey;
    private final String baseUrl;
    private final ConcurrentHashMap<String, BigDecimal> ratesFromUSD = new ConcurrentHashMap<>();

    public CurrencyService(
            RestTemplate restTemplate,
            @Value("${app.fx.api-key}") String apiKey,
            @Value("${app.fx.base-url}") String baseUrl
    ) {
        this.restTemplate = restTemplate;
        this.apiKey = apiKey;
        this.baseUrl = baseUrl;
    }

    @PostConstruct
    public void init() {
        fetchRates();
    }

    @Scheduled(fixedRateString = "${app.fx.refresh-rate-ms:3600000}")
    public void scheduledFetchRates() {
        fetchRates();
    }

    @SuppressWarnings("unchecked")
    private void fetchRates() {
        try {
            String url = String.format("%s/%s/latest/USD", baseUrl, apiKey);
            Map<String, Object> response = restTemplate.getForObject(url, Map.class);
            
            if (response != null && response.containsKey("conversion_rates")) {
                Map<String, Number> rates = (Map<String, Number>) response.get("conversion_rates");
                rates.forEach((currency, rate) -> 
                    ratesFromUSD.put(currency, new BigDecimal(rate.toString()))
                );
                log.info("Successfully fetched {} currency rates", ratesFromUSD.size());
            }
        } catch (Exception e) {
            log.warn("Failed to fetch currency rates: {}. Using cached rates.", e.getMessage());
            if (ratesFromUSD.isEmpty()) {
                ratesFromUSD.put("USD", BigDecimal.ONE);
            }
        }
    }

    public BigDecimal convert(BigDecimal amount, String fromCurrency, String toCurrency) {
        if (fromCurrency.equals(toCurrency)) {
            return amount;
        }

        BigDecimal fromRate = ratesFromUSD.get(fromCurrency);
        BigDecimal toRate = ratesFromUSD.get(toCurrency);

        if (fromRate == null) {
            throw new ResourceNotFoundException("Currency not supported: " + fromCurrency);
        }
        if (toRate == null) {
            throw new ResourceNotFoundException("Currency not supported: " + toCurrency);
        }

        return amount
                .divide(fromRate, 10, RoundingMode.HALF_UP)
                .multiply(toRate)
                .setScale(2, RoundingMode.HALF_UP);
    }

    public BigDecimal toUSD(BigDecimal amount, String fromCurrency) {
        return convert(amount, fromCurrency, "USD");
    }

    public Map<String, BigDecimal> getAllRates() {
        return Collections.unmodifiableMap(ratesFromUSD);
    }

    public boolean isCurrencySupported(String currency) {
        return ratesFromUSD.containsKey(currency);
    }
}
