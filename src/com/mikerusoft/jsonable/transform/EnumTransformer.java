package com.mikerusoft.jsonable.transform;

import com.mikerusoft.jsonable.utils.Configuration;
import com.mikerusoft.jsonable.utils.ContextManager;
import com.mikerusoft.jsonable.utils.Outputter;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;

/**
 * Converts enum into Json Object in following syntax:
 * {
 *  'name': this.name(),
 *  'class': getClass().name()  // in case there is not set {@link Configuration.EXCLUDE_CLASS_PROPERTY}
 * }
 * @author Grinfeld Mikhail
 * @since 12/5/2014.
 */
public class EnumTransformer implements Transformer {
    @Override
    public boolean match(Object o) { return matchClass(o.getClass()); }

    @Override
    public boolean matchClass(Class<?> clazz) {
        return clazz.isEnum();
    }

    @Override
    public void transform(Object o, Outputter<String> out, String... groups) throws IOException, InvocationTargetException, IllegalAccessException {
        out.write("{");
        out.write("\"name");
        out.write("\": \"");
        out.write(((Enum) o).name());
        out.write("\" ");

        boolean excludeClass = Configuration.getBooleanProperty(ContextManager.get(Configuration.class), Configuration.EXCLUDE_CLASS_PROPERTY, false);
        if (!excludeClass) {
            out.write((", \"class\": \"" + o.getClass().getName() + "\""));
        }

        out.write("}");
    }

    @Override
    public int matchPriority() {
        return 0;
    }
}
