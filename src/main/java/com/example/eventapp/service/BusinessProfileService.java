package com.example.eventapp.service;

import com.example.eventapp.model.BusinessProfile;
import com.example.eventapp.model.User;
import com.example.eventapp.repository.BusinessProfileRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class BusinessProfileService {

    private final BusinessProfileRepository repository;

    public BusinessProfileService(BusinessProfileRepository repository) {

        this.repository = repository;

    }

    public void createBusiness(BusinessProfile profile, User user) {

        profile.setUser(user);

        repository.save(profile);
    }

    public BusinessProfile save(BusinessProfile profile) {

        return repository.save(profile);
    }

    public List<BusinessProfile> findAll() {
        return repository.findAll();
    }

    public BusinessProfile findById(Long id) {
        return repository.findById(id).orElseThrow();
    }

    public List<BusinessProfile> search(String category, String city) {

        if (!category.isEmpty() && !city.isEmpty()) {
            return repository.findByCategoryContainingIgnoreCaseAndCityContainingIgnoreCase(category, city);
        }

        if (!category.isEmpty()) {
            return repository.findByCategoryContainingIgnoreCase(category);
        }

        if (!city.isEmpty()) {
            return repository.findByCityContainingIgnoreCase(city);
        }

        return repository.findAll();
    }

    public BusinessProfile findByUser(User user) {
        return repository.findByUser(user).orElseThrow();
    }

    public void delete(Long id) {
        repository.deleteById(id);
    }
}