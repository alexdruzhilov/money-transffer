package com.revolut.moneytransfer.util;

import io.micronaut.validation.validator.DefaultAnnotatedElementValidator;
import io.micronaut.validation.validator.Validator;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class Assertions {
    private static final Validator validator = new DefaultAnnotatedElementValidator();

    public static void validationAssert(final Object object, final String path, final String message) {
        final boolean hasProperViolationMessage = validator.validate(object).stream()
                .filter(v -> v.getPropertyPath().toString().equals(path))
                .anyMatch(v -> v.getMessage().equals(message));
        assertTrue(hasProperViolationMessage,
                "no violation constraint for " + path + " and message " + message);
    }

}
