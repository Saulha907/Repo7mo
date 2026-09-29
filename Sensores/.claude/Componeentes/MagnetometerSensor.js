import { useEffect, useState } from "react";
import {
  View,
  Text,
  StyleSheet,
} from "react-native";

import { Magnetometer } from "expo-sensors";

export default function MagnetometerSensor() {
  const [datos, setDatos] = useState({
    x: 0,
    y: 0,
    z: 0,
  });

  useEffect(() => {
    Magnetometer.setUpdateInterval(500);

    const subscribir = Magnetometer.addListener((measurements) => {
      setDatos(measurements);
    });

    return () => {
      subscribir.remove();
    };
  }, []);

  const calcularIntensidad = () => {
    const intensidad = Math.sqrt(
      datos.x * datos.x +
      datos.y * datos.y +
      datos.z * datos.z
    );

    return intensidad.toFixed(2);
  };

  const obtenerEjeMayor = () => {
    const x = Math.abs(datos.x);
    const y = Math.abs(datos.y);
    const z = Math.abs(datos.z);

    if (x > y && x > z) {
      return "Mayor intensidad en eje X";
    }

    if (y > x && y > z) {
      return "Mayor intensidad en eje Y";
    }

    if (z > x && z > y) {
      return "Mayor intensidad en eje Z";
    }

    return "Intensidad similar";
  };

  return (
    <View style={styles.container}>

      <Text style={styles.title}>
        Magnetómetro
      </Text>

      <Text style={styles.subtitle}>
        Campo magnético del dispositivo
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
          Intensidad total
        </Text>

        <Text style={styles.intensidad}>
          {calcularIntensidad()}
        </Text>

        <Text style={styles.estadoTexto}>
          {obtenerEjeMayor()}
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

  intensidad: {
    color: "#fff",
    textAlign: "center",
    fontSize: 32,
    fontWeight: "bold",
    marginBottom: 10,
  },

  estadoTexto: {
    color: "#fff",
    textAlign: "center",
    fontSize: 20,
    fontWeight: "bold",
  },
});