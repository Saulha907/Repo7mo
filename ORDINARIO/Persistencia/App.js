
import React, { useState } from 'react';

// Importamos los componentes de React Native.
import {
  View,
  Text,
  TouchableOpacity,
  ScrollView,
  StyleSheet,
  SafeAreaView
} from 'react-native';

// Importamos los cuatro componentes del CRUD.
// Utilizamos "componentes" porque así se llama nuestra carpeta.
import AgregarProducto from './componentes/AgregarProducto';
import ConsultarProductos from './componentes/ConsultarProductos';
import ActualizarProducto from './componentes/ActualizarProducto';
import EliminarProducto from './componentes/EliminarProducto';

// Componente principal de la aplicación.
export default function App() {

  // Controla qué pantalla aparece en la aplicación.
  // Al iniciar, mostramos los productos registrados.
  const [pantalla, setPantalla] = useState('consultar');

  // Esta variable permite actualizar la lista de productos
  // cuando agregamos, modificamos o eliminamos un registro.
  const [version, setVersion] = useState(0);

  // Función que actualiza la lista después de una operación.
  const refrescarProductos = () => {

    // Aumentamos la versión para ejecutar nuevamente la consulta.
    setVersion((anterior) => anterior + 1);

    // Regresamos automáticamente a la pantalla de consulta.
    setPantalla('consultar');
  };

  // Función para seleccionar qué componente mostrar.
  const mostrarPantalla = () => {

    switch (pantalla) {

      // Pantalla para agregar nuevos productos.
      case 'agregar':
        return (
          <AgregarProducto
            alGuardar={refrescarProductos}
          />
        );

      // Pantalla para consultar todos los productos.
      case 'consultar':
        return (
          <ConsultarProductos
            version={version}
          />
        );

      // Pantalla para modificar productos existentes.
      case 'actualizar':
        return (
          <ActualizarProducto
            alActualizar={refrescarProductos}
          />
        );

      // Pantalla para eliminar productos.
      case 'eliminar':
        return (
          <EliminarProducto
            alEliminar={refrescarProductos}
          />
        );

      // Si no existe la pantalla, no mostramos nada.
      default:
        return null;
    }
  };

  // Interfaz principal de nuestra aplicación.
  return (
    <SafeAreaView style={styles.contenedor}>

      <ScrollView
        contentContainerStyle={styles.contenido}
        keyboardShouldPersistTaps="handled"
      >

        {/* Título principal de la aplicación. */}
        <Text style={styles.titulo}>
          Persistencia
        </Text>

        {/* Subtítulo que describe el proyecto. */}
        <Text style={styles.subtitulo}>
          CRUD con React Native y Supabase
        </Text>

        {/* Menú con las cuatro operaciones CRUD. */}
        <View style={styles.menu}>

          {/* Botón para abrir el componente AgregarProducto. */}
          <TouchableOpacity
            style={styles.boton}
            onPress={() => setPantalla('agregar')}
          >
            <Text style={styles.textoBoton}>
              Agregar
            </Text>
          </TouchableOpacity>

          {/* Botón para consultar los registros guardados. */}
          <TouchableOpacity
            style={styles.boton}
            onPress={() => setPantalla('consultar')}
          >
            <Text style={styles.textoBoton}>
              Consultar
            </Text>
          </TouchableOpacity>

          {/* Botón para actualizar un producto existente. */}
          <TouchableOpacity
            style={styles.boton}
            onPress={() => setPantalla('actualizar')}
          >
            <Text style={styles.textoBoton}>
              Actualizar
            </Text>
          </TouchableOpacity>

          {/* Botón para eliminar un producto. */}
          <TouchableOpacity
            style={styles.botonEliminar}
            onPress={() => setPantalla('eliminar')}
          >
            <Text style={styles.textoBoton}>
              Eliminar
            </Text>
          </TouchableOpacity>

        </View>

        {/* Área donde aparece el componente seleccionado. */}
        <View style={styles.area}>
          {mostrarPantalla()}
        </View>

      </ScrollView>

    </SafeAreaView>
  );
}

// Estilos de la aplicación.
const styles = StyleSheet.create({

  // Contenedor principal.
  contenedor: {
    flex: 1,
    backgroundColor: '#F5F5F5'
  },

  // Espaciado general de la pantalla.
  contenido: {
    paddingTop: 55,
    paddingBottom: 40,
    paddingHorizontal: 15
  },

  // Diseño del título.
  titulo: {
    fontSize: 30,
    fontWeight: 'bold',
    textAlign: 'center',
    color: '#111111'
  },

  // Diseño del subtítulo.
  subtitulo: {
    fontSize: 15,
    textAlign: 'center',
    marginTop: 8,
    marginBottom: 25,
    color: '#555555'
  },

  // Distribución de los botones del menú.
  menu: {
    flexDirection: 'row',
    flexWrap: 'wrap',
    justifyContent: 'center',
    marginBottom: 20
  },

  // Diseño de los botones verdes.
  boton: {
    backgroundColor: '#16803C',
    padding: 12,
    borderRadius: 8,
    margin: 4,
    minWidth: 105,
    alignItems: 'center'
  },

  // Diseño del botón rojo para eliminar.
  botonEliminar: {
    backgroundColor: '#D32F2F',
    padding: 12,
    borderRadius: 8,
    margin: 4,
    minWidth: 105,
    alignItems: 'center'
  },

  // Texto de los botones.
  textoBoton: {
    color: '#FFFFFF',
    fontWeight: 'bold',
    textAlign: 'center'
  },

  // Área donde se muestran los componentes CRUD.
  area: {
    backgroundColor: '#FFFFFF',
    borderRadius: 12,
    padding: 5
  }

});
