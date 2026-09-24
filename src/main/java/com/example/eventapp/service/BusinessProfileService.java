package com.example.eventapp.service;

import com.example.eventapp.model.*;
import com.example.eventapp.repository.BusinessProfileRepository;
import com.example.eventapp.repository.EmailVerificationTokenRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.access.AccessDeniedException;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.List;

@Service
public class BusinessProfileService {

    private final BusinessProfileRepository businessProfileRepository;
    private final EmailVerificationTokenRepository emailVerificationTokenRepository;
    private final BusinessStorageCleanupService businessStorageCleanupService;
    private final SubscriptionService subscriptionService;

    public BusinessProfileService(
            BusinessProfileRepository businessProfileRepository,
            EmailVerificationTokenRepository emailVerificationTokenRepository,
            BusinessStorageCleanupService businessStorageCleanupService,
            SubscriptionService subscriptionService
    ) {
        this.businessProfileRepository = businessProfileRepository;
        this.emailVerificationTokenRepository = emailVerificationTokenRepository;
        this.businessStorageCleanupService = businessStorageCleanupService;
        this.subscriptionService = subscriptionService;
    }

    public BusinessProfile findByUuid(String uuid){

        return businessProfileRepository
                .findByUuid(uuid)
                .orElseThrow(() ->
                        new RuntimeException("Serviciul nu există."));
    }

    public BusinessProfile findById(Long id) {
        return businessProfileRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Serviciul nu există."));
    }

    public void save(BusinessProfile businessProfile) {
        if (!businessProfile.isServiceTypesValid() || !businessProfile.isOtherServiceDetailsValid()) {
            throw new IllegalArgumentException("Verifică tipurile de servicii și detaliile pentru Alt serviciu.");
        }
        if (businessProfile.getServiceTypes().stream().noneMatch(BusinessServiceType::isOther)) {
            businessProfile.setOtherServiceDetails(null);
        }
        businessProfile.normalizeServiceArea();
        businessProfileRepository.save(businessProfile);
    }

    public List<String> getCitiesByCategory(BusinessCategory category) {
        // Nationwide providers must be discoverable for any county.
        return RomanianCounties.ALL;
    }

    public List<BusinessCategory> getCategories() {
        return Arrays.asList(BusinessCategory.values());
    }

    public boolean isOwner(BusinessProfile businessProfile, User user) {
        return businessProfile.getUser() != null
                && businessProfile.getUser().getId().equals(user.getId());
    }

    public BusinessProfile findByIdAndValidateOwner(String uuid, User user) {
        BusinessProfile profile = findByUuid(uuid);

        if (!isOwner(profile, user)) {
            throw new RuntimeException("You are not allowed to access this business profile");
        }

        return profile;
    }

    public boolean canCreateBusinessProfile(User user) {

        List<BusinessProfile> profiles =
                businessProfileRepository.findByUser(user);

        Subscription subscription =
                subscriptionService.findActiveSubscription(user);

        if (subscription == null) {
            return false;
        }

        if (subscription.getPlan().getType()
                == SubscriptionPlan.SubscriptionType.PREMIUM) {

            return true;
        }

        return profiles.isEmpty();
    }

    public BusinessProfile findByUuidAndValidateOwner(
            String uuid,
            User user
    ) {

        BusinessProfile profile = findByUuid(uuid);

        if (!profile.getUser().getId()
                .equals(user.getId())) {

            throw new RuntimeException("Nu ai permisiunea.");}


        return profile;
    }

    public Page<BusinessProfile> searchAvailableByCategoryNameCityAndDate(
            BusinessCategory category,
            String keyword,
            String city,
            LocalDate eventDate,
            BusinessServiceType serviceType,
            BusinessEventType eventType,
            int page,
            int size
    ) {
        return businessProfileRepository.searchAvailableByCategoryNameCityAndDate(
                category,
                keyword,
                city,
                eventDate,
                serviceType,
                eventType,
                PageRequest.of(Math.max(page, 0), size)
        );
    }

    public List<BusinessProfile> findByUser(User user) {
        return businessProfileRepository.findByUser(user);
    }

    public List<BusinessProfile> getPremiumBusinesses() {
        return businessProfileRepository.findTop10ByPremiumTrue();
    }

    public List<BusinessProfile> getRecentBusinesses() {
        return businessProfileRepository.findRecentBusinesses(PageRequest.of(0, 10));
    }

    //    Top servicii favorite
    public List<BusinessProfile> getMostFavoriteBusinesses() {

        Pageable pageable = PageRequest.of(0, 10);

        return businessProfileRepository
                .findMostFavoriteBusinesses(pageable);
    }

    //    Top reviews
    public List<BusinessProfile> getTopRatedBusinesses() {

        Pageable pageable = PageRequest.of(0, 10);

        return businessProfileRepository
                .findTopRatedBusinesses(pageable);
    }

    @Transactional
    public VisibilityUpdate updateVisibility(
            String uuid,
            User user,
            boolean active
    ) {
        BusinessProfile selected = findByUuid(uuid);
        if (!isOwner(selected, user)) {
            throw new AccessDeniedException("Nu ai permisiunea să modifici acest serviciu.");
        }

        if (!active) {
            selected.setActive(false);
            businessProfileRepository.save(selected);
            return new VisibilityUpdate(false, List.of());
        }

        if (selected.getStatus() != BusinessProfile.BusinessStatus.APPROVED) {
            throw new IllegalArgumentException(
                    "Serviciul poate deveni public numai după aprobare."
            );
        }

        Subscription subscription = subscriptionService.findActiveSubscription(user);
        List<String> deactivatedUuids = new ArrayList<>();
        if (subscription != null
                && subscription.getPlan().getType()
                == SubscriptionPlan.SubscriptionType.STANDARD) {
            List<BusinessProfile> profiles = businessProfileRepository.findByUser(user);
            for (BusinessProfile profile : profiles) {
                if (!profile.getId().equals(selected.getId()) && profile.isActive()) {
                    profile.setActive(false);
                    deactivatedUuids.add(profile.getUuid());
                }
            }
            businessProfileRepository.saveAll(profiles);
        }

        selected.setActive(true);
        businessProfileRepository.save(selected);
        return new VisibilityUpdate(true, List.copyOf(deactivatedUuids));
    }

    public record VisibilityUpdate(
            boolean active,
            List<String> deactivatedUuids
    ) {
    }

    //    Delete business
    @Transactional
    public void delete(BusinessProfile businessProfile) {
        emailVerificationTokenRepository.deleteByBusinessProfile(businessProfile);
        businessProfileRepository.delete(businessProfile);
        businessStorageCleanupService.deleteAfterCommit(businessProfile.getUuid());
    }

}
