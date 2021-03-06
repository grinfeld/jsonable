package com.mikerusoft.jsonable.parser;

import com.mikerusoft.jsonable.transform.JsonParser;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.*;

/**
 * Class to parse JSON
 * @author Grinfeld Mikhail
 * @since 5/27/2014.
 */
public class JsonReader {

    private static Log log = LogFactory.getLog(JsonReader.class);

    /**
     * Reads JSON from input stream and converts to clazz
     * @param in input stream to read JSON
     * @param clazz class to convert to
     * @param groups list of groups to serialize data for them
     * @param <T> type of class to convert to
     * @return returns T
     * @throws IOException on failure of reading stream
     * @throws IllegalArgumentException on unexpected data
     */
    public static <T> T read(InputStream in, Class<T> clazz, String...groups) throws IOException, IllegalArgumentException {
        BufferedReader bf = new BufferedReader(new InputStreamReader(in));
        try {
            return JsonParser.get(groups).parse(bf, clazz);
        } catch (InstantiationException | IllegalAccessException | ClassNotFoundException | NoSuchFieldException e) {
            log.error(e);
            throw new IllegalArgumentException("Failed to convert Json to Object", e);
        }
    }

    /**
     * Reads JSON from String and converts to clazz
     * @param in input stream to read JSON
     * @param clazz class to convert to
     * @param groups list of groups to serialize data for them
     * @param <T> type of class to convert to
     * @return returns T
     * @throws IOException on reading string
     * @throws IllegalArgumentException on invalid data
     */
    public static <T> T read(String in, Class<T> clazz, String...groups) throws IOException, IllegalArgumentException {
        BufferedReader bf = new BufferedReader(new StringReader(in));
        try {
            return JsonParser.get(groups).parse(bf, clazz);
        } catch (InstantiationException | IllegalAccessException | ClassNotFoundException | NoSuchFieldException e) {
            log.error(e);
            throw new IllegalArgumentException("Failed to convert Json to Object", e);
        }
    }

    /**
     * Reads JSON from input stream and converts to java object
     * @param in input stream to read JSON
     * @param groups list of groups to serialize data for them
     * @return returns T
     * @throws IOException on failure reading stream
     * @throws IllegalArgumentException if data is invalid
     */
    public static Object read(InputStream in, String...groups) throws IOException, IllegalArgumentException {
        BufferedReader bf = new BufferedReader(new InputStreamReader(in));
        try {
            return JsonParser.get(groups).parse(bf, null);
        } catch (InstantiationException | IllegalAccessException | ClassNotFoundException | NoSuchFieldException e) {
            log.error(e);
            throw new IllegalArgumentException("Failed to convert Json to Object", e);
        }
    }

    /**
     * Reads JSON from String and converts to java object
     * @param in input stream to read JSON
     * @param groups list of groups to serialize data for them
     * @return returns T
     * @throws IOException on failure reading string
     * @throws IllegalArgumentException if data is invalid
     */
    public static Object read(String in, String...groups) throws IOException, IllegalArgumentException {
        BufferedReader bf = new BufferedReader(new StringReader(in));
        try {
            return JsonParser.get(groups).parse(bf, null);
        } catch (InstantiationException | IllegalAccessException | ClassNotFoundException | NoSuchFieldException e) {
            log.error(e);
            throw new IllegalArgumentException("Failed to convert Json to Object", e);
        }
    }
}

