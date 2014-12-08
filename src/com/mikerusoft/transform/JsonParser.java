package com.mikerusoft.transform;

import com.mikerusoft.annotations.CustomField;
import com.mikerusoft.utils.Configuration;
import com.mikerusoft.utils.ContextManager;
import com.mikerusoft.utils.ReflectionCache;
import com.sun.istack.internal.NotNull;
import org.apache.commons.lang3.EnumUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;

/**
 * @author Grinfeld Mikhail
 * @since 12/3/2014.
 */
public class JsonParser {

    private Log log = LogFactory.getLog(JsonParser.class);

    private static final char
        START_MAP = '{',
        END_MAP = '}',
        START_ARRAY = '[',
        END_ARRAY = ']',
        ESCAPE_CHAR = '\\',
        STRING_CHAR = '"',
        CHAR_CHAR = '\'',
        ELEM_DELIM = ',',
        VALUE_DELIM = ':',
        SPACE_CHAR = ' ',
        TAB_CHAR = '\t',
        EMPTY_CHAR = '\0';

    LinkedList<Object> queue = null;

    @NotNull public static JsonParser get() {
        return new JsonParser();
    }

    private JsonParser() {
        queue = new LinkedList<Object>();
    }

    public Object parse(BufferedReader bf) throws IOException, IllegalArgumentException, InstantiationException, IllegalAccessException {
        Pair<Character, Object> resp = parseRecursive(bf);
        if (resp != null && resp.getRight() != null) {
            return resp.getRight();
        }
        return null;
    }

    /**
     * From this method starts our recursion
     * @param bf parser
     * @return returns Pair of last read character and parsed object
     * @throws IOException
     * @throws IllegalArgumentException
     */
    private Pair<Character, Object> parseRecursive(BufferedReader bf) throws IOException {
        StringBuilder sb = new StringBuilder();
        char c = '\0';
        int r = -1;
        while ( (r = bf.read()) != -1 ) {
            c = (char) r;
            if (r != SPACE_CHAR && r != TAB_CHAR) {
                Pair<Character, Object> p = parseStructure(bf, c);
                Object o = p.getRight();
                c = p.getLeft();
                if (o != null) {
                    return new ImmutablePair<Character, Object>(c, o);
                } else if (c != ELEM_DELIM) {
                    sb.append(c);
                }
            }
        }
        return new ImmutablePair<Character, Object>(c, null);
    }

    private Pair<Character, Object> parseListInnerElement(BufferedReader bf, char c) throws IOException {
        StringBuilder sb = new StringBuilder();
        do {
            Pair<Character, Object> p = parseStructure(bf, c);
            Object o = p.getRight();
            c = p.getLeft();
            if (o != null) {
                return p;
            } else if (c != ELEM_DELIM) {
                if (!(sb.length() == 0 && c == EMPTY_CHAR)) // avoid empty string at the beginning
                    sb.append(c);
            }
            int r = bf.read();
            c = r != -1 ? (char) r : EMPTY_CHAR;
        } while (bf.ready());
        return new ImmutablePair<Character, Object>(c, null);
    }

    private Object createClass(Map<String, Object> possible) {
        String cl = Configuration.getStringProperty(ContextManager.get(Configuration.class), Configuration.CLASS_PROPERTY, Configuration.DEFAULT_CLASS_PROPERTY_VALUE);
        String className = (String)possible.get(cl);
        if (StringUtils.isEmpty(className))
            return possible;
        Class<?> clazz = null;
        try {
            // using cache in order to avoid searching class in ClassLoader, but get it directly from cache.
            // There is pitfall: cache could be large same as ClassLoader, if most classes will be serialized
            clazz = ReflectionCache.get().getClass(className);
        } catch (ClassNotFoundException e) {
            return possible;
        }
        if (clazz == null)
            return possible;

        if (clazz.isEnum()) {
            try {
                return createEnum(clazz, possible);
            } catch (Exception e) {
                log.debug("Failed to create enum, return Map");
                return possible;
            }
        }
        try {
            Object o = clazz.newInstance();
            List<Method> methods = new ArrayList<Method>();
            List<Field> fields = new ArrayList<Field>();
            fields.addAll(Arrays.asList(clazz.getDeclaredFields()));

            Class<?> inherit = clazz;
            while ((inherit = inherit.getSuperclass()) != null) {
                fields.addAll(Arrays.asList(inherit.getDeclaredFields()));
                methods.addAll(ReflectionCache.getMethodsByAnnotation(inherit, CustomField.class));
            }
            for (Field f : fields) {
                Object data = possible.get(f.getName());
                if (data != null) {
                    f.setAccessible(true);
                    fill(f, o, data);
                }
            }

            for (Method m : methods) {
                String customName = m.getAnnotation(CustomField.class).name();
                Object data = possible.get(customName);
                if (data != null) {
                    m.setAccessible(true);
                    m.invoke(o, data);
                }
            }

            return o;
        } catch (IllegalAccessException iae) {
            log.debug("Failed to create class " + className);
        } catch (InstantiationException e) {
            log.debug("Failed to create class " + className);
        } catch (InvocationTargetException e) {
            log.debug("Failed to create class " + className);
        }
        return possible;
    }

    private Object createEnum(Class<?> clazz, Map<String, Object> possible) {
        String enumName = (String)possible.get("name");
        return EnumUtils.getEnum((Class<? extends Enum>)clazz, enumName);
    }

    private void fill(Field f, Object owner, Object data) throws IllegalArgumentException, IllegalAccessException {
        if (data == null || (data instanceof String && "".equals(data))) {
            data = null;
        }
        if (f.getType().isPrimitive() && data == null) {
            throw new IllegalArgumentException("Incompatible types for " + f.getName());
        }
        if (f.getType().equals(Boolean.class) || f.getType().equals(Boolean.TYPE)) {
            String value = (String)data;
            f.setBoolean(owner, !("".equals(value) || "0".equals(value) || "false".equalsIgnoreCase(value)));
        } else if (f.getType().equals(Byte.class) || f.getType().equals(Byte.TYPE)) {
            f.setByte(owner, Byte.parseByte((String) data));
        } else if (f.getType().equals(Short.class) || f.getType().equals(Short.TYPE)) {
            f.setShort(owner, Short.parseShort((String) data));
        } else if (f.getType().equals(Integer.class) || f.getType().equals(Integer.TYPE)) {
            f.setInt(owner, Integer.parseInt((String)data));
        }else if (f.getType().equals(Long.class) || f.getType().equals(Long.TYPE)) {
            f.setLong(owner, Long.parseLong((String) data));
        } else if (f.getType().equals(Double.class) || f.getType().equals(Double.TYPE)) {
            f.setDouble(owner, Double.parseDouble((String) data));
        } else if (f.getType().equals(Float.class) || f.getType().equals(Float.TYPE)) {
            f.setFloat(owner, Float.parseFloat((String) data));
        } else if (f.getType().equals(String.class)) {
            f.set(owner, data);
        } else if (f.getType().isArray() && data != null && data instanceof List) {
            List<?> l = ((List) data);
            Object ar = Array.newInstance(f.getType().getComponentType(), l.size());
            System.arraycopy(l.toArray(), 0, ar, 0, l.size());
            f.set(owner, ar);
        } else {
            f.set(owner, f.getType().cast(data));
        }

    }

    /**
     * Method determinate if structure is complicated and tries to parse it into Array, Map, Object (if class parameter exists), String or Number
     * @param bf parser
     * @param c current character
     * @return pair of last read character and parsed object
     * @throws IOException
     * @throws IllegalArgumentException
     */
    private Pair<Character, Object> parseStructure(BufferedReader bf, char c) throws IOException {
        Map<String, Object> m = null;
        List<Object> l = null;
        StringBuilder sb = new StringBuilder();
        String pn = null;
        switch (c) {
            case START_MAP:
                m = new HashMap<String, Object>();
                queue.offer(m);
                c = parseMap(bf, m);
                if (c == END_MAP) {
                    m = (Map<String,Object>)queue.pollLast();
                    String cl = Configuration.getStringProperty(ContextManager.get(Configuration.class), Configuration.CLASS_PROPERTY, Configuration.DEFAULT_CLASS_PROPERTY_VALUE);
                    if (m.containsKey(cl)) {
                        Object o = createClass(m);
                        int r = bf.read();
                        return new ImmutablePair<Character, Object>(r != -1 ? (char)r : c, o);
                    }
                    return new ImmutablePair<Character, Object>(c, m);
                }
                break;
            case START_ARRAY:
                l = new ArrayList<Object>();
                queue.offer(l);
                c = parseList(bf, l);
                if (c == END_ARRAY) {
                    l = (List<Object>)queue.pollLast();
                    int r = bf.read();
                    return new ImmutablePair<Character, Object>(r != -1 ? (char)r : c, l);
                }
                break;
            case STRING_CHAR:
            case CHAR_CHAR:
                sb = new StringBuilder();
                queue.offer(sb);
                c = parseString(bf, sb);
                pn = sb.toString().trim();
                queue.pollLast();
                if (pn.equalsIgnoreCase("null"))
                    return new ImmutablePair<Character, Object>(c, "");
                return new ImmutablePair<Character, Object>(c, pn);
            default:
                sb = new StringBuilder();
                sb.append(c);
                queue.offer(sb);
                c = parseNumber(bf, sb);
                pn = sb.toString().trim();
                Object o = pn;
                if (pn.replaceAll("[0-9]", "").equals(""))
                    o = Long.valueOf(pn);
                queue.pollLast();
                if (pn.equalsIgnoreCase("null"))
                    return new ImmutablePair<Character, Object>(c, "");
                return new ImmutablePair<Character, Object>(c, o);
        }

        return new ImmutablePair<Character, Object>(c, null);
    }

    private char parseMap(BufferedReader bf, Map<String, Object> m) throws IOException {
        char c = SPACE_CHAR;
        int r = -1;
        while ( (r = bf.read()) != -1) {
            c = (char) r;
            if (c == END_MAP) {
                return c;
            } else {
                StringBuilder sb = new StringBuilder();
                // searching key
                do {
                    if (c == -1)
                        throw new IllegalArgumentException("Reached end of stream - un-parsed data");
                    if (c != ELEM_DELIM)
                        sb.append(c);
                } while ((r = bf.read()) != -1 && (c = (char)r) != VALUE_DELIM);
                String key = sb.toString().trim();
                if (key.startsWith(String.valueOf(CHAR_CHAR)) || key.startsWith(String.valueOf(STRING_CHAR)))
                    key = key.substring(1);
                if (key.endsWith(String.valueOf(CHAR_CHAR)) || key.endsWith(String.valueOf(STRING_CHAR)))
                    key = key.substring(0, key.length() - 1);

                Pair<Character, Object> p = parseRecursive(bf);
                Object o = p.getRight();
                c = p.getLeft();
                m.put(key, o);

                if (c == END_MAP) {
                    return c;
                }
            }
        }

        return c;
    }

    private char parseList(BufferedReader bf, List<Object> l) throws IOException {
        char c = SPACE_CHAR;
        int r = -1;
        while ((r = bf.read()) != -1) {
            c = (char) r;
            if (c == END_ARRAY) {
                return c;
            } else if (c == ELEM_DELIM || c == VALUE_DELIM) {
                // do nothing
            } else {
                Pair<Character, Object> p = parseListInnerElement(bf, c);
                Object o = p.getRight();
                c = p.getLeft();
                l.add(o);
                if (c == END_ARRAY) {
                    return c;
                }
            }
        }


        return c;
    }

    private char parseNumber(BufferedReader bf, StringBuilder sb) throws IOException, IllegalArgumentException {
        char c = SPACE_CHAR;
        int r = -1;
        while ((r = bf.read()) != -1) {
            c = (char) r;
            switch (c) {
                case END_ARRAY:
                case END_MAP:
                case ELEM_DELIM:
                    return c;
            }
            sb.append(c);
        }
        return c;
    }

    private char parseString (BufferedReader bf, StringBuilder sb) throws IOException, IllegalArgumentException {
        char c = SPACE_CHAR;
        char prevC = SPACE_CHAR;
        int r = -1;
        while ( (r = bf.read()) != -1 && (c = (char) r) != -1 && !(prevC != ESCAPE_CHAR && (c == CHAR_CHAR || c == STRING_CHAR))) {
            sb.append(c);
            prevC = c;
        }
        return c;
    }
}
