package com.halaq.backend.user.entity;

import com.halaq.backend.core.security.entity.User;
import com.halaq.backend.service.entity.Booking;
import com.halaq.backend.service.entity.Review;
import jakarta.persistence.Entity;
import jakarta.persistence.OneToMany;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Entity
@Getter
@Setter
public class Client extends User {
    private String address;
    private String preferences;

    @OneToMany(mappedBy = "client")
    private List<Favorite> favorites;

    @OneToMany(mappedBy = "client")
    private List<Booking> bookings;

    @OneToMany(mappedBy = "author")
    private List<Review> reviews;
}