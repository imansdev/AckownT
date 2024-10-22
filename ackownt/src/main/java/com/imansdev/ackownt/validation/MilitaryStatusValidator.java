package com.imansdev.ackownt.validation;

import com.imansdev.ackownt.enums.Gender;
import com.imansdev.ackownt.enums.MilitaryStatus;
import com.imansdev.ackownt.exception.ValidationException;
import com.imansdev.ackownt.model.Customer;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import java.time.LocalDate;
import java.time.Period;
import org.springframework.beans.factory.annotation.Value;

public class MilitaryStatusValidator implements ConstraintValidator<ValidMilitaryStatus, Customer> {

    @Value("${validation.age}")
    private int cutOffAge;

    @Override
    public boolean isValid(Customer user, ConstraintValidatorContext context) {
        try {
            if (isInvalidDateOfBirth(user, context))
                return false;
            if (isInvalidGender(user, context))
                return false;
            if (isInvalidMilitaryStatus(user, context))
                return false;
        } catch (ValidationException e) {
            // Add the validation error message to the context
            addConstraintViolation(context, e.getMessage(), e.getFieldName());
            return false;
        }

        return true;
    }

    private boolean isInvalidDateOfBirth(Customer user, ConstraintValidatorContext context)
            throws ValidationException {
        LocalDate dateOfBirth;
        dateOfBirth = user.getDateOfBirth();
        if (dateOfBirth == null || dateOfBirth.isAfter(LocalDate.now())) {
            throw new ValidationException("The date of birth is required and must be in the past",
                    "dateOfBirth");
        }
        return false;
    }

    private boolean isInvalidGender(Customer user, ConstraintValidatorContext context)
            throws ValidationException {
        Gender gender;
        gender = user.getGender();
        if (gender == null) {
            throw new ValidationException("The user's gender is required and must not be empty",
                    "gender");
        }

        return false;
    }

    private boolean isInvalidMilitaryStatus(Customer user, ConstraintValidatorContext context)
            throws ValidationException {
        MilitaryStatus militaryStatus;
        militaryStatus = user.getMilitaryStatus();
        if (militaryStatus == null) {
            throw new ValidationException("The military status is required and must not be empty",
                    "militaryStatus");
        }


        // Calculate age based on the date of birth
        int age = Period.between(user.getDateOfBirth(), LocalDate.now()).getYears();

        // If the user is male and older than the cutoff age, military status must not be NONE
        if (user.getGender() == Gender.MALE && age > cutOffAge
                && militaryStatus == MilitaryStatus.NONE) {
            throw new ValidationException("Male users above the age of " + cutOffAge
                    + " must not have military status of 'NONE'", "militaryStatus");
        }

        // If the user is female, the military status must always be NONE
        if (user.getGender() == Gender.FEMALE && militaryStatus != MilitaryStatus.NONE) {
            throw new ValidationException("The military status for female users must be 'NONE'",
                    "militaryStatus");
        }

        return false;
    }

    private void addConstraintViolation(ConstraintValidatorContext context, String message,
            String propertyName) {
        context.disableDefaultConstraintViolation();
        context.buildConstraintViolationWithTemplate(message).addPropertyNode(propertyName)
                .addConstraintViolation();
    }
}
