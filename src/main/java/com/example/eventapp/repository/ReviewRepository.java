package com.example.eventapp.repository;

import com.example.eventapp.model.BusinessProfile;
import com.example.eventapp.model.Review;
import com.example.eventapp.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface ReviewRepository extends JpaRepository<Review, Long> {

    Optional<Review> findByBusinessProfileAndUser(BusinessProfile businessProfile, User user);

    long countByUser(User user);

    List<Review> findAllByOrderByCreatedAtDesc();

    List<Review> findByUserOrderByCreatedAtDesc(User user);

    @Query("""
        SELECT AVG(r.rating)
        FROM Review r
        WHERE r.user = :user
        AND r.reviewStatus = com.example.eventapp.model.Review.ReviewStatus.APPROVED
        """)
    Double getAverageRatingByUser(User user);

    List<Review> findByReviewStatusOrderByCreatedAtDesc(
            Review.ReviewStatus status
    );

    List<Review> findTop5ByUserOrderByCreatedAtDesc(User user);

    List<Review> findByBusinessProfileAndReviewStatus(
            BusinessProfile businessProfile,
            Review.ReviewStatus status);

    boolean existsByBusinessProfileUuidAndUserAndReviewStatus(
            String uuid,
            User user,
            Review.ReviewStatus status
    );

    long countByReviewStatus(Review.ReviewStatus status);
}