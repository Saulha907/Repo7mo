
import React, { useEffect, useState } from 'react';

// Elementos visuales de React Native.
import {
  View,
  Text,
  TextInput,
  TouchableOpacity,
  Alert,
  ScrollView,
  StyleSheet
} from 'react-native';

// Conexión con Supabase.
import { supabase } from '../lib/supabase';

export default function ActualizarProducto({ alActualizar }) {

  // Lista de productos disponibles.
  const [productos, setProductos] = useState([]);

  // ID del producto seleccionado.
  const [idSeleccionado, setIdSeleccionado] = useState(null);

  // Datos que vamos a modificar.
  const [producto, setProducto] = useState('');
  const [descripcion, setDescripcion] = useState('');
  const [precio, setPrecio] = useState('');

  // Consultamos los productos existentes.
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

  // Se ejecuta al abrir el componente.
  useEffect(() => {
    cargarProductos();
  }, []);

  // Al seleccionar un producto, cargamos sus datos.
  const seleccionarProducto = (item) => {
    setIdSeleccionado(item.id);
    setProducto(item.producto);
    setDescripcion(item.descripcion || '');
    setPrecio(String(item.precio));
  };

  // Esta función guarda los cambios.
  const actualizarProducto = async () => {

    // Verificamos que exista un producto seleccionado.
    if (idSeleccionado === null) {
      Alert.alert('Aviso', 'Selecciona un producto');
      return;
    }

    // Validamos los campos del formulario.
    if (!producto.trim() || !descripcion.trim() || !precio.trim()) {
      Alert.alert('Aviso', 'Completa todos los campos');
      return;
    }

    // Convertimos el precio a número.
    const precioNumero = Number(precio.replace(',', '.'));

    if (!Number.isFinite(precioNumero) || precioNumero < 0) {
      Alert.alert('Error', 'Ingresa un precio válido');
      return;
    }

    // UPDATE modifica el registro cuyo ID coincide.
    const { data, error } = await supabase
      .from('productos')
      .update({
        producto: producto.trim(),
        descripcion: descripcion.trim(),
        precio: precioNumero
      })
      .eq('id', idSeleccionado)
      .select('id');

    if (error) {
      Alert.alert('Error', error.message);
      return;
    }

    // Comprobamos que realmente se actualizó un registro.
    if (!data || data.length === 0) {
      Alert.alert('Aviso', 'No se encontró el producto');
      return;
    }

    Alert.alert('Éxito', 'Producto actualizado correctamente');

    // Limpiamos el formulario.
    setIdSeleccionado(null);
    setProducto('');
    setDescripcion('');
    setPrecio('');

    // Actualizamos nuestra lista.
    await cargarProductos();

    // Avisamos a la pantalla principal.
    if (alActualizar) alActualizar();
  };

  return (
    <ScrollView style={styles.contenedor}>
      <Text style={styles.titulo}>Actualizar producto</Text>

      <Text style={styles.subtitulo}>
        Selecciona el producto que deseas modificar:
      </Text>

      {/* Mostramos los productos para seleccionarlos. */}
      {productos.map((item) => (
        <TouchableOpacity
          key={item.id}
          style={styles.item}
          onPress={() => seleccionarProducto(item)}
        >
          <Text>
            {item.id} - {item.producto}
          </Text>
        </TouchableOpacity>
      ))}

      {/* Formulario que aparece al seleccionar un producto. */}
      {idSeleccionado !== null && (
        <View>
          <Text style={styles.subtitulo}>
            Editando producto #{idSeleccionado}
          </Text>

          <TextInput
            style={styles.input}
            value={producto}
            onChangeText={setProducto}
            placeholder="Producto"
          />

          <TextInput
            style={styles.input}
            value={descripcion}
            onChangeText={setDescripcion}
            placeholder="Descripción"
          />

          <TextInput
            style={styles.input}
            value={precio}
            onChangeText={setPrecio}
            keyboardType="decimal-pad"
            placeholder="Precio"
          />

          {/* Botón para guardar los cambios. */}
          <TouchableOpacity
            style={styles.boton}
            onPress={actualizarProducto}
          >
            <Text style={styles.textoBoton}>
              Guardar cambios
            </Text>
          </TouchableOpacity>
        </View>
      )}
    </ScrollView>
  );
}

// Estilos del componente.
const styles = StyleSheet.create({
  contenedor: {
    padding: 15
  },
  titulo: {
    fontSize: 22,
    fontWeight: 'bold',
    marginBottom: 15
  },
  subtitulo: {
    fontSize: 16,
    marginBottom: 12
  },
  item: {
    padding: 15,
    backgroundColor: '#EEEEEE',
    borderRadius: 8,
    marginBottom: 10
  },
  input: {
    borderWidth: 1,
    borderColor: '#CCCCCC',
    borderRadius: 8,
    padding: 12,
    marginBottom: 10,
    backgroundColor: '#FFFFFF'
  },
  boton: {
    padding: 15,
    backgroundColor: '#16803C',
    borderRadius: 8,
    marginBottom: 20
  },
  textoBoton: {
    color: '#FFFFFF',
    textAlign: 'center',
    fontWeight: 'bold'
  }
});
