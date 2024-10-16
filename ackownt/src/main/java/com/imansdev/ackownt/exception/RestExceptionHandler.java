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
import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import com.imansdev.ackownt.enums.Gender;
import java.util.Arrays;
import java.time.format.DateTimeParseException;
import java.util.HashMap;
import java.util.Map;

@ControllerAdvice
public class RestExceptionHandler {

    // Handle Validation exceptions
    @ExceptionHandler(ValidationException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResponseEntity<Map<String, String>> handleValidationException(ValidationException ex) {
        Map<String, String> errorResponse = new HashMap<>();
        errorResponse.put("error", ex.getMessage());
        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    // Handle User not found exceptions
    @ExceptionHandler(UsernameNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ResponseEntity<Map<String, String>> handleUsernameNotFoundException(
            UsernameNotFoundException ex) {
        Map<String, String> errorResponse = new HashMap<>();
        errorResponse.put("error", ex.getMessage());
        return new ResponseEntity<>(errorResponse, HttpStatus.NOT_FOUND);
    }

    // Handle MethodArgumentNotValidException for @Valid related errors
    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResponseEntity<Map<String, String>> handleMethodArgumentNotValidException(
            MethodArgumentNotValidException ex) {
        Map<String, String> errorResponse = new HashMap<>();

        ex.getBindingResult().getFieldErrors().forEach(error -> {
            errorResponse.put(error.getField(), error.getDefaultMessage());
        });

        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    // Handle general JSON parsing errors (e.g., invalid JSON or data types)
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<Object> handleInvalidJsonFormat(HttpMessageNotReadableException ex,
            WebRequest request) {
        Throwable cause = ex.getRootCause();
        Map<String, String> errors = new HashMap<>();

        // Handle date parsing issues
        if (cause instanceof DateTimeParseException) {
            errors.put("dateOfBirth",
                    "Invalid date format. Please use the correct format: YYYY-MM-DD.");
        }

        // Handle enum parsing issues (e.g., Gender or MilitaryStatus)
        if (cause instanceof InvalidFormatException) {
            InvalidFormatException invalidEx = (InvalidFormatException) cause;

            // Check if we're dealing with Gender enum
            if (invalidEx.getTargetType().isEnum()
                    && invalidEx.getTargetType().getSimpleName().equals("Gender")) {
                errors.put("gender", "Invalid gender value. Accepted values are: MALE, FEMALE.");
            }

            // Check if we're dealing with MilitaryStatus enum
            if (invalidEx.getTargetType().isEnum()
                    && invalidEx.getTargetType().getSimpleName().equals("MilitaryStatus")) {
                String validGenders = String.join(", ",
                        Arrays.stream(Gender.values()).map(Enum::name).toArray(String[]::new));
                errors.put("militaryStatus",
                        "Invalid military status value. Accepted values are: " + validGenders);
            }
        }

        // Handle empty values for enum fields
        if (cause instanceof JsonMappingException
                && cause.getMessage().contains("Cannot deserialize value of type")) {
            if (cause.getMessage().contains("Gender")) {
                errors.put("gender", "Gender cannot be empty. Accepted values are: MALE, FEMALE.");
            }
            if (cause.getMessage().contains("MilitaryStatus")) {
                errors.put("militaryStatus",
                        "Military status cannot be empty. Accepted values are: CURRENTLY_SERVING, EXEMPT_FROM_SERVICE, CONSCRIPTED, COMPLETED_SERVICE, NONE.");
            }
        }

        // Return combined errors if any
        if (!errors.isEmpty()) {
            return new ResponseEntity<>(errors, HttpStatus.BAD_REQUEST);
        }

        String message = "Invalid request format. Please ensure all fields are correct.";
        return new ResponseEntity<>(message, HttpStatus.BAD_REQUEST);
    }

    // Catch-all exception handler
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, String>> handleGeneralException(Exception ex) {
        Map<String, String> errorResponse = new HashMap<>();
        errorResponse.put("error", "An unexpected error occurred: " + ex.getMessage());
        return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResponseEntity<Map<String, String>> handleConstraintViolationException(
            ConstraintViolationException ex) {
        Map<String, String> errorResponse = new HashMap<>();
        ex.getConstraintViolations().forEach(violation -> {
            errorResponse.put(violation.getPropertyPath().toString(), violation.getMessage());
        });
        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }
}
