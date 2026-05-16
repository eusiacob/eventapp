package com.example.eventapp.repository;

import com.example.eventapp.model.BusinessProfile;
import com.example.eventapp.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface BusinessProfileRepository extends JpaRepository<BusinessProfile, Long> {
    List<BusinessProfile> findByCategoryContainingIgnoreCase(String category);

    List<BusinessProfile> findByCityContainingIgnoreCase(String city);

    List<BusinessProfile> findByCategoryContainingIgnoreCaseAndCityContainingIgnoreCase(String category, String city);

    Optional<BusinessProfile> findByUser(User user);
}
