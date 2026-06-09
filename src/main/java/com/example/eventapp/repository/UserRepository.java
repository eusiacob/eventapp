package com.example.eventapp.repository;

import com.example.eventapp.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository <User, Long> {
    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);

    @Query("SELECT u FROM User u JOIN u.favoriteBusinesses b WHERE b.id = :businessId")
    List<User> findUsersWhoFavoritedBusiness(@Param("businessId") Long businessId);
}