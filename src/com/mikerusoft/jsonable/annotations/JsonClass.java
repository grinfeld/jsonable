package com.mikerusoft.jsonable.annotations;

import java.lang.annotation.ElementType;

/**
 * Defines class to be serialized by JsonDefinedTransformer
 */
@java.lang.annotation.Retention(java.lang.annotation.RetentionPolicy.RUNTIME)
@java.lang.annotation.Target({ElementType.TYPE})
public @interface JsonClass {

}