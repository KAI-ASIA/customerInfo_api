package com.kaiasia.app.register;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
// Annotation đánh dấu phương thức xử lý
public @interface KaiMethod {
    String name() default "";

    String type() default Register.PROCESSING;
}
