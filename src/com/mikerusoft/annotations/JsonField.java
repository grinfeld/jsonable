package com.mikerusoft.annotations;

import java.lang.annotation.ElementType;

/**
 * Defines field to be serialized by JsonDefinedTransformer {@link JsonClass}
 */
@java.lang.annotation.Retention(java.lang.annotation.RetentionPolicy.RUNTIME)
@java.lang.annotation.Target({ElementType.FIELD})
public @interface JsonField {

}