import React, { useEffect, useRef } from 'react';
import {
  View,
  Text,
  StyleSheet,
  Animated
} from 'react-native';

import { Ionicons } from '@expo/vector-icons';

export default function AnimatedSplash({ onFinish }) {

  const fadeAnim = useRef(new Animated.Value(0)).current;
  const scaleAnim = useRef(new Animated.Value(0.6)).current;
  const moveAnim = useRef(new Animated.Value(25)).current;

  useEffect(() => {

    Animated.parallel([

      Animated.timing(fadeAnim, {
        toValue: 1,
        duration: 1000,
        useNativeDriver: true,
      }),

      Animated.spring(scaleAnim, {
        toValue: 1,
        friction: 5,
        tension: 60,
        useNativeDriver: true,
      }),

      Animated.timing(moveAnim, {
        toValue: 0,
        duration: 900,
        useNativeDriver: true,
      }),

    ]).start(() => {

      setTimeout(() => {
        onFinish();
      }, 1200);

    });

  }, []);

  return (
    <View style={styles.container}>

      <Animated.View
        style={[
          styles.content,
          {
            opacity: fadeAnim,
            transform: [
              { scale: scaleAnim },
              { translateY: moveAnim }
            ]
          }
        ]}
      >

        <View style={styles.iconContainer}>

          <Ionicons
            name="apps-outline"
            size={75}
            color="#FFFFFF"
          />

        </View>

        <Text style={styles.title}>
          VidaEmma
        </Text>

        <Text style={styles.subtitle}>
          Todo lo que necesitas en un solo lugar
        </Text>

        <View style={styles.line} />

        <Text style={styles.loadingText}>
          Iniciando aplicación...
        </Text>

      </Animated.View>

    </View>
  );
}

const styles = StyleSheet.create({

  container: {
    flex: 1,
    backgroundColor: '#0F172A',
    justifyContent: 'center',
    alignItems: 'center',
  },

  content: {
    alignItems: 'center',
    paddingHorizontal: 30,
  },

  iconContainer: {
    width: 120,
    height: 120,
    borderRadius: 35,
    backgroundColor: '#2563EB',
    justifyContent: 'center',
    alignItems: 'center',
    marginBottom: 25,

    shadowColor: '#2563EB',
    shadowOffset: {
      width: 0,
      height: 5,
    },
    shadowOpacity: 0.4,
    shadowRadius: 10,
    elevation: 8,
  },

  title: {
    fontSize: 40,
    fontWeight: 'bold',
    color: '#FFFFFF',
    letterSpacing: 1,
  },

  subtitle: {
    fontSize: 16,
    color: '#94A3B8',
    marginTop: 8,
    textAlign: 'center',
  },

  line: {
    width: 80,
    height: 3,
    backgroundColor: '#2563EB',
    borderRadius: 3,
    marginTop: 25,
  },

  loadingText: {
    fontSize: 14,
    color: '#64748B',
    marginTop: 15,
  },

});