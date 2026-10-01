package com.example.eventapp.service;

import com.example.eventapp.model.AccountPurpose;
import com.example.eventapp.model.Role;
import com.example.eventapp.repository.BusinessProfileRepository;
import org.springframework.security.access.AccessDeniedException;
import com.example.eventapp.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AccountPurposeService {
    private final UserRepository users;
    private final BusinessProfileRepository businesses;

    public AccountPurposeService(UserRepository users, BusinessProfileRepository businesses) {
        this.users = users;
        this.businesses = businesses;
    }

    @Transactional
    public void update(Long userId, AccountPurpose purpose) {
        var user = users.findForPurposeUpdate(userId).orElseThrow();
        if (user.getRole() == Role.ADMIN) {
            throw new AccessDeniedException("Această preferință nu se aplică administratorilor.");
        }
        user.changeAccountPurpose(purpose);
        if (purpose == AccountPurpose.SEARCH_SERVICES) {
            var profiles = businesses.findByUser(user);
            profiles.forEach(profile -> profile.setActive(false));
            businesses.saveAll(profiles);
        }
    }
}
