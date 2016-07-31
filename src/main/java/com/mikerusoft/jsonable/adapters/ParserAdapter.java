package com.mikerusoft.jsonable.adapters;

import java.lang.reflect.Method;
import java.util.Collection;

/**
 * Adapter for Jsonable of classes which sources unavailable.
 * Main purpose to enable using 3rd party classes with jsonable
 * without trying to extends such classes only for one reason - adding jsonable annotation
 *
 * It works only for POJO beans, i.e. getting name and searching for appropriate bean public method
 * with suffixes set/get/is
 *
 * @author Grinfeld Mikhail
 * @since 7/31/2016 9:50 AM
 */
public interface ParserAdapter<T> {
    /**
     * returns class to be adapted to work with jsonable
     * @return class to be adapted to work with jsonable
     */
    public Class<T> getClazz();
    public Method getParam(String name);
    public Collection<MethodWrapper> getParams();
}
