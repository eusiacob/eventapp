package com.example.eventapp.repository;

import com.example.eventapp.model.BusinessProfile;
import com.example.eventapp.model.BusinessVideo;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BusinessVideoRepository
        extends JpaRepository<BusinessVideo, Long> {

    long countByBusinessProfile(BusinessProfile businessProfile);
}