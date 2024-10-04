package com.imansdev.ackownt.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class NationalIdValidator implements ConstraintValidator<ValidNationalId, String> {

    @Override
    public void initialize(ValidNationalId constraintAnnotation) {}

    @Override
    public boolean isValid(String nationalId, ConstraintValidatorContext context) {
        // Check if the national ID is exactly 10 digits
        if (nationalId == null || nationalId.length() != 10 || !nationalId.matches("\\d{10}")) {
            addConstraintViolation(context, "Invalid national ID format");
            return false;
        }

        // Convert the string to a char array
        char[] nationalIdDigits = nationalId.toCharArray();

        int sum = 0;
        int controlDigit = Character.getNumericValue(nationalIdDigits[9]);

        // Calculate the sum of the first 9 digits
        for (int i = 0; i < 9; i++) {
            sum += Character.getNumericValue(nationalIdDigits[i]) * (10 - i);
        }

        // Calculate the remainder when sum is divided by 11
        int remainder = sum % 11;

        // Validate the control digit
        boolean isValid;
        if (remainder < 2) {
            isValid = controlDigit == remainder;
        } else {
            isValid = controlDigit == 11 - remainder;
        }

        if (!isValid) {
            addConstraintViolation(context, "Invalid national ID");
        }

        return isValid;
    }

    // Helper method to add a custom constraint violation
    private void addConstraintViolation(ConstraintValidatorContext context, String message) {
        context.disableDefaultConstraintViolation();
        context.buildConstraintViolationWithTemplate(message).addPropertyNode("nationalId")
                .addConstraintViolation();
    }
}
