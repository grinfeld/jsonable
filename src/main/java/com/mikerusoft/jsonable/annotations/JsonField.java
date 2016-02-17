package com.mikerusoft.jsonable.annotations;

import java.lang.annotation.ElementType;

/**
 * Defines field to be serialized by JsonDefinedTransformer {@link JsonClass}<br/>
 * name is optional. If not set - using annotated field name<br/>
 * groups is list of groups supported by this Field
 */
@java.lang.annotation.Retention(java.lang.annotation.RetentionPolicy.RUNTIME)
@java.lang.annotation.Target({ElementType.FIELD})
public @interface JsonField {
    String name() default "";
    String[] groups() default {};
}