package com.mikerusoft.jsonable.refelection;

import com.mikerusoft.jsonable.annotations.JsonField;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;

/**
 * @author Grinfeld Mikhail
 * @since 8/11/2016.
 */
public class FieldInvoker implements Invoker {
    Field field;
    String name;

    public FieldInvoker(String name, Field field) {
        this.field = field;
        this.name = name;
    }

    @Override
    public Object get(Object o) throws IllegalAccessException {
        field.setAccessible(true);
        return field.get(o);
    }

    @Override
    public void set(Object o, Object param) throws IllegalAccessException, InvocationTargetException, InstantiationException {
        ReflectionCache.fill(field, o, param);
    }

    @Override
    public boolean setEnabled() {
        return true;
    }

    @Override
    public boolean getEnabled() {
        return true;
    }

    @Override
    public String getSetterName() { return name; }

    @Override
    public String getGetterName() { return name; }

    @Override
    public String[] getGetterGroups() {
        return field.isAnnotationPresent(JsonField.class) ? field.getAnnotation(JsonField.class).groups() : null;
    }

    @Override
    public String[] getSetterGroups() {
        return field.isAnnotationPresent(JsonField.class) ? field.getAnnotation(JsonField.class).groups() : null;
    }
}
