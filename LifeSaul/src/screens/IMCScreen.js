import React, { useState } from 'react';
import {
  View,
  Text,
  TextInput,
  StyleSheet,
  TouchableOpacity
} from 'react-native';

export default function IMCScreen() {
  const [peso, setPeso] = useState('');
  const [altura, setAltura] = useState('');
  const [resultado, setResultado] = useState(null);

  const calcularIMC = () => {
    const p = parseFloat(peso);
    const a = parseFloat(altura) / 100;

    if (p > 0 && a > 0) {
      const imc = p / (a * a);

      let estado = '';
      let color = '#475569';

      if (imc < 18.5) {
        estado = 'Bajo peso';
        color = '#0EA5E9';
      } else if (imc < 24.9) {
        estado = 'Peso normal';
        color = '#22C55E';
      } else if (imc < 29.9) {
        estado = 'Sobrepeso';
        color = '#F59E0B';
      } else {
        estado = 'Obesidad';
        color = '#EF4444';
      }

      setResultado({
        imc: imc.toFixed(1),
        estado: estado,
        color: color
      });
    }
  };

  return (
    <View style={styles.container}>

      <Text style={styles.title}>
        Mi Calculadora IMC
      </Text>

      <Text style={styles.subtitle}>
        Ingresa tus datos para conocer tu resultado
      </Text>

      <View style={styles.formCard}>

        <Text style={styles.label}>
          Peso
        </Text>

        <TextInput
          style={styles.input}
          keyboardType="numeric"
          placeholder="Ejemplo: 70 kg"
          value={peso}
          onChangeText={setPeso}
        />

        <Text style={styles.label}>
          Altura
        </Text>

        <TextInput
          style={styles.input}
          keyboardType="numeric"
          placeholder="Ejemplo: 175 cm"
          value={altura}
          onChangeText={setAltura}
        />

        <TouchableOpacity
          style={styles.button}
          onPress={calcularIMC}
        >
          <Text style={styles.buttonText}>
            Calcular IMC
          </Text>
        </TouchableOpacity>

      </View>

      {resultado && (
        <View style={styles.resultContainer}>

          <Text style={styles.resultTitle}>
            Tu resultado
          </Text>

          <View
            style={[
              styles.resultCircle,
              { backgroundColor: resultado.color }
            ]}
          >
            <Text style={styles.resultValue}>
              {resultado.imc}
            </Text>
          </View>

          <Text
            style={[
              styles.resultStatus,
              { color: resultado.color }
            ]}
          >
            {resultado.estado}
          </Text>

        </View>
      )}

    </View>
  );
}

const styles = StyleSheet.create({

  container: {
    flex: 1,
    backgroundColor: '#EEF2F7',
    padding: 25,
    justifyContent: 'center',
  },

  title: {
    fontSize: 30,
    fontWeight: 'bold',
    color: '#1E293B',
    textAlign: 'center',
  },

  subtitle: {
    fontSize: 15,
    color: '#64748B',
    textAlign: 'center',
    marginTop: 5,
    marginBottom: 25,
  },

  formCard: {
    backgroundColor: '#FFFFFF',
    padding: 20,
    borderRadius: 18,
    elevation: 4,
  },

  label: {
    fontSize: 16,
    fontWeight: '600',
    color: '#334155',
    marginBottom: 7,
    marginTop: 10,
  },

  input: {
    backgroundColor: '#F8FAFC',
    borderWidth: 1,
    borderColor: '#CBD5E1',
    borderRadius: 12,
    padding: 13,
    fontSize: 16,
  },

  button: {
    backgroundColor: '#2563EB',
    paddingVertical: 15,
    borderRadius: 25,
    alignItems: 'center',
    marginTop: 25,
  },

  buttonText: {
    color: '#FFFFFF',
    fontSize: 17,
    fontWeight: 'bold',
  },

  resultContainer: {
    alignItems: 'center',
    marginTop: 30,
  },

  resultTitle: {
    fontSize: 18,
    fontWeight: '600',
    color: '#475569',
    marginBottom: 15,
  },

  resultCircle: {
    width: 130,
    height: 130,
    borderRadius: 65,
    justifyContent: 'center',
    alignItems: 'center',
    elevation: 5,
  },

  resultValue: {
    fontSize: 38,
    fontWeight: 'bold',
    color: '#FFFFFF',
  },

  resultStatus: {
    fontSize: 22,
    fontWeight: 'bold',
    marginTop: 15,
  },

});