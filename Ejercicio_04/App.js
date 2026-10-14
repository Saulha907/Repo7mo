import React, { useState } from "react";
import { StyleSheet, Button, View, SafeAreaView, TextInput, } from "react-native";

import CustomModal from "./Componentes/CustomModal";

export default function App() {
  const [modalVisible, setModalVisible] = useState(false);
  const [texto, setTexto] = useState("");

  const objetoContenido = {
    valor: texto,
  };

  return (
    <SafeAreaView style={styles.container}>
      <View style={styles.contenido}>

        <TextInput
          style={styles.input}
          placeholder="Escribe algo..."
          value={texto}
          onChangeText={(textoIngresado) => setTexto(textoIngresado)}
        />

        <CustomModal
          visible={modalVisible}
          onClose={() => setModalVisible(false)}
          contenido={objetoContenido}
        />

        <Button
          title="Abrir modal"
          onPress={() => setModalVisible(true)}
        />

      </View>
    </SafeAreaView>
  );
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
    backgroundColor: "#fff",
    alignItems: "center",
    justifyContent: "center",
  },

  contenido: {
    width: "80%",
  },

  input: {
    borderWidth: 1,
    borderColor: "#999",
    borderRadius: 5,
    padding: 10,
    marginBottom: 15,
  },
});
//Agregar un input Text, cuando preciones el button el contenido de input text, lo pongas dentro de modal