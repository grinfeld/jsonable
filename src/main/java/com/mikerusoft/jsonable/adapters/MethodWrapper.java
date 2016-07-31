package com.mikerusoft.jsonable.adapters;

import java.lang.reflect.Method;

/**
 * Wrapper for java.lang.Method contains additional info for serializing with jsonable
 * property name (name
 * @author Grinfeld Mikhail
 * @since 7/31/2016 10:09 AM
 */
public class MethodWrapper {
    Method setter;
    Method getter;
    String property;
    String name;

    public MethodWrapper(String property, String name, Method setter, Method getter) {
        this.property = property;
        this.name = name;
        this.setter = setter;
        this.getter = getter;
    }

    public Method getSetter() { return setter; }
    public void setSetter(Method setter) { this.setter = setter; }
    public Method getGetter() { return getter; }
    public void setGetter(Method getter) { this.getter = getter; }
    public String getProperty() { return property; }
    public void setProperty(String property) { this.property = property; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
}
