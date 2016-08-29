package com.mikerusoft.jsonable.transform;

import com.mikerusoft.jsonable.utils.ConfInfo;
import com.mikerusoft.jsonable.refelection.ReflectionCache;
import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.commons.lang3.text.translate.*;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.*;

/**
 * @author Grinfeld Mikhail
 * @since 12/3/2014.
 */
public class JsonParser {

    private static Log log = LogFactory.getLog(JsonParser.class);

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

    public <T> T parse(BufferedReader bf, Class<T> clazz) throws IOException, IllegalArgumentException, InstantiationException, IllegalAccessException, ClassNotFoundException, NoSuchFieldException {
        Pair<Character, Object> resp = parseRecursive(bf);
        if (resp != null && resp.getRight() != null) {
            return (T)resp.getRight();
            /*if (ConfInfo.isExcludeClass() && clazz != null) {
                ret = ReflectionCache.guessClass(ret, clazz);
            }
            return ret;*/
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
    private Pair<Character, Object> parseRecursive(BufferedReader bf) throws IOException, InstantiationException {
        StringBuilder sb = new StringBuilder();
        char c = '\0';
        int r = -1;
        while ( (r = bf.read()) != -1 ) {
            c = (char) r;
            if (c != SPACE_CHAR && c != TAB_CHAR && c != END_LINE) {
                Pair<Character, Object> p = parseStructure(bf, c);
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

    private Pair<Character, Object> parseListInnerElement(BufferedReader bf, char c) throws IOException, InstantiationException {
        StringBuilder sb = new StringBuilder();
        do {
            Pair<Character, Object> p = parseStructure(bf, c);
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

    /**
     * Method determinate if structure is complicated and tries to parse it into Array, Map, Object (if class parameter exists), String or Number
     *
     * @param bf parser
     * @param c current character
     * @return pair of last read character and parsed object
     * @throws IOException
     * @throws IllegalArgumentException
     */
    private Pair<Character, Object> parseStructure(BufferedReader bf, char c) throws IOException, InstantiationException {
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
                    if (m.containsKey(cl)) {
                        Object o = ReflectionCache.createClass(m, this.groups);
                        return new ImmutablePair<>(r != -1 ? (char)r : c, o);
                    }
                    return new ImmutablePair<Character, Object>(r != -1 ? (char)r : c, m);
                }
                break;
            case START_ARRAY:
                l = new ArrayList<>();
                queue.offer(l);
                c = parseList(bf, l);
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
                pn = sb.toString();
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
                if ("".equals(pn.trim()))
                    return new ImmutablePair<Character, Object>(c, "");
                if (pn.replaceAll("[0-9]", "").equals(""))
                    o = ReflectionCache.getPrimitive(Long.class, pn);
                if (pn.replaceAll("[0-9]", "").equals("."))
                    o = ReflectionCache.getPrimitive(Double.class, pn);
                if (pn.trim().toLowerCase().equals("false") || pn.trim().toLowerCase().equals("true"))
                    o = ReflectionCache.getPrimitive(Boolean.class, pn);
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
                key = UNESCAPE_JSON.translate(key);
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

    private char parseList(BufferedReader bf, List l) throws IOException, InstantiationException {
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

    private static char parseNumber(BufferedReader bf, StringBuilder sb) throws IOException, IllegalArgumentException {
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

    private static char parseString (BufferedReader bf, StringBuilder all) throws IOException, IllegalArgumentException {
        char c = SPACE_CHAR;
        char prevC = SPACE_CHAR;
        int r = -1;
        StringBuilder sb = new StringBuilder();
        while ( (r = bf.read()) != -1 && (c = (char) r) != -1 && !(prevC != ESCAPE_CHAR && (c == CHAR_CHAR || c == STRING_CHAR))) {
            if (prevC == ESCAPE_CHAR && (c == CHAR_CHAR || c == STRING_CHAR)) {
                // do nothing
            } else if (prevC == ESCAPE_CHAR && c == ESCAPE_CHAR) {
                sb.append(ESCAPE_CHAR);
            } else if (prevC == ESCAPE_CHAR && c == 'u') {
                sb.append(ESCAPE_CHAR);
            }
            if (c != ESCAPE_CHAR) {
                sb.append(c);
            }
            prevC = c;
        }
        all.append(UNESCAPE_JSON.translate(sb.toString()));
        return c;
    }

    private static final CharSequenceTranslator UNESCAPE_JSON =
        new AggregateTranslator(
            new OctalUnescaper(),     // .between('\1', '\377'),
            // new UnicodeUnescaper(), - we don't want unicode translator
            new LookupTranslator(EntityArrays.JAVA_CTRL_CHARS_UNESCAPE()),
            new LookupTranslator(
                    new String[][] {
                            {"\\\\", "\\"},
                            {"\\\"", "\""},
                            {"\\'", "'"},
                            {"\\", ""}
                    })
    );
}
