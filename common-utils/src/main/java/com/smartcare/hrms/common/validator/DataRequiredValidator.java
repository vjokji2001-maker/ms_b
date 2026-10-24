package com.smartcare.hrms.common.validator;

import com.smartcare.hrms.common.custom.annotations.DataRequired;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.springframework.util.StringUtils;

import java.util.regex.Pattern;

public class DataRequiredValidator implements ConstraintValidator<DataRequired, String> {

    private String requiredMessageKey;
    private String pattern;
    private String patternMessageKey;

    @Override
    public void initialize(DataRequired annotation) {
        this.requiredMessageKey = annotation.message();
        this.pattern = annotation.pattern();
        this.patternMessageKey = annotation.patternMessage();
    }

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {

        // Required validation
        if (!StringUtils.hasText(value)) {
            buildViolation(context, requiredMessageKey);
            return false;
        }

        // Pattern validation (optional)
        if (StringUtils.hasText(pattern)
                && !Pattern.matches(pattern, value)) {
            buildViolation(context, patternMessageKey);
            return false;
        }

        return true;
    }

    private void buildViolation(
            ConstraintValidatorContext context,
            String messageKey
    ) {
        context.disableDefaultConstraintViolation();
        context.buildConstraintViolationWithTemplate(messageKey)
                .addConstraintViolation();
    }
}
