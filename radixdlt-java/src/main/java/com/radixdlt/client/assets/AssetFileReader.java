package com.radixdlt.client.assets;

import com.radixdlt.client.util.JsonFileReader;

import java.io.FileNotFoundException;

/**
 * "default comment"
 *
 * @author GoodforGod
 * @since 22.06.2018
 */
public class AssetFileReader {

    public static Asset read(final String filename) {
        try {
            return JsonFileReader.read(filename, Asset.class);
        } catch (FileNotFoundException e) {
            throw new NullPointerException("File not found.");
        }
    }

    public static Asset read(final String filename,
                             final String path) {
        try {
            return JsonFileReader.read(filename, Asset.class, path);
        } catch (FileNotFoundException e) {
            throw new NullPointerException("File not found.");
        }
    }
}
