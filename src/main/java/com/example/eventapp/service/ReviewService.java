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

    public void addReview(Long businessId, String userEmail, Review review) {
        BusinessProfile business = businessProfileService.findById(businessId);
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

        reviewRepository.save(existingReview);
    }

    public Long deleteReview(Long reviewId, String userEmail) {
        Review review = findByIdAndValidateOwner(reviewId, userEmail);

        Long businessId = review.getBusinessProfile().getId();

        reviewRepository.delete(review);

        return businessId;
    }

    public List<Review> getReviewsForBusiness(BusinessProfile businessProfile) {
        return reviewRepository.findByBusinessProfile(businessProfile);
    }

    public double getAverageRating(BusinessProfile businessProfile) {
        List<Review> reviews = reviewRepository.findByBusinessProfile(businessProfile);

        if (reviews.isEmpty()) {
            return 0.0;
        }

        return reviews.stream()
                .mapToInt(Review::getRating)
                .average()
                .orElse(0.0);
    }

    public long getReviewCount(BusinessProfile businessProfile) {
        return reviewRepository.findByBusinessProfile(businessProfile).size();
    }

    public boolean hasUserReviewed(Long businessId, String userEmail) {
        BusinessProfile business = businessProfileService.findById(businessId);
        User user = userService.findByEmail(userEmail);

        return reviewRepository.findByBusinessProfileAndUser(business, user).isPresent();
    }
}