package com.halaq.backend.user.dto;

import com.halaq.backend.core.security.controller.dto.UserDto;
import com.halaq.backend.core.security.controller.dto.UserInfo;
import com.halaq.backend.core.security.entity.VerificationTracker;
import com.halaq.backend.core.security.enums.AccountStatus;
import com.halaq.backend.service.dto.AvailabilityDto;
import com.halaq.backend.service.dto.BookingDto;
import com.halaq.backend.service.dto.OfferedServiceDto;
import com.halaq.backend.service.dto.ReviewDto;
import com.halaq.backend.core.dto.AuditBaseDto;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class BarberDto extends UserDto {
    private String fullName;
    private String email;
    private String phone;
    private String avatar;
    private Boolean isAvailable = true;
    private String about;
    private String cin;
    protected String createdAt;
    protected String updatedAt;
    private Integer age;
    private Integer yearsOfExperience;
    private Double averageRating;
    private Integer reviewCount;
    private UserInfo UserInfo;
    private String birthDate;
    private List<ServiceZoneDto> zones;
    private List<String> portfolio;
    private String location;
    private String barberShopName;
    private AccountStatus status;
    private Integer currentStep = 0;
    private List<DocumentDto> documents;
    private List<AvailabilityDto> availability;
    private List<BookingDto> bookings;
    private List<ReviewDto> reviews;
    private List<OfferedServiceDto> offeredServices;
}