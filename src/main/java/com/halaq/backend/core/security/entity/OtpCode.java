package com.halaq.backend.core.security.entity;


import com.halaq.backend.core.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "otp_codes")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@SequenceGenerator(name = "otp_code_seq", sequenceName = "otp_code_seq", allocationSize = 50, initialValue = 1)
public class OtpCode extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "otp_code_seq")
    private Long id;

    @Column(name = "phone", nullable = false, length = 20)
    private String phone;

    @Column(name = "code", nullable = false, length = 6)
    private String code;

    @Column(name = "action", length = 20)
    private String action; // LOGIN, REGISTER, VERIFY_PHONE

    @Column(name = "user_type", length = 20)
    private String userType; // CLIENT, BARBER

    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "used_at")
    private LocalDateTime usedAt;

    @Column(name = "used", nullable = false)
    private Boolean used = false;

    @Column(name = "attempts", nullable = false)
    private Integer attempts = 0;

    @Column(name = "ip_address", length = 45)
    private String ipAddress;

    @Column(name = "user_agent", length = 500)
    private String userAgent;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        if (used == null) {
            used = false;
        }
        if (attempts == null) {
            attempts = 0;
        }
    }
}
