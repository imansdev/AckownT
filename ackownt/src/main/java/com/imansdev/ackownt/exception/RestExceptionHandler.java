package com.imansdev.ackownt.exception;

import jakarta.validation.ConstraintViolationException;
import jakarta.validation.ValidationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.context.request.WebRequest;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.imansdev.ackownt.enums.Gender;
import com.imansdev.ackownt.enums.MilitaryStatus;
import java.util.Arrays;
import java.time.format.DateTimeParseException;
import java.util.HashMap;
import java.util.Map;

@ControllerAdvice
public class RestExceptionHandler {

    // Handle Validation exceptions
    @ExceptionHandler(ValidationException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResponseEntity<Object> handleValidationException(ValidationException ex) {
        return buildErrorResponse(ex.getMessage(), HttpStatus.BAD_REQUEST);
    }

    // Handle UsernameNotFoundException
    @ExceptionHandler(UsernameNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ResponseEntity<Object> handleUsernameNotFoundException(UsernameNotFoundException ex) {
        return buildErrorResponse(ex.getMessage(), HttpStatus.NOT_FOUND);
    }

    // Handle MethodArgumentNotValidException for @Valid related errors
    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResponseEntity<Object> handleMethodArgumentNotValidException(
            MethodArgumentNotValidException ex) {
        Map<String, String> errorResponse = new HashMap<>();
        ex.getBindingResult().getFieldErrors()
                .forEach(error -> errorResponse.put(error.getField(), error.getDefaultMessage()));
        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    // Handle general JSON parsing errors (e.g., invalid JSON or data types)
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<Object> handleInvalidJsonFormat(HttpMessageNotReadableException ex,
            WebRequest request) {
        Map<String, String> errors = getJsonMappingErrors(ex.getRootCause());

        if (!errors.isEmpty()) {
            return new ResponseEntity<>(errors, HttpStatus.BAD_REQUEST);
        }

        return buildErrorResponse("Invalid request format. Please ensure all fields are correct.",
                HttpStatus.BAD_REQUEST);
    }

    // Handle ConstraintViolationException for specific constraint violations (e.g., @Size,
    // @NotNull)
    @ExceptionHandler(ConstraintViolationException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResponseEntity<Object> handleConstraintViolationException(
            ConstraintViolationException ex) {
        Map<String, String> errorResponse = new HashMap<>();
        ex.getConstraintViolations().forEach(violation -> errorResponse
                .put(violation.getPropertyPath().toString(), violation.getMessage()));
        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    // Catch-all exception handler
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Object> handleGeneralException(Exception ex) {
        return buildErrorResponse("An unexpected error occurred: " + ex.getMessage(),
                HttpStatus.INTERNAL_SERVER_ERROR);
    }

    // Helper method to handle JSON mapping errors for specific fields
    private Map<String, String> getJsonMappingErrors(Throwable cause) {
        Map<String, String> errors = new HashMap<>();

        if (cause instanceof DateTimeParseException) {
            errors.put("dateOfBirth",
                    "Invalid date format. Please use the correct format: YYYY-MM-DD.");
        }

        if (cause instanceof JsonMappingException) {
            handleEnumFields((JsonMappingException) cause, errors);
        }

        return errors;
    }

    // Handles empty enum field values (e.g., Gender, MilitaryStatus)
    private void handleEnumFields(JsonMappingException cause, Map<String, String> errors) {
        if (cause.getMessage().contains("Gender")) {
            String valid = String.join(", ",
                    Arrays.stream(Gender.values()).map(Enum::name).toArray(String[]::new));
            errors.put("gender", "Accepted values are: " + valid);
        }
        if (cause.getMessage().contains("MilitaryStatus")) {
            String valid = String.join(", ",
                    Arrays.stream(MilitaryStatus.values()).map(Enum::name).toArray(String[]::new));
            errors.put("militaryStatus", "Accepted values are: " + valid);
        }
    }

    // Builds a standardized error response
    private ResponseEntity<Object> buildErrorResponse(String message, HttpStatus status) {
        Map<String, String> errorResponse = new HashMap<>();
        errorResponse.put("error", message);
        return new ResponseEntity<>(errorResponse, status);
    }
}
