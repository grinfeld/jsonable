package com.mikerusoft.jsonable.refelection;

import com.mikerusoft.jsonable.annotations.JsonField;

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
    public Object get(Object o) throws IllegalAccessException, InvocationTargetException {
        getter.setAccessible(true);
        return getter.invoke(o);
    }

    @Override
    public void set(Object o, Object param) throws IllegalAccessException, InvocationTargetException, InstantiationException {
        ReflectionCache.fill(setter, o, param);
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
}
