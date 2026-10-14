import React, { useState } from "react";
import { StyleSheet, Button, View, SafeAreaView, TextInput, Text,} from "react-native";

import CustomModal from "./Componentes/CustomModal";

export default function App() {
  const [modalVisible, setModalVisible] = useState(false);
  const [peso, setPeso] = useState("");
  const [altura, setAltura] = useState("");
  const [imc, setImc] = useState("");
  const [clasificacion, setClasificacion] = useState("");

  const calcularIMC = () => {
    const pesoNumero = parseFloat(peso);
    const alturaNumero = parseFloat(altura);

    if (
      isNaN(pesoNumero) ||
      isNaN(alturaNumero) ||
      pesoNumero <= 0 ||
      alturaNumero <= 0
    ) {
      setImc("Datos inválidos");
      setClasificacion("Introduce peso y altura válidos");
      setModalVisible(true);
      return;
    }

    const resultado = pesoNumero / (alturaNumero * alturaNumero);

    let tipo = "";

    if (resultado < 18.5) {
      tipo = "Bajo peso";
    } else if (resultado < 25) {
      tipo = "Peso normal";
    } else if (resultado < 30) {
      tipo = "Sobrepeso";
    } else {
      tipo = "Obesidad";
    }

    setImc(resultado.toFixed(1));
    setClasificacion(tipo);
    setModalVisible(true);
  };

  const objetoContenido = {
    imc: imc,
    clasificacion: clasificacion,
  };

  return (
    <SafeAreaView style={styles.container}>
      <View style={styles.contenido}>

        <Text style={styles.titulo}>Calculadora IMC</Text>

        <Text style={styles.label}>Peso (kg)</Text>

        <TextInput
          style={styles.input}
          placeholder="Ejemplo: 70"
          value={peso}
          onChangeText={(textoIngresado) => setPeso(textoIngresado)}
          keyboardType="decimal-pad"
        />

        <Text style={styles.label}>Altura (m)</Text>

        <TextInput
          style={styles.input}
          placeholder="Ejemplo: 1.75"
          value={altura}
          onChangeText={(textoIngresado) => setAltura(textoIngresado)}
          keyboardType="decimal-pad"
        />

        <CustomModal
          visible={modalVisible}
          onClose={() => setModalVisible(false)}
          contenido={objetoContenido}
        />

        <Button
          title="Calcular IMC"
          onPress={calcularIMC}
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

  titulo: {
    fontSize: 26,
    fontWeight: "bold",
    textAlign: "center",
    marginBottom: 30,
  },

  label: {
    fontSize: 16,
    marginBottom: 5,
    fontWeight: "500",
  },

  input: {
    borderWidth: 1,
    borderColor: "#999",
    borderRadius: 5,
    padding: 10,
    marginBottom: 20,
  },
});