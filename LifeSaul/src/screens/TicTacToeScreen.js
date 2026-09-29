import React, { useState } from 'react';
import {
  View,
  Text,
  TouchableOpacity,
  StyleSheet
} from 'react-native';

export default function TicTacToeScreen() {
  const [board, setBoard] = useState(Array(9).fill(null));
  const [isXNext, setIsXNext] = useState(true);

  const checkWinner = (squares) => {
    const lines = [
      [0, 1, 2],
      [3, 4, 5],
      [6, 7, 8],
      [0, 3, 6],
      [1, 4, 7],
      [2, 5, 8],
      [0, 4, 8],
      [2, 4, 6],
    ];

    for (let [a, b, c] of lines) {
      if (
        squares[a] &&
        squares[a] === squares[b] &&
        squares[a] === squares[c]
      ) {
        return squares[a];
      }
    }

    return null;
  };

  const winner = checkWinner(board);

  const handleClick = (index) => {
    if (board[index] || winner) return;

    const newBoard = [...board];

    newBoard[index] = isXNext ? 'X' : 'O';

    setBoard(newBoard);
    setIsXNext(!isXNext);
  };

  const resetGame = () => {
    setBoard(Array(9).fill(null));
    setIsXNext(true);
  };

  return (
    <View style={styles.container}>

      <Text style={styles.title}>
        Juego del Gato
      </Text>

      <View style={styles.statusContainer}>
        <Text style={styles.status}>
          {winner
            ? `¡Ganó ${winner}! 🎉`
            : `Turno del jugador ${isXNext ? 'X' : 'O'}`
          }
        </Text>
      </View>

      <View style={styles.board}>
        {board.map((value, index) => (
          <TouchableOpacity
            key={index}
            style={styles.square}
            onPress={() => handleClick(index)}
          >

            <Text
              style={[
                styles.squareText,
                value === 'X'
                  ? styles.textX
                  : styles.textO
              ]}
            >
              {value}
            </Text>

          </TouchableOpacity>
        ))}
      </View>

      <TouchableOpacity
        style={styles.resetButton}
        onPress={resetGame}
      >
        <Text style={styles.resetText}>
          Reiniciar
        </Text>
      </TouchableOpacity>

    </View>
  );
}

const styles = StyleSheet.create({

  container: {
    flex: 1,
    justifyContent: 'center',
    alignItems: 'center',
    backgroundColor: '#EEF2F7',
    padding: 20,
  },

  title: {
    fontSize: 30,
    fontWeight: 'bold',
    color: '#2C3E50',
    marginBottom: 15,
  },

  statusContainer: {
    backgroundColor: '#FFFFFF',
    paddingVertical: 12,
    paddingHorizontal: 25,
    borderRadius: 20,
    marginBottom: 25,
    elevation: 3,
  },

  status: {
    fontSize: 18,
    fontWeight: '600',
    color: '#34495E',
  },

  board: {
    width: 315,
    height: 315,
    flexDirection: 'row',
    flexWrap: 'wrap',
    justifyContent: 'center',
  },

  square: {
    width: 95,
    height: 95,
    margin: 5,
    borderRadius: 15,
    backgroundColor: '#FFFFFF',
    justifyContent: 'center',
    alignItems: 'center',
    elevation: 4,
  },

  squareText: {
    fontSize: 42,
    fontWeight: 'bold',
  },

  textX: {
    color: '#FF6B6B',
  },

  textO: {
    color: '#4D96FF',
  },

  resetButton: {
    marginTop: 30,
    backgroundColor: '#34495E',
    paddingVertical: 14,
    paddingHorizontal: 35,
    borderRadius: 25,
    elevation: 3,
  },

  resetText: {
    color: '#FFFFFF',
    fontSize: 17,
    fontWeight: 'bold',
  },

});