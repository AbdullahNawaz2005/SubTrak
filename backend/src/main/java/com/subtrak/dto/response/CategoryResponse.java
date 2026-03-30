package com.subtrak.dto.response;

import com.subtrak.entity.Category;

public record CategoryResponse(
        String id,
        String name,
        String color,
        String icon
) {
    public static CategoryResponse from(Category c) {
        return new CategoryResponse(
                c.getId(),
                c.getName(),
                c.getColor(),
                c.getIcon()
        );
    }
}
