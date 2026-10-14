import React, {
  useCallback,
  useState
} from 'react';

import {
  View,
  Text,
  TextInput,
  StyleSheet,
  TouchableOpacity,
  ScrollView,
  Modal,
  FlatList,
  Alert
} from 'react-native';

import { Ionicons } from '@expo/vector-icons';

import {
  useFocusEffect
} from '@react-navigation/native';

import {
  obtenerJornadasStorage,
  obtenerGastosStorage,
  obtenerRetirosStorage,
  guardarRetiroStorage,
  eliminarRetiroStorage
} from '../storage/storage';


export default function RetirosScreen() {

  const [monto, setMonto] = useState('');
  const [concepto, setConcepto] = useState('');

  const [retiros, setRetiros] = useState([]);

  const [saldoDisponible, setSaldoDisponible] =
    useState(0);

  const [modalVisible, setModalVisible] =
    useState(false);

  const [retiroSeleccionado, setRetiroSeleccionado] =
    useState(null);


  const cargarDatos = async () => {

    const jornadas =
      await obtenerJornadasStorage();

    const gastos =
      await obtenerGastosStorage();

    const retirosGuardados =
      await obtenerRetirosStorage();


    const ingresos =
      jornadas.reduce(
        (total, jornada) =>
          total +
          Number(
            jornada.ingresoTotal || 0
          ),
        0
      );


    const diesel =
      jornadas.reduce(
        (total, jornada) =>
          total +
          Number(
            jornada.gastoDiesel || 0
          ),
        0
      );


    const otrosGastos =
      gastos.reduce(
        (total, gasto) =>
          total +
          Number(
            gasto.monto || 0
          ),
        0
      );


    const totalRetiros =
      retirosGuardados.reduce(
        (total, retiro) =>
          total +
          Number(
            retiro.monto || 0
          ),
        0
      );


    const saldo =
      ingresos -
      diesel -
      otrosGastos -
      totalRetiros;


    setSaldoDisponible(saldo);


    const ordenados =
      [...retirosGuardados].sort(
        (a, b) =>
          new Date(b.fecha) -
          new Date(a.fecha)
      );

    setRetiros(ordenados);
  };


  useFocusEffect(

    useCallback(() => {

      cargarDatos();

    }, [])

  );


  const registrarRetiro = async () => {

    const montoNumero =
      parseFloat(monto);


    if (
      isNaN(montoNumero) ||
      montoNumero <= 0
    ) {

      Alert.alert(
        'Monto inválido',
        'Ingresa un monto mayor a cero.'
      );

      return;
    }


    if (
      montoNumero >
      saldoDisponible
    ) {

      Alert.alert(
        'Saldo insuficiente',
        'El retiro no puede ser mayor al saldo disponible.'
      );

      return;
    }


    const nuevoRetiro = {

      id:
        Date.now().toString(),

      fecha:
        new Date().toISOString(),

      monto:
        montoNumero,

      concepto:
        concepto.trim() === ''
          ? 'Retiro de efectivo'
          : concepto.trim()

    };


    const guardado =
      await guardarRetiroStorage(
        nuevoRetiro
      );


    if (!guardado) {

      Alert.alert(
        'Error',
        'No se pudo registrar el retiro.'
      );

      return;
    }


    setMonto('');
    setConcepto('');

    await cargarDatos();

    setModalVisible(true);
  };


  const eliminarRetiro = (retiro) => {

    Alert.alert(
      'Eliminar retiro',
      '¿Deseas eliminar este retiro?',
      [

        {
          text: 'Cancelar',
          style: 'cancel'
        },

        {
          text: 'Eliminar',
          style: 'destructive',

          onPress: async () => {

            const eliminado =
              await eliminarRetiroStorage(
                retiro.id
              );


            if (eliminado) {

              setRetiroSeleccionado(null);

              await cargarDatos();
            }

          }

        }

      ]
    );
  };


  const formatearFecha = (fecha) => {

    return new Date(fecha)
      .toLocaleDateString(
        'es-MX',
        {
          day: '2-digit',
          month: '2-digit',
          year: 'numeric'
        }
      );
  };


  const formatearHora = (fecha) => {

    return new Date(fecha)
      .toLocaleTimeString(
        'es-MX',
        {
          hour: '2-digit',
          minute: '2-digit'
        }
      );
  };


  const totalRetirado =
    retiros.reduce(
      (total, retiro) =>
        total +
        Number(
          retiro.monto || 0
        ),
      0
    );


  const renderRetiro = ({ item }) => (

    <TouchableOpacity
      style={styles.retiroCard}
      onPress={() =>
        setRetiroSeleccionado(item)
      }
      activeOpacity={0.8}
    >

      <View style={styles.retiroIcon}>

        <Ionicons
          name="cash-outline"
          size={25}
          color="#DC2626"
        />

      </View>


      <View style={styles.retiroInfo}>

        <Text style={styles.retiroConcepto}>
          {item.concepto}
        </Text>

        <Text style={styles.retiroFecha}>
          {formatearFecha(item.fecha)}
          {' • '}
          {formatearHora(item.fecha)}
        </Text>

      </View>


      <Text style={styles.retiroMonto}>
        -${Number(
          item.monto
        ).toFixed(2)}
      </Text>

    </TouchableOpacity>

  );


  return (

    <ScrollView
      style={styles.container}
      contentContainerStyle={styles.content}
      keyboardShouldPersistTaps="handled"
      showsVerticalScrollIndicator={false}
    >

      <Text style={styles.title}>
        Retiros
      </Text>

      <Text style={styles.subtitle}>
        Registra el dinero retirado de la unidad
      </Text>


      {/* SALDO */}

      <View style={styles.balanceCard}>

        <View>

          <Text style={styles.balanceLabel}>
            Saldo disponible
          </Text>

          <Text
            style={[
              styles.balanceValue,

              {
                color:
                  saldoDisponible >= 0
                    ? '#FFFFFF'
                    : '#FCA5A5'
              }
            ]}
          >
            ${saldoDisponible.toFixed(2)}
          </Text>

        </View>


        <View style={styles.balanceIcon}>

          <Ionicons
            name="wallet-outline"
            size={30}
            color="#FFFFFF"
          />

        </View>

      </View>


      {/* TOTAL RETIRADO */}

      <View style={styles.withdrawnCard}>

        <View>

          <Text style={styles.withdrawnLabel}>
            Total retirado
          </Text>

          <Text style={styles.withdrawnValue}>
            ${totalRetirado.toFixed(2)}
          </Text>

        </View>


        <Ionicons
          name="arrow-up-circle-outline"
          size={40}
          color="#DC2626"
        />

      </View>


      {/* NUEVO RETIRO */}

      <View style={styles.formCard}>

        <Text style={styles.sectionTitle}>
          Nuevo retiro
        </Text>


        <Text style={styles.label}>
          Monto
        </Text>


        <View style={styles.moneyInput}>

          <Text style={styles.moneySymbol}>
            $
          </Text>

          <TextInput
            style={styles.moneyField}
            keyboardType="decimal-pad"
            placeholder="Ejemplo: 1000"
            placeholderTextColor="#94A3B8"
            value={monto}
            onChangeText={setMonto}
          />

        </View>


        <Text style={styles.label}>
          Concepto
        </Text>


        <TextInput
          style={styles.input}
          placeholder="Ejemplo: Entrega al propietario"
          placeholderTextColor="#94A3B8"
          value={concepto}
          onChangeText={setConcepto}
        />


        <Text style={styles.helper}>
          El retiro se descontará del saldo disponible, pero no se considera un gasto operativo.
        </Text>


        <TouchableOpacity
          style={styles.saveButton}
          onPress={registrarRetiro}
        >

          <Ionicons
            name="cash-outline"
            size={22}
            color="#FFFFFF"
          />

          <Text style={styles.saveText}>
            Registrar retiro
          </Text>

        </TouchableOpacity>

      </View>


      {/* HISTORIAL */}

      <Text style={styles.historyTitle}>
        Historial de retiros
      </Text>


      {retiros.length === 0 ? (

        <View style={styles.emptyCard}>

          <Ionicons
            name="cash-outline"
            size={48}
            color="#94A3B8"
          />

          <Text style={styles.emptyTitle}>
            Sin retiros registrados
          </Text>

          <Text style={styles.emptyText}>
            Los retiros aparecerán aquí.
          </Text>

        </View>

      ) : (

        <FlatList
          data={retiros}
          keyExtractor={(item) =>
            item.id
          }
          renderItem={renderRetiro}
          scrollEnabled={false}
        />

      )}


      {/* MODAL GUARDADO */}

      <Modal
        transparent={true}
        visible={modalVisible}
        animationType="fade"
        onRequestClose={() =>
          setModalVisible(false)
        }
      >

        <View style={styles.modalOverlay}>

          <View style={styles.modalContent}>

            <View style={styles.successCircle}>

              <Ionicons
                name="checkmark"
                size={42}
                color="#FFFFFF"
              />

            </View>


            <Text style={styles.modalTitle}>
              Retiro registrado
            </Text>

            <Text style={styles.modalText}>
              El retiro fue descontado del saldo disponible.
            </Text>


            <TouchableOpacity
              style={styles.modalButton}
              onPress={() =>
                setModalVisible(false)
              }
            >

              <Text style={styles.modalButtonText}>
                Aceptar
              </Text>

            </TouchableOpacity>

          </View>

        </View>

      </Modal>


      {/* MODAL DETALLE */}

      <Modal
        transparent={true}
        visible={
          retiroSeleccionado !== null
        }
        animationType="slide"
        onRequestClose={() =>
          setRetiroSeleccionado(null)
        }
      >

        <View style={styles.modalOverlay}>

          <View style={styles.detailModal}>

            {retiroSeleccionado && (

              <>

                <Text style={styles.detailTitle}>
                  Detalle del retiro
                </Text>


                <View style={styles.detailBox}>

                  <Text style={styles.detailLabel}>
                    Monto
                  </Text>

                  <Text style={styles.detailAmount}>
                    $
                    {Number(
                      retiroSeleccionado.monto
                    ).toFixed(2)}
                  </Text>


                  <Text style={styles.detailLabel}>
                    Concepto
                  </Text>

                  <Text style={styles.detailValue}>
                    {retiroSeleccionado.concepto}
                  </Text>


                  <Text style={styles.detailLabel}>
                    Fecha
                  </Text>

                  <Text style={styles.detailValue}>
                    {formatearFecha(
                      retiroSeleccionado.fecha
                    )}
                    {' • '}
                    {formatearHora(
                      retiroSeleccionado.fecha
                    )}
                  </Text>

                </View>


                <TouchableOpacity
                  style={styles.deleteButton}
                  onPress={() =>
                    eliminarRetiro(
                      retiroSeleccionado
                    )
                  }
                >

                  <Ionicons
                    name="trash-outline"
                    size={20}
                    color="#DC2626"
                  />

                  <Text style={styles.deleteText}>
                    Eliminar retiro
                  </Text>

                </TouchableOpacity>


                <TouchableOpacity
                  style={styles.closeButton}
                  onPress={() =>
                    setRetiroSeleccionado(null)
                  }
                >

                  <Text style={styles.closeText}>
                    Cerrar
                  </Text>

                </TouchableOpacity>

              </>

            )}

          </View>

        </View>

      </Modal>

    </ScrollView>
  );
}


const styles = StyleSheet.create({

  container: {
    flex: 1,
    backgroundColor: '#F1F5F9'
  },

  content: {
    padding: 20,
    paddingBottom: 50
  },

  title: {
    fontSize: 30,
    fontWeight: 'bold',
    color: '#0F172A'
  },

  subtitle: {
    color: '#64748B',
    fontSize: 15,
    marginTop: 5,
    marginBottom: 20
  },

  balanceCard: {
    backgroundColor: '#0F172A',
    borderRadius: 20,
    padding: 22,
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'center'
  },

  balanceLabel: {
    color: '#CBD5E1',
    fontSize: 14
  },

  balanceValue: {
    fontSize: 32,
    fontWeight: 'bold',
    marginTop: 4
  },

  balanceIcon: {
    width: 58,
    height: 58,
    borderRadius: 18,
    backgroundColor: '#2563EB',
    justifyContent: 'center',
    alignItems: 'center'
  },

  withdrawnCard: {
    backgroundColor: '#FEF2F2',
    borderRadius: 17,
    padding: 18,
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'center',
    marginTop: 15
  },

  withdrawnLabel: {
    color: '#991B1B',
    fontSize: 13
  },

  withdrawnValue: {
    color: '#DC2626',
    fontSize: 25,
    fontWeight: 'bold',
    marginTop: 3
  },

  formCard: {
    backgroundColor: '#FFFFFF',
    padding: 18,
    borderRadius: 18,
    marginTop: 20,
    elevation: 3
  },

  sectionTitle: {
    color: '#0F172A',
    fontSize: 20,
    fontWeight: 'bold',
    marginBottom: 10
  },

  label: {
    color: '#334155',
    fontWeight: '600',
    fontSize: 15,
    marginTop: 12,
    marginBottom: 7
  },

  moneyInput: {
    backgroundColor: '#F8FAFC',
    borderWidth: 1,
    borderColor: '#CBD5E1',
    borderRadius: 12,
    flexDirection: 'row',
    alignItems: 'center',
    paddingHorizontal: 14
  },

  moneySymbol: {
    color: '#475569',
    fontSize: 18,
    fontWeight: 'bold'
  },

  moneyField: {
    flex: 1,
    paddingVertical: 13,
    marginLeft: 5,
    fontSize: 16,
    color: '#0F172A'
  },

  input: {
    backgroundColor: '#F8FAFC',
    borderWidth: 1,
    borderColor: '#CBD5E1',
    borderRadius: 12,
    padding: 13,
    fontSize: 16,
    color: '#0F172A'
  },

  helper: {
    color: '#94A3B8',
    fontSize: 12,
    lineHeight: 17,
    marginTop: 8
  },

  saveButton: {
    backgroundColor: '#2563EB',
    borderRadius: 14,
    paddingVertical: 15,
    flexDirection: 'row',
    justifyContent: 'center',
    alignItems: 'center',
    marginTop: 18
  },

  saveText: {
    color: '#FFFFFF',
    fontSize: 16,
    fontWeight: 'bold',
    marginLeft: 7
  },

  historyTitle: {
    color: '#0F172A',
    fontSize: 20,
    fontWeight: 'bold',
    marginTop: 28,
    marginBottom: 14
  },

  retiroCard: {
    backgroundColor: '#FFFFFF',
    borderRadius: 16,
    padding: 15,
    flexDirection: 'row',
    alignItems: 'center',
    marginBottom: 11,
    elevation: 2
  },

  retiroIcon: {
    width: 48,
    height: 48,
    borderRadius: 14,
    backgroundColor: '#FEE2E2',
    justifyContent: 'center',
    alignItems: 'center'
  },

  retiroInfo: {
    flex: 1,
    marginLeft: 12
  },

  retiroConcepto: {
    color: '#0F172A',
    fontSize: 15,
    fontWeight: 'bold'
  },

  retiroFecha: {
    color: '#94A3B8',
    fontSize: 12,
    marginTop: 4
  },

  retiroMonto: {
    color: '#DC2626',
    fontWeight: 'bold',
    fontSize: 17
  },

  emptyCard: {
    backgroundColor: '#FFFFFF',
    borderRadius: 16,
    padding: 30,
    alignItems: 'center'
  },

  emptyTitle: {
    color: '#334155',
    fontWeight: 'bold',
    marginTop: 10
  },

  emptyText: {
    color: '#94A3B8',
    fontSize: 13,
    marginTop: 4
  },

  modalOverlay: {
    flex: 1,
    backgroundColor: 'rgba(15,23,42,0.70)',
    justifyContent: 'center',
    padding: 25
  },

  modalContent: {
    backgroundColor: '#FFFFFF',
    borderRadius: 24,
    padding: 25,
    alignItems: 'center'
  },

  successCircle: {
    width: 75,
    height: 75,
    borderRadius: 40,
    backgroundColor: '#16A34A',
    justifyContent: 'center',
    alignItems: 'center'
  },

  modalTitle: {
    color: '#0F172A',
    fontSize: 23,
    fontWeight: 'bold',
    marginTop: 15
  },

  modalText: {
    color: '#64748B',
    textAlign: 'center',
    marginTop: 7
  },

  modalButton: {
    width: '100%',
    backgroundColor: '#2563EB',
    borderRadius: 14,
    paddingVertical: 14,
    alignItems: 'center',
    marginTop: 22
  },

  modalButtonText: {
    color: '#FFFFFF',
    fontWeight: 'bold'
  },

  detailModal: {
    backgroundColor: '#FFFFFF',
    borderRadius: 22,
    padding: 22
  },

  detailTitle: {
    color: '#0F172A',
    fontSize: 23,
    fontWeight: 'bold',
    marginBottom: 18
  },

  detailBox: {
    backgroundColor: '#F8FAFC',
    borderRadius: 15,
    padding: 16
  },

  detailLabel: {
    color: '#64748B',
    fontSize: 13,
    marginTop: 10
  },

  detailValue: {
    color: '#0F172A',
    fontSize: 16,
    fontWeight: '600',
    marginTop: 2
  },

  detailAmount: {
    color: '#DC2626',
    fontSize: 27,
    fontWeight: 'bold',
    marginTop: 3
  },

  deleteButton: {
    borderWidth: 1,
    borderColor: '#FECACA',
    backgroundColor: '#FEF2F2',
    paddingVertical: 13,
    borderRadius: 13,
    flexDirection: 'row',
    justifyContent: 'center',
    alignItems: 'center',
    marginTop: 18
  },

  deleteText: {
    color: '#DC2626',
    fontWeight: 'bold',
    marginLeft: 7
  },

  closeButton: {
    backgroundColor: '#2563EB',
    paddingVertical: 14,
    borderRadius: 13,
    alignItems: 'center',
    marginTop: 10
  },

  closeText: {
    color: '#FFFFFF',
    fontWeight: 'bold'
  }

});