package com.infra.api_gateway.utils;

import java.util.HashMap;
import java.util.Map;

/**
 * Utility class for structured logging with key-value pairs.
 * Works with logstash-logback-encoder to produce JSON structured logs.
 */
public class StructuredLogger {

    /**
     * Creates a key-value pair for structured logging.
     * Usage: log.info("Message", kv("key", value), kv("another", value2));
     *
     * @param key   the key name
     * @param value the value
     * @return a Map.Entry representing the key-value pair
     */
    public static Map.Entry<String, Object> kv(String key, Object value) {
        return new HashMap.SimpleEntry<>(key, value);
    }

    /**
     * Creates a formatted string representation for use in log messages.
     *
     * @param key   the key name
     * @param value the value
     * @return formatted string "key=value"
     */
    public static String fmt(String key, Object value) {
        return key + "=" + value;
    }
}
