package com.example.eventapp.repository;

import com.example.eventapp.model.BusinessProfile;
import com.example.eventapp.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface BusinessProfileRepository extends JpaRepository<BusinessProfile, Long> {

    // Pentru dashboard business - toate profilurile userului logat
    List<BusinessProfile> findByUser(User user);

    // Pentru pagina unei categorii
    List<BusinessProfile> findByCategoryIgnoreCaseOrderByNameAsc(String category);

    // Pentru search în interiorul unei categorii după nume și oraș
    @Query("""
        SELECT b FROM BusinessProfile b
        WHERE LOWER(b.category) = LOWER(:category)
        AND (:keyword IS NULL OR :keyword = '' OR LOWER(b.name) LIKE LOWER(CONCAT('%', :keyword, '%')))
        AND (:city IS NULL OR :city = '' OR LOWER(b.city) = LOWER(:city))
        ORDER BY b.name ASC
    """)
    List<BusinessProfile> searchByCategoryNameAndCity(
            @Param("category") String category,
            @Param("keyword") String keyword,
            @Param("city") String city
    );

    // Pentru dropdown de orașe în pagina categoriei
    @Query("""
        SELECT DISTINCT b.city FROM BusinessProfile b
        WHERE LOWER(b.category) = LOWER(:category)
        ORDER BY b.city ASC
    """)
    List<String> findDistinctCitiesByCategory(@Param("category") String category);

    // Alt dropdown global de categorii - daca va mai fi nevoie
    @Query("""
        SELECT DISTINCT b.category FROM BusinessProfile b
        ORDER BY b.category ASC
    """)
    List<String> findDistinctCategories();

    // Alt dropdown global de orase - daca va mai fi nevoie
    @Query("""
        SELECT DISTINCT b.city FROM BusinessProfile b
        ORDER BY b.city ASC
    """)
    List<String> findDistinctCities();
}