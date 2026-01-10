package com.halaq.backend.user.criteria;

import com.halaq.backend.shared.DocumentType;
import com.halaq.backend.shared.VerificationStatus;
import com.halaq.backend.core.criteria.BaseCriteria;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DocumentCriteria extends BaseCriteria {
    private Long barberId;
    private DocumentType type;
    private VerificationStatus verificationStatus;
}