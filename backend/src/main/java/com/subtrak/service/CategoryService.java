package com.subtrak.service;

import com.subtrak.dto.request.CategoryRequest;
import com.subtrak.dto.response.CategoryResponse;
import com.subtrak.entity.Category;
import com.subtrak.entity.User;
import com.subtrak.exception.ResourceNotFoundException;
import com.subtrak.repository.CategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CategoryService {

    private final CategoryRepository categoryRepository;

    public List<CategoryResponse> getAll(User user) {
        return categoryRepository.findByUser(user).stream()
                .map(CategoryResponse::from)
                .toList();
    }

    @Transactional
    public CategoryResponse create(User user, CategoryRequest req) {
        Category category = Category.builder()
                .user(user)
                .name(sanitize(req.name))
                .color(sanitize(req.color))
                .icon(sanitize(req.icon))
                .build();
        return CategoryResponse.from(categoryRepository.save(category));
    }

    @Transactional
    public CategoryResponse update(User user, String id, CategoryRequest req) {
        Category category = categoryRepository.findByIdAndUser(id, user)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found"));
        category.setName(sanitize(req.name));
        category.setColor(sanitize(req.color));
        category.setIcon(sanitize(req.icon));
        return CategoryResponse.from(categoryRepository.save(category));
    }

    @Transactional
    public void delete(User user, String id) {
        categoryRepository.findByIdAndUser(id, user)
                .ifPresent(categoryRepository::delete);
    }

    private String sanitize(String input) {
        return input != null ? input.trim() : null;
    }
}
