package com.halaq.backend.user.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.halaq.backend.shared.DocumentType;
import com.halaq.backend.shared.VerificationStatus;
import com.halaq.backend.core.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
public class Document extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", length = 50)
    private DocumentType type;

    private String url;
    private String name;
    private Long size;

    @Enumerated(EnumType.STRING)
    private VerificationStatus verificationStatus;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "barber_id", nullable = false)
    private Barber barber;
}