package com.halaq.backend.user.repository;

import com.halaq.backend.user.entity.BarberLiveLocations;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BarberLiveLocationsRepository extends JpaRepository<BarberLiveLocations, Long> {
}
