package com.mikerusoft.jsonable.annotations;

import java.lang.annotation.ElementType;

/**
 * @author Grinfeld Mikhail
 * @since 1/5/2015.
 */
@java.lang.annotation.Retention(java.lang.annotation.RetentionPolicy.RUNTIME)
@java.lang.annotation.Target({ElementType.FIELD})
public @interface DateField {
    int type() default 0;
}
