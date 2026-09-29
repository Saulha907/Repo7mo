import React, { useEffect, useRef } from 'react';
import {
  View,
  Text,
  StyleSheet,
  TouchableOpacity,
  Animated,
  ScrollView
} from 'react-native';

import { Ionicons } from '@expo/vector-icons';

export default function HomeScreen({ navigation }) {

  const fadeAnim = useRef(new Animated.Value(0)).current;
  const translateYAnim = useRef(new Animated.Value(40)).current;

  useEffect(() => {

    Animated.parallel([
      Animated.timing(fadeAnim, {
        toValue: 1,
        duration: 700,
        useNativeDriver: true,
      }),

      Animated.timing(translateYAnim, {
        toValue: 0,
        duration: 700,
        useNativeDriver: true,
      }),

    ]).start();

  }, []);

  const cards = [
    {
      title: 'Calculadora de Propinas',
      description: 'Calcula propinas y divide la cuenta',
      icon: 'wallet-outline',
      screen: 'Propinas',
      color: '#16A34A'
    },

    {
      title: 'Calculadora IMC',
      description: 'Conoce tu índice de masa corporal',
      icon: 'body-outline',
      screen: 'IMC',
      color: '#2563EB'
    },

    {
      title: 'Juegos',
      description: 'Gato y juego de memoria',
      icon: 'game-controller-outline',
      screen: 'JuegosTab',
      color: '#7C3AED'
    },
  ];

  return (
    <ScrollView
      style={styles.container}
      contentContainerStyle={styles.content}
    >

      <Animated.View
        style={{
          opacity: fadeAnim,
          transform: [
            {
              translateY: translateYAnim
            }
          ]
        }}
      >

        <View style={styles.header}>

          <Text style={styles.welcomeText}>
            VidaEmma
          </Text>

          <Text style={styles.description}>
            ¿Qué deseas hacer hoy?
          </Text>

        </View>

        {cards.map((item, index) => (

          <TouchableOpacity
            key={index}
            style={styles.card}
            onPress={() => navigation.navigate(item.screen)}
            activeOpacity={0.8}
          >

            <View
              style={[
                styles.iconContainer,
                {
                  backgroundColor: item.color
                }
              ]}
            >

              <Ionicons
                name={item.icon}
                size={30}
                color="#FFFFFF"
              />

            </View>

            <View style={styles.cardContent}>

              <Text style={styles.cardTitle}>
                {item.title}
              </Text>

              <Text style={styles.cardDescription}>
                {item.description}
              </Text>

            </View>

            <Ionicons
              name="arrow-forward-circle-outline"
              size={28}
              color={item.color}
            />

          </TouchableOpacity>

        ))}

      </Animated.View>

    </ScrollView>
  );
}

const styles = StyleSheet.create({

  container: {
    flex: 1,
    backgroundColor: '#EEF2F7',
  },

  content: {
    padding: 22,
    paddingTop: 45,
  },

  header: {
    marginBottom: 30,
  },

  welcomeText: {
    fontSize: 32,
    fontWeight: 'bold',
    color: '#1E293B',
    marginBottom: 8,
  },

  description: {
    fontSize: 17,
    color: '#64748B',
  },

  card: {
    flexDirection: 'row',
    alignItems: 'center',
    backgroundColor: '#FFFFFF',
    padding: 18,
    borderRadius: 18,
    marginBottom: 18,

    elevation: 4,

    shadowColor: '#000',
    shadowOpacity: 0.08,
    shadowOffset: {
      width: 0,
      height: 3,
    },
    shadowRadius: 5,
  },

  iconContainer: {
    width: 58,
    height: 58,
    borderRadius: 18,
    justifyContent: 'center',
    alignItems: 'center',
  },

  cardContent: {
    flex: 1,
    marginLeft: 15,
  },

  cardTitle: {
    fontSize: 17,
    fontWeight: 'bold',
    color: '#1E293B',
    marginBottom: 4,
  },

  cardDescription: {
    fontSize: 14,
    color: '#64748B',
  },

});