package com.subtrak.repository;

import com.subtrak.entity.Category;
import com.subtrak.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CategoryRepository extends JpaRepository<Category, String> {

    List<Category> findByUser(User user);

    Optional<Category> findByIdAndUser(String id, User user);
}
