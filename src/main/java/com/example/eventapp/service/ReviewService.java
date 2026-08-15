package com.example.eventapp.service;

import com.example.eventapp.model.BusinessProfile;
import com.example.eventapp.model.Review;
import com.example.eventapp.model.User;
import com.example.eventapp.repository.ReviewRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final BusinessProfileService businessProfileService;
    private final UserService userService;
    private final UserNotificationService userNotificationService;

    public ReviewService(ReviewRepository reviewRepository,
                         BusinessProfileService businessProfileService,
                         UserService userService, UserNotificationService userNotificationService) {
        this.reviewRepository = reviewRepository;
        this.businessProfileService = businessProfileService;
        this.userService = userService;
        this.userNotificationService = userNotificationService;
    }

    public List<Review> findAll() {

        return reviewRepository.findAllByOrderByCreatedAtDesc();

    }

    public long countByUser(User user) {

        return reviewRepository.countByUser(user);

    }

    public List<Review> findUserReviews(User user) {

        return reviewRepository
                .findByUserOrderByCreatedAtDesc(user);
    }

    public double getUserAverageRating(User user) {

        Double average =
                reviewRepository.getAverageRatingByUser(user);

        return average != null ? average : 0.0;
    }

    public List<Review> findByStatus(Review.ReviewStatus status) {
        return reviewRepository.findByReviewStatusOrderByCreatedAtDesc(status);
    }

    public List<Review> findLatestByUser(User user) {

        return reviewRepository.findTop5ByUserOrderByCreatedAtDesc(user);

    }

    public void addReview(String businessId, String userEmail, Review review) {
        BusinessProfile business = businessProfileService.findByUuid(businessId);
        User user = userService.findByEmail(userEmail);

        boolean alreadyReviewed = reviewRepository
                .findByBusinessProfileAndUser(business, user)
                .isPresent();

        if (alreadyReviewed) {
            throw new RuntimeException("You already reviewed this business");
        }

        review.setId(null);
        review.setBusinessProfile(business);
        review.setUser(user);

        reviewRepository.save(review);

        userNotificationService.notifyAdminsNewReview(review);
    }

    public Review findById(Long reviewId) {
        return reviewRepository.findById(reviewId)
                .orElseThrow(() -> new RuntimeException("Review not found"));
    }

    public List<Review> getReviewsForBusiness(BusinessProfile businessProfile) {
        return reviewRepository.findByBusinessProfileAndReviewStatus(businessProfile, Review.ReviewStatus.APPROVED);
    }

    public double getAverageRating(BusinessProfile businessProfile) {
        List<Review> reviews = reviewRepository.findByBusinessProfileAndReviewStatus(businessProfile, Review.ReviewStatus.APPROVED);

        if (reviews.isEmpty()) {
            return 0.0;
        }

        return reviews.stream()
                .mapToInt(Review::getRating)
                .average()
                .orElse(0.0);
    }

    public long getReviewCount(BusinessProfile businessProfile) {
        return reviewRepository.findByBusinessProfileAndReviewStatus(businessProfile, Review.ReviewStatus.APPROVED).size();
    }

    public boolean hasUserReviewed(String businessUuid, String userEmail) {
        BusinessProfile business = businessProfileService.findByUuid(businessUuid);
        User user = userService.findByEmail(userEmail);

        return reviewRepository.findByBusinessProfileAndUser(business, user).isPresent();
    }

    public boolean hasPendingReview(
            String businessUuid,
            String email
    ) {

        User user = userService.findByEmail(email);

        return reviewRepository.existsByBusinessProfileUuidAndUserAndReviewStatus(
                businessUuid,
                user,
                Review.ReviewStatus.PENDING
        );
    }

    public void approveReview(Long id) {

        Review review = findById(id);

        review.setReviewStatus(Review.ReviewStatus.APPROVED);

        review.setRejectionReason(null);

        reviewRepository.save(review);

        userNotificationService.notifyReviewApproved(review);
    }

    public void rejectReview(Long id, String reason) {

        Review review = findById(id);

        review.setReviewStatus(Review.ReviewStatus.REJECTED);

        if (reason == null || reason.isBlank()) {
            throw new IllegalArgumentException(
                    "Motivul respingerii este obligatoriu."
            );
        }

        review.setRejectionReason(reason.trim());

        reviewRepository.save(review);

        userNotificationService.notifyReviewRejected(review);

    }
}