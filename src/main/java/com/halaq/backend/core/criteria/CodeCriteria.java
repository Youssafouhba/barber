package com.halaq.backend.core.criteria;

import com.halaq.backend.core.criteria.BaseCriteria;
import com.halaq.backend.core.criteria.BaseCriteria;

public class CodeCriteria extends BaseCriteria {

    /**
     * Fields.
     */

    protected String code;
    protected String codeLike;


    /**
     * Methods.
     */

    public String getCodeLike() {
        return this.codeLike;
    }

    public void setCodeLike(String codeLike) {
        this.codeLike = codeLike;
    }

    public String getCode() {
        return this.code;
    }

    public void setCode(String code) {
        this.code = code;
    }


}
