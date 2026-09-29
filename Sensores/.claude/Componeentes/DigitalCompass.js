import { useEffect, useState } from "react";
import {
  View,
  Text,
  StyleSheet,
  Dimensions,
} from "react-native";

import { Magnetometer } from "expo-sensors";

const { width } = Dimensions.get("window");

const COMPASS_SIZE = width * 0.75;

export default function DigitalCompass() {
  const [grados, setGrados] = useState(0);

  useEffect(() => {
    Magnetometer.setUpdateInterval(30);

    const subscribir = Magnetometer.addListener((data) => {
      let anguloRad = Math.atan2(data.y, data.x);

      let anguloDeg = anguloRad * (180 / Math.PI);

      if (anguloDeg < 0) {
        anguloDeg += 360;
      }

      setGrados(Math.round(anguloDeg));
    });

    return () => {
      subscribir.remove();
    };
  }, []);

  const obtenerDireccion = (deg) => {
    if (deg >= 338 || deg < 23) {
      return "N";
    }

    if (deg >= 23 && deg < 68) {
      return "NE";
    }

    if (deg >= 68 && deg < 113) {
      return "E";
    }

    if (deg >= 113 && deg < 158) {
      return "SE";
    }

    if (deg >= 158 && deg < 203) {
      return "S";
    }

    if (deg >= 203 && deg < 248) {
      return "SO";
    }

    if (deg >= 248 && deg < 293) {
      return "O";
    }

    return "NO";
  };

  return (
    <View style={styles.container}>

      <Text style={styles.title}>
        Brújula Digital
      </Text>

      <Text style={styles.cardinal}>
        {obtenerDireccion(grados)}
      </Text>

      <Text style={styles.degrees}>
        {grados}°
      </Text>

      <View style={styles.compassContainer}>

        <View
          style={[
            styles.compassDisk,
            {
              transform: [
                {
                  rotate: `${-grados}deg`,
                },
              ],
            },
          ]}
        >

          <Text
            style={[
              styles.pointerText,
              styles.northText,
            ]}
          >
            N
          </Text>

          <Text
            style={[
              styles.pointerText,
              styles.eastText,
            ]}
          >
            E
          </Text>

          <Text
            style={[
              styles.pointerText,
              styles.southText,
            ]}
          >
            S
          </Text>

          <Text
            style={[
              styles.pointerText,
              styles.westText,
            ]}
          >
            O
          </Text>

          <View style={styles.centerDot} />

        </View>

        <View style={styles.northNeedle} />

      </View>

      <Text style={styles.instruction}>
        Gira tu teléfono para encontrar el norte
      </Text>

    </View>
  );
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
    backgroundColor: "#0a0f1d",
    alignItems: "center",
    justifyContent: "center",
  },

  title: {
    color: "#a0aec0",
    fontSize: 18,
    letterSpacing: 2,
    textTransform: "uppercase",
    marginBottom: 10,
  },

  cardinal: {
    color: "#fff",
    fontSize: 48,
    fontWeight: "bold",
  },

  degrees: {
    color: "#4fd1c5",
    fontSize: 24,
    marginBottom: 40,
  },

  compassContainer: {
    width: COMPASS_SIZE,
    height: COMPASS_SIZE,
    justifyContent: "center",
    alignItems: "center",
    position: "relative",
  },

  compassDisk: {
    width: COMPASS_SIZE,
    height: COMPASS_SIZE,
    borderRadius: COMPASS_SIZE / 2,
    borderWidth: 4,
    borderColor: "#4fd1c5",
    backgroundColor: "#171e30",
    position: "relative",

    shadowColor: "#4fd1c5",
    shadowOffset: {
      width: 0,
      height: 0,
    },
    shadowOpacity: 0.3,
    shadowRadius: 15,

    elevation: 6,
  },

  pointerText: {
    position: "absolute",
    color: "#fff",
    fontSize: 22,
    fontWeight: "bold",
  },

  northText: {
    top: 10,
    alignSelf: "center",
    color: "#ff4d4d",
  },

  eastText: {
    right: 15,
    top: COMPASS_SIZE / 2 - 13,
  },

  southText: {
    bottom: 10,
    alignSelf: "center",
  },

  westText: {
    left: 15,
    top: COMPASS_SIZE / 2 - 13,
  },

  centerDot: {
    width: 18,
    height: 18,
    borderRadius: 9,
    backgroundColor: "#4fd1c5",

    position: "absolute",

    left: COMPASS_SIZE / 2 - 9,
    top: COMPASS_SIZE / 2 - 9,
  },

  northNeedle: {
    position: "absolute",
    top: -12,

    width: 0,
    height: 0,

    borderLeftWidth: 12,
    borderRightWidth: 12,
    borderBottomWidth: 28,

    borderLeftColor: "transparent",
    borderRightColor: "transparent",
    borderBottomColor: "#ff4d4d",
  },

  instruction: {
    color: "#a0aec0",
    fontSize: 15,
    marginTop: 40,
    textAlign: "center",
  },
});