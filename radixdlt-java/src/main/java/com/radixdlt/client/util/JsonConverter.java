package com.radixdlt.client.util;

import com.google.gson.Gson;

/**
 * "default comment"
 *
 * @author GoodforGod
 * @since 22.06.2018
 */
public class JsonConverter {

    public static <T> T convert(final String string,
                                final Class<T> tClass) {
        return new Gson().fromJson(string, tClass);
    }
}
