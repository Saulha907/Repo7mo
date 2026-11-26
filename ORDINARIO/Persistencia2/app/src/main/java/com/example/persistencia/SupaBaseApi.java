
package com.example.persistencia;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

// Esta clase permite conectar nuestra aplicación
// de Android Studio con la base de datos Supabase.
public class SupaBaseApi {

    // URL de nuestro proyecto de Supabase.
    private static final String SUPABASE_URL =
            "https://eziobhavxduxgeisytsl.supabase.co";

    // Clave pública para comunicarnos con Supabase.
    private static final String SUPABASE_KEY =
            "sb_publishable_m_IMXJyXARuCKM3StP-NzA_Rboqvvyd";

    // Dirección de la tabla productos.
    private static final String TABLA_URL =
            SUPABASE_URL + "/rest/v1/productos";

    // Clase que almacena la respuesta del servidor.
    public static class Respuesta {

        public int codigo;
        public String contenido;

        // Constructor de la respuesta.
        public Respuesta(int codigo, String contenido) {
            this.codigo = codigo;
            this.contenido = contenido;
        }

        // Verifica si la petición fue exitosa.
        public boolean esExitosa() {
            return codigo >= 200 && codigo < 300;
        }
    }

    // Método general para realizar peticiones HTTP.
    private static Respuesta peticion(
            String metodo,
            String direccion,
            JSONObject datos
    ) throws Exception {

        // Creamos la dirección del servidor.
        URL url = new URL(direccion);

        // Abrimos la conexión.
        HttpURLConnection conexion =
                (HttpURLConnection) url.openConnection();

        try {

            // Establecemos el método HTTP.
            conexion.setRequestMethod(metodo);

            // Configuramos los tiempos de espera.
            conexion.setConnectTimeout(15000);
            conexion.setReadTimeout(15000);

            // Enviamos la clave pública.
            conexion.setRequestProperty(
                    "apikey", SUPABASE_KEY
            );

            // Indicamos que utilizamos JSON.
            conexion.setRequestProperty(
                    "Content-Type", "application/json"
            );

            conexion.setRequestProperty(
                    "Accept", "application/json"
            );

            // Solicitamos los registros afectados.
            conexion.setRequestProperty(
                    "Prefer", "return=representation"
            );

            // Si tenemos datos, los enviamos al servidor.
            if (datos != null) {

                conexion.setDoOutput(true);

                byte[] cuerpo = datos.toString().getBytes(
                        StandardCharsets.UTF_8
                );

                try (OutputStream salida =
                             conexion.getOutputStream()) {

                    salida.write(cuerpo);
                }
            }

            // Obtenemos el código de respuesta HTTP.
            int codigo = conexion.getResponseCode();

            // Seleccionamos la respuesta o el error.
            InputStream entrada;

            if (codigo >= 200 && codigo < 300) {
                entrada = conexion.getInputStream();
            } else {
                entrada = conexion.getErrorStream();
            }

            // Guardamos el contenido recibido.
            StringBuilder resultado = new StringBuilder();

            if (entrada != null) {

                try (BufferedReader lector =
                             new BufferedReader(
                                     new InputStreamReader(
                                             entrada,
                                             StandardCharsets.UTF_8
                                     )
                             )) {

                    String linea;

                    while ((linea = lector.readLine()) != null) {
                        resultado.append(linea);
                    }
                }
            }

            // Regresamos la respuesta.
            return new Respuesta(
                    codigo,
                    resultado.toString()
            );

        } finally {

            // Cerramos la conexión.
            conexion.disconnect();
        }
    }

    // CREATE: agrega un producto a Supabase.
    public static Respuesta agregar(
            String producto,
            String descripcion,
            double precio
    ) throws Exception {

        // Creamos el JSON con los datos del producto.
        JSONObject datos = new JSONObject();

        datos.put("producto", producto);
        datos.put("descripcion", descripcion);
        datos.put("precio", precio);

        // POST inserta un nuevo registro.
        return peticion(
                "POST",
                TABLA_URL,
                datos
        );
    }

    // READ: consulta todos los productos.
    public static JSONArray consultar() throws Exception {

        // GET solicita los datos a Supabase.
        Respuesta respuesta = peticion(
                "GET",
                TABLA_URL + "?select=*&order=id.asc",
                null
        );

        // Verificamos que la consulta haya funcionado.
        if (!respuesta.esExitosa()) {

            throw new Exception(
                    "HTTP " + respuesta.codigo + ": "
                            + respuesta.contenido
            );
        }

        // Convertimos el resultado a un arreglo JSON.
        return new JSONArray(respuesta.contenido);
    }

    // UPDATE: modifica un producto por su ID.
    public static Respuesta actualizar(
            long id,
            String producto,
            String descripcion,
            double precio
    ) throws Exception {

        // Preparamos los nuevos datos del producto.
        JSONObject datos = new JSONObject();

        datos.put("producto", producto);
        datos.put("descripcion", descripcion);
        datos.put("precio", precio);

        // PATCH actualiza el registro seleccionado.
        return peticion(
                "PATCH",
                TABLA_URL + "?id=eq." + id,
                datos
        );
    }

    // DELETE: elimina un producto por su ID.
    public static Respuesta eliminar(long id) throws Exception {

        // DELETE elimina el registro seleccionado.
        return peticion(
                "DELETE",
                TABLA_URL + "?id=eq." + id,
                null
        );
    }
}
