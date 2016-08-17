package com.mikerusoft.jsonable.transform;

import com.mikerusoft.jsonable.refelection.ReflectionCache;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

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
            return Null;

        transformer = (Transformer)matched.values().toArray()[0];
        cache.put(o.getClass().getName(), transformer);
        return transformer;
    }

    private static Transformer[] all() {
        List<Transformer> transformers = new ArrayList<Transformer>();
        try {
            Package p = TransformerFactory.class.getPackage();
            List<Class<Transformer>> transformerClasses = ReflectionCache.getClassesForPackage(p.getName(), Transformer.class);
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

}
