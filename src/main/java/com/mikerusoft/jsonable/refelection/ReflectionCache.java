package com.mikerusoft.jsonable.refelection;

import com.mikerusoft.jsonable.adapters.MethodWrapper;
import com.mikerusoft.jsonable.adapters.ParserAdapter;
import com.mikerusoft.jsonable.annotations.CustomField;
import com.mikerusoft.jsonable.annotations.DateField;
import com.mikerusoft.jsonable.annotations.JsonClass;
import com.mikerusoft.jsonable.annotations.JsonField;
import com.mikerusoft.jsonable.transform.DateTransformer;
import com.mikerusoft.jsonable.utils.ConfInfo;
import org.apache.commons.lang3.EnumUtils;
import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.commons.lang3.StringUtils;

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
        List<Set<Invoker>> classInvokers = new ArrayList<>();
        List<Class<?>> clazzes = new ArrayList<>();
        do {
            invokers = new HashSet<>();
            ParserAdapter<?> adapter = ConfInfo.getAdapter(inherit);
            Class<?> superClass = inherit.getSuperclass();
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

            if (this.invokers.containsKey(superClass)) {
                invokers.addAll(this.invokers.get(superClass));
            }

            classInvokers.add(invokers);
            clazzes.add(inherit);

            inherit = superClass;
        } while (inherit != null && !this.invokers.containsKey(inherit) && !Object.class.equals(inherit));

        invokers = new HashSet<>();
        for (int i=classInvokers.size() - 1; i >= 0; i--) {
            Set<Invoker> l = classInvokers.get(i);
            invokers.addAll(l);
            Class<?> cl = clazzes.get(i);
            putClass(cl);
            this.invokers.put(cl, invokers);
        }

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

    public static Object getValue(Class<?> expected, Object data, int dateTimeType, String format) throws InstantiationException {
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

    public static void fill(Method m, Object owner, Object data) throws IllegalArgumentException, IllegalAccessException, InstantiationException, InvocationTargetException {
        if (data == null || (data instanceof String && "".equals(data))) {
            data = null;
        }
        if (m.getReturnType().isPrimitive() && data == null) {
            throw new IllegalArgumentException("Incompatible types for " + m.getName());
        }
        if (data == null) {
            return;
        }

        int dateType = -1;
        String format = "";
        if (m.isAnnotationPresent(DateField.class)) {
            dateType = m.getAnnotation(DateField.class).type();
            format = m.getAnnotation(DateField.class).format();
        }
        Object value = getValue(m.getParameterTypes()[0], data, dateType, format);
        if (owner == null)
            throw new IllegalArgumentException("Trying to invoke setter " + m.getName() + " for value " + String.valueOf(value) + " for Object which is null");
        m.setAccessible(true);
        m.invoke(owner, value);
    }

    public static void fill(Field f, Object owner, Object data) throws IllegalArgumentException, IllegalAccessException, InstantiationException {
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
        Object value = getValue(f.getType(), data, dateType, format);
        if (owner == null)
            throw new IllegalArgumentException("Trying to set field " + f.getName() + " for value " + String.valueOf(value) + " for Object which is null");
        f.setAccessible(true);
        f.set(owner, value);
    }
}
