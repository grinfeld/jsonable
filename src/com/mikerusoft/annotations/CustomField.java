package com.mikerusoft.annotations;

import java.lang.annotation.ElementType;

/**
 * Defines method as custom field.
 *
 * @author Grinfeld Mikhail
 * @since 12/6/2014.
 */
@java.lang.annotation.Retention(java.lang.annotation.RetentionPolicy.RUNTIME)
@java.lang.annotation.Target({ElementType.METHOD})
public @interface CustomField {
    String name();
}
