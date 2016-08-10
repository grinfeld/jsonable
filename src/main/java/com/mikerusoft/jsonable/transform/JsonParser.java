package com.mikerusoft.jsonable.transform;

import com.mikerusoft.jsonable.adapters.MethodWrapper;
import com.mikerusoft.jsonable.adapters.ParserAdapter;
import com.mikerusoft.jsonable.annotations.CustomField;
import com.mikerusoft.jsonable.annotations.DateField;
import com.mikerusoft.jsonable.annotations.JsonField;
import com.mikerusoft.jsonable.utils.ConfInfo;
import com.mikerusoft.jsonable.utils.ReflectionCache;
import org.apache.commons.lang3.EnumUtils;
import org.apache.commons.lang3.StringEscapeUtils;
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
import java.math.BigDecimal;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
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
        END_LINE = '\n',
        EMPTY_CHAR = '\0';

    LinkedList<Object> queue = null;
    List<String> groups;
    public static JsonParser get(String...groups) {
        return new JsonParser(groups);
    }

    private JsonParser(String...groups) {
        this.queue = new LinkedList<>();
        this.groups = groups == null? null : new ArrayList<>(Arrays.asList(groups));
    }

    public <T> T parse(BufferedReader bf, Class<T> clazz) throws IOException, IllegalArgumentException, InstantiationException, IllegalAccessException {
        Pair<Character, Object> resp = parseRecursive(bf, clazz);
        if (resp != null && resp.getRight() != null) {
            return (T)resp.getRight();
        }
        return null;
    }

    /**
     * From this method starts our recursion
     * @param bf parser
     * @param clazz
     * @return returns Pair of last read character and parsed object
     * @throws IOException
     * @throws IllegalArgumentException
     */
    private Pair<Character, Object> parseRecursive(BufferedReader bf, Class<?> clazz) throws IOException, InstantiationException {
        StringBuilder sb = new StringBuilder();
        char c = '\0';
        int r = -1;
        while ( (r = bf.read()) != -1 ) {
            c = (char) r;
            if (c != SPACE_CHAR && c != TAB_CHAR && c != END_LINE) {
                Pair<Character, Object> p = parseStructure(clazz, bf, c);
                Object o = p.getRight();
                c = p.getLeft();
                if (o != null) {
                    return new ImmutablePair<>(c, o);
                } else if (c != ELEM_DELIM) {
                    sb.append(c);
                }
            }
        }
        return new ImmutablePair<>(c, null);
    }

    private Pair<Character, Object> parseListInnerElement(BufferedReader bf, char c, Class<?> initialClass) throws IOException, InstantiationException {
        StringBuilder sb = new StringBuilder();
        do {
            Pair<Character, Object> p = parseStructure(initialClass, bf, c);
            Object o = p.getRight();
            c = p.getLeft();
            if (o != null) {
                return p;
            } else if (c != ELEM_DELIM) {
                if (!(sb.length() == 0 && c == EMPTY_CHAR) && c == END_LINE) // avoid empty string at the beginning
                    sb.append(c);
            }
            int r = bf.read();
            c = r != -1 ? (char) r : EMPTY_CHAR;
        } while (bf.ready());
        return new ImmutablePair<>(c, null);
    }

    private boolean inGroup(String[] groups) {
        if (this.groups == null || this.groups.size() == 0)
            return true;
        if (groups == null || groups.length == 0)
            return true;
        return new ArrayList<>(Arrays.asList(groups)).removeAll(this.groups);
    }

    private boolean isPrimitiveLike(Class<?> clazz) {
        return clazz.isPrimitive() || Boolean.class.equals(clazz) || Byte.class.equals(clazz) ||
                Short.class.equals(clazz) || Character.class.equals(clazz) ||
                Integer.class.equals(clazz) || Long.class.equals(clazz) ||
                Double.class.equals(clazz) || Float.class.equals(clazz) || clazz.equals(BigDecimal.class);
    }

    private Object getPrimitive(Class<?> clazz, Object value) throws InstantiationException {
        if (value == null)
            return null;
        if (Byte.TYPE.equals(clazz) || Byte.class.equals(clazz)) {
            Byte b = Byte.valueOf(String.valueOf(value));
            if (b != null && Byte.TYPE.equals(clazz))
                return b.byteValue();
            return b;
        } else if (Boolean.TYPE.equals(clazz) || Boolean.class.equals(clazz)) {
            Boolean s = Boolean.valueOf(String.valueOf(value));
            if (s != null && Byte.TYPE.equals(clazz))
                return s.booleanValue();
            return s;
        } else if (Short.TYPE.equals(clazz) || Short.class.equals(clazz)) {
            Short s = Short.valueOf(String.valueOf(value));
            if (s != null && Byte.TYPE.equals(clazz))
                return s.shortValue();
            return s;
        } else if (Character.TYPE.equals(clazz) || Character.class.equals(clazz)) {
            Character c = String.valueOf(value).charAt(0);
            if (c != null && Byte.TYPE.equals(clazz))
                return c.charValue();
            return c;
        } else if (Integer.TYPE.equals(clazz) || Integer.class.equals(clazz)) {
            Integer i = Integer.valueOf(String.valueOf(value));
            if (i != null && Byte.TYPE.equals(clazz))
                return i.intValue();
            return i;
        } else if (Long.TYPE.equals(clazz) || Long.class.equals(clazz)) {
            Long l = Long.valueOf(String.valueOf(value));
            if (l != null && Byte.TYPE.equals(clazz))
                return l.longValue();
            return l;
        } else if (Float.TYPE.equals(clazz) || Float.class.equals(clazz)) {
            Float f = Float.valueOf(String.valueOf(value));
            if (f != null && Byte.TYPE.equals(clazz))
                return f.floatValue();
            return f;
        } else if (Double.TYPE.equals(clazz) || Double.class.equals(clazz)) {
            Double d = Double.valueOf(String.valueOf(value));
            if (d != null && Byte.TYPE.equals(clazz))
                return d.doubleValue();
            return d;
        } else if (BigDecimal.class.equals(clazz)) {
            return BigDecimal.valueOf(Double.valueOf(String.valueOf(value)));
        }
        throw new InstantiationException("Invalid type" + clazz + " for value " + value); // should never occur
    }

    private void createFromAdapter(Map<String, Object> data, Object o, ParserAdapter<?> adapter) throws InvocationTargetException, IllegalAccessException, InstantiationException {
        for (MethodWrapper wrapper : adapter.getParams()) {
            Method m = wrapper.getSetter();
            if (m != null){
                m.invoke(o, getValue(m.getParameterTypes()[0], data.get(wrapper.getName()), -1, ""));
            }
        }
    }

    private Object createClass(Map<String, Object> possible, Class<?> initialClass) {
        String cl = ConfInfo.getClassProperty(); // Configuration.getStringProperty(ContextManager.get(Configuration.class), Configuration.CLASS_PROPERTY, Configuration.DEFAULT_CLASS_PROPERTY_VALUE);
        String className = (String)possible.get(cl);
        if (initialClass == null || StringUtils.isEmpty(className))
            return possible;
        Class<?> clazz = initialClass;
        if (!StringUtils.isEmpty(className)) {
            try {
                // using cache in order to avoid searching class in ClassLoader, but get it directly from cache.
                // There is pitfall: cache could be large same as ClassLoader, if most classes will be serialized
                clazz = ReflectionCache.get().getClass(className);
            } catch (ClassNotFoundException e) {
                return possible;
            }
            if (initialClass != null && !initialClass.isAssignableFrom(clazz)) {
                throw new IllegalArgumentException("Class " + initialClass.getName() + " couldn't be assigned by " + className);
            }
        }

        if (clazz == null)
            return possible;

        try {
            if (isPrimitiveLike(clazz)) {
                Object value = possible.get("value");
                return getPrimitive(clazz, value);
            }
            if (clazz.isEnum()) {
                return createEnum(clazz, possible);
            }
            ParserAdapter adapter = ConfInfo.getAdapter(clazz);
            Object o = clazz.newInstance();
            if (adapter != null) {
                createFromAdapter(possible, o, adapter);
            }

            createFromAnnotation(possible, o, clazz);

            return o;
        } catch (IllegalAccessException | InstantiationException | InvocationTargetException e) {
            log.debug("Failed to create class " + className + " with error: " + e.getMessage());
        }
        return possible;
    }

    public void createFromAnnotation(Map<String, Object> possible, Object o, Class<?> clazz) throws IllegalAccessException, InvocationTargetException, InstantiationException {
        Set<Method> methods = new HashSet<Method>();
        Set<Field> fields = new HashSet<Field>();
        fields.addAll(ReflectionCache.getFieldsByAnnotation(clazz, JsonField.class));
        methods.addAll(ReflectionCache.getMethodsByAnnotation(clazz, CustomField.class));

        Class<?> inherit = clazz;
        while ((inherit = inherit.getSuperclass()) != null) {
            fields.addAll(ReflectionCache.getFieldsByAnnotation(inherit, JsonField.class));
            methods.addAll(ReflectionCache.getMethodsByAnnotation(inherit, CustomField.class));
        }
        for (Field f : fields) {
            JsonField an = f.getAnnotation(JsonField.class);
            String name = an != null && !StringUtils.isEmpty(an.name()) ? an.name() : f.getName();
            String[] groups = an != null ? an.groups() : null;
            if (inGroup(groups)) {
                Object data = possible.get(name);
                if (data != null) {
                    f.setAccessible(true);
                    fill(f, o, data);
                }
            }
        }

        for (Method m : methods) {
            CustomField an = m.getAnnotation(CustomField.class);
            if (m.getReturnType().equals(Void.class) && m.getParameterTypes() != null && m.getParameterTypes().length == 1) {
                String customName = an.name();
                String[] groups = an.groups();
                if (inGroup(groups)) {
                    Object data = possible.get(customName);
                    if (data != null) {
                        m.setAccessible(true);
                        int dateType = -1;
                        String format = "";
                        if (m.isAnnotationPresent(DateField.class)) {
                            dateType = m.getAnnotation(DateField.class).type();
                            format = m.getAnnotation(DateField.class).format();
                        }
                        m.invoke(o, getValue(m.getParameterTypes()[0], data, dateType, format));
                    }
                }
            }
        }
    }

    private Object createEnum(Class<?> clazz, Map<String, Object> possible) {
        String enumName = (String)possible.get("name");
        return EnumUtils.getEnum((Class<? extends Enum>) clazz, enumName);
    }

    private Object getValue(Class<?> expected, Object data, int dateTimeType, String format) throws InstantiationException {
        if (data == null)
            return null;
        if (expected.equals(Boolean.TYPE) || expected.equals(Boolean.class)) {
            if (data instanceof String) {
                String value = (String) data;
                return !("".equals(value) || "0".equals(value) || "false".equalsIgnoreCase(value));
            } else {
                return data;
            }
        } else if (expected.isPrimitive()) {
            return getPrimitive(expected, data);
        } else if (Date.class.isAssignableFrom(expected) && (dateTimeType == DateTransformer.TIMESTAMP_TYPE || dateTimeType == DateTransformer.STRING_TYPE)) {
            switch (dateTimeType) {
                case DateTransformer.TIMESTAMP_TYPE:
                    return new Date((Long)data);
                case DateTransformer.STRING_TYPE:
                    try {
                        DateFormat dt = new SimpleDateFormat(format);
                        return dt.parse((String)data);
                    } catch (ParseException e) {
                        throw new IllegalArgumentException("Incompatible types for " + expected.getName(), e);
                    }
            }
        } else if (expected.equals(String.class)) {
            return StringEscapeUtils.unescapeJson((String)data);
        } else if (expected.isArray() && data instanceof List) {
            List<?> l = ((List) data);
            Object ar = Array.newInstance(expected.getComponentType(), l.size());
            System.arraycopy(l.toArray(), 0, ar, 0, l.size());
            return ar;
        } else if (List.class.isAssignableFrom(expected) && data instanceof List) {
            List<?> l = ((List) data);
            return l;
        } else if (Set.class.isAssignableFrom(expected) && data instanceof List) {
            List<?> l = ((List) data);
            Set<?> s = new HashSet<>(l);
            return s;
        } else if (Collection.class.isAssignableFrom(expected) && data instanceof List) {
            List<?> l = ((List) data);
            return l;
        } else  if (expected.isEnum()) {
            if (data.getClass().isEnum())
                return data;
            else if (data instanceof String) {
                String s = (String)data;
                if (!StringUtils.isEmpty(s)) {
                    return Enum.valueOf((Class<? extends Enum>)expected, s);
                }
                return null;
            }
        } else {
            if (data.getClass().equals(Long.class)) {
                Long val = (Long) data;
                if (expected.equals(Byte.class)) {
                    return val.byteValue();
                } else if (expected.equals(Short.class)) {
                    return val.shortValue();
                } else if (expected.equals(Integer.class)) {
                    return val.intValue();
                } else if (expected.equals(Long.class)) {
                    return val;
                }
            } else  if (data.getClass().equals(Double.class)) {
                Double val = (Double) data;
                if (expected.equals(Float.class)) {
                    return val.floatValue();
                } else if (expected.equals(Double.class)) {
                    return val;
                }
            } else
                return expected.cast(data);
        }
        return data;
    }

    private void fill(Field f, Object owner, Object data) throws IllegalArgumentException, IllegalAccessException, InstantiationException {
        if (data == null || (data instanceof String && "".equals(data))) {
            data = null;
        }
        if (f.getType().isPrimitive() && data == null) {
            throw new IllegalArgumentException("Incompatible types for " + f.getName());
        }
        if (data == null) {
            return;
        }

        int dateType = -1;
        String format = "";
        if (f.isAnnotationPresent(DateField.class)) {
            dateType = f.getAnnotation(DateField.class).type();
            format = f.getAnnotation(DateField.class).format();
        }
        f.set(owner, getValue(f.getType(), data, dateType, format));
    }

    private <T> T cast(Class<T> clazz, Object value) {
        return clazz.cast(value);
    }

    /**
     * Method determinate if structure is complicated and tries to parse it into Array, Map, Object (if class parameter exists), String or Number
     *
     * @param initialClass initial class to start parse
     * @param bf parser
     * @param c current character
     * @return pair of last read character and parsed object
     * @throws IOException
     * @throws IllegalArgumentException
     */
    private Pair<Character, Object> parseStructure(Class<?> initialClass, BufferedReader bf, char c) throws IOException, InstantiationException {
        Map<String, Object> m = null;
        List l = null;
        StringBuilder sb = new StringBuilder();
        String pn = null;
        switch (c) {
            case START_MAP:
                m = new HashMap<>();
                queue.offer(m);
                c = parseMap(bf, m);
                if (c == END_MAP) {
                    m = (Map<String,Object>)queue.pollLast();
                    String cl = ConfInfo.getClassProperty(); // Configuration.getStringProperty(ContextManager.get(Configuration.class), Configuration.CLASS_PROPERTY, Configuration.DEFAULT_CLASS_PROPERTY_VALUE);
                    int r = bf.read();
                    if (m.containsKey(cl) || initialClass != null) {
                        Object o = createClass(m, initialClass);
                        return new ImmutablePair<>(r != -1 ? (char)r : c, o);
                    }
                    return new ImmutablePair<Character, Object>(r != -1 ? (char)r : c, m);
                }
                break;
            case START_ARRAY:
                l = new ArrayList<>();
                queue.offer(l);
                c = parseList(bf, l, null);
                if (c == END_ARRAY) {
                    l = (List)queue.pollLast();
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
                if (pn == null)
                    return new ImmutablePair<>(c, null);
                Object result = pn;
                if (initialClass != null) {
                    if (initialClass.isAssignableFrom(Character.class) || initialClass.isAssignableFrom(Character.TYPE)) {
                        if (pn.length() == 1)
                            result = pn.charAt(0);
                        else
                            throw new IllegalArgumentException(initialClass.getName() + " should be with legth 1");
                    }
                }
                return new ImmutablePair<>(c, result);
            default:
                sb = new StringBuilder();
                sb.append(c);
                queue.offer(sb);
                c = parseNumber(bf, sb);
                pn = sb.toString().trim();
                Object o = pn;
                if ("".equals(pn.trim()))
                    return new ImmutablePair<Character, Object>(c, "");
                if (pn.replaceAll("[0-9]", "").equals(""))
                    o = getPrimitive((initialClass == null ? Long.class : initialClass), pn);
                if (pn.replaceAll("[0-9]", "").equals("."))
                    o = getPrimitive((initialClass == null ? Double.class : initialClass), pn);
                if (pn.trim().toLowerCase().equals("false") || pn.trim().toLowerCase().equals("true"))
                    o = getPrimitive((initialClass == null ? Boolean.class : initialClass), pn);
                queue.pollLast();
                if (pn.equalsIgnoreCase("null")) {
                    return new ImmutablePair<Character, Object>(c, "");
                }
                return new ImmutablePair<>(c, o);
        }

        return new ImmutablePair<>(c, null);
    }

    private char parseMap(BufferedReader bf, Map<String, Object> m) throws IOException, InstantiationException {
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
                    if (c != ELEM_DELIM && c != END_LINE)
                        sb.append(c);
                } while ((r = bf.read()) != -1 && (c = (char)r) != VALUE_DELIM);
                String key = sb.toString().trim();
                if (sb.length() == 1 && sb.charAt(0) == END_MAP) {
                    return END_MAP;
                }
                if (key.startsWith(String.valueOf(CHAR_CHAR)) || key.startsWith(String.valueOf(STRING_CHAR)))
                    key = key.substring(1);
                if (key.endsWith(String.valueOf(CHAR_CHAR)) || key.endsWith(String.valueOf(STRING_CHAR)))
                    key = key.substring(0, key.length() - 1);

                Pair<Character, Object> p = parseRecursive(bf, null);
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

    private char parseList(BufferedReader bf, List l, Class<?> initialClass) throws IOException, InstantiationException {
        char c = SPACE_CHAR;
        int r = -1;
        while ((r = bf.read()) != -1) {
            c = (char) r;
            if (c == END_ARRAY) {
                return c;
            } else if (c == ELEM_DELIM || c == VALUE_DELIM) {
                // do nothing
            } else {
                Pair<Character, Object> p = parseListInnerElement(bf, c, initialClass);
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
            if (c != END_LINE)
                sb.append(c);
        }
        return c;
    }

    private char parseString (BufferedReader bf, StringBuilder sb) throws IOException, IllegalArgumentException {
        char c = SPACE_CHAR;
        char prevC = SPACE_CHAR;
        int r = -1;
        boolean escaped = false;
        while ( (r = bf.read()) != -1 && (c = (char) r) != -1 && !(prevC != ESCAPE_CHAR && (c == CHAR_CHAR || c == STRING_CHAR))) {
            if (prevC == ESCAPE_CHAR && (c == CHAR_CHAR || c == STRING_CHAR)) {
                // do nothing
            } else if (prevC == ESCAPE_CHAR && c == ESCAPE_CHAR) {
                sb.append(ESCAPE_CHAR);
            }
            if (c != ESCAPE_CHAR) {
                sb.append(c);
            }
            prevC = c;
        }
        return c;
    }
}
