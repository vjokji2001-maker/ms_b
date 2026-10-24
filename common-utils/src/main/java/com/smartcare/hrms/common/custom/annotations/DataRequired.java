package com.smartcare.hrms.common.custom.annotations;

import com.smartcare.hrms.common.validator.DataRequiredValidator;
import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = DataRequiredValidator.class)
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface DataRequired {

    /**
     * Message key when value is missing
     */
    String message();

    /**
     * Optional regex pattern
     */
    String pattern() default "";

    /**
     * Message key when pattern validation fails
     */
    String patternMessage() default "";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
