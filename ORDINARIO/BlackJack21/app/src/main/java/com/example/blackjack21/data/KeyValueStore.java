package com.example.blackjack21.data;

import java.util.Map;

/** Almacén clave-texto. En Android: SharedPreferences propias del juego. */
public interface KeyValueStore {

    String get(String key);

    /** Escribe todas las claves en una sola operación atómica (valor null = borrar). */
    void writeAll(Map<String, String> values);
}
