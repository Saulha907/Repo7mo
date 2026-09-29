import { useEffect, useState } from "react";
import { View, Text, StyleSheet, Dimensions } from 'react-native';
// Importamos Magnetometer de expo-sensors
import { Magnetometer } from "expo-sensors";

const { width } = Dimensions.get('window');
const COMPASS_SIZE = width * 0.75; // Tamaño del círculo de la brújula

export default function DigitalCompass() {
    const [grados, setGrados] = useState(0);

    useEffect(() => {
        // Un intervalo de 30ms dará una rotación fluida y en tiempo real
        Magnetometer.setUpdateInterval(30);

        const subscribir = Magnetometer.addListener(data => {
            // Aplicamos la fórmula matemática para calcular el ángulo en radianes
            let anguloRad = Math.atan2(data.y, data.x);
            
            // Convertimos los radianes a grados numéricos (0° a 360°)
            let anguloDeg = anguloRad * (180 / Math.PI);
            
            // Normalizamos el resultado para que no dé valores negativos
            if (anguloDeg < 0) {
                anguloDeg += 360;
            }

            // Redondeamos el valor final para evitar decimales molestos en pantalla
            setGrados(Math.round(anguloDeg));
        });

        return () => subscribir.remove();
    }, []);

    // Función auxiliar para saber qué letra cardinal mostrar según los grados actuales
    const obtenerDireccion = (deg) => {
        if (deg >= 338 || deg < 23) return "N ⬆️";
        if (deg >= 23 && deg < 68) return "NE ↗️";
        if (deg >= 68 && deg < 113) return "E ➡️";
        if (deg >= 113 && deg < 158) return "SE ↘️";
        if (deg >= 158 && deg < 203) return "S ⬇️";
        if (deg >= 203 && deg < 248) return "SO ↙️";
        if (deg >= 248 && deg < 293) return "O ⬅️";
        return "NO ↖️"; // Entre 293 y 338
    };

    return (
        <View style={styles.container}>
            <Text style={styles.title}>Brújula Digital</Text>
            <Text style={styles.cardinal}>{obtenerDireccion(grados)}</Text>
            <Text style={styles.degrees}>{grados}°</Text>

            {/* Contenedor del disco de la brújula */}
            <View style={styles.compassContainer}>
                {/* La magia de rotación ocurre aquí: rotamos todo el disco en sentido contrario */}
                <View style={[styles.compassDisk, { transform: [{ rotate: `${-grados}deg` }] }]}>
                    <Text style={[styles.pointerText, { top: 10 }]}>N</Text>
                    <Text style={[styles.pointerText, { right: 10, top: COMPASS_SIZE/2 - 12 }]}>E</Text>
                    <Text style={[styles.pointerText, { bottom: 10 }]}>S</Text>
                    <Text style={[styles.pointerText, { left: 10, top: COMPASS_SIZE/2 - 12 }]}>O</Text>
                    
                    {/* Centro del círculo decorativo */}
                    <View style={styles.centerDot} />
                </View>
                
                {/* Aguja indicadora fija en la parte superior */}
                <View style={styles.northNeedle} />
            </View>
        </View>
    );
}

const styles = StyleSheet.create({
    container: {
        flex: 1,
        backgroundColor: '#0a0f1d', // Fondo oscuro espacial
        alignItems: 'center',
        justifyContent: 'center',
    },
    title: {
        color: '#a0aec0',
        fontSize: 18,
        letterSpacing: 2,
        textTransform: 'uppercase',
        marginBottom: 10,
    },
    cardinal: {
        color: '#fff',
        fontSize: 48,
        fontWeight: 'bold',
    },
    degrees: {
        color: '#4fd1c5', // Color verde agua neón
        fontSize: 24,
        marginBottom: 40,
    },
    compassContainer: {
        width: COMPASS_SIZE,
        height: COMPASS_SIZE,
        justifyContent: 'center',
        alignItems: 'center',
        position: 'relative',
    },
    compassDisk: {
        width: COMPASS_SIZE,
        height: COMPASS_SIZE,
        borderRadius: COMPASS_SIZE / 2,
        borderWidth: 4,
        borderColor: '#4fd1c5',
        backgroundColor: '#171e30',
        alignItems: 'center',
        position: 'relative',
        shadowColor: "#4fd1c5",
        shadowOffset: { width: 0, height: 0 },
        shadowOpacity: 0.3,
        shadowRadius: 15,
        elevation: 6,
    },
    pointerText: {
        position: 'absolute',
        color: '#fff',
        fontSize: 22,
        fontWeight: 'bold',
    },
    centerDot: {
        width: 15,
        height: 15,
        borderRadius: 7.5,
        backgroundColor: '#4fd1c5',
        position: 'absolute',
        top: COMPASS_SIZE / 2 - 7.5,
    },
    northNeedle: {
        position: 'absolute',
        top: -10,
        width: 0,
        height: 0,
        backgroundColor: 'transparent',
        borderStyle: 'solid',
        borderLeftWidth: 10,
        borderRightWidth: 10,
        borderBottomWidth: 25,
        borderLeftColor: 'transparent',
        borderRightColor: 'transparent',
        borderBottomColor: '#ff4d4d', // Flecha roja arriba apuntando fijo hacia el frente
    },
});
