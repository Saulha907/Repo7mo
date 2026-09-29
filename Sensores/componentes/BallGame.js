import { useEffect, useState } from "react";
import { View, Text, StyleSheet, Dimensions } from 'react-native';
import { Accelerometer } from "expo-sensors";

const { width, height } = Dimensions.get('window');
const BALL_SIZE = 40; 

export default function BallGame() {
    const [position, setPosition] = useState({
        x: width / 2 - BALL_SIZE / 2,
        y: height / 2 - BALL_SIZE / 2
    });

    useEffect(() => {
        Accelerometer.setUpdateInterval(16); 

        const subscribir = Accelerometer.addListener(data => {
            setPosition(prev => {
                let nextX = prev.x - data.x * 7;
                let nextY = prev.y + data.y * 7;

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
            <Text style={styles.instruction}>¡Inclina tu teléfono para mover la pelota!</Text>
            
            <View style={[
                styles.ball, 
                { left: position.x, top: position.x, transform: [{ translateX: position.x }, { translateY: position.y }] }
            ]} />
        </View>
    );
}

const styles = StyleSheet.create({
    container: {
        flex: 1,
        backgroundColor: '#1a1a2e',
    },
    instruction: {
        color: '#fff',
        position: 'absolute',
        top: 60,
        width: '100%',
        textAlign: 'center',
        fontSize: 16,
        opacity: 0.7
    },
    ball: {
        position: 'absolute',
        width: BALL_SIZE,
        height: BALL_SIZE,
        borderRadius: BALL_SIZE / 2,
        backgroundColor: '#00f2fe', 
        shadowColor: "#00f2fe",
        shadowOffset: { width: 0, height: 0 },
        shadowOpacity: 0.8,
        shadowRadius: 10,
        elevation: 5,
    },
});
