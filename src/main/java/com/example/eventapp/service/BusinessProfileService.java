package com.example.eventapp.service;

import com.example.eventapp.model.BusinessCategory;
import com.example.eventapp.model.BusinessProfile;
import com.example.eventapp.model.User;
import com.example.eventapp.repository.BusinessProfileRepository;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;

@Service
public class BusinessProfileService {

    private final BusinessProfileRepository businessProfileRepository;

    public BusinessProfileService(BusinessProfileRepository businessProfileRepository) {
        this.businessProfileRepository = businessProfileRepository;
    }

    public List<BusinessProfile> findAll() {
        return businessProfileRepository.findAll();
    }

    public BusinessProfile findById(Long id) {
        return businessProfileRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Business profile not found"));
    }

    public List<BusinessProfile> findByUser(User user) {
        return businessProfileRepository.findByUser(user);
    }

    public BusinessProfile save(BusinessProfile businessProfile) {
        return businessProfileRepository.save(businessProfile);
    }

    public void deleteById(Long id) {
        businessProfileRepository.deleteById(id);
    }

    public List<BusinessProfile> findByCategory(BusinessCategory category) {
        return businessProfileRepository.findByCategoryOrderByNameAsc(category);
    }

    public List<BusinessProfile> searchByCategoryNameAndCity(BusinessCategory category,
                                                             String keyword,
                                                             String city) {
        return businessProfileRepository.searchByCategoryNameAndCity(
                category,
                keyword,
                city
        );
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

    public BusinessProfile findByIdAndValidateOwner(Long profileId, User user) {
        BusinessProfile profile = findById(profileId);

        if (!isOwner(profile, user)) {
            throw new RuntimeException("You are not allowed to access this business profile");
        }

        return profile;
    }

    //    Delete business
    public void delete(Long id) {
        businessProfileRepository.deleteById(id);
    }
}