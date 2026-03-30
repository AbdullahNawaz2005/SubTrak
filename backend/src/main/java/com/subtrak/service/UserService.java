package com.subtrak.service;

import org.springframework.stereotype.Service;

import com.subtrak.dto.request.UpdateUserRequest;
import com.subtrak.dto.response.UserResponse;
import com.subtrak.entity.User;
import com.subtrak.exception.ResourceNotFoundException;
import com.subtrak.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    public User getUserById(String id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }

    public UserResponse updateUser(User user, UpdateUserRequest req) {
        if (req.getDisplayCurrency() != null) {
            user.setDisplayCurrency(req.getDisplayCurrency().trim());
        }
        if (req.getSalary() != null) {
            user.setSalary(req.getSalary());
        }
        if (req.getSalaryCurrency() != null) {
            user.setSalaryCurrency(req.getSalaryCurrency().trim());
        }
        if (req.getBudgetLimitPercent() != null) {
            user.setBudgetLimitPercent(req.getBudgetLimitPercent());
        }
        if (req.getLocale() != null) {
            user.setLocale(req.getLocale().trim());
        }
        
        User savedUser = userRepository.save(user);
        return UserResponse.from(savedUser);
    }
}
