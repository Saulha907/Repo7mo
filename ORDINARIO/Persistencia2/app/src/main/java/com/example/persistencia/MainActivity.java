
package com.example.persistencia;

import android.app.Activity;
import android.app.AlertDialog;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

// Clase principal que controla nuestra aplicación.
public class MainActivity extends Activity {

    // Campos donde capturamos los datos.
    private EditText edtProducto;
    private EditText edtDescripcion;
    private EditText edtPrecio;

    // Botones para las cuatro operaciones CRUD.
    private Button btnAgregar;
    private Button btnConsultar;
    private Button btnActualizar;
    private Button btnEliminar;

    // Elementos para mostrar información.
    private TextView txtEstado;
    private ListView listaProductos;

    // Lista de productos recuperados de Supabase.
    private final ArrayList<JSONObject> productos =
            new ArrayList<>();

    // Lista de textos que se mostrarán en pantalla.
    private final ArrayList<String> nombres =
            new ArrayList<>();

    // Adaptador para conectar los datos con la lista.
    private ArrayAdapter<String> adaptador;

    // ID del producto que seleccionamos.
    // -1 indica que no tenemos ningún producto seleccionado.
    private long idSeleccionado = -1;

    // Permite ejecutar peticiones sin bloquear la pantalla.
    private final ExecutorService ejecutor =
            Executors.newSingleThreadExecutor();

    // Se ejecuta cuando iniciamos nuestra aplicación.
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Cargamos la interfaz XML.
        setContentView(R.layout.activity_main);

        // Vinculamos los campos del XML.
        edtProducto = findViewById(R.id.edtProducto);
        edtDescripcion = findViewById(R.id.edtDescripcion);
        edtPrecio = findViewById(R.id.edtPrecio);

        // Vinculamos los botones.
        btnAgregar = findViewById(R.id.btnAgregar);
        btnConsultar = findViewById(R.id.btnConsultar);
        btnActualizar = findViewById(R.id.btnActualizar);
        btnEliminar = findViewById(R.id.btnEliminar);

        // Vinculamos el texto y la lista.
        txtEstado = findViewById(R.id.txtEstado);
        listaProductos = findViewById(R.id.listaProductos);

        // Creamos el adaptador para mostrar productos.
        adaptador = new ArrayAdapter<>(
                this,
                android.R.layout.simple_list_item_1,
                nombres
        );

        // Asignamos el adaptador a la lista.
        listaProductos.setAdapter(adaptador);

        // Botón que registra nuevos productos.
        btnAgregar.setOnClickListener(
                v -> agregarProducto()
        );

        // Botón que consulta productos.
        btnConsultar.setOnClickListener(
                v -> consultarProductos()
        );

        // Botón que actualiza productos.
        btnActualizar.setOnClickListener(
                v -> actualizarProducto()
        );

        // Botón que elimina productos.
        btnEliminar.setOnClickListener(
                v -> confirmarEliminacion()
        );

        // Detectamos cuando seleccionamos un producto.
        listaProductos.setOnItemClickListener(
                (parent, view, position, id) -> {
                    seleccionarProducto(position);
                }
        );

        // Al iniciar, consultamos los productos de Supabase.
        consultarProductos();
    }

    // Muestra un mensaje corto al usuario.
    private void mensaje(String texto) {

        Toast.makeText(
                this,
                texto,
                Toast.LENGTH_SHORT
        ).show();
    }

    // Cambia el mensaje del estado de conexión.
    private void estado(String texto) {

        txtEstado.setText(texto);
    }

    // Obtiene y valida los datos del formulario.
    private JSONObject obtenerDatosFormulario() {

        // Recuperamos el nombre del producto.
        String producto = edtProducto.getText()
                .toString().trim();

        // Recuperamos la descripción.
        String descripcion = edtDescripcion.getText()
                .toString().trim();

        // Recuperamos el precio.
        String precioTexto = edtPrecio.getText()
                .toString().trim().replace(",", ".");

        // Comprobamos que ningún campo esté vacío.
        if (producto.isEmpty()
                || descripcion.isEmpty()
                || precioTexto.isEmpty()) {

            mensaje("Completa todos los campos");
            return null;
        }

        try {

            // Convertimos el precio a un número decimal.
            double precio = Double.parseDouble(precioTexto);

            // Validamos que el precio sea correcto.
            if (Double.isNaN(precio)
                    || Double.isInfinite(precio)
                    || precio < 0) {

                mensaje("Ingresa un precio válido");
                return null;
            }

            // Creamos un JSON para enviar a Supabase.
            JSONObject datos = new JSONObject();

            datos.put("producto", producto);
            datos.put("descripcion", descripcion);
            datos.put("precio", precio);

            return datos;

        } catch (Exception e) {

            mensaje("El precio debe ser un número válido");
            return null;
        }
    }

    // CREATE: agrega un producto nuevo.
    private void agregarProducto() {

        // Validamos los datos capturados.
        JSONObject datos = obtenerDatosFormulario();

        if (datos == null) {
            return;
        }

        estado("Guardando producto...");

        // Ejecutamos la petición en segundo plano.
        ejecutor.execute(() -> {

            try {

                // Llamamos a nuestra clase SupaBaseApi.
                SupaBaseApi.Respuesta respuesta =
                        SupaBaseApi.agregar(
                                datos.getString("producto"),
                                datos.getString("descripcion"),
                                datos.getDouble("precio")
                        );

                // Regresamos al hilo principal de Android.
                runOnUiThread(() -> {

                    if (respuesta.esExitosa()
                            && tieneRegistros(respuesta)) {

                        mensaje("Producto agregado correctamente");

                        // Limpiamos los campos.
                        limpiarCampos();

                        // Consultamos la lista actualizada.
                        consultarProductos();

                    } else {

                        estado("Error al agregar");

                        mensaje(
                                "No se agregó. HTTP: "
                                        + respuesta.codigo
                        );
                    }
                });

            } catch (Exception e) {

                mostrarError(e);
            }
        });
    }

    // READ: consulta los productos de Supabase.
    private void consultarProductos() {

        estado("Consultando Supabase...");

        // Ejecutamos la consulta en segundo plano.
        ejecutor.execute(() -> {

            try {

                // Obtenemos los productos desde Supabase.
                JSONArray resultado = SupaBaseApi.consultar();

                // Actualizamos los elementos de la interfaz.
                runOnUiThread(() -> {

                    // Limpiamos la lista anterior.
                    productos.clear();
                    nombres.clear();

                    // Recorremos todos los productos recibidos.
                    for (int i = 0; i < resultado.length(); i++) {

                        JSONObject item =
                                resultado.optJSONObject(i);

                        if (item != null) {

                            // Guardamos el producto completo.
                            productos.add(item);

                            // Obtenemos el nombre.
                            String nombre =
                                    item.optString("producto");

                            // Obtenemos la descripción.
                            String descripcion =
                                    item.optString("descripcion");

                            // Obtenemos el precio.
                            double precio =
                                    item.optDouble("precio", 0);

                            // Preparamos el texto de cada producto.
                            String texto =
                                    nombre + "\n"
                                            + descripcion + "\n"
                                            + "$" + String.format(
                                            Locale.US,
                                            "%.2f",
                                            precio
                                    );

                            // Agregamos el texto al listado.
                            nombres.add(texto);
                        }
                    }

                    // Actualizamos visualmente la lista.
                    adaptador.notifyDataSetChanged();

                    // Mostramos el número de productos.
                    estado(
                            "Conexión exitosa. Productos: "
                                    + productos.size()
                    );
                });

            } catch (Exception e) {

                mostrarError(e);
            }
        });
    }

    // Permite seleccionar un producto de la lista.
    private void seleccionarProducto(int posicion) {

        try {

            // Obtenemos el producto seleccionado.
            JSONObject item = productos.get(posicion);

            // Guardamos el ID.
            idSeleccionado = item.getLong("id");

            // Colocamos el nombre en el formulario.
            edtProducto.setText(
                    item.optString("producto")
            );

            // Colocamos la descripción.
            edtDescripcion.setText(
                    item.optString("descripcion")
            );

            // Colocamos el precio.
            edtPrecio.setText(
                    item.optString("precio")
            );

            // Mostramos cuál producto fue seleccionado.
            estado(
                    "Producto seleccionado: "
                            + item.optString("producto")
            );

        } catch (Exception e) {

            mensaje("Error al seleccionar producto");
        }
    }

    // UPDATE: actualiza un producto seleccionado.
    private void actualizarProducto() {

        // Comprobamos que seleccionamos un producto.
        if (idSeleccionado == -1) {

            mensaje("Selecciona un producto de la lista");
            return;
        }

        // Validamos los nuevos datos.
        JSONObject datos = obtenerDatosFormulario();

        if (datos == null) {
            return;
        }

        // Guardamos el ID del producto seleccionado.
        long id = idSeleccionado;

        estado("Actualizando producto...");

        // Ejecutamos la actualización en segundo plano.
        ejecutor.execute(() -> {

            try {

                // Enviamos los cambios a Supabase.
                SupaBaseApi.Respuesta respuesta =
                        SupaBaseApi.actualizar(
                                id,
                                datos.getString("producto"),
                                datos.getString("descripcion"),
                                datos.getDouble("precio")
                        );

                // Actualizamos la pantalla.
                runOnUiThread(() -> {

                    if (respuesta.esExitosa()
                            && tieneRegistros(respuesta)) {

                        mensaje("Producto actualizado correctamente");

                        // Limpiamos el formulario.
                        limpiarCampos();

                        // Consultamos los productos actualizados.
                        consultarProductos();

                    } else {

                        estado("No se pudo actualizar");

                        mensaje(
                                "No se actualizó. HTTP: "
                                        + respuesta.codigo
                        );
                    }
                });

            } catch (Exception e) {

                mostrarError(e);
            }
        });
    }

    // Muestra una confirmación antes de borrar.
    private void confirmarEliminacion() {

        // Verificamos que exista un producto seleccionado.
        if (idSeleccionado == -1) {

            mensaje("Selecciona un producto para eliminar");
            return;
        }

        // Mostramos una ventana de confirmación.
        new AlertDialog.Builder(this)
                .setTitle("Eliminar producto")
                .setMessage(
                        "¿Estás seguro de eliminar este producto?"
                )
                .setNegativeButton("Cancelar", null)
                .setPositiveButton(
                        "Eliminar",
                        (dialog, which) -> eliminarProducto()
                )
                .show();
    }

    // DELETE: elimina el producto seleccionado.
    private void eliminarProducto() {

        // Guardamos el ID antes de ejecutar la petición.
        long id = idSeleccionado;

        if (id == -1) {
            return;
        }

        estado("Eliminando producto...");

        // Ejecutamos la eliminación en segundo plano.
        ejecutor.execute(() -> {

            try {

                // Solicitamos eliminar el registro por su ID.
                SupaBaseApi.Respuesta respuesta =
                        SupaBaseApi.eliminar(id);

                // Actualizamos la pantalla.
                runOnUiThread(() -> {

                    if (respuesta.esExitosa()
                            && tieneRegistros(respuesta)) {

                        mensaje("Producto eliminado correctamente");

                        // Limpiamos los datos del formulario.
                        limpiarCampos();

                        // Actualizamos los productos.
                        consultarProductos();

                    } else {

                        estado("No se pudo eliminar");

                        mensaje(
                                "No se eliminó. HTTP: "
                                        + respuesta.codigo
                        );
                    }
                });

            } catch (Exception e) {

                mostrarError(e);
            }
        });
    }

    // Verifica que Supabase devolvió registros.
    private boolean tieneRegistros(
            SupaBaseApi.Respuesta respuesta
    ) {

        try {

            // Convertimos la respuesta en una lista JSON.
            JSONArray registros =
                    new JSONArray(respuesta.contenido);

            // True significa que existe al menos un registro.
            return registros.length() > 0;

        } catch (Exception e) {

            return false;
        }
    }

    // Limpia los campos del formulario.
    private void limpiarCampos() {

        edtProducto.setText("");
        edtDescripcion.setText("");
        edtPrecio.setText("");

        // Quitamos la selección anterior.
        idSeleccionado = -1;
    }

    // Muestra los errores de conexión o del servidor.
    private void mostrarError(Exception e) {

        runOnUiThread(() -> {

            estado("Error de conexión");

            String detalle = e.getMessage();

            if (detalle == null) {
                detalle = "Error desconocido";
            }

            // Mostramos una ventana con el error.
            new AlertDialog.Builder(this)
                    .setTitle("Error")
                    .setMessage(detalle)
                    .setPositiveButton("Aceptar", null)
                    .show();
        });
    }

    // Se ejecuta cuando se destruye nuestra Activity.
    @Override
    protected void onDestroy() {
        super.onDestroy();

        // Finalizamos el ejecutor de tareas.
        ejecutor.shutdown();
    }
}
