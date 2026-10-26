package com.example.eventapp.repository;

import com.example.eventapp.model.AdminAuditLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.time.LocalDateTime;
import java.util.List;

public interface AdminAuditLogRepository extends JpaRepository<AdminAuditLog, Long>,
        JpaSpecificationExecutor<AdminAuditLog> {

    Page<AdminAuditLog> findByTargetUserIdOrderByCreatedAtDesc(Long targetUserId, Pageable pageable);

    @org.springframework.data.jpa.repository.Query(
            "select distinct entry.adminUserId from AdminAuditLog entry order by entry.adminUserId")
    List<Long> findDistinctAdminUserIds();

    void deleteByCreatedAtBefore(LocalDateTime cutoff);
}
