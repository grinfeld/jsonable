package com.mikerusoft.jsonable.transform;

import com.mikerusoft.jsonable.utils.Configuration;
import com.mikerusoft.jsonable.utils.ContextManager;
import com.mikerusoft.jsonable.utils.Outputter;
import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;

/**
 * Converts enum into Json Object in following syntax:
 * {
 *  'name': this.name()
 * }
 * @author Grinfeld Mikhail
 * @since 12/5/2014.
 */
public class EnumTransformer  extends TransformerImpl {
    @Override
    public boolean match(Object o) { return matchClass(o.getClass()); }

    @Override
    public boolean matchClass(Class<?> clazz) {
        return clazz.isEnum();
    }

    @Override
    public void transform(Object o, Outputter<String> out, String... groups) throws IOException, InvocationTargetException, IllegalAccessException {
        Configuration c = ContextManager.get(Configuration.class);
        boolean asClass = Configuration.getBooleanProperty(c, Configuration.ENUM_AS_CLASS_PROPERTY, false);
        boolean excludeClass = Configuration.getBooleanProperty(c, Configuration.EXCLUDE_CLASS_PROPERTY, false);
        if (asClass && !excludeClass) {
            String cl = Configuration.getStringProperty(c, Configuration.CLASS_PROPERTY, Configuration.DEFAULT_CLASS_PROPERTY_VALUE);
            out.write("{\"name\" : \"" + ((Enum) o).name() + "\",\"" + cl + "\":\"" + o.getClass().getName() + "\"}");
        } else {
            out.write(("\"" + StringEscapeUtils.escapeJson(((Enum) o).name()) + "\""));
        }
    }

    @Override
    public int matchPriority() {
        return 0;
    }
}
