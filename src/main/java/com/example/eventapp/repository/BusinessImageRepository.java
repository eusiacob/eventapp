package com.example.eventapp.repository;

import com.example.eventapp.model.BusinessImage;
import com.example.eventapp.model.BusinessProfile;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BusinessImageRepository extends JpaRepository<BusinessImage, Long> {

    List<BusinessImage> findByBusinessProfile(BusinessProfile businessProfile);
}