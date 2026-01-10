package com.halaq.backend.service.entity;

import com.halaq.backend.core.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
public class ServiceCategory extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;
    private String iconUrl;

    // --- AMÉLIORATIONS ---
    @OneToMany(
            mappedBy = "category",
            cascade = CascadeType.ALL, // 1. Ajout de la cascade
            fetch = FetchType.LAZY,
            orphanRemoval = true       // 2. Ajout de orphanRemoval
    )
    private List<OfferedService> services = new ArrayList<>(); // 3. Initialisation de la liste

    // --- HELPER METHOD (BONNE PRATIQUE) ---
    // 4. Méthode pour synchroniser les deux côtés de la relation
    public void addService(OfferedService service) {
        this.services.add(service);
        service.setCategory(this);
    }

    public void removeService(OfferedService service) {
        this.services.remove(service);
        service.setCategory(null);
    }
}
