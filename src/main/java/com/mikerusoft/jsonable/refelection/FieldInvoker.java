package com.mikerusoft.jsonable.refelection;

import com.mikerusoft.jsonable.annotations.DateField;
import com.mikerusoft.jsonable.annotations.JsonField;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;

/**
 * @author Grinfeld Mikhail
 * @since 8/11/2016.
 */
public class FieldInvoker implements Invoker {
    Field field;
    String name;
    boolean setEnabled = true;
    boolean getEnabled = true;

    public FieldInvoker(String name, Field field) {
        this.field = field;
        this.name = name;
    }

    @Override
    public Object get(Object o) throws IllegalAccessException, InstantiationException {
        field.setAccessible(true);
        Object result = field.get(o);
        if (result == null)
            return null;
        Class<?>[] generics = field.getType().getComponentType() != null ?  new Class[] { field.getType().getComponentType() } : null;
        return ReflectionCache.getValue(field.getType(), generics, result, getGetterAnnotation(DateField.class));
    }

    @Override
    public void set(Object o, Object param) throws IllegalAccessException, InvocationTargetException, InstantiationException {
        Class<?>[] generics = field.getType().getComponentType() != null ?  new Class[] { field.getType().getComponentType() } : null;
        ReflectionCache.fill(field, generics, o, param);
    }

    @Override
    public boolean setEnabled() { return setEnabled; }

    @Override
    public boolean getEnabled() { return getEnabled; }

    public void setEnabled(boolean setEnabled) { this.setEnabled = setEnabled; }
    public void getEnabled(boolean getEnabled) { this.getEnabled = getEnabled; }

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

    @Override
    public <T extends Annotation> T getGetterAnnotation(Class<T> annotationClass) {
        return field.isAnnotationPresent(annotationClass) ? field.getAnnotation(annotationClass) : null;
    }

    @Override
    public <T extends Annotation> T getSetterAnnotation(Class<T> annotationClass) {
        return field.isAnnotationPresent(annotationClass) ? field.getAnnotation(annotationClass) : null;
    }
}
