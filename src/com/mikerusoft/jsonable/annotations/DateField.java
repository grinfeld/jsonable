package com.mikerusoft.jsonable.annotations;

import java.lang.annotation.ElementType;

/**
 * This Annotation defines field as Date element. Works with {@link com.mikerusoft.jsonable.annotations.JsonField} only. <br/>
 * There are 2 supported types: <br/>
 * 1. {@link com.mikerusoft.jsonable.transform.DateTransformer.TIMESTAMP_TYPE} and it's default. Means that date is converted into long represented Timestamp <br/>
 * 2. {@link com.mikerusoft.jsonable.transform.DateTransformer.STRING_TYPE}. Means default string is returned. <br/>
 *
 * @author Grinfeld Mikhail
 * @since 1/5/2015.
 */
@java.lang.annotation.Retention(java.lang.annotation.RetentionPolicy.RUNTIME)
@java.lang.annotation.Target({ElementType.FIELD})
public @interface DateField {
    int type() default 0;
}
