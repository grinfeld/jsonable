package com.mikerusoft.jsonable.utils;

/**
 * Class to store data about property where property is a name of property and name (know, sounds bad)
 * is a name to be used in serialization
 * @author Grinfeld Mikhail
 * @since 7/31/2016 11:40 AM
 */
public class PropertyPair {
    String property;
    String name;

    public PropertyPair(String property, String name) {
        this.property = property;
        this.name = name;
    }

    public String getProperty() { return property; }
    public void setProperty(String property) { this.property = property; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
}
