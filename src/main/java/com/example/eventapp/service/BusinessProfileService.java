package com.example.eventapp.service;

import com.example.eventapp.model.BusinessProfile;
import com.example.eventapp.model.User;
import com.example.eventapp.repository.BusinessProfileRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class BusinessProfileService {

    private final BusinessProfileRepository businessProfileRepository;

    public BusinessProfileService(BusinessProfileRepository businessProfileRepository) {
        this.businessProfileRepository = businessProfileRepository;
    }

    // Toate business-urile - folosit pentru /businesses la grupare pe categorii
    public List<BusinessProfile> findAll() {
        return businessProfileRepository.findAll();
    }

    // Caută un business după ID - folosit la details/edit/delete/reviews/contact
    public BusinessProfile findById(Long id) {
        return businessProfileRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Business profile not found"));
    }

    // Business-urile unui user business - folosit la dashboard
    public List<BusinessProfile> findByUser(User user) {
        return businessProfileRepository.findByUser(user);
    }

    // Salvare business profile
    public BusinessProfile save(BusinessProfile businessProfile) {
        return businessProfileRepository.save(businessProfile);
    }

    // Ștergere business profile
    public void deleteById(Long id) {
        businessProfileRepository.deleteById(id);
    }

    // Pentru pagina /businesses/category/{category}
    public List<BusinessProfile> findByCategory(String category) {
        return businessProfileRepository.findByCategoryIgnoreCaseOrderByNameAsc(category);
    }

    // Pentru search în pagina categoriei după nume și oraș
    public List<BusinessProfile> searchByCategoryNameAndCity(String category,
                                                             String keyword,
                                                             String city) {
        return businessProfileRepository.searchByCategoryNameAndCity(
                category,
                keyword,
                city
        );
    }

    // Dropdown orașe doar pentru categoria selectată
    public List<String> getCitiesByCategory(String category) {
        return businessProfileRepository.findDistinctCitiesByCategory(category);
    }

    // Dropdown global categorii, dacă va mai fi nevoie
    public List<String> getCategories() {
        return businessProfileRepository.findDistinctCategories();
    }

    // Dropdown global orașe, dacă va mai fi nevoie
    public List<String> getCities() {
        return businessProfileRepository.findDistinctCities();
    }

    // Ownership validation
    public boolean isOwner(BusinessProfile businessProfile, User user) {
        return businessProfile.getUser() != null
                && businessProfile.getUser().getId().equals(user.getId());
    }

    // Variantă pentru edit/delete
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