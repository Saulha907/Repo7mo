import React, { useState, useEffect } from 'react';
import {
  View,
  Text,
  TouchableOpacity,
  StyleSheet
} from 'react-native';

const EMOJIS = ['🐶', '🐱', '🦊', '🐻', '🐼', '🐨', '🐯', '🦁'];

export default function MemoramaScreen() {
  const [cards, setCards] = useState([]);
  const [flipped, setFlipped] = useState([]);
  const [matched, setMatched] = useState([]);

  useEffect(() => {
    reiniciarJuego();
  }, []);

  const reiniciarJuego = () => {
    const duplicated = [...EMOJIS, ...EMOJIS];

    const shuffled = duplicated.sort(() => Math.random() - 0.5);

    setCards(shuffled);
    setFlipped([]);
    setMatched([]);
  };

  const handleCardPress = (index) => {
    if (
      flipped.length === 2 ||
      flipped.includes(index) ||
      matched.includes(index)
    ) {
      return;
    }

    const newFlipped = [...flipped, index];

    setFlipped(newFlipped);

    if (newFlipped.length === 2) {
      const [first, second] = newFlipped;

      if (cards[first] === cards[second]) {
        setMatched((prev) => [
          ...prev,
          first,
          second
        ]);

        setFlipped([]);
      } else {
        setTimeout(() => {
          setFlipped([]);
        }, 1000);
      }
    }
  };

  const paresEncontrados = matched.length / 2;

  return (
    <View style={styles.container}>

      <Text style={styles.title}>
        Juego de Memoria
      </Text>

      <Text style={styles.subtitle}>
        Encuentra todos los pares
      </Text>

      <View style={styles.scoreContainer}>
        <Text style={styles.scoreText}>
          Pares encontrados: {paresEncontrados} / {EMOJIS.length}
        </Text>
      </View>

      <View style={styles.grid}>
        {cards.map((emoji, index) => {
          const isFlipped =
            flipped.includes(index) ||
            matched.includes(index);

          return (
            <TouchableOpacity
              key={index}
              style={[
                styles.card,
                isFlipped && styles.cardFlipped
              ]}
              onPress={() => handleCardPress(index)}
            >
              <Text style={styles.cardText}>
                {isFlipped ? emoji : '★'}
              </Text>
            </TouchableOpacity>
          );
        })}
      </View>

      {matched.length === cards.length && cards.length > 0 && (
        <Text style={styles.winText}>
          ¡Felicidades! 🎉
        </Text>
      )}

      <TouchableOpacity
        style={styles.resetButton}
        onPress={reiniciarJuego}
      >
        <Text style={styles.resetText}>
          Jugar de nuevo
        </Text>
      </TouchableOpacity>

    </View>
  );
}

const styles = StyleSheet.create({

  container: {
    flex: 1,
    padding: 20,
    alignItems: 'center',
    justifyContent: 'center',
    backgroundColor: '#EEF2F7',
  },

  title: {
    fontSize: 30,
    fontWeight: 'bold',
    color: '#1E293B',
    marginBottom: 5,
  },

  subtitle: {
    fontSize: 16,
    color: '#64748B',
    marginBottom: 15,
  },

  scoreContainer: {
    backgroundColor: '#FFFFFF',
    paddingVertical: 10,
    paddingHorizontal: 20,
    borderRadius: 20,
    marginBottom: 20,
    elevation: 2,
  },

  scoreText: {
    fontSize: 16,
    fontWeight: '600',
    color: '#334155',
  },

  grid: {
    flexDirection: 'row',
    flexWrap: 'wrap',
    justifyContent: 'center',
    width: 330,
  },

  card: {
    width: 72,
    height: 72,
    backgroundColor: '#334155',
    justifyContent: 'center',
    alignItems: 'center',
    margin: 5,
    borderRadius: 15,
    elevation: 4,
  },

  cardFlipped: {
    backgroundColor: '#FFFFFF',
    borderWidth: 2,
    borderColor: '#38BDF8',
  },

  cardText: {
    fontSize: 32,
  },

  winText: {
    fontSize: 20,
    fontWeight: 'bold',
    color: '#16A34A',
    marginTop: 20,
  },

  resetButton: {
    marginTop: 25,
    backgroundColor: '#2563EB',
    paddingVertical: 14,
    paddingHorizontal: 35,
    borderRadius: 25,
    elevation: 3,
  },

  resetText: {
    color: '#FFFFFF',
    fontWeight: 'bold',
    fontSize: 16,
  },

});