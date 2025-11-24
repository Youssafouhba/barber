package com.halaq.backend.user.dto;

import com.halaq.backend.core.dto.AuditBaseDto;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ClientDto extends AuditBaseDto {
    private String fullName;
    private String email;
    private String phone;
    private String avatar;
    private String address;
    private String preferences;

    // We omit lists of bookings, favorites, and reviews to prevent circular dependencies in the API response.
    // These can be fetched from their respective endpoints, e.g., /api/bookings/client/{id}
}