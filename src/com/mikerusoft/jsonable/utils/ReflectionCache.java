package com.mikerusoft.jsonable.utils;

import com.mikerusoft.jsonable.annotations.CustomField;
import com.sun.istack.internal.NotNull;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author Grinfeld Mikhail
 * @since 12/6/2014.
 */
public class ReflectionCache {

    public static @NotNull List<Method> getMethodsByAnnotation(Class<?> clazz, Class<? extends Annotation> annotation) {
        Method[] methods = clazz.getDeclaredMethods();
        List<Method> ms = new ArrayList<Method>();
        if (methods != null) {
            for (Method m : methods) {
                if (m.getAnnotation(annotation) != null)
                    ms.add(m);
            }
        }

        return ms;
    }

    public static @NotNull List<Field> getFieldsByAnnotation(Class<?> clazz, Class<? extends Annotation> annotation) {
        Field[] fields = clazz.getDeclaredFields();
        List<Field> fs = new ArrayList<Field>();
        if (fields != null) {
            for (Field f : fields) {
                if (f.getAnnotation(annotation) != null)
                    fs.add(f);
            }
        }

        return fs;
    }

    public static ReflectionCache instance;
    private static final Object lock = new Object();

    public static ReflectionCache get() {
        if (instance == null) {
            synchronized (lock) {
                if (instance == null)
                    instance = new ReflectionCache();
            }
        }
        return instance;
    }

    Map<String, Class<?>> classes;
    Map<String, List<Method>> customMethods;

    private ReflectionCache() {
        classes = new ConcurrentHashMap<String, Class<?>>();
        customMethods = new ConcurrentHashMap<String, List<Method>>();
    }

    public void clear() {
        classes.clear();
        customMethods.clear();
    }

    protected void putClass(String className, Class<?> clazz) {
        classes.put(className, clazz);
    }

    public Class<?> getClass(String className) throws ClassNotFoundException {
        Class<?> clazz = classes.get(className);
        if (clazz == null) {
            clazz = Class.forName(className);
            classes.put(className, clazz);
        }
        return clazz;
    }

    public List<Method> getCustomMethods(String className) throws ClassNotFoundException {
        List<Method> methods = customMethods.get(className);
        if (methods == null) {
            methods = new ArrayList<Method>();
            customMethods.put(className, methods);
            Class<?> clazz = getClass(className);
            if (clazz != null) {
                Class<?> inherit = clazz;
                while ((inherit = inherit.getSuperclass()) != null) {
                    methods.addAll(getMethodsByAnnotation(inherit, CustomField.class));
                }
            }
        }
        return methods;
    }

}
