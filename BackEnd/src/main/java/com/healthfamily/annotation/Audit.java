package com.healthfamily.annotation;

import com.healthfamily.domain.constant.SensitivityLevel;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Audit {
    String action();
    String resource() default "";
    SensitivityLevel sensitivity() default SensitivityLevel.NORMAL;
}
