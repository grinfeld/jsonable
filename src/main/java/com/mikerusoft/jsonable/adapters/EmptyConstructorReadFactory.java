package com.mikerusoft.jsonable.adapters;

import java.util.Map;

/**
 * @author Grinfeld Mikhail
 * @since 8/24/2016.
 */
public class EmptyConstructorReadFactory<T> implements ReadInstanceFactory<T, T> {
    Class<T> clazz;

    public EmptyConstructorReadFactory(Class<T> clazz) {
        try {
            if (clazz.getConstructor() == null)
                throw new IllegalArgumentException("No empty constructor");
        } catch (NoSuchMethodException e) {
            throw new IllegalArgumentException("No empty constructor");
        }
        this.clazz = clazz;

    }

    @Override
    public Class<T> getFactoryClass() {
        return clazz;
    }

    @Override
    public T newInstance(Map<String, Object> data) throws IllegalAccessException, InstantiationException {
        return clazz.newInstance();
    }
}
