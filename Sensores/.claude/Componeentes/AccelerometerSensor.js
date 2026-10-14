import { useEffect, useState } from "react";
import { View, Text, StyleSheet } from "react-native";

import { Accelerometer } from "expo-sensors";

export default function AccelerometerSensor() {

    const [datos, setDatos] = useState({
        x: 0,
        y: 0,
        z: 0,
    });

    useEffect(() => {

        const subscribir = Accelerometer.addListener(
            measurements => {
                setDatos(measurements);
            }
        );

        Accelerometer.setUpdateInterval(100);

        return () => {
            subscribir.remove();
        };

    }, []);

    const detectarInclinacion = () => {

        if (datos.x > 0.5) {
            return "Inclinado hacia la izquierda";
        }

        if (datos.x < -0.5) {
            return "Inclinado hacia la derecha";
        }

        if (datos.y > 0.5) {
            return "Inclinado hacia abajo";
        }

        if (datos.y < -0.5) {
            return "Inclinado hacia arriba";
        }

        return "Teléfono estable";
    };

    return (
        <View style={styles.container}>

            <Text style={styles.title}>
                Acelerómetro
            </Text>

            <Text style={styles.subtitulo}>
                Movimiento del dispositivo
            </Text>

            <View style={styles.card}>
                <Text style={styles.axis}>X</Text>

                <Text style={styles.value}>
                    {datos.x.toFixed(2)}
                </Text>
            </View>

            <View style={styles.card}>
                <Text style={styles.axis}>Y</Text>

                <Text style={styles.value}>
                    {datos.y.toFixed(2)}
                </Text>
            </View>

            <View style={styles.card}>
                <Text style={styles.axis}>Z</Text>

                <Text style={styles.value}>
                    {datos.z.toFixed(2)}
                </Text>
            </View>

            <View style={styles.estado}>

                <Text style={styles.estadoTitulo}>
                    Estado del teléfono
                </Text>

                <Text style={styles.estadoTexto}>
                    {detectarInclinacion()}
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

    subtitulo: {
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