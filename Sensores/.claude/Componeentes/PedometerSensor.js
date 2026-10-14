import { useEffect, useState } from "react";
import {
  View,
  Text,
  StyleSheet,
} from "react-native";

import { Pedometer } from "expo-sensors";

export default function PedometerSensor() {
  const [pasos, setPasos] = useState(0);
  const [disponible, setDisponible] = useState(false);

  useEffect(() => {
    let subscribir;

    const iniciar = async () => {
      const disponibleSensor = await Pedometer.isAvailableAsync();

      setDisponible(disponibleSensor);

      if (disponibleSensor) {
        subscribir = Pedometer.watchStepCount((result) => {
          setPasos(result.steps);
        });
      }
    };

    iniciar();

    return () => {
      if (subscribir) {
        subscribir.remove();
      }
    };
  }, []);

  const obtenerMensaje = () => {
    if (!disponible) {
      return "Podómetro no disponible";
    }

    if (pasos === 0) {
      return "Empieza a caminar";
    }

    if (pasos < 10) {
      return "Buen comienzo";
    }

    if (pasos < 50) {
      return "Sigue caminando";
    }

    return "¡Excelente!";
  };

  return (
    <View style={styles.container}>
      <Text style={styles.title}>
        Podómetro
      </Text>

      <Text style={styles.subtitle}>
        Contador de pasos
      </Text>

      <View style={styles.card}>
        <Text style={styles.axis}>
          Disponible
        </Text>

        <Text style={styles.value}>
          {disponible ? "SÍ" : "NO"}
        </Text>
      </View>

      <View style={styles.card}>
        <Text style={styles.axis}>
          Pasos
        </Text>

        <Text style={styles.value}>
          {pasos}
        </Text>
      </View>

      <View style={styles.estado}>
        <Text style={styles.estadoTitulo}>
          Estado
        </Text>

        <Text style={styles.estadoTexto}>
          {obtenerMensaje()}
        </Text>
      </View>
    </View>
  );
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
    justifyContent: "center",
    padding: 25,
    backgroundColor: "#efefef",
  },

  title: {
    fontSize: 35,
    textAlign: "center",
    marginBottom: 5,
    color: "#3a4a5a",
    fontWeight: "bold",
  },

  subtitle: {
    fontSize: 18,
    textAlign: "center",
    marginBottom: 35,
    color: "#777",
  },

  card: {
    backgroundColor: "#fff",
    padding: 20,
    marginBottom: 20,
    flexDirection: "row",
    justifyContent: "space-between",
    borderRadius: 10,
    elevation: 3,
  },

  axis: {
    fontSize: 24,
    fontWeight: "bold",
    color: "#3a4a5a",
  },

  value: {
    fontSize: 24,
    fontWeight: "bold",
  },

  estado: {
    backgroundColor: "#3a4a5a",
    padding: 20,
    borderRadius: 10,
    marginTop: 10,
  },

  estadoTitulo: {
    color: "#fff",
    textAlign: "center",
    fontSize: 18,
    marginBottom: 10,
  },

  estadoTexto: {
    color: "#fff",
    textAlign: "center",
    fontSize: 22,
    fontWeight: "bold",
  },
});