package com.example.eventapp.service;

import com.example.eventapp.model.BusinessProfile;
import com.example.eventapp.repository.BusinessProfileRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class BusinessProfileService {

    private final BusinessProfileRepository repository;

    public BusinessProfileService(BusinessProfileRepository repository) {

        this.repository = repository;

    }

    public void save(BusinessProfile profile) {
        repository.save(profile);
    }

    public BusinessProfile findById(Long id) {
        return repository.findById(id).orElseThrow();
    }

    public List<BusinessProfile> search(String category, String city, String keyword) {

        return repository.findByCategoryContainingIgnoreCaseAndCityContainingIgnoreCaseAndNameContainingIgnoreCase(category, city, keyword);
    }

    public List<String> getCategories() {
        return repository.findDistinctCategories();
    }

    public List<String> getCities() {
        return repository.findDistinctCities();

    }

    public void delete(Long id) {
        repository.deleteById(id);
    }
}