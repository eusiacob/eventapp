package com.example.eventapp.service;

import com.example.eventapp.model.AdminAuditLog;
import com.example.eventapp.repository.AdminAuditLogRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class AdminAuditService {

    private final AdminAuditLogRepository repository;

    public AdminAuditService(AdminAuditLogRepository repository) {
        this.repository = repository;
    }

    @Transactional
    public void record(Long adminUserId, Long targetUserId, String actionType,
                       String targetType, String targetId, String summary) {
        if (adminUserId == null || actionType == null || targetType == null || summary == null) return;

        AdminAuditLog entry = new AdminAuditLog();
        entry.setAdminUserId(adminUserId);
        entry.setTargetUserId(targetUserId);
        entry.setActionType(actionType);
        entry.setTargetType(targetType);
        entry.setTargetId(targetId);
        entry.setSummary(summary);
        repository.save(entry);
    }

    @Transactional(readOnly = true)
    public Page<AdminAuditLog> findForUser(Long userId, Pageable pageable) {
        return repository.findByTargetUserIdOrderByCreatedAtDesc(userId, pageable);
    }

    @Transactional(readOnly = true)
    public Page<AdminAuditLog> search(Long adminUserId, Long targetUserId, String actionType,
                                      LocalDateTime fromDate, LocalDateTime toDateExclusive,
                                      Pageable pageable) {
        Specification<AdminAuditLog> specification = (root, query, criteriaBuilder) -> {
            List<jakarta.persistence.criteria.Predicate> predicates = new ArrayList<>();
            if (adminUserId != null) {
                predicates.add(criteriaBuilder.equal(root.get("adminUserId"), adminUserId));
            }
            if (targetUserId != null) {
                predicates.add(criteriaBuilder.equal(root.get("targetUserId"), targetUserId));
            }
            if (actionType != null && !actionType.isBlank()) {
                predicates.add(criteriaBuilder.equal(root.get("actionType"), actionType));
            }
            if (fromDate != null) {
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("createdAt"), fromDate));
            }
            if (toDateExclusive != null) {
                predicates.add(criteriaBuilder.lessThan(root.get("createdAt"), toDateExclusive));
            }
            return criteriaBuilder.and(predicates.toArray(jakarta.persistence.criteria.Predicate[]::new));
        };
        return repository.findAll(specification, pageable);
    }

    @Transactional(readOnly = true)
    public List<Long> findAdminIds() {
        return repository.findDistinctAdminUserIds();
    }

    @Transactional(readOnly = true)
    public long countEntries() {
        return repository.count();
    }

    @Scheduled(cron = "0 30 3 * * *")
    @Transactional
    public void removeExpiredEntries() {
        repository.deleteByCreatedAtBefore(java.time.LocalDateTime.now().minusYears(2));
    }
}
