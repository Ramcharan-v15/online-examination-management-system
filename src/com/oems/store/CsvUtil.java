package com.oems.store;

import java.util.ArrayList;
import java.util.List;

/** Minimal CSV quoting/parsing (RFC 4180 style) for the data files. No external library. */
public final class CsvUtil {
    private CsvUtil() {}

    public static String quote(String value) {
        if (value == null) return "";
        if (value.contains(",") || value.contains("\"") || value.contains("\n")) {
            return "\"" + value.replace("\"", "\"\"") + "\"";
        }
        return value;
    }

    public static String join(String... fields) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < fields.length; i++) {
            if (i > 0) sb.append(',');
            sb.append(quote(fields[i]));
        }
        return sb.toString();
    }

    public static List<String> parse(String line) {
        List<String> fields = new ArrayList<>();
        StringBuilder cur = new StringBuilder();
        boolean inQuotes = false;
        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);
            if (inQuotes) {
                if (c == '"') {
                    if (i + 1 < line.length() && line.charAt(i + 1) == '"') { cur.append('"'); i++; }
                    else inQuotes = false;
                } else cur.append(c);
            } else if (c == '"') {
                inQuotes = true;
            } else if (c == ',') {
                fields.add(cur.toString()); cur.setLength(0);
            } else cur.append(c);
        }
        fields.add(cur.toString());
        return fields;
    }
}
