package com.example.blackjack21.model;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Random;

/**
 * Guarda y recupera el estado interno exacto de un {@link Random} (que es
 * serializable), para que tras restaurar produzca la misma secuencia.
 */
public final class RandomCodec {

    private RandomCodec() {
    }

    public static byte[] encode(Random random) {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        try (ObjectOutputStream out = new ObjectOutputStream(bytes)) {
            out.writeObject(random);
        } catch (IOException e) {
            throw new IllegalStateException("No se pudo guardar el generador", e);
        }
        return bytes.toByteArray();
    }

    /** @throws IllegalArgumentException si los datos no son un Random válido. */
    public static Random decode(byte[] data) {
        try (ObjectInputStream in = new ObjectInputStream(new ByteArrayInputStream(data)) {
            @Override
            protected Class<?> resolveClass(java.io.ObjectStreamClass desc)
                    throws IOException, ClassNotFoundException {
                // Solo se acepta java.util.Random: nada de clases arbitrarias.
                if (!Random.class.getName().equals(desc.getName())) {
                    throw new IOException("Clase no permitida: " + desc.getName());
                }
                return super.resolveClass(desc);
            }
        }) {
            Object value = in.readObject();
            if (!(value instanceof Random)) {
                throw new IllegalArgumentException("No es un generador");
            }
            return (Random) value;
        } catch (IOException | ClassNotFoundException | RuntimeException e) {
            throw new IllegalArgumentException("Generador guardado inválido", e);
        }
    }
}
