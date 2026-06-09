package com.example.eventapp.repository;

import com.example.eventapp.model.BusinessProfile;
import com.example.eventapp.model.BusinessUnavailableDate;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.Optional;

public interface BusinessUnavailableDateRepository extends JpaRepository<BusinessUnavailableDate, Long> {

    boolean existsByBusinessProfileAndUnavailableDate(BusinessProfile businessProfile,
                                                      LocalDate unavailableDate);

    Optional<BusinessUnavailableDate> findByBusinessProfileAndUnavailableDate(BusinessProfile businessProfile,
                                                                              LocalDate unavailableDate);
}