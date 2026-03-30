package com.subtrak.controller;

import com.subtrak.dto.request.CategoryRequest;
import com.subtrak.dto.response.CategoryResponse;
import com.subtrak.entity.User;
import com.subtrak.exception.RateLimitExceededException;
import com.subtrak.security.RateLimiter;
import com.subtrak.service.CategoryService;
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
@RequestMapping("/api/categories")
@RequiredArgsConstructor
@Validated
public class CategoryController {

    private final CategoryService categoryService;
    private final RateLimiter rateLimiter;

    @GetMapping
    public ResponseEntity<List<CategoryResponse>> getAll(HttpServletRequest request) {
        User user = getCurrentUser();
        checkRateLimit(request, user);
        return ResponseEntity.ok(categoryService.getAll(user));
    }

    @PostMapping
    public ResponseEntity<CategoryResponse> create(@Valid @RequestBody CategoryRequest req,
                                                   HttpServletRequest request) {
        User user = getCurrentUser();
        checkRateLimit(request, user);
        return ResponseEntity.status(HttpStatus.CREATED).body(categoryService.create(user, req));
    }

    @PutMapping("/{id}")
    public ResponseEntity<CategoryResponse> update(@PathVariable String id,
                                                   @Valid @RequestBody CategoryRequest req,
                                                   HttpServletRequest request) {
        User user = getCurrentUser();
        checkRateLimit(request, user);
        return ResponseEntity.ok(categoryService.update(user, id, req));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable String id, HttpServletRequest request) {
        User user = getCurrentUser();
        checkRateLimit(request, user);
        categoryService.delete(user, id);
        return ResponseEntity.noContent().build();
    }

    private User getCurrentUser() {
        return (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    }

    private void checkRateLimit(HttpServletRequest request, User user) {
        String key = getClientIp(request) + ":" + user.getId();
        if (!rateLimiter.tryConsumeApi(key)) {
            throw new RateLimitExceededException();
        }
    }

    private String getClientIp(HttpServletRequest request) {
        String forwarded = request.getHeader("X-Forwarded-For");
        return (forwarded != null) ? forwarded.split(",")[0].trim() : request.getRemoteAddr();
    }
}
