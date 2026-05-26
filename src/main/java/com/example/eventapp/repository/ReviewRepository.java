package com.example.eventapp.repository;

import com.example.eventapp.model.BusinessProfile;
import com.example.eventapp.model.Review;
import com.example.eventapp.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ReviewRepository extends JpaRepository<Review, Long> {

    List<Review> findByBusinessProfile(BusinessProfile businessProfile);

    Optional<Review> findByBusinessProfileAndUser(BusinessProfile businessProfile, User user);

    long countByBusinessProfile(BusinessProfile businessProfile);
}
