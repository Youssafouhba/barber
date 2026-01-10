package com.halaq.backend.user.criteria;

import com.halaq.backend.core.criteria.BaseCriteria;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ClientCriteria extends BaseCriteria {
    private String fullName;
    private String email;
    private String phone;
    private String address;
}