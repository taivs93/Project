package com.taivs.project.validation.passwordChanges;

import com.taivs.project.dto.request.PasswordChangeRequest;
import com.taivs.project.validation.passwordMatches.PasswordMatches;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;


public class PasswordChangeValidator  implements ConstraintValidator<PasswordMatches,Object> {
    @Override
    public void initialize(PasswordMatches constraintAnnotation) {
        ConstraintValidator.super.initialize(constraintAnnotation);
    }

    @Override
    public boolean isValid(Object object, ConstraintValidatorContext constraintValidatorContext) {
        PasswordChangeRequest request = (PasswordChangeRequest) object;
        return !request.getOldPassword().equals(request.getNewPassword());
    }
}
