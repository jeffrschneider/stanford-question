package me.rabrg.util;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Stream;

public final class MapUtil {

    private MapUtil() {
    }

    public static <K, V extends Comparable<? super V>> Map<K, V> orderValue(final Map<K, V> map) {
        final Map<K, V> result = new LinkedHashMap<>();
        final Stream<Map.Entry<K, V>> st = map.entrySet().stream();
        st.sorted(Map.Entry.comparingByValue()).forEachOrdered(e -> result.put(e.getKey(), e.getValue()));
        return result;
    }
}
