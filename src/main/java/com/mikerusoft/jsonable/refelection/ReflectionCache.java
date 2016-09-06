package com.mikerusoft.jsonable.refelection;

import com.mikerusoft.jsonable.adapters.MethodWrapper;
import com.mikerusoft.jsonable.adapters.ParserAdapter;
import com.mikerusoft.jsonable.annotations.CustomField;
import com.mikerusoft.jsonable.annotations.DateField;
import com.mikerusoft.jsonable.annotations.JsonClass;
import com.mikerusoft.jsonable.annotations.JsonField;
import com.mikerusoft.jsonable.transform.DateTransformer;
import com.mikerusoft.jsonable.transform.JsonParser;
import com.mikerusoft.jsonable.utils.ConfInfo;
import org.apache.commons.lang3.EnumUtils;
import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.*;
import java.math.BigDecimal;
import java.net.JarURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLDecoder;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

/**
 * @author Grinfeld Mikhail
 * @since 12/6/2014.
 */
public class ReflectionCache {

    private static Log log = LogFactory.getLog(ReflectionCache.class);

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
    Map<Class<?>, Set<Invoker>> invokers;

    private ReflectionCache() {
        classes = new ConcurrentHashMap<>();
        invokers = new ConcurrentHashMap<>();
    }

    /**
     * Private helper method.
     *
     * @param connection the connection to the jar
     * @param pckgname the package name to search for
     * @param classes the current ArrayList of all classes. This method will simply add new classes.
     * @param _interface interface to bring its sub classes
     * @throws ClassNotFoundException if a file isn't loaded but still is in the jar file
     * @throws IOException if it can't correctly read from the jar file.
     */
    public static <T> void checkJarFile(JarURLConnection connection, String pckgname, List<Class<T>> classes, Class<T> _interface)
            throws ClassNotFoundException, IOException {
        final JarFile jarFile = connection.getJarFile();
        final Enumeration<JarEntry> entries = jarFile.entries();
        String name;

        for (JarEntry jarEntry = null; entries.hasMoreElements() && ((jarEntry = entries.nextElement()) != null);) {
            name = jarEntry.getName();

            if (name.contains(".class")) {
                name = name.substring(0, name.length() - 6).replace('/', '.');

                if (name.contains(pckgname)) {
                    Class<?> clazz = Class.forName(name);
                    if (_interface.isAssignableFrom(clazz) && !clazz.isInterface())
                        classes.add( (Class<T>)clazz );
                }
            }
        }
    }

    /**
     * Private helper method
     *
     * @param directory The directory to start with
     * @param pckgname The package name to search for. Will be needed for getting the Class object.
     * @param classes if a file isn't loaded but still is in the directory
     * @throws ClassNotFoundException
     */
    public static <T> void checkDirectory(File directory, String pckgname, List<Class<T>> classes, Class<T> _interface) throws ClassNotFoundException {
        File tmpDirectory;

        if (directory.exists() && directory.isDirectory()) {
            final String[] files = directory.list();

            for (final String file : files) {
                if (file.endsWith(".class")) {
                    try {
                        Class<?> clazz = Class.forName(pckgname + '.' + file.substring(0, file.length() - 6));
                        if (_interface.isAssignableFrom(clazz) && !clazz.isInterface())
                            classes.add( (Class<T>)clazz );
                    } catch (final NoClassDefFoundError e) {
                        // do nothing. this class hasn't been found by the
                        // loader, and we don't care.
                    }
                } else if ((tmpDirectory = new File(directory, file)).isDirectory()) {
                    checkDirectory(tmpDirectory, pckgname + "." + file, classes, _interface);
                }
            }
        }
    }

    /**
     * Attempts to list all the classes in the specified package as determined
     * by the context class loader
     *
     * @param pckgname the package name to search
     * @param _interface searching criteria, i.e. classes implement for _interface
     * @param <T> type of implemented interface
     * @return a list of classes that exist within that package
     * @throws ClassNotFoundException if something went wrong
     */
    public static <T> List<Class<T>> getClassesForPackage(String pckgname, Class<T> _interface)
            throws ClassNotFoundException {
        final List<Class<T>> classes = new ArrayList<Class<T>>();

        try {
            final ClassLoader cld = Thread.currentThread().getContextClassLoader();

            if (cld == null)
                throw new ClassNotFoundException("Can't get class loader.");

            final Enumeration<URL> resources = cld.getResources(pckgname.replace('.', '/'));
            URLConnection connection;
            for (URL url = null; resources.hasMoreElements() && ((url = resources.nextElement()) != null);) {
                try {
                    connection = url.openConnection();
                    if (connection instanceof JarURLConnection) {
                        checkJarFile((JarURLConnection) connection, pckgname, classes, _interface);
                    } else {
                        try {
                            checkDirectory(new File(URLDecoder.decode(url.getPath(), "UTF-8")), pckgname, classes, _interface);
                        } catch (final UnsupportedEncodingException ex) {
                            throw new ClassNotFoundException(pckgname + " does not appear to be a valid package (Unsupported encoding)", ex);
                        }
                    }
                } catch (final IOException ioex) {
                    throw new ClassNotFoundException("IOException was thrown when trying to get all resources for " + pckgname, ioex);
                }
            }
        } catch (final NullPointerException ex) {
            throw new ClassNotFoundException(pckgname + " does not appear to be a valid package (Null pointer exception)", ex);
        } catch (final IOException ioex) {
            throw new ClassNotFoundException("IOException was thrown when trying to get all resources for " + pckgname, ioex);
        }

        return classes;
    }

    public static boolean inGroup(String[] groups, String[] allGroups) {
        if (allGroups == null || allGroups.length == 0)
            return true;
        if (groups == null || groups.length == 0)
            return true;
        return new ArrayList<>(Arrays.asList(groups)).removeAll(Arrays.asList(allGroups));
    }

    public static boolean inGroup(String[] groups, List<String> allGroups) {
        if (allGroups == null || allGroups.size() == 0)
            return true;
        if (groups == null || groups.length == 0)
            return true;
        return new ArrayList<>(Arrays.asList(groups)).removeAll(allGroups);
    }

    public static void createSpecific(Map<String, Object> possible, Object o, Class<?> clazz, List<String> groups) throws IllegalAccessException, InvocationTargetException, InstantiationException {
        Collection<Invoker> invokers = get().getInvokers(clazz);
        for (Invoker i : invokers) {
            if (i.setEnabled()) {
                if (inGroup(i.getSetterGroups(), groups))
                    i.set(o, possible.get(i.getSetterName()));
            }
        }
    }

    public static Object createClass(Map<String, Object> possible, List<String> groups) {
        String cl = ConfInfo.getClassProperty();
        String className = (String)possible.get(cl);
        if (StringUtils.isEmpty(className))
            return possible;
        Class<?> clazz = null;

        try {
            // using cache in order to avoid searching class in ClassLoader, but get it directly from cache.
            // There is pitfall: cache could be large same as ClassLoader, if most classes will be serialized
            clazz = get().getClass(className);
        } catch (ClassNotFoundException e) {
            return possible;
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
            Object o = ConfInfo.getFactory(clazz).newInstance(Collections.unmodifiableMap(possible));
            createSpecific(possible, o, o.getClass(), groups);

            return o;
        } catch (IllegalAccessException | InstantiationException | InvocationTargetException e) {
            log.debug("Failed to create class " + className + " with error: " + e.getMessage());
        }
        return possible;
    }

    public static Object guessClass(Object possible, Class<?> clazz) throws ClassNotFoundException, NoSuchFieldException, InstantiationException, IllegalAccessException {
        if (clazz == null)
            return possible;
        if (possible == null)
            return null;
        if (isPrimitiveLike(clazz)) {
            if (clazz.isAssignableFrom(possible.getClass()))
                return possible;
            return getPrimitive(clazz, possible);
        } else if (Map.class.isAssignableFrom(clazz)) {
            Object actual = ConfInfo.getFactory(clazz).newInstance(Collections.unmodifiableMap((Map)possible));
            Class<?> actualClass = actual.getClass();

            Collection<Invoker> invokers = ReflectionCache.get().getInvokers(actualClass);
            // todo: add to Invoker method which return class of setter and getter
            // todo: check if it's the same class as expected in Map. If yes, call setter, else call itself again
            for (Object entry : ((Map)possible).entrySet()) {
                Map.Entry e = (Map.Entry)entry;
            }

            Method[] methods = get().getClass(clazz.getName()).getMethods();
        } else if (clazz.isArray()) {
            Class<?> compType = clazz.getComponentType();
            if (compType != null && !compType.isPrimitive() && !compType.equals(String.class)) {
                // todo: fill data
            }
        }
        return possible;
    }

    public void clear() {
        classes.clear();
        invokers.clear();
    }

    public Collection<Invoker> getInvokers(Class<?> clazz) {
        if (clazz == null || Object.class.equals(clazz))
            return new ArrayList<>();

        Set<Invoker> invokers = this.invokers.get(clazz);
        if (invokers != null)
            return Collections.unmodifiableCollection(invokers);


        Class<?> inherit = clazz;
        invokers = new HashSet<>();
        do {
            ParserAdapter<?> adapter = ConfInfo.getAdapter(inherit);
            if (inherit.getAnnotation(JsonClass.class) != null || adapter != null) {

                Field[] fields = inherit.getDeclaredFields();
                Method[] methods = inherit.getDeclaredMethods();

                for (Field f : fields) {
                    if (f.getAnnotation(JsonField.class) != null) {
                        String name = f.getAnnotation(JsonField.class).name();
                        if ("".equals(name.trim()))
                            name = f.getName();
                        invokers.add(new FieldInvoker(name, f));
                    }
                }

                if (adapter != null) {
                    for (MethodWrapper mw : adapter.getParams()) {
                        invokers.add(new MethodInvoker(mw.getName(), mw.getSetter() ,mw.getName(), mw.getGetter()));
                    }
                }

                for (Method m : methods) {
                    if (m.getAnnotation(CustomField.class) != null) {
                        String name = m.getAnnotation(CustomField.class).name();
                        if ("".equals(name.trim()))
                            name = m.getName();
                        Method setter = m.getParameterTypes().length == 1 ? m : null;
                        Method getter = m.getParameterTypes().length == 0 ? m : null;
                        String setterName = m.getParameterTypes().length == 1 ? name : null;
                        String getterName = m.getParameterTypes().length == 0 ? name : null;
                        invokers.add(new MethodInvoker(setterName, setter, getterName, getter));
                    }
                }
            }

            inherit = inherit.getSuperclass();
        } while (inherit != null && !Object.class.equals(inherit));

        putClass(clazz);
        this.invokers.put(clazz, invokers);

        return Collections.unmodifiableCollection(invokers);
    }

    protected void putClass(Class<?> clazz) { classes.put(clazz.getName(), clazz); }

    public Class<?> getClass(String className) throws ClassNotFoundException {
        Class<?> clazz = classes.get(className);
        if (clazz == null) {
            clazz = Class.forName(className);
            putClass(clazz);
        }
        return clazz;
    }

    public static Object getPrimitive(Class<?> clazz, Object value) throws InstantiationException {
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

    public static boolean isPrimitiveLike(Class<?> clazz) {
        return clazz.isPrimitive() || Boolean.class.equals(clazz) || Byte.class.equals(clazz) ||
                Short.class.equals(clazz) || Character.class.equals(clazz) ||
                Integer.class.equals(clazz) || Long.class.equals(clazz) ||
                Double.class.equals(clazz) || Float.class.equals(clazz) || clazz.equals(BigDecimal.class);
    }

    public static Object createEnum(Class<?> clazz, Map<String, Object> possible) {
        String enumName = (String)possible.get("name");
        return EnumUtils.getEnum((Class<? extends Enum>) clazz, enumName);
    }

    public static Object getValue(Class<?> expected, Class<?>[] generic, Object data, DateField annoted) throws InstantiationException {
        int dateType = -1;
        String format = "";
        if (annoted != null) {
            dateType = annoted.type();
            format = annoted.format();
        }
        return getValue(expected, generic, data, dateType, format);
    }

    private static <T> Collection<T> createCollection(Class<T> clazz) {
        return createList(clazz);
    }

    private static <T> Set<T> createSet(Class<T> clazz) {
        return new HashSet<T>();
    }

    private static <K,V> Map<K, V> createMap(Class<K> clazz1, Class<V> clazz2) {
        return new HashMap<K, V>();
    }

    private static <T> List<T> createList(Class<T> clazz) {
        return new ArrayList<T>();
    }

    public static Object getValue(Class<?> expected, Class<?>[] generic, Object data) throws InstantiationException {
        return getValue(expected, generic, data, -1, "");
    }

    public static Object getCollectionValueFromList(Class<?> expected, Class<?>[] generic, Object data) throws InstantiationException {
        Collection c = null;
        if ((Collection.class.isAssignableFrom(expected) || List.class.isAssignableFrom(expected))) {
            c = createList(data.getClass());
        } else if (Set.class.isAssignableFrom(expected)) {
            c = createSet(data.getClass());
        }

        Class<?> listType = null;
        if (generic != null && generic.length > 0)
            listType = generic[0];
        for (Object v : (Iterable) data) {
            if (v == null) {
                c.add(null);
            } else if (listType != null && !listType.isAssignableFrom(v.getClass())) {
                if (isPrimitiveLike(listType) && isPrimitiveLike(v.getClass())) {
                    c.add(getPrimitive(listType, v));
                } else {
                    c.add(null);
                }

            } else if (listType == null || listType.isAssignableFrom(v.getClass())) {
                c.add(v);
            } else {
                c.add(getValue(listType, null, v));
            }
        }
        return c;
    }

    public static Object getValue(Class<?> expected, Class<?>[] generic, Object data, int dateTimeType, String format) throws InstantiationException {
        if (data == null)
            return null;
        boolean hasJsonAnot = expected.getAnnotation(JsonClass.class) != null;
        if (hasJsonAnot && expected.isAssignableFrom(data.getClass())) {
            return expected.cast(data);
        }
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
                    if (Date.class.isAssignableFrom(data.getClass())) {
                        return ((Date)data).getTime(); // means we write out the date
                    } else {
                        return new Date((Long) data); // means we read in the date
                    }
                case DateTransformer.STRING_TYPE:
                    try {
                        DateFormat dt = new SimpleDateFormat(format);
                        if (Date.class.isAssignableFrom(data.getClass())) {
                            return dt.format((Date)data); // means we write out the date
                        } else {
                            return dt.parse((String) data);  // means we read in the date
                        }
                    } catch (ParseException e) {
                        throw new IllegalArgumentException("Incompatible types for " + expected.getName(), e);
                    }
            }
        } else if (expected.equals(String.class)) {
            return StringEscapeUtils.unescapeJson((String)data);
        } else if (expected.isArray() && data instanceof Collection) {
            Collection<?> l = ((Collection) data);
            Object ar = Array.newInstance(expected.getComponentType(), l.size());
            System.arraycopy(l.toArray(), 0, ar, 0, l.size());
            return ar;
        } else if (Collection.class.isAssignableFrom(expected) && data.getClass().isArray()) {
            return getCollectionValueFromList(expected, generic, data);
        } else if (Collection.class.isAssignableFrom(expected) && data instanceof Collection) {
            return getCollectionValueFromList(expected, generic, data);
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
                try {
                    return expected.cast(data);
                } catch (Exception e) {
                    throw new RuntimeException(String.valueOf(expected.getName()) + "  " + String.valueOf(data), e);
                }
        }
        return data;
    }

    public static void fill(Method m, Class[] generics, Object owner, Object data) throws IllegalArgumentException, IllegalAccessException, InstantiationException, InvocationTargetException {
        if (data == null || data instanceof String && "".equals(data)) {
            return;
        }

        int dateType = -1;
        String format = "";
        if (m.isAnnotationPresent(DateField.class)) {
            dateType = m.getAnnotation(DateField.class).type();
            format = m.getAnnotation(DateField.class).format();
        }

        Object value = getValue(m.getParameterTypes()[0], generics, data, dateType, format);
        if (owner == null)
            throw new IllegalArgumentException("Trying to invoke setter " + m.getName() + " for value " + String.valueOf(value) + " for Object which is null");
        m.setAccessible(true);
        m.invoke(owner, value);
    }

    public static void fill(Field f, Class[] generics, Object owner, Object data) throws IllegalArgumentException, IllegalAccessException, InstantiationException {
        if (data == null || data instanceof String && "".equals(data)) {
            return;
        }

        int dateType = -1;
        String format = "";
        if (f.isAnnotationPresent(DateField.class)) {
            dateType = f.getAnnotation(DateField.class).type();
            format = f.getAnnotation(DateField.class).format();
        }

        if (f.getGenericType() != null && f.getGenericType() instanceof ParameterizedType) {
            Type[] types = ((ParameterizedType)f.getGenericType()).getActualTypeArguments();
            if (types != null && types.length > 0) {
                generics = new Class<?>[types.length];
                for (int i=0; i<types.length; i++) {
                    if (types[i] instanceof Class)
                        generics[i] = (Class<?>)types[i];
                }
            }
        }

        Object value = getValue(f.getType(), generics, data, dateType, format);
        if (owner == null)
            throw new IllegalArgumentException("Trying to set field " + f.getName() + " for value " + String.valueOf(value) + " for Object which is null");
        f.setAccessible(true);
        f.set(owner, value);
    }
}
