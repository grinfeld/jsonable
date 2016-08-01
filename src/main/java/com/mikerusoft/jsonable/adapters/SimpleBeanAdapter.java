package com.mikerusoft.jsonable.adapters;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * This adapter is for POJO Beans where properties has setters and getters (get and is)
 * This adapter checks all methods and tries to define properties by following rules:
 * if exists public method with 'get' or 'is' prefix without any param or method with set prefix with one parameter -
 * It takes such method, removes prefix and define it as bean property.
 * If there are more than one setter or getter for same property - takes the last one
 * Methods defined in {@link Object} are not taken
 * @author Grinfeld Mikhail
 * @since 8/1/2016 6:09 PM
 */
public class SimpleBeanAdapter<T> implements ParserAdapter<T> {
    private Class<T> clazz;
    final Map<String, MethodWrapper> methods;

    public SimpleBeanAdapter(Class<T> clazz) {
        if (clazz == null)
            throw new IllegalArgumentException("You should specify class for Adapter");
        this.clazz = clazz;
        methods = new ConcurrentHashMap<>();
        extractWrappers();
    }

    private void extractWrappers() {
        for (Method m : clazz.getMethods()) {
            if (Modifier.isPublic(m.getModifiers()) && m.getParameterTypes().length <= 1) {
                Method getter = null;
                Method setter = null;
                String property = null;
                if (m.getName().startsWith("get") && m.getParameterTypes().length <= 0) {
                    property = m.getName().substring(3);
                    getter = m;
                } else if (m.getName().startsWith("is") && m.getParameterTypes().length <= 0) {
                    property = m.getName().substring(2);
                    getter = m;
                } else if (m.getName().startsWith("set") && m.getParameterTypes().length == 1) {
                    property = m.getName().substring(3);
                    setter = m;
                }
                if (property != null) {
                    // we don't want methods inherited from object to be added into Bean
                    try {
                        if (Object.class.getMethod(m.getName()) != null) {
                            property = null;
                        }
                    } catch (Exception ignore){}
                }
                if (property != null) {
                    property = property.substring(0,1).toLowerCase() + property.substring(1);
                    MethodWrapper mw = methods.get(property);
                    if (mw == null) {
                        methods.put(property, new MethodWrapper(property, property, setter, getter));
                    } else {
                        if (getter != null)
                            mw.setGetter(m);
                        if (setter != null)
                            mw.setSetter(m);
                    }
                }
            }
        }
    }

    public Class<T> getClazz() { return clazz; }
    public Method getParam(String name) { return null; }
    public Collection<MethodWrapper> getParams() { return Collections.unmodifiableCollection(this.methods.values()); }
}
