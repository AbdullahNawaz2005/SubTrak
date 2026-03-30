package com.subtrak.controller;

import com.subtrak.dto.request.SubscriptionRequest;
import com.subtrak.dto.response.DashboardResponse;
import com.subtrak.dto.response.SubscriptionResponse;
import com.subtrak.entity.User;
import com.subtrak.exception.RateLimitExceededException;
import com.subtrak.security.RateLimiter;
import com.subtrak.service.SubscriptionService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/subscriptions")
@RequiredArgsConstructor
@Validated
public class SubscriptionController {

    private final SubscriptionService subscriptionService;
    private final RateLimiter rateLimiter;

    @GetMapping
    public ResponseEntity<List<SubscriptionResponse>> getAll(HttpServletRequest request) {
        User user = getCurrentUser();
        checkApiRateLimit(request, user);
        return ResponseEntity.ok(subscriptionService.getAll(user));
    }

    @PostMapping
    public ResponseEntity<SubscriptionResponse> create(@Valid @RequestBody SubscriptionRequest req,
                                                       HttpServletRequest request) {
        User user = getCurrentUser();
        checkApiRateLimit(request, user);
        return ResponseEntity.status(HttpStatus.CREATED).body(subscriptionService.create(user, req));
    }

    @PutMapping("/{id}")
    public ResponseEntity<SubscriptionResponse> update(@PathVariable String id,
                                                       @Valid @RequestBody SubscriptionRequest req,
                                                       HttpServletRequest request) {
        User user = getCurrentUser();
        checkApiRateLimit(request, user);
        return ResponseEntity.ok(subscriptionService.update(user, id, req));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable String id, HttpServletRequest request) {
        User user = getCurrentUser();
        checkApiRateLimit(request, user);
        subscriptionService.delete(user, id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/upcoming")
    public ResponseEntity<List<SubscriptionResponse>> getUpcoming(
            @RequestParam(defaultValue = "30") int days,
            HttpServletRequest request) {
        User user = getCurrentUser();
        checkApiRateLimit(request, user);
        return ResponseEntity.ok(subscriptionService.getUpcoming(user, days));
    }

    @GetMapping("/dashboard")
    public ResponseEntity<DashboardResponse> getDashboard(HttpServletRequest request) {
        User user = getCurrentUser();
        checkDashboardRateLimit(request);
        return ResponseEntity.ok(subscriptionService.getDashboard(user));
    }

    private User getCurrentUser() {
        return (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    }

    private void checkApiRateLimit(HttpServletRequest request, User user) {
        String key = getClientIp(request) + ":" + user.getId();
        if (!rateLimiter.tryConsumeApi(key)) {
            throw new RateLimitExceededException();
        }
    }

    private void checkDashboardRateLimit(HttpServletRequest request) {
        String ip = getClientIp(request);
        if (!rateLimiter.tryConsumeDashboard(ip)) {
            throw new RateLimitExceededException();
        }
    }

    private String getClientIp(HttpServletRequest request) {
        String forwarded = request.getHeader("X-Forwarded-For");
        return (forwarded != null) ? forwarded.split(",")[0].trim() : request.getRemoteAddr();
    }
}
