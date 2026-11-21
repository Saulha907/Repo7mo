package com.example.blackjack21.data;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * JSON mínimo en Java puro (org.json de Android no funciona en las pruebas
 * locales). Objetos → LinkedHashMap, listas → ArrayList, enteros → Long,
 * decimales → Double. La escritura es determinista (respeta el orden).
 */
final class Json {

    private static final int MAX_DEPTH = 32;

    private final String text;
    private int pos;

    private Json(String text) {
        this.text = text;
    }

    // --- Escritura -------------------------------------------------------------------

    static String write(Object value) {
        StringBuilder out = new StringBuilder();
        write(value, out);
        return out.toString();
    }

    private static void write(Object value, StringBuilder out) {
        if (value == null) {
            out.append("null");
        } else if (value instanceof String) {
            quote((String) value, out);
        } else if (value instanceof Boolean || value instanceof Integer || value instanceof Long) {
            out.append(value);
        } else if (value instanceof Map) {
            out.append('{');
            boolean first = true;
            for (Map.Entry<?, ?> e : ((Map<?, ?>) value).entrySet()) {
                if (!first) {
                    out.append(',');
                }
                first = false;
                quote(String.valueOf(e.getKey()), out);
                out.append(':');
                write(e.getValue(), out);
            }
            out.append('}');
        } else if (value instanceof List) {
            out.append('[');
            boolean first = true;
            for (Object item : (List<?>) value) {
                if (!first) {
                    out.append(',');
                }
                first = false;
                write(item, out);
            }
            out.append(']');
        } else {
            throw new IllegalArgumentException("Tipo no admitido: " + value.getClass());
        }
    }

    private static void quote(String s, StringBuilder out) {
        out.append('"');
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            switch (c) {
                case '"':
                    out.append("\\\"");
                    break;
                case '\\':
                    out.append("\\\\");
                    break;
                case '\n':
                    out.append("\\n");
                    break;
                case '\r':
                    out.append("\\r");
                    break;
                case '\t':
                    out.append("\\t");
                    break;
                default:
                    if (c < 0x20) {
                        out.append(String.format("\\u%04x", (int) c));
                    } else {
                        out.append(c);
                    }
            }
        }
        out.append('"');
    }

    // --- Lectura ---------------------------------------------------------------------

    /** @throws IllegalArgumentException si el texto no es JSON válido. */
    static Object parse(String text) {
        if (text == null) {
            throw new IllegalArgumentException("Sin datos");
        }
        Json p = new Json(text);
        Object value = p.value(0);
        p.skipSpaces();
        if (p.pos != text.length()) {
            throw p.error("datos sobrantes");
        }
        return value;
    }

    private Object value(int depth) {
        if (depth > MAX_DEPTH) {
            throw error("anidamiento excesivo");
        }
        skipSpaces();
        if (pos >= text.length()) {
            throw error("fin inesperado");
        }
        char c = text.charAt(pos);
        switch (c) {
            case '{':
                return object(depth);
            case '[':
                return array(depth);
            case '"':
                return string();
            case 't':
                return literal("true", Boolean.TRUE);
            case 'f':
                return literal("false", Boolean.FALSE);
            case 'n':
                return literal("null", null);
            default:
                return number();
        }
    }

    private Map<String, Object> object(int depth) {
        Map<String, Object> map = new LinkedHashMap<>();
        pos++;
        skipSpaces();
        if (peek() == '}') {
            pos++;
            return map;
        }
        while (true) {
            skipSpaces();
            if (peek() != '"') {
                throw error("se esperaba una clave");
            }
            String key = string();
            skipSpaces();
            expect(':');
            map.put(key, value(depth + 1));
            skipSpaces();
            char c = next();
            if (c == '}') {
                return map;
            }
            if (c != ',') {
                throw error("se esperaba , o }");
            }
        }
    }

    private List<Object> array(int depth) {
        List<Object> list = new ArrayList<>();
        pos++;
        skipSpaces();
        if (peek() == ']') {
            pos++;
            return list;
        }
        while (true) {
            list.add(value(depth + 1));
            skipSpaces();
            char c = next();
            if (c == ']') {
                return list;
            }
            if (c != ',') {
                throw error("se esperaba , o ]");
            }
        }
    }

    private String string() {
        expect('"');
        StringBuilder sb = new StringBuilder();
        while (true) {
            char c = next();
            if (c == '"') {
                return sb.toString();
            }
            if (c == '\\') {
                char e = next();
                switch (e) {
                    case '"':
                    case '\\':
                    case '/':
                        sb.append(e);
                        break;
                    case 'n':
                        sb.append('\n');
                        break;
                    case 'r':
                        sb.append('\r');
                        break;
                    case 't':
                        sb.append('\t');
                        break;
                    case 'b':
                        sb.append('\b');
                        break;
                    case 'f':
                        sb.append('\f');
                        break;
                    case 'u':
                        if (pos + 4 > text.length()) {
                            throw error("escape incompleto");
                        }
                        try {
                            sb.append((char) Integer.parseInt(text.substring(pos, pos + 4), 16));
                        } catch (NumberFormatException ex) {
                            throw error("escape inválido");
                        }
                        pos += 4;
                        break;
                    default:
                        throw error("escape inválido");
                }
            } else {
                sb.append(c);
            }
        }
    }

    private Object number() {
        int start = pos;
        while (pos < text.length() && "+-0123456789.eE".indexOf(text.charAt(pos)) >= 0) {
            pos++;
        }
        String n = text.substring(start, pos);
        if (n.isEmpty()) {
            throw error("valor inesperado");
        }
        try {
            if (n.contains(".") || n.contains("e") || n.contains("E")) {
                return Double.parseDouble(n);
            }
            return Long.parseLong(n);
        } catch (NumberFormatException e) {
            throw error("número inválido");
        }
    }

    private Object literal(String word, Object value) {
        if (!text.startsWith(word, pos)) {
            throw error("literal inválido");
        }
        pos += word.length();
        return value;
    }

    private void skipSpaces() {
        while (pos < text.length() && Character.isWhitespace(text.charAt(pos))) {
            pos++;
        }
    }

    private char peek() {
        return pos < text.length() ? text.charAt(pos) : '\0';
    }

    private char next() {
        if (pos >= text.length()) {
            throw error("fin inesperado");
        }
        return text.charAt(pos++);
    }

    private void expect(char c) {
        if (next() != c) {
            throw error("se esperaba " + c);
        }
    }

    private IllegalArgumentException error(String what) {
        return new IllegalArgumentException("JSON inválido (" + what + ") en " + pos);
    }
}
