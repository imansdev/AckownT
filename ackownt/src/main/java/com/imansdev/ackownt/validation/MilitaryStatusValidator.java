package com.imansdev.ackownt.validation;

import com.imansdev.ackownt.model.Users;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import java.time.LocalDate;
import java.util.Arrays;
import org.springframework.beans.factory.annotation.Value;

public class MilitaryStatusValidator implements ConstraintValidator<ValidMilitaryStatus, Users> {

    @Value("${validation.age}")
    private int cutOffAge;

    @Override
    public boolean isValid(Users user, ConstraintValidatorContext context) {
        if (isInvalidDateOfBirth(user, context))
            return false;
        if (isInvalidGender(user, context))
            return false;
        if (isInvalidMilitaryStatus(user, context))
            return false;

        return true;
    }

    private boolean isInvalidDateOfBirth(Users user, ConstraintValidatorContext context) {
        LocalDate dateOfBirth = user.getDateOfBirth();
        if (dateOfBirth == null || dateOfBirth.isAfter(LocalDate.now())) {
            addConstraintViolation(context, "The date of birth is required and must be in the past",
                    "dateOfBirth");
            return true;
        }
        return false;
    }

    private boolean isInvalidGender(Users user, ConstraintValidatorContext context) {
        Users.Gender gender = user.getGender();

        if (gender == null) {
            String validGenders = String.join(", ",
                    Arrays.stream(Users.Gender.values()).map(Enum::name).toArray(String[]::new));
            addConstraintViolation(context,
                    "The user's gender is required and must be include " + validGenders, "gender");
            return true;
        }
        return false;
    }

    private boolean isInvalidMilitaryStatus(Users user, ConstraintValidatorContext context) {
        Users.Gender gender = user.getGender();
        Users.MilitaryStatus militaryStatus = user.getMilitaryStatus();

        if (militaryStatus == null) {
            String validStatuses = String.join(", ", Arrays.stream(Users.MilitaryStatus.values())
                    .map(Enum::name).toArray(String[]::new));
            addConstraintViolation(context,
                    "The military status is required and must include " + validStatuses,
                    "militaryStatus");
            return true;
        }

        if (gender == Users.Gender.MALE && user.getAge() > cutOffAge) {
            if (militaryStatus == Users.MilitaryStatus.NONE) {
                addConstraintViolation(context, "Male users above the " + cutOffAge
                        + " must not have military status of 'NONE'", "militaryStatus");
                return true;
            }
        }

        if (gender == Users.Gender.FEMALE) {
            if (militaryStatus != Users.MilitaryStatus.NONE) {
                addConstraintViolation(context,
                        "The military status for female users must be 'NONE'", "militaryStatus");
                return true;
            }
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
