import { useEffect, useState } from "react";
import { View, Text, StyleSheet, Dimensions } from 'react-native';
// 1. Importamos Gyroscope en lugar de Accelerometer
import { Gyroscope } from "expo-sensors";

const { width, height } = Dimensions.get('window');
const BALL_SIZE = 40;

export default function GyroBallGame() {
    // Posición de la pelota
    const [position, setPosition] = useState({
        x: width / 2 - BALL_SIZE / 2,
        y: height / 2 - BALL_SIZE / 2
    });

    useEffect(() => {
        // Intervalo rápido (16ms = ~60 FPS) para que responda al instante
        Gyroscope.setUpdateInterval(16); 

        const subscribir = Gyroscope.addListener(data => {
            setPosition(prev => {
                // El giroscopio mide radianes por segundo.
                // data.y controla el movimiento horizontal (eje X de la pantalla)
                // data.x controla el movimiento vertical (eje Y de la pantalla)
                // Multiplicamos por 5 para convertir esa velocidad de rotación en píxeles.
                let nextX = prev.x + data.y * 5;
                let nextY = prev.y + data.x * 5;

                // LÍMITES: Evitamos que la pelota se salga de la pantalla
                if (nextX < 0) nextX = 0;
                if (nextX > width - BALL_SIZE) nextX = width - BALL_SIZE;
                if (nextY < 0) nextY = 0;
                if (nextY > height - BALL_SIZE) nextY = height - BALL_SIZE;

                return { x: nextX, y: nextY };
            });
        });

        return () => subscribir.remove();
    }, []);

    return (
        <View style={styles.container}>
            <Text style={styles.instruction}>¡Gira o rota tu teléfono para mover la pelota!</Text>
            
            {/* Renderizamos la pelota usando transformaciones */}
            <View style={[
                styles.ball, 
                { transform: [{ translateX: position.x }, { translateY: position.y }] }
            ]} />
        </View>
    );
}

const styles = StyleSheet.create({
    container: {
        flex: 1,
        backgroundColor: '#1b1b2f', // Fondo morado oscuro
    },
    instruction: {
        color: '#fff',
        position: 'absolute',
        top: 60,
        width: '100%',
        textAlign: 'center',
        fontSize: 16,
        opacity: 0.7,
        paddingHorizontal: 20
    },
    ball: {
        position: 'absolute',
        width: BALL_SIZE,
        height: BALL_SIZE,
        borderRadius: BALL_SIZE / 2,
        backgroundColor: '#ff2a74', // Color rosa/fucsia neón para diferenciarlo del anterior
        shadowColor: "#ff2a74",
        shadowOffset: { width: 0, height: 0 },
        shadowOpacity: 0.8,
        shadowRadius: 10,
        elevation: 5,
    },
});
