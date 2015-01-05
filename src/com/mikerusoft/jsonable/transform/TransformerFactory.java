package com.mikerusoft.jsonable.transform;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import sun.net.www.protocol.file.FileURLConnection;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.JarURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLDecoder;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

/**
 * @author Grinfeld Mikhail
 * Thanks to BrainStone (http://stackoverflow.com/users/1996022/brainstone) stackoverflow user for helpers methods
 * @since 5/25/2014.
 */
public class TransformerFactory {

    private static Log log = LogFactory.getLog(TransformerFactory.class);

    final static Transformer Null = new NullTransformer();

    private static final Transformer[] transformers = all();

    private static Map<String, Transformer> cache = new ConcurrentHashMap<String, Transformer>();

    public static Transformer get(Object o) {
        Map<Integer, Transformer> matched = new TreeMap<Integer, Transformer>();
        if (Null.match(o))
            return Null;
        Transformer transformer = cache.get(o.getClass().getName());
        if (transformer != null)
            return transformer;
        for (Transformer t : transformers) {
            if (t.match(o))
                matched.put(t.matchPriority(), t);
        }
        if (matched.size() <= 0)
            return null;

        transformer = (Transformer)matched.values().toArray()[0];
        cache.put(o.getClass().getName(), transformer);
        return transformer;
    }

    private static Transformer[] all() {
        List<Transformer> transformers = new ArrayList<Transformer>();
        try {
            Package p = TransformerFactory.class.getPackage();
            List<Class<Transformer>> transformerClasses = getClassesForPackage(p.getName(), Transformer.class);
            for (Class<Transformer> cl : transformerClasses) {
                try {
                    transformers.add(cl.newInstance());
                } catch (Exception e) {
                    log.debug("Failed to load class " + cl.getCanonicalName());
                }
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        Transformer[] transformersArr = new Transformer[transformers.size()];
        transformersArr = transformers.toArray(transformersArr);
        return transformersArr;
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
    private static <T> void checkJarFile(JarURLConnection connection, String pckgname, List<Class<T>> classes, Class<T> _interface)
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
     * Attempts to list all the classes in the specified package as determined
     * by the context class loader
     *
     * @param pckgname the package name to search
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
                    } else if (connection instanceof FileURLConnection) {
                        try {
                            checkDirectory(new File(URLDecoder.decode(url.getPath(), "UTF-8")), pckgname, classes, _interface);
                        } catch (final UnsupportedEncodingException ex) {
                            throw new ClassNotFoundException(pckgname + " does not appear to be a valid package (Unsupported encoding)", ex);
                        }
                    } else
                        throw new ClassNotFoundException(pckgname + " (" + url.getPath() + ") does not appear to be a valid package");
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

    /**
     * Private helper method
     *
     * @param directory The directory to start with
     * @param pckgname The package name to search for. Will be needed for getting the Class object.
     * @param classes if a file isn't loaded but still is in the directory
     * @throws ClassNotFoundException
     */
    private static <T> void checkDirectory(File directory, String pckgname, List<Class<T>> classes, Class<T> _interface) throws ClassNotFoundException {
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
}
