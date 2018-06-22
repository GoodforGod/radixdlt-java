package com.radixdlt.client.util;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.InputStreamReader;
import java.util.stream.Collectors;

/**
 * "default comment"
 *
 * @author GoodforGod
 * @since 22.06.2018
 */
public class JsonFileReader {

    private static final String HOME_PATH = "";

    public static <T> T read(final String filename,
                             final Class<T> tClass) throws FileNotFoundException {

        return read(filename, tClass, HOME_PATH);
    }

    public static <T> T read(final String filename,
                             final Class<T> tClass,
                             final String path) throws FileNotFoundException {
        if(path == null) {
            return read(filename, tClass);
        }

        final InputStreamReader inputStreamReader = new FileReader(path + filename);
        final BufferedReader reader = new BufferedReader(inputStreamReader);
        final String fileAsString = reader.lines().collect(Collectors.joining());
        return JsonConverter.convert(fileAsString, tClass);
    }
}
