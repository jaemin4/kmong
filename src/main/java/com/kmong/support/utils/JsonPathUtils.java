package com.kmong.support.utils;

import java.util.Map;

@SuppressWarnings("unchecked")
public final class JsonPathUtils {
    private JsonPathUtils() {}

    public static String getS(Map<String, Object> root, String path) {
        Object v = get(root, path);
        return v == null ? null : String.valueOf(v);
    }

    public static Integer getI(Map<String, Object> root, String path) {
        Object v = get(root, path);
        if (v instanceof Number n) return n.intValue();
        if (v instanceof String s) try { return Integer.parseInt(s); } catch (Exception ignore) {}
        return null;
    }

    public static Double getD(Map<String, Object> root, String path) {
        Object v = get(root, path);
        if (v instanceof Number n) return n.doubleValue();
        if (v instanceof String s) try { return Double.parseDouble(s); } catch (Exception ignore) {}
        return null;
    }

    private static Object get(Map<String, Object> cur, String path) {
        Object node = cur;
        for (String k : path.split("\\.")) {
            if (!(node instanceof Map)) return null;
            node = ((Map<String, Object>) node).get(k);
            if (node == null) return null;
        }
        return node;
    }
}
