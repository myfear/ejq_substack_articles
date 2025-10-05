package org.acme.validation;

import org.acme.event.Event;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class ChronologicalDatesValidator implements ConstraintValidator<ChronologicalDates, Event> {
    @Override
    public boolean isValid(Event event, ConstraintValidatorContext ctx) {
        if (event.getStart() == null || event.getEnd() == null)
            return true;
        return event.getEnd().isAfter(event.getStart());
    }
}