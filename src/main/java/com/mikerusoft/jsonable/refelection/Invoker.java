package com.mikerusoft.jsonable.refelection;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;

/**
 * @author Grinfeld Mikhail
 * @since 8/11/2016.
 */
public interface Invoker {
    public Object get(Object o) throws IllegalAccessException, InvocationTargetException;
    public void set(Object o, Object param) throws IllegalAccessException, InvocationTargetException, InstantiationException;
    public boolean setEnabled();
    public boolean getEnabled();
    public String getSetterName();
    public String getGetterName();
    public String[] getGetterGroups();
    public String[] getSetterGroups();
}
