package com.example.eventapp.service;

import com.example.eventapp.model.*;
import com.example.eventapp.repository.BusinessProfileRepository;
import com.example.eventapp.repository.ReviewRepository;
import com.example.eventapp.repository.SubscriptionRepository;
import com.example.eventapp.repository.SupportMessageRepository;
import com.example.eventapp.repository.SupportTicketRepository;
import com.example.eventapp.repository.UserNotificationRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

@Service
public class PersonalDataExportService {

    private final UserService userService;
    private final BusinessProfileRepository businessProfileRepository;
    private final ReviewRepository reviewRepository;
    private final SubscriptionRepository subscriptionRepository;
    private final UserNotificationRepository userNotificationRepository;
    private final SupportTicketRepository supportTicketRepository;
    private final SupportMessageRepository supportMessageRepository;

    public PersonalDataExportService(
            UserService userService,
            BusinessProfileRepository businessProfileRepository,
            ReviewRepository reviewRepository,
            SubscriptionRepository subscriptionRepository,
            UserNotificationRepository userNotificationRepository,
            SupportTicketRepository supportTicketRepository,
            SupportMessageRepository supportMessageRepository
    ) {
        this.userService = userService;
        this.businessProfileRepository = businessProfileRepository;
        this.reviewRepository = reviewRepository;
        this.subscriptionRepository = subscriptionRepository;
        this.userNotificationRepository = userNotificationRepository;
        this.supportTicketRepository = supportTicketRepository;
        this.supportMessageRepository = supportMessageRepository;
    }

    @Transactional(readOnly = true)
    public PersonalDataExport exportFor(User user) {
        User account = userService.findById(user.getId());

        return new PersonalDataExport(
                LocalDateTime.now(),
                new AccountData(
                        account.getFirstName(),
                        account.getLastName(),
                        userService.getEmailAddress(account),
                        userService.getPhoneNumber(account),
                        account.getRole().name(),
                        account.getCreatedAt(),
                        account.getLastActivityAt(),
                        account.getPrivacyPolicyVersion(),
                        account.getPrivacyPolicyAcceptedAt(),
                        account.getTermsVersion(),
                        account.getTermsAcceptedAt()
                ),
                favoriteData(account.getFavoriteBusinesses()),
                businessData(account),
                reviewData(account),
                subscriptionData(account),
                notificationData(account),
                supportData(account)
        );
    }

    private List<FavoriteData> favoriteData(Set<BusinessProfile> favorites) {
        if (favorites == null) {
            return List.of();
        }

        return favorites.stream()
                .map(business -> new FavoriteData(
                        business.getUuid(),
                        business.getName(),
                        business.getCategory().name(),
                        business.getCity()
                ))
                .toList();
    }

    private List<BusinessData> businessData(User user) {
        return businessProfileRepository.findByUser(user).stream()
                .map(business -> new BusinessData(
                        business.getUuid(),
                        business.getName(),
                        business.getCategory().name(),
                        business.getDescription(),
                        business.getCity(),
                        business.getPhone(),
                        business.getEmail(),
                        business.getWebsite(),
                        business.getImagePath(),
                        business.getGalleryImages().stream()
                                .map(BusinessImage::getImagePath)
                                .toList(),
                        business.getGalleryVideos().stream()
                                .map(BusinessVideo::getVideoPath)
                                .toList(),
                        business.getStatus().name(),
                        business.getRejectionReason(),
                        business.getCreatedAt()
                ))
                .toList();
    }

    private List<ReviewData> reviewData(User user) {
        return reviewRepository.findByUserOrderByCreatedAtDesc(user).stream()
                .map(review -> new ReviewData(
                        review.getId(),
                        review.getBusinessProfile().getUuid(),
                        review.getBusinessProfile().getName(),
                        review.getRating(),
                        review.getComment(),
                        review.getReviewStatus().name(),
                        review.getRejectionReason(),
                        review.getCreatedAt()
                ))
                .toList();
    }

    private List<SubscriptionData> subscriptionData(User user) {
        return subscriptionRepository.findAllByUserOrderByCreatedAtDesc(user).stream()
                .map(subscription -> new SubscriptionData(
                        subscription.getStatus().name(),
                        subscription.getPlan().getType().name(),
                        subscription.getPlan().getDuration().name(),
                        subscription.getPrice(),
                        subscription.getStartDate(),
                        subscription.getEndDate(),
                        subscription.getCreatedAt()
                ))
                .toList();
    }

    private List<NotificationData> notificationData(User user) {
        return userNotificationRepository.findByUserOrderByCreatedAtDesc(user).stream()
                .map(notification -> new NotificationData(
                        notification.getTitle(),
                        notification.getMessage(),
                        notification.getLink(),
                        notification.isRead(),
                        notification.getCreatedAt()
                ))
                .toList();
    }

    private List<SupportTicketData> supportData(User user) {
        return supportTicketRepository.findAllByUserOrderByUpdatedAtDesc(user).stream()
                .map(ticket -> new SupportTicketData(
                        ticket.getId(),
                        ticket.getSubject(),
                        ticket.getCategory().name(),
                        ticket.getStatus().name(),
                        ticket.getCreatedAt(),
                        ticket.getUpdatedAt(),
                        messageData(ticket)
                ))
                .toList();
    }

    private List<SupportMessageData> messageData(SupportTicket ticket) {
        return supportMessageRepository.findAllByTicketOrderByCreatedAtAsc(ticket).stream()
                .map(message -> new SupportMessageData(
                        message.getMessage(),
                        message.getCreatedAt(),
                        message.getUser().getId().equals(ticket.getUser().getId())
                                ? "utilizator"
                                : "administrator"
                ))
                .toList();
    }

    public record PersonalDataExport(
            LocalDateTime exportedAt,
            AccountData account,
            List<FavoriteData> favorites,
            List<BusinessData> services,
            List<ReviewData> reviews,
            List<SubscriptionData> subscriptions,
            List<NotificationData> notifications,
            List<SupportTicketData> supportTickets
    ) {
    }

    public record AccountData(
            String firstName,
            String lastName,
            String email,
            String phone,
            String role,
            LocalDateTime createdAt,
            LocalDateTime lastLoginAt,
            String privacyPolicyVersion,
            LocalDateTime privacyPolicyAcceptedAt,
            String termsVersion,
            LocalDateTime termsAcceptedAt
    ) {
    }

    public record FavoriteData(
            String serviceId,
            String name,
            String category,
            String city
    ) {
    }

    public record BusinessData(
            String serviceId,
            String name,
            String category,
            String description,
            String city,
            String phone,
            String email,
            String website,
            String coverImage,
            List<String> galleryImages,
            List<String> galleryVideos,
            String status,
            String rejectionReason,
            LocalDate createdAt
    ) {
    }

    public record ReviewData(
            Long id,
            String serviceId,
            String serviceName,
            int rating,
            String comment,
            String status,
            String rejectionReason,
            LocalDateTime createdAt
    ) {
    }

    public record SubscriptionData(
            String status,
            String planType,
            String duration,
            BigDecimal price,
            LocalDateTime startDate,
            LocalDateTime endDate,
            LocalDateTime createdAt
    ) {
    }

    public record NotificationData(
            String title,
            String message,
            String link,
            boolean read,
            LocalDateTime createdAt
    ) {
    }

    public record SupportTicketData(
            Long id,
            String subject,
            String category,
            String status,
            LocalDateTime createdAt,
            LocalDateTime updatedAt,
            List<SupportMessageData> messages
    ) {
    }

    public record SupportMessageData(
            String message,
            LocalDateTime createdAt,
            String authorType
    ) {
    }
}
