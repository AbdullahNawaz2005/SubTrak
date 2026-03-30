package com.subtrak.repository;

import com.subtrak.entity.Subscription;
import com.subtrak.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface SubscriptionRepository extends JpaRepository<Subscription, String> {

    List<Subscription> findByUserAndActiveTrue(User user);

    List<Subscription> findByUserAndActiveTrueAndNextRenewalDateBetween(
            User user, LocalDate start, LocalDate end);

    Optional<Subscription> findByIdAndUser(String id, User user);
}
