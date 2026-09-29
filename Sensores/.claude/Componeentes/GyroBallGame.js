import { useEffect, useState } from "react";
import {
  View,
  Text,
  StyleSheet,
  Dimensions,
  Button,
} from "react-native";

import { Gyroscope } from "expo-sensors";

const { width, height } = Dimensions.get("window");

const BALL_SIZE = 40;

export default function GyroBallGame() {
  const posicionInicial = {
    x: width / 2 - BALL_SIZE / 2,
    y: height / 2 - BALL_SIZE / 2,
  };

  const [position, setPosition] = useState(posicionInicial);

  useEffect(() => {
    Gyroscope.setUpdateInterval(16);

    const subscribir = Gyroscope.addListener((data) => {
      setPosition((prev) => {
        let nextX = prev.x + data.y * 5;
        let nextY = prev.y + data.x * 5;

        if (nextX < 0) {
          nextX = 0;
        }

        if (nextX > width - BALL_SIZE) {
          nextX = width - BALL_SIZE;
        }

        if (nextY < 0) {
          nextY = 0;
        }

        if (nextY > height - BALL_SIZE) {
          nextY = height - BALL_SIZE;
        }

        return {
          x: nextX,
          y: nextY,
        };
      });
    });

    return () => {
      subscribir.remove();
    };
  }, []);

  const reiniciarPelota = () => {
    setPosition({
      x: width / 2 - BALL_SIZE / 2,
      y: height / 2 - BALL_SIZE / 2,
    });
  };

  return (
    <View style={styles.container}>
      <Text style={styles.title}>
        Juego con Giroscopio
      </Text>

      <Text style={styles.instruction}>
        ¡Gira o rota tu teléfono para mover la pelota!
      </Text>

      <View
        style={[
          styles.ball,
          {
            left: position.x,
            top: position.y,
          },
        ]}
      />

      <View style={styles.buttonContainer}>
        <Button
          title="Reiniciar"
          onPress={reiniciarPelota}
          color="#ff2a74"
        />
      </View>
    </View>
  );
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
    backgroundColor: "#1b1b2f",
  },

  title: {
    position: "absolute",
    top: 50,
    width: "100%",
    textAlign: "center",
    fontSize: 28,
    fontWeight: "bold",
    color: "#ffffff",
  },

  instruction: {
    color: "#ffffff",
    position: "absolute",
    top: 95,
    width: "100%",
    textAlign: "center",
    fontSize: 16,
    opacity: 0.7,
    paddingHorizontal: 20,
  },

  ball: {
    position: "absolute",
    width: BALL_SIZE,
    height: BALL_SIZE,
    borderRadius: BALL_SIZE / 2,
    backgroundColor: "#ff2a74",

    shadowColor: "#ff2a74",
    shadowOffset: {
      width: 0,
      height: 0,
    },
    shadowOpacity: 0.8,
    shadowRadius: 10,
    elevation: 5,
  },

  buttonContainer: {
    position: "absolute",
    bottom: 40,
    width: "100%",
    paddingHorizontal: 80,
  },
});