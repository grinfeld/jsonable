package com.mikerusoft.transform;

import com.mikerusoft.utils.Configuration;
import com.mikerusoft.utils.ContextManager;
import com.mikerusoft.utils.Outputter;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;

/**
 * @author Grinfeld Mikhail
 * @since 12/5/2014.
 */
public class EnumTransformer implements Transformer {
    @Override
    public boolean match(Object o) { return o.getClass().isEnum(); }

    @Override
    public void transform(Object o, Outputter<String> out) throws IOException, InvocationTargetException, IllegalAccessException {
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
