import { useEffect, useState } from "react";
import {
  View,
  Text,
  StyleSheet,
} from "react-native";

import { Gyroscope } from "expo-sensors";

export default function GyroscopeSensor() {
  const [datos, setDatos] = useState({
    x: 0,
    y: 0,
    z: 0,
  });

  useEffect(() => {
    Gyroscope.setUpdateInterval(100);

    const subscribir = Gyroscope.addListener((measurements) => {
      setDatos(measurements);
    });

    return () => {
      subscribir.remove();
    };
  }, []);

  const obtenerMovimiento = () => {
    const x = Math.abs(datos.x);
    const y = Math.abs(datos.y);
    const z = Math.abs(datos.z);

    if (x < 0.1 && y < 0.1 && z < 0.1) {
      return "Teléfono estable";
    }

    if (x > y && x > z) {
      return "Mayor movimiento en eje X";
    }

    if (y > x && y > z) {
      return "Mayor movimiento en eje Y";
    }

    return "Mayor movimiento en eje Z";
  };

  return (
    <View style={styles.container}>

      <Text style={styles.title}>
        Giroscopio
      </Text>

      <Text style={styles.subtitle}>
        Movimiento del dispositivo
      </Text>

      <View style={styles.card}>
        <Text style={styles.axis}>
          X
        </Text>

        <Text style={styles.value}>
          {datos.x.toFixed(2)}
        </Text>
      </View>

      <View style={styles.card}>
        <Text style={styles.axis}>
          Y
        </Text>

        <Text style={styles.value}>
          {datos.y.toFixed(2)}
        </Text>
      </View>

      <View style={styles.card}>
        <Text style={styles.axis}>
          Z
        </Text>

        <Text style={styles.value}>
          {datos.z.toFixed(2)}
        </Text>
      </View>

      <View style={styles.estado}>
        <Text style={styles.estadoTitulo}>
          Estado
        </Text>

        <Text style={styles.estadoTexto}>
          {obtenerMovimiento()}
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