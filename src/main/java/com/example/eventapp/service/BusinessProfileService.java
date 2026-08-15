package com.example.eventapp.service;

import com.example.eventapp.model.*;
import com.example.eventapp.repository.BusinessProfileRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

@Service
public class BusinessProfileService {

    private final BusinessProfileRepository businessProfileRepository;
    private final SubscriptionService subscriptionService;

    public BusinessProfileService(BusinessProfileRepository businessProfileRepository, SubscriptionService subscriptionService) {
        this.businessProfileRepository = businessProfileRepository;
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
        businessProfileRepository.save(businessProfile);
    }

    public List<String> getCitiesByCategory(BusinessCategory category) {
        return businessProfileRepository.findDistinctCitiesByCategory(category);
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

    public List<BusinessProfile> searchAvailableByCategoryNameCityAndDate(
            BusinessCategory category,
            String keyword,
            String city,
            LocalDate eventDate
    ) {
        return businessProfileRepository.searchAvailableByCategoryNameCityAndDate(
                category,
                keyword,
                city,
                eventDate
        );
    }

    public List<BusinessProfile> findByUser(User user) {
        return businessProfileRepository.findByUser(user);
    }

    public List<BusinessProfile> getPremiumBusinesses() {
        return businessProfileRepository.findTop10ByPremiumTrue();
    }

    //    Top servicii favorite
    public List<BusinessProfile> getMostFavoriteBusinesses() {

        Pageable pageable = PageRequest.of(0, 8);

        return businessProfileRepository
                .findMostFavoriteBusinesses(pageable);
    }

    //    Top reviews
    public List<BusinessProfile> getTopRatedBusinesses() {

        Pageable pageable = PageRequest.of(0, 8);

        return businessProfileRepository
                .findTopRatedBusinesses(pageable);
    }

    public void activateStandardBusiness(
            String uuid,
            User user
    ) {

        BusinessProfile selected =
                findByIdAndValidateOwner(uuid, user);

        List<BusinessProfile> profiles =
                businessProfileRepository.findByUser(user);

        for (BusinessProfile profile : profiles) {

            profile.setActive(false);
            profile.setPremium(false);
        }

        selected.setActive(true);
        selected.setPremium(false);

        businessProfileRepository.saveAll(profiles);
    }

    //    Delete business
    public void delete(Long id) {
        businessProfileRepository.deleteById(id);
    }

}