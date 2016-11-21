package com.mikerusoft.jsonable.refelection;

import com.mikerusoft.jsonable.annotations.DateField;
import com.mikerusoft.jsonable.annotations.JsonField;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * @author Grinfeld Mikhail
 * @since 8/11/2016.
 */
public class MethodInvoker implements Invoker {

    Method setter;
    Method getter;
    String setterName;
    String getterName;

    public MethodInvoker(String setterName, Method setter,String getterName,  Method getter) {
        this.setter = setter;
        this.getter = getter;
        this.setterName = setterName;
        this.getterName = getterName;
    }

    @Override
    public Object get(Object o) throws IllegalAccessException, InvocationTargetException, InstantiationException {
        getter.setAccessible(true);
        Object result = getter.invoke(o);
        if (result == null)
            return null;
        Class<?>[] generic = null;
        if (getter.getReturnType().getComponentType() != null) {
            generic = new Class<?>[] { getter.getReturnType().getComponentType() };
        }
        return ReflectionCache.getValue(getter.getReturnType(), generic, result, getGetterAnnotation(DateField.class));
    }

    @Override
    public void set(Object o, Object param) throws IllegalAccessException, InvocationTargetException, InstantiationException {
        /*Class<?>[] generic = null;
        if (setter.getParameterTypes()[0] != null) {
            generic = new Class<?>[] { setter.getParameterTypes()[0] };
        }*/
        ReflectionCache.fill(setter, null, o, param);
    }

    @Override
    public boolean setEnabled() {
        return setter != null;
    }

    @Override
    public boolean getEnabled() {
        return getter != null;
    }

    @Override
    public String getSetterName() { return setterName; }

    @Override
    public String getGetterName() { return getterName; }

    @Override
    public String[] getGetterGroups() {
        return getEnabled() && getter.isAnnotationPresent(JsonField.class) ? getter.getAnnotation(JsonField.class).groups() : null;
    }

    @Override
    public String[] getSetterGroups() {
        return setEnabled() && setter.isAnnotationPresent(JsonField.class) ? setter.getAnnotation(JsonField.class).groups() : null;
    }

    @Override
    public <T extends Annotation> T getGetterAnnotation(Class<T> annotationClass) {
        return getEnabled() && getter.isAnnotationPresent(annotationClass) ? getter.getAnnotation(annotationClass) : null;
    }

    @Override
    public <T extends Annotation> T getSetterAnnotation(Class<T> annotationClass) {
        return setEnabled() && setter.isAnnotationPresent(annotationClass) ? setter.getAnnotation(annotationClass) : null;
    }
}
