package com.example.eventapp.service;

import com.example.eventapp.model.BusinessProfile;
import com.example.eventapp.model.Review;
import com.example.eventapp.model.User;
import com.example.eventapp.repository.ReviewRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.time.LocalDateTime;

@Service
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final BusinessProfileService businessProfileService;
    private final UserService userService;
    private final UserNotificationService userNotificationService;
    private final EmailService emailService;

    public ReviewService(ReviewRepository reviewRepository,
                         BusinessProfileService businessProfileService,
                         UserService userService,
                         UserNotificationService userNotificationService,
                         EmailService emailService) {
        this.reviewRepository = reviewRepository;
        this.businessProfileService = businessProfileService;
        this.userService = userService;
        this.userNotificationService = userNotificationService;
        this.emailService = emailService;
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

    @Transactional
    public void addReview(String businessId, String userEmail, Review review) {
        BusinessProfile business = businessProfileService.findByUuid(businessId);
        User user = userService.findByEmail(userEmail);

        boolean alreadyReviewed = reviewRepository
                .findByBusinessProfileAndUser(business, user)
                .isPresent();

        if (alreadyReviewed) {
            throw new IllegalStateException("Ai trimis deja o recenzie pentru acest serviciu.");
        }

        review.setId(null);
        review.setBusinessProfile(business);
        review.setUser(user);
        // Moderation state and metadata are server-controlled; ignore client values.
        review.setReviewStatus(Review.ReviewStatus.PENDING);
        review.setRejectionReason(null);
        review.setCreatedAt(LocalDateTime.now());

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

    public Page<Review> getReviewsForBusiness(
            BusinessProfile businessProfile,
            int page,
            int size
    ) {
        return reviewRepository.findByBusinessProfileAndReviewStatusOrderByCreatedAtDesc(
                businessProfile,
                Review.ReviewStatus.APPROVED,
                PageRequest.of(Math.max(page, 0), size)
        );
    }

    public double getAverageRating(BusinessProfile businessProfile) {
        Double average = reviewRepository
                .getAverageRatingByBusinessProfileAndReviewStatus(
                        businessProfile,
                        Review.ReviewStatus.APPROVED
                );

        return average != null ? average : 0.0;
    }

    public long getReviewCount(BusinessProfile businessProfile) {
        return reviewRepository.countByBusinessProfileAndReviewStatus(
                businessProfile,
                Review.ReviewStatus.APPROVED
        );
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
        boolean newlyApproved = review.getReviewStatus() != Review.ReviewStatus.APPROVED;

        review.setReviewStatus(Review.ReviewStatus.APPROVED);

        review.setRejectionReason(null);

        reviewRepository.save(review);

        if (newlyApproved) {
            userNotificationService.notifyReviewApproved(review);
            userNotificationService.notifyBusinessOwnerReviewApproved(review);
        }
        emailService.sendReviewApprovedEmail(
                userService.getEmailAddress(review.getUser()),
                review.getUser().getFirstName(),
                review.getBusinessProfile().getName()
        );
    }

    public void rejectReview(Long id, String reason) {

        if (reason == null || reason.isBlank()) {
            throw new IllegalArgumentException(
                    "Motivul respingerii este obligatoriu."
            );
        }

        String normalizedReason = reason.trim();
        if (normalizedReason.length() > 500) {
            throw new IllegalArgumentException(
                    "Motivul respingerii nu poate depăși 500 de caractere."
            );
        }

        Review review = findById(id);

        review.setReviewStatus(Review.ReviewStatus.REJECTED);

        review.setRejectionReason(normalizedReason);

        reviewRepository.save(review);

        userNotificationService.notifyReviewRejected(review);
        emailService.sendReviewRejectedEmail(
                userService.getEmailAddress(review.getUser()),
                review.getUser().getFirstName(),
                review.getBusinessProfile().getName(),
                review.getRejectionReason()
        );

    }
}
