package com.mikerusoft.jsonable.annotations;

import java.lang.annotation.ElementType;

/**
 * @author Grinfeld Mikhail
 * @since 8/15/2015.
 */
@java.lang.annotation.Retention(java.lang.annotation.RetentionPolicy.RUNTIME)
@java.lang.annotation.Target({ElementType.FIELD, ElementType.METHOD})
public @interface DisplayNull {
    boolean value() default true;
}
