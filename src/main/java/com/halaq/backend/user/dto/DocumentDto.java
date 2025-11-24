package com.halaq.backend.user.dto;

import com.halaq.backend.shared.DocumentType;
import com.halaq.backend.shared.VerificationStatus;
import com.halaq.backend.core.dto.AuditBaseDto;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DocumentDto extends AuditBaseDto {
    private DocumentType type;
    private String url;
    private String name;
    private Long size;
    private VerificationStatus verificationStatus;
    private BarberDto barber;
}