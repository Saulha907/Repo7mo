
import React, { useEffect, useState } from 'react';

// Importamos los elementos necesarios.
import {
  View,
  Text,
  FlatList,
  TouchableOpacity,
  StyleSheet,
  ActivityIndicator
} from 'react-native';

// Importamos la conexión con Supabase.
import { supabase } from '../lib/supabase';

// Este componente muestra todos los productos.
export default function ConsultarProductos({ version }) {

  // Lista donde guardaremos los productos consultados.
  const [productos, setProductos] = useState([]);

  // Estado para indicar cuando estamos cargando datos.
  const [cargando, setCargando] = useState(false);

  // Mensaje para mostrar posibles errores.
  const [error, setError] = useState('');

  // Función que obtiene los productos de Supabase.
  const obtenerProductos = async () => {
    setCargando(true);
    setError('');

    // SELECT consulta todos los registros de la tabla.
    const { data, error: errorConsulta } = await supabase
      .from('productos')
      .select('*')
      .order('id', { ascending: true });

    if (errorConsulta) {
      // Mostramos el error si la consulta falla.
      setError(errorConsulta.message);
      setProductos([]);
    } else {
      // Guardamos los registros recibidos.
      setProductos(data || []);
    }

    setCargando(false);
  };

  // Ejecutamos la consulta al abrir el componente
  // y cuando cambia la versión enviada por App.js.
  useEffect(() => {
    obtenerProductos();
  }, [version]);

  return (
    <View style={styles.contenedor}>
      <Text style={styles.titulo}>Consultar productos</Text>

      {/* Botón para volver a consultar la base de datos. */}
      <TouchableOpacity
        style={styles.boton}
        onPress={obtenerProductos}
      >
        <Text style={styles.textoBoton}>
          Actualizar lista
        </Text>
      </TouchableOpacity>

      {/* Mostramos un indicador mientras cargamos. */}
      {cargando ? (
        <ActivityIndicator size="large" color="#16803C" />
      ) : (
        <FlatList
          data={productos}
          scrollEnabled={false}
          keyExtractor={(item) => String(item.id)}

          // Diseñamos cada tarjeta de producto.
          renderItem={({ item }) => (
            <View style={styles.tarjeta}>
              <Text style={styles.nombre}>
                {item.producto}
              </Text>

              <Text>{item.descripcion}</Text>

              <Text style={styles.precio}>
                ${Number(item.precio).toFixed(2)}
              </Text>
            </View>
          )}

          // Mensaje si no existen productos.
          ListEmptyComponent={
            <Text>No hay productos registrados</Text>
          }
        />
      )}

      {/* Mostramos cualquier error de consulta. */}
      {error !== '' && (
        <Text style={styles.error}>{error}</Text>
      )}
    </View>
  );
}

// Estilos para mostrar los productos.
const styles = StyleSheet.create({
  contenedor: {
    padding: 15
  },
  titulo: {
    fontSize: 22,
    fontWeight: 'bold',
    marginBottom: 15
  },
  boton: {
    backgroundColor: '#16803C',
    padding: 14,
    borderRadius: 8,
    marginBottom: 15
  },
  textoBoton: {
    color: '#FFFFFF',
    textAlign: 'center',
    fontWeight: 'bold'
  },
  tarjeta: {
    backgroundColor: '#FFFFFF',
    padding: 15,
    borderRadius: 10,
    marginBottom: 10,
    borderWidth: 1,
    borderColor: '#EEEEEE'
  },
  nombre: {
    fontSize: 19,
    fontWeight: 'bold',
    marginBottom: 5
  },
  precio: {
    fontSize: 17,
    color: '#16803C',
    fontWeight: 'bold',
    marginTop: 8
  },
  error: {
    color: 'red',
    marginTop: 10
  }
});
