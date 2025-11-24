package com.halaq.backend.core.transverse.emailling.enums;

public enum EmailTemplateName {

    DOCUMENT("document"),
    VALIDATION_CODE("validation_code"),
    PROCESS_VALIDATION("process-validation-email"),
    LEAVE_REQUEST_STATUS("leave-request-status"),
    ;


    private final String name;

    EmailTemplateName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}