package com.example.eventapp.repository;

import com.example.eventapp.model.BusinessProfile;
import com.example.eventapp.model.Review;
import com.example.eventapp.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ReviewRepository extends JpaRepository<Review, Long> {

    Optional<Review> findByBusinessProfileAndUser(BusinessProfile businessProfile, User user);

    long countByUser(User user);

    List<Review> findAllByOrderByCreatedAtDesc();

    List<Review> findByUserOrderByCreatedAtDesc(User user);

    void deleteByUser(User user);

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

    @Query("""
            SELECT r
            FROM Review r
            JOIN r.businessProfile b
            JOIN r.user u
            WHERE (:status IS NULL OR r.reviewStatus = :status)
            AND (
                :search IS NULL OR :search = ''
                OR LOWER(b.name) LIKE LOWER(CONCAT('%', :search, '%'))
                OR LOWER(u.firstName) LIKE LOWER(CONCAT('%', :search, '%'))
                OR LOWER(u.lastName) LIKE LOWER(CONCAT('%', :search, '%'))
                OR LOWER(CONCAT(CONCAT(u.firstName, ' '), u.lastName)) LIKE LOWER(CONCAT('%', :search, '%'))
            )
            ORDER BY r.createdAt DESC
            """)
    List<Review> searchForAdmin(
            @Param("status") Review.ReviewStatus status,
            @Param("search") String search
    );

    List<Review> findTop5ByUserOrderByCreatedAtDesc(User user);

    List<Review> findByBusinessProfileAndReviewStatus(
            BusinessProfile businessProfile,
            Review.ReviewStatus status);

    Page<Review> findByBusinessProfileAndReviewStatusOrderByCreatedAtDesc(
            BusinessProfile businessProfile,
            Review.ReviewStatus status,
            Pageable pageable
    );

    long countByBusinessProfileAndReviewStatus(
            BusinessProfile businessProfile,
            Review.ReviewStatus status
    );

    @Query("""
            SELECT AVG(r.rating)
            FROM Review r
            WHERE r.businessProfile = :businessProfile
            AND r.reviewStatus = :status
            """)
    Double getAverageRatingByBusinessProfileAndReviewStatus(
            BusinessProfile businessProfile,
            Review.ReviewStatus status
    );

    boolean existsByBusinessProfileUuidAndUserAndReviewStatus(
            String uuid,
            User user,
            Review.ReviewStatus status
    );

    long countByReviewStatus(Review.ReviewStatus status);
}
