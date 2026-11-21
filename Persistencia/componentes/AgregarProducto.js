
import React, { useState } from 'react';

// Importamos los elementos visuales de React Native.
import {
  View,
  Text,
  TextInput,
  TouchableOpacity,
  Alert,
  StyleSheet
} from 'react-native';

// Importamos nuestra conexión con Supabase.
import { supabase } from '../lib/supabase';

// Creamos el componente que permite registrar productos.
export default function AgregarProducto({ alGuardar }) {

  // Guardamos temporalmente lo que escribe el usuario.
  const [producto, setProducto] = useState('');
  const [descripcion, setDescripcion] = useState('');
  const [precio, setPrecio] = useState('');

  // Esta función registra el producto en Supabase.
  const guardarProducto = async () => {

    // Comprobamos que los campos no estén vacíos.
    if (!producto.trim() || !descripcion.trim() || !precio.trim()) {
      Alert.alert('Aviso', 'Completa todos los campos');
      return;
    }

    // Convertimos el precio de texto a número.
    const precioNumero = Number(precio.replace(',', '.'));

    // Validamos que el precio sea correcto.
    if (!Number.isFinite(precioNumero) || precioNumero < 0) {
      Alert.alert('Error', 'Ingresa un precio válido');
      return;
    }

    // INSERT agrega un nuevo registro en la tabla productos.
    const { error } = await supabase
      .from('productos')
      .insert([
        {
          producto: producto.trim(),
          descripcion: descripcion.trim(),
          precio: precioNumero
        }
      ]);

    // Mostramos un mensaje si ocurre un error.
    if (error) {
      Alert.alert('Error', error.message);
      return;
    }

    // Confirmamos que el producto se guardó correctamente.
    Alert.alert('Éxito', 'Producto agregado correctamente');

    // Limpiamos los campos del formulario.
    setProducto('');
    setDescripcion('');
    setPrecio('');

    // Avisamos a la pantalla principal que debe actualizarse.
    if (alGuardar) alGuardar();
  };

  return (
    <View style={styles.contenedor}>
      <Text style={styles.titulo}>Agregar producto</Text>

      {/* Campo para escribir el nombre del producto. */}
      <TextInput
        style={styles.input}
        placeholder="Nombre del producto"
        value={producto}
        onChangeText={setProducto}
      />

      {/* Campo para escribir su descripción. */}
      <TextInput
        style={styles.input}
        placeholder="Descripción"
        value={descripcion}
        onChangeText={setDescripcion}
      />

      {/* Campo para escribir el precio. */}
      <TextInput
        style={styles.input}
        placeholder="Precio"
        value={precio}
        onChangeText={setPrecio}
        keyboardType="decimal-pad"
      />

      {/* Botón que ejecuta la función guardarProducto. */}
      <TouchableOpacity
        style={styles.boton}
        onPress={guardarProducto}
      >
        <Text style={styles.textoBoton}>
          Guardar producto
        </Text>
      </TouchableOpacity>
    </View>
  );
}

// Estilos visuales del componente.
const styles = StyleSheet.create({
  contenedor: {
    padding: 15
  },
  titulo: {
    fontSize: 22,
    fontWeight: 'bold',
    marginBottom: 15
  },
  input: {
    borderWidth: 1,
    borderColor: '#CCCCCC',
    borderRadius: 8,
    padding: 12,
    marginBottom: 12,
    backgroundColor: '#FFFFFF'
  },
  boton: {
    backgroundColor: '#16803C',
    padding: 15,
    borderRadius: 8
  },
  textoBoton: {
    color: '#FFFFFF',
    textAlign: 'center',
    fontWeight: 'bold'
  }
});
