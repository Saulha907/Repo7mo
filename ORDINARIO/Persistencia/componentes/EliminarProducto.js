
import React, { useEffect, useState } from 'react';

// Componentes visuales de React Native.
import {
  View,
  Text,
  TouchableOpacity,
  Alert,
  StyleSheet
} from 'react-native';

// Conexión con nuestra base de datos.
import { supabase } from '../lib/supabase';

export default function EliminarProducto({ alEliminar }) {

  // Lista de productos registrados.
  const [productos, setProductos] = useState([]);

  // Consultamos los productos en Supabase.
  const cargarProductos = async () => {
    const { data, error } = await supabase
      .from('productos')
      .select('*')
      .order('id', { ascending: true });

    if (error) {
      Alert.alert('Error', error.message);
    } else {
      setProductos(data || []);
    }
  };

  // Cargamos los productos al abrir el componente.
  useEffect(() => {
    cargarProductos();
  }, []);

  // Función que elimina un producto por su ID.
  const eliminarProducto = async (id) => {

    // DELETE elimina el registro que tenga ese ID.
    const { data, error } = await supabase
      .from('productos')
      .delete()
      .eq('id', id)
      .select('id');

    if (error) {
      Alert.alert('Error', error.message);
      return;
    }

    // Confirmamos que el producto realmente se eliminó.
    if (!data || data.length === 0) {
      Alert.alert('Aviso', 'No se encontró el producto');
      return;
    }

    Alert.alert('Éxito', 'Producto eliminado correctamente');

    // Volvemos a consultar los productos.
    await cargarProductos();

    // Avisamos a App.js que hubo un cambio.
    if (alEliminar) alEliminar();
  };

  // Mostramos una confirmación antes de eliminar.
  const confirmarEliminacion = (id) => {
    Alert.alert(
      'Confirmar eliminación',
      '¿Estás seguro de eliminar este producto?',
      [
        {
          text: 'Cancelar',
          style: 'cancel'
        },
        {
          text: 'Eliminar',
          style: 'destructive',
          onPress: () => eliminarProducto(id)
        }
      ]
    );
  };

  return (
    <View style={styles.contenedor}>
      <Text style={styles.titulo}>Eliminar producto</Text>

      {/* Mostramos todos los productos disponibles. */}
      {productos.map((item) => (
        <View key={item.id} style={styles.tarjeta}>
          <Text style={styles.nombre}>
            {item.producto}
          </Text>

          <Text>{item.descripcion}</Text>

          <Text>
            ${Number(item.precio).toFixed(2)}
          </Text>

          {/* Botón para eliminar este producto. */}
          <TouchableOpacity
            style={styles.boton}
            onPress={() => confirmarEliminacion(item.id)}
          >
            <Text style={styles.textoBoton}>
              Eliminar
            </Text>
          </TouchableOpacity>
        </View>
      ))}

      {/* Mensaje cuando no existen registros. */}
      {productos.length === 0 && (
        <Text>No hay productos para eliminar</Text>
      )}
    </View>
  );
}

// Estilos de nuestro componente.
const styles = StyleSheet.create({
  contenedor: {
    padding: 15
  },
  titulo: {
    fontSize: 22,
    fontWeight: 'bold',
    marginBottom: 15
  },
  tarjeta: {
    padding: 15,
    backgroundColor: '#FFFFFF',
    borderRadius: 8,
    marginBottom: 12
  },
  nombre: {
    fontSize: 18,
    fontWeight: 'bold',
    marginBottom: 5
  },
  boton: {
    backgroundColor: '#D32F2F',
    padding: 12,
    borderRadius: 8,
    marginTop: 12
  },
  textoBoton: {
    color: '#FFFFFF',
    textAlign: 'center',
    fontWeight: 'bold'
  }
});
