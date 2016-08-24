package com.mikerusoft.jsonable.adapters;

import java.util.Map;

/**
 * @author Grinfeld Mikhail
 * @since 8/20/2016.
 */
public interface ReadInstanceFactory<O, F> {
    public Class<O> getFactoryClass();
    public F newInstance(Map<String, Object> data) throws IllegalAccessException, InstantiationException;
}
