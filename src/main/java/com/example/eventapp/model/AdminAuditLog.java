package com.example.eventapp.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(name = "admin_audit_log", indexes = {
        @Index(name = "idx_admin_audit_target_user_created", columnList = "target_user_id, created_at"),
        @Index(name = "idx_admin_audit_created", columnList = "created_at")
})
public class AdminAuditLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Scalar IDs avoid foreign keys that would block account deletion.
    @Column(name = "admin_user_id", nullable = false)
    private Long adminUserId;

    @Column(name = "target_user_id")
    private Long targetUserId;

    @Column(name = "action_type", nullable = false, length = 60)
    private String actionType;

    @Column(name = "target_type", nullable = false, length = 40)
    private String targetType;

    @Column(name = "target_id", length = 80)
    private String targetId;

    @Column(nullable = false, length = 300)
    private String summary;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    void onCreate() {
        if (createdAt == null) createdAt = LocalDateTime.now();
    }

    @Transient
    public String getActionLabel() {
        return switch (actionType == null ? "" : actionType) {
            case "USER_EMAIL_CORRECTED" -> "Email corectat";
            case "USER_SUSPENDED" -> "Cont suspendat";
            case "USER_REACTIVATED" -> "Cont reactivat";
            case "BUSINESS_APPROVED" -> "Serviciu aprobat";
            case "BUSINESS_REJECTED" -> "Serviciu respins";
            case "REVIEW_APPROVED" -> "Recenzie aprobată";
            case "REVIEW_REJECTED" -> "Recenzie respinsă";
            case "SUPPORT_REPLY_SENT" -> "Răspuns trimis la suport";
            case "SUPPORT_STATUS_CHANGED" -> "Status suport schimbat";
            case "ADMIN_NOTIFICATION_SENT" -> "Notificare administrativă trimisă";
            case "LEGAL_DOCUMENT_UPDATED" -> "Document legal actualizat";
            default -> actionType;
        };
    }
}
