package com.example.eventapp.repository;

import com.example.eventapp.model.BusinessCategory;
import com.example.eventapp.model.BusinessProfile;
import com.example.eventapp.model.User;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface BusinessProfileRepository extends JpaRepository<BusinessProfile, Long> {

    List<BusinessProfile> findByUser(User user);

    List<BusinessProfile> findByCategoryOrderByNameAsc(BusinessCategory category);

    @Query("""
                SELECT b FROM BusinessProfile b
                WHERE b.category = :category
                AND (:keyword IS NULL OR :keyword = '' OR LOWER(b.name) LIKE LOWER(CONCAT('%', :keyword, '%')))
                AND (:city IS NULL OR :city = '' OR LOWER(b.city) = LOWER(:city))
                ORDER BY b.name ASC
            """)
    List<BusinessProfile> searchByCategoryNameAndCity(
            @Param("category") BusinessCategory category,
            @Param("keyword") String keyword,
            @Param("city") String city
    );

    @Query("""
                SELECT DISTINCT b.city FROM BusinessProfile b
                WHERE b.category = :category
                ORDER BY b.city ASC
            """)
    List<String> findDistinctCitiesByCategory(@Param("category") BusinessCategory category);

    @Query("""
                SELECT b FROM BusinessProfile b
                WHERE b.category = :category
                AND (:keyword IS NULL OR :keyword = '' OR LOWER(b.name) LIKE LOWER(CONCAT('%', :keyword, '%')))
                AND (:city IS NULL OR :city = '' OR LOWER(b.city) = LOWER(:city))
                AND (
                    :eventDate IS NULL OR NOT EXISTS (
                        SELECT d FROM BusinessUnavailableDate d
                        WHERE d.businessProfile = b
                        AND d.unavailableDate = :eventDate
                    )
                )
                ORDER BY b.name ASC
            """)
    List<BusinessProfile> searchAvailableByCategoryNameCityAndDate(
            @Param("category") BusinessCategory category,
            @Param("keyword") String keyword,
            @Param("city") String city,
            @Param("eventDate") LocalDate eventDate
    );

    List<BusinessProfile> findTop10ByPremiumTrue();

    @Query("""
    SELECT b
    FROM User u
    JOIN u.favoriteBusinesses b
    GROUP BY b
    ORDER BY COUNT(u) DESC
""")
    List<BusinessProfile> findMostFavoriteBusinesses(Pageable pageable);

    @Query("""
    SELECT b
    FROM BusinessProfile b
    JOIN b.reviews r
    GROUP BY b
    HAVING COUNT(r) >= 1
    ORDER BY AVG(r.rating) DESC, COUNT(r) DESC
""")
    List<BusinessProfile> findTopRatedBusinesses(Pageable pageable);

}