package com.halaq.backend.core.criteria;

import com.halaq.backend.core.criteria.CodeLibelleCriteria;
import com.halaq.backend.core.criteria.CodeLibelleCriteria;

public class CodeLibelleDescCriteria extends CodeLibelleCriteria {

    /**
     * Fields.
     */
    private String description;
    private String descriptionLike;


    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getDescriptionLike() {
        return descriptionLike;
    }

    public void setDescriptionLike(String descriptionLike) {
        this.descriptionLike = descriptionLike;
    }
}
