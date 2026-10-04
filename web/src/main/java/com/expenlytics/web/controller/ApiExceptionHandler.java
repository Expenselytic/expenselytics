package com.expenlytics.web.controller;

import com.expenlytics.core.exception.InvalidInformationException;
import com.expenlytics.core.exception.NotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class ApiExceptionHandler {

    @ExceptionHandler({
        InvalidInformationException.class,
        IllegalArgumentException.class,
    })
    ProblemDetail badRequest(RuntimeException exception) {
        ProblemDetail detail = ProblemDetail.forStatusAndDetail(
            HttpStatus.BAD_REQUEST,
            exception.getMessage()
        );
        detail.setTitle("Invalid request");
        return detail;
    }

    @ExceptionHandler(NotFoundException.class)
    ProblemDetail notFound(NotFoundException exception) {
        ProblemDetail detail = ProblemDetail.forStatusAndDetail(
            HttpStatus.NOT_FOUND,
            exception.getMessage()
        );
        detail.setTitle("Expense not found");
        return detail;
    }
}
