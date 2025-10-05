package org.acme.validation;

import org.acme.service.UserService;

import jakarta.inject.Inject;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class DiscountValidator implements ConstraintValidator<ValidDiscount, Integer> {

    @Inject
    UserService userService;

    @Override
    public boolean isValid(Integer discount, ConstraintValidatorContext ctx) {
        if (discount == null)
            return true;
        int limit = userService.getCurrentUserDiscountLimit();
        return discount <= limit;
    }
}