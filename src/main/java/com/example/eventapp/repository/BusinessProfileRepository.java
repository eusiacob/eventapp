package com.example.eventapp.repository;

import com.example.eventapp.model.BusinessProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface BusinessProfileRepository extends JpaRepository<BusinessProfile, Long> {
    List<BusinessProfile> findByCategoryContainingIgnoreCase(String category);

    List<BusinessProfile> findByCityContainingIgnoreCase(String city);

    List<BusinessProfile> findByCategoryContainingIgnoreCaseAndCityContainingIgnoreCaseAndNameContainingIgnoreCase(String category, String city, String keyword);

    List<BusinessProfile> findByNameContainingIgnoreCase(String keyword);

    @Query("SELECT DISTINCT b.category FROM BusinessProfile b")
    List<String> findDistinctCategories();

    @Query("SELECT DISTINCT b.city FROM BusinessProfile b")
    List<String> findDistinctCities();

}