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

    public ReviewService(ReviewRepository reviewRepository,
                         BusinessProfileService businessProfileService,
                         UserService userService) {
        this.reviewRepository = reviewRepository;
        this.businessProfileService = businessProfileService;
        this.userService = userService;
    }

    public List<Review> findAll() {
        return reviewRepository.findAllByOrderByCreatedAtDesc();
    }

    public List<Review> findByStatus(Review.ReviewStatus status) {
        return reviewRepository.findByReviewStatusOrderByCreatedAtDesc(status);
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
    }

    public Review findById(Long reviewId) {
        return reviewRepository.findById(reviewId)
                .orElseThrow(() -> new RuntimeException("Review not found"));
    }

    public boolean isOwner(Review review, String userEmail) {
        return review.getUser() != null
                && review.getUser().getEmail().equals(userEmail);
    }

    public Review findByIdAndValidateOwner(Long reviewId, String userEmail) {
        Review review = findById(reviewId);

        if (!isOwner(review, userEmail)) {
            throw new RuntimeException("You are not allowed to modify this review");
        }

        return review;
    }

    public void updateReview(Long reviewId, String userEmail, Review updatedReview) {
        Review existingReview = findByIdAndValidateOwner(reviewId, userEmail);

        existingReview.setRating(updatedReview.getRating());
        existingReview.setComment(updatedReview.getComment());
        existingReview.setReviewStatus(Review.ReviewStatus.PENDING);

        reviewRepository.save(existingReview);
    }

    public Long deleteReview(Long reviewId, String userEmail) {
        Review review = findByIdAndValidateOwner(reviewId, userEmail);

        Long businessId = review.getBusinessProfile().getId();

        reviewRepository.delete(review);

        return businessId;
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

        reviewRepository.save(review);

    }

    public void rejectReview(Long id) {

        Review review = findById(id);

        review.setReviewStatus(Review.ReviewStatus.REJECTED);

        reviewRepository.save(review);

    }
}