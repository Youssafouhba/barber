package com.halaq.backend.service.entity;

import com.halaq.backend.core.entity.BaseEntity;
import com.halaq.backend.core.util.ResolveAssociation;
import com.halaq.backend.user.entity.Barber;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.math.BigDecimal;

@Entity
@Getter
@Setter
public class OfferedService extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;
    private String description;
    private BigDecimal price;
    private Integer durationInMinutes;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "barber_id", nullable = false)
    private Barber barber;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    @ResolveAssociation // This annotation is used by the AbstractServiceImpl
    private ServiceCategory category;
}
