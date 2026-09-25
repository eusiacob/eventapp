package com.example.eventapp.service;

import com.example.eventapp.model.*;
import com.example.eventapp.repository.BusinessProfileRepository;
import com.example.eventapp.repository.BusinessUnavailableDateRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.HashSet;
import java.util.List;

@Service
public class BusinessAvailabilityService {
    private final BusinessProfileRepository businesses;
    private final BusinessUnavailableDateRepository dates;

    public BusinessAvailabilityService(BusinessProfileRepository businesses,
                                       BusinessUnavailableDateRepository dates) {
        this.businesses = businesses;
        this.dates = dates;
    }

    @Transactional
    public List<String> update(String uuid, User owner, LocalDate start, LocalDate end, boolean unavailable) {
        LocalDate today = LocalDate.now(ZoneId.of("Europe/Bucharest"));
        if (start == null || end == null || start.isBefore(today) || end.isBefore(start)
                || (unavailable && ChronoUnit.DAYS.between(start, end) >= 366)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Alege un interval de maximum 366 de zile, începând de astăzi.");
        }
        BusinessProfile business = businesses.findForAvailabilityUpdate(uuid)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        if (owner == null || business.getUser() == null || !business.getUser().getId().equals(owner.getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        }
        // Serialize concurrent changes for the same service; repeated requests are idempotent.
        var existing = dates.findByBusinessProfile(business);
        var blocked = new HashSet<LocalDate>();
        existing.forEach(row -> blocked.add(row.getUnavailableDate()));
        if (unavailable) {
            for (LocalDate day = start; !day.isAfter(end); day = day.plusDays(1)) {
                if (blocked.add(day)) {
                    BusinessUnavailableDate row = new BusinessUnavailableDate();
                    row.setBusinessProfile(business);
                    row.setUnavailableDate(day);
                    dates.save(row);
                }
            }
        } else {
            for (var row : existing) {
                if (!row.getUnavailableDate().isBefore(start) && !row.getUnavailableDate().isAfter(end)) {
                    dates.delete(row);
                    blocked.remove(row.getUnavailableDate());
                }
            }
        }
        return blocked.stream().sorted().map(LocalDate::toString).toList();
    }
}
