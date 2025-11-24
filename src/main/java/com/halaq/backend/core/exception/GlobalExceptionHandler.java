package com.halaq.backend.core.exception;

import com.halaq.backend.core.exception.BusinessRuleException;
import com.halaq.backend.core.exception.BusinessRuleException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class GlobalExceptionHandler {

  @ExceptionHandler(BusinessRuleException.class)
  public ResponseEntity<String> handleBusinessRuleException(BusinessRuleException ex) {
    return new ResponseEntity<>(ex.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
  }

}