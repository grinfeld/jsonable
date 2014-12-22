package com.mikerusoft.jsonable.annotations;

import java.lang.annotation.ElementType;

/**
 * Defines what field to ignore inside the class. Irrelevant for objects defined by {@see JsonClass}
 * @author Grinfeld Mikhail
 * @since 12/5/2014.
 */
@java.lang.annotation.Retention(java.lang.annotation.RetentionPolicy.RUNTIME)
@java.lang.annotation.Target({ElementType.FIELD})
public @interface IgnoreJson {
}
