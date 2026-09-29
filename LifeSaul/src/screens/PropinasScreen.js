import React, { useState } from 'react';
import {
  View,
  Text,
  TextInput,
  StyleSheet,
  TouchableOpacity,
  ScrollView
} from 'react-native';

export default function PropinasScreen() {
  const [monto, setMonto] = useState('');
  const [porcentaje, setPorcentaje] = useState(15);
  const [personas, setPersonas] = useState('1');

  const numMonto = parseFloat(monto) || 0;
  const numPersonas = parseInt(personas) || 1;

  const propinaTotal = (numMonto * porcentaje) / 100;
  const totalConPropina = numMonto + propinaTotal;
  const totalPorPersona = totalConPropina / numPersonas;

  return (
    <ScrollView contentContainerStyle={styles.container}>

      <Text style={styles.title}>
        Calculadora de Propinas
      </Text>

      <Text style={styles.subtitle}>
        Calcula cuánto debe pagar cada persona
      </Text>

      <View style={styles.formCard}>

        <Text style={styles.label}>
          Monto de la cuenta
        </Text>

        <TextInput
          style={styles.input}
          keyboardType="numeric"
          placeholder="$ 500"
          value={monto}
          onChangeText={setMonto}
        />

        <Text style={styles.label}>
          Selecciona la propina
        </Text>

        <View style={styles.percentContainer}>
          {[10, 15, 20, 25].map((pct) => (
            <TouchableOpacity
              key={pct}
              style={[
                styles.pctButton,
                porcentaje === pct && styles.pctActive
              ]}
              onPress={() => setPorcentaje(pct)}
            >
              <Text
                style={[
                  styles.pctText,
                  porcentaje === pct && styles.pctActiveText
                ]}
              >
                {pct}%
              </Text>
            </TouchableOpacity>
          ))}
        </View>

        <Text style={styles.label}>
          Número de personas
        </Text>

        <TextInput
          style={styles.input}
          keyboardType="numeric"
          placeholder="1"
          value={personas}
          onChangeText={setPersonas}
        />

      </View>

      <View style={styles.resultCard}>

        <View style={styles.resultRow}>
          <Text style={styles.resultLabel}>
            Propina
          </Text>

          <Text style={styles.resultValue}>
            ${propinaTotal.toFixed(2)}
          </Text>
        </View>

        <View style={styles.resultRow}>
          <Text style={styles.resultLabel}>
            Total
          </Text>

          <Text style={styles.resultValue}>
            ${totalConPropina.toFixed(2)}
          </Text>
        </View>

        <View style={styles.divider} />

        <Text style={styles.personLabel}>
          Total por persona
        </Text>

        <Text style={styles.personValue}>
          ${totalPorPersona.toFixed(2)}
        </Text>

      </View>

    </ScrollView>
  );
}

const styles = StyleSheet.create({

  container: {
    flexGrow: 1,
    padding: 25,
    backgroundColor: '#EEF2F7',
  },

  title: {
    fontSize: 28,
    fontWeight: 'bold',
    color: '#1E293B',
    textAlign: 'center',
    marginTop: 25,
  },

  subtitle: {
    textAlign: 'center',
    color: '#64748B',
    fontSize: 15,
    marginTop: 5,
    marginBottom: 25,
  },

  formCard: {
    backgroundColor: '#FFFFFF',
    padding: 20,
    borderRadius: 16,
    elevation: 3,
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
    borderRadius: 10,
    padding: 13,
    fontSize: 17,
    marginBottom: 10,
  },

  percentContainer: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    marginVertical: 10,
  },

  pctButton: {
    flex: 1,
    paddingVertical: 12,
    marginHorizontal: 4,
    backgroundColor: '#E2E8F0',
    borderRadius: 20,
    alignItems: 'center',
  },

  pctActive: {
    backgroundColor: '#2563EB',
  },

  pctText: {
    fontSize: 15,
    fontWeight: 'bold',
    color: '#475569',
  },

  pctActiveText: {
    color: '#FFFFFF',
  },

  resultCard: {
    backgroundColor: '#1E293B',
    padding: 22,
    borderRadius: 16,
    marginTop: 25,
  },

  resultRow: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    marginBottom: 12,
  },

  resultLabel: {
    color: '#CBD5E1',
    fontSize: 16,
  },

  resultValue: {
    color: '#FFFFFF',
    fontSize: 17,
    fontWeight: 'bold',
  },

  divider: {
    height: 1,
    backgroundColor: '#475569',
    marginVertical: 12,
  },

  personLabel: {
    color: '#94A3B8',
    textAlign: 'center',
    fontSize: 16,
  },

  personValue: {
    color: '#38BDF8',
    textAlign: 'center',
    fontSize: 32,
    fontWeight: 'bold',
    marginTop: 5,
  },

});