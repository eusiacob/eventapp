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
import java.util.Optional;

public interface BusinessProfileRepository extends JpaRepository<BusinessProfile, Long> {

    List<BusinessProfile> findByUser(User user);

    Optional<BusinessProfile> findByUuid(String uuid);

    BusinessProfile findBySlug(String slug);

    List<BusinessProfile> findByStatus(BusinessProfile.BusinessStatus status);

    @Query("""
                SELECT DISTINCT b.city FROM BusinessProfile b
                WHERE b.category = :category
                AND b.status = APPROVED
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
                AND b.status = APPROVED
                ORDER BY b.name ASC
            """)
    List<BusinessProfile> searchAvailableByCategoryNameCityAndDate(
            @Param("category") BusinessCategory category,
            @Param("keyword") String keyword,
            @Param("city") String city,
            @Param("eventDate") LocalDate eventDate
    );

    @Query("""
            SELECT b
            FROM BusinessProfile b
            WHERE b.premium = true
            AND b.status = 'APPROVED'
            """)
    List<BusinessProfile> findTop10ByPremiumTrue();

    @Query("""
                SELECT b
                FROM User u
                JOIN u.favoriteBusinesses b
                WHERE b.status = APPROVED
                GROUP BY b
                ORDER BY COUNT(u) DESC
            """)
    List<BusinessProfile> findMostFavoriteBusinesses(Pageable pageable);

    @Query("""
                SELECT b
                FROM BusinessProfile b
                JOIN b.reviews r
                WHERE b.status = APPROVED
                GROUP BY b
                HAVING COUNT(r) >= 1
                ORDER BY AVG(r.rating) DESC, COUNT(r) DESC
            """)

    List<BusinessProfile> findTopRatedBusinesses(Pageable pageable);

    long countByStatus(BusinessProfile.BusinessStatus status);

}