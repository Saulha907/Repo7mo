import React, {
  useCallback,
  useState
} from 'react';

import {
  View,
  Text,
  StyleSheet,
  FlatList,
  TouchableOpacity,
  Modal,
  Alert
} from 'react-native';

import { Ionicons } from '@expo/vector-icons';

import {
  useFocusEffect
} from '@react-navigation/native';

import {
  obtenerJornadasStorage,
  eliminarJornadaStorage
} from '../storage/storage';


export default function HistorialScreen() {

  const [jornadas, setJornadas] = useState([]);

  const [jornadaSeleccionada, setJornadaSeleccionada] =
    useState(null);

  const [modalVisible, setModalVisible] =
    useState(false);


  const cargarJornadas = async () => {

    const datos =
      await obtenerJornadasStorage();

    const ordenadas = [...datos].sort(
      (a, b) =>
        new Date(b.fecha) -
        new Date(a.fecha)
    );

    setJornadas(ordenadas);
  };


  useFocusEffect(

    useCallback(() => {

      cargarJornadas();

    }, [])

  );


  const abrirDetalle = (jornada) => {

    setJornadaSeleccionada(jornada);

    setModalVisible(true);
  };


  const cerrarModal = () => {

    setModalVisible(false);

    setJornadaSeleccionada(null);
  };


  const eliminarJornada = (jornada) => {

    Alert.alert(
      'Eliminar jornada',
      '¿Estás seguro de que deseas eliminar esta jornada?',
      [
        {
          text: 'Cancelar',
          style: 'cancel'
        },

        {
          text: 'Eliminar',
          style: 'destructive',

          onPress: async () => {

            const eliminada =
              await eliminarJornadaStorage(
                jornada.id
              );

            if (eliminada) {

              cerrarModal();

              await cargarJornadas();
            }

          }
        }
      ]
    );

  };


  const formatearFecha = (fecha) => {

    const date =
      new Date(fecha);

    return date.toLocaleDateString(
      'es-MX',
      {
        day: '2-digit',
        month: '2-digit',
        year: 'numeric'
      }
    );
  };


  const formatearHora = (fecha) => {

    const date =
      new Date(fecha);

    return date.toLocaleTimeString(
      'es-MX',
      {
        hour: '2-digit',
        minute: '2-digit'
      }
    );
  };


  const renderJornada = ({ item }) => {

    const gananciaPositiva =
      item.ganancia >= 0;

    return (

      <TouchableOpacity
        style={styles.card}
        onPress={() =>
          abrirDetalle(item)
        }
        activeOpacity={0.8}
      >

        <View style={styles.cardHeader}>

          <View style={styles.calendarIcon}>

            <Ionicons
              name="calendar-outline"
              size={25}
              color="#2563EB"
            />

          </View>


          <View style={styles.dateContainer}>

            <Text style={styles.date}>
              {formatearFecha(item.fecha)}
            </Text>

            <Text style={styles.time}>
              {formatearHora(item.fecha)}
            </Text>

          </View>


          <Ionicons
            name="chevron-forward"
            size={23}
            color="#94A3B8"
          />

        </View>


        <View style={styles.divider} />


        <View style={styles.infoRow}>

          <View>

            <Text style={styles.infoLabel}>
              Ingresos
            </Text>

            <Text style={styles.income}>
              ${Number(
                item.ingresoTotal || 0
              ).toFixed(2)}
            </Text>

          </View>


          <View>

            <Text style={styles.infoLabel}>
              Gastos
            </Text>

            <Text style={styles.expense}>
              ${Number(
                item.gastosTotales || 0
              ).toFixed(2)}
            </Text>

          </View>


          <View>

            <Text style={styles.infoLabel}>
              Ganancia
            </Text>

            <Text
              style={[
                styles.profit,

                {
                  color:
                    gananciaPositiva
                      ? '#16A34A'
                      : '#DC2626'
                }

              ]}
            >

              ${Number(
                item.ganancia || 0
              ).toFixed(2)}

            </Text>

          </View>

        </View>


        <View style={styles.ticketInfo}>

          <Ionicons
            name="ticket-outline"
            size={17}
            color="#64748B"
          />

          <Text style={styles.ticketText}>

            {(item.boletosEstudiante || 0) +
              (item.boletosEnteros || 0)}
            {' '}boletos vendidos

          </Text>

        </View>

      </TouchableOpacity>

    );
  };


  return (

    <View style={styles.container}>

      <View style={styles.header}>

        <Text style={styles.title}>
          Historial
        </Text>

        <Text style={styles.subtitle}>
          Consulta las jornadas registradas
        </Text>

      </View>


      {jornadas.length === 0 ? (

        <View style={styles.emptyContainer}>

          <View style={styles.emptyIcon}>

            <Ionicons
              name="document-text-outline"
              size={55}
              color="#94A3B8"
            />

          </View>


          <Text style={styles.emptyTitle}>
            Sin jornadas registradas
          </Text>

          <Text style={styles.emptyText}>
            Cuando registres una jornada,
            aparecerá aquí.
          </Text>

        </View>

      ) : (

        <FlatList
          data={jornadas}
          keyExtractor={(item) =>
            item.id
          }
          renderItem={renderJornada}
          contentContainerStyle={
            styles.listContent
          }
          showsVerticalScrollIndicator={false}
        />

      )}


      {/* MODAL DE DETALLE */}

      <Modal
        animationType="slide"
        transparent={true}
        visible={modalVisible}
        onRequestClose={cerrarModal}
      >

        <View style={styles.modalOverlay}>

          <View style={styles.modalContent}>

            {jornadaSeleccionada && (

              <>

                <View style={styles.modalTop}>

                  <View>

                    <Text style={styles.modalTitle}>
                      Detalle de Jornada
                    </Text>

                    <Text style={styles.modalDate}>
                      {formatearFecha(
                        jornadaSeleccionada.fecha
                      )}
                      {' • '}
                      {formatearHora(
                        jornadaSeleccionada.fecha
                      )}
                    </Text>

                  </View>


                  <TouchableOpacity
                    style={styles.closeIcon}
                    onPress={cerrarModal}
                  >

                    <Ionicons
                      name="close"
                      size={25}
                      color="#475569"
                    />

                  </TouchableOpacity>

                </View>


                <View style={styles.modalSection}>

                  <Text style={styles.modalSectionTitle}>
                    Boletos
                  </Text>


                  <View style={styles.modalRow}>

                    <Text style={styles.modalLabel}>
                      Estudiantes
                    </Text>

                    <Text style={styles.modalValue}>
                      {
                        jornadaSeleccionada
                          .boletosEstudiante
                      }
                    </Text>

                  </View>


                  <View style={styles.modalRow}>

                    <Text style={styles.modalLabel}>
                      Enteros
                    </Text>

                    <Text style={styles.modalValue}>
                      {
                        jornadaSeleccionada
                          .boletosEnteros
                      }
                    </Text>

                  </View>


                  <View style={styles.modalRow}>

                    <Text style={styles.modalLabel}>
                      Ingreso total
                    </Text>

                    <Text style={styles.greenText}>
                      $
                      {Number(
                        jornadaSeleccionada
                          .ingresoTotal || 0
                      ).toFixed(2)}
                    </Text>

                  </View>

                </View>


                <View style={styles.modalSection}>

                  <Text style={styles.modalSectionTitle}>
                    Diésel
                  </Text>


                  <View style={styles.modalRow}>

                    <Text style={styles.modalLabel}>
                      Litros
                    </Text>

                    <Text style={styles.modalValue}>
                      {
                        jornadaSeleccionada
                          .litrosDiesel
                      } L
                    </Text>

                  </View>


                  <View style={styles.modalRow}>

                    <Text style={styles.modalLabel}>
                      Precio por litro
                    </Text>

                    <Text style={styles.modalValue}>
                      $
                      {Number(
                        jornadaSeleccionada
                          .precioDiesel || 0
                      ).toFixed(2)}
                    </Text>

                  </View>


                  <View style={styles.modalRow}>

                    <Text style={styles.modalLabel}>
                      Gasto en diésel
                    </Text>

                    <Text style={styles.redText}>
                      -$
                      {Number(
                        jornadaSeleccionada
                          .gastoDiesel || 0
                      ).toFixed(2)}
                    </Text>

                  </View>

                </View>


                <View style={styles.modalSection}>

                  <Text style={styles.modalSectionTitle}>
                    Recorrido
                  </Text>


                  <View style={styles.modalRow}>

                    <Text style={styles.modalLabel}>
                      Kilómetros
                    </Text>

                    <Text style={styles.modalValue}>
                      {Number(
                        jornadaSeleccionada
                          .kilometrosRecorridos || 0
                      ).toFixed(0)}
                      {' '}km
                    </Text>

                  </View>


                  <View style={styles.modalRow}>

                    <Text style={styles.modalLabel}>
                      Rendimiento
                    </Text>

                    <Text style={styles.modalValue}>
                      {Number(
                        jornadaSeleccionada
                          .rendimiento || 0
                      ).toFixed(2)}
                      {' '}km/L
                    </Text>

                  </View>

                </View>


                <View style={styles.balanceBox}>

                  <Text style={styles.balanceLabel}>
                    Ganancia de la jornada
                  </Text>

                  <Text
                    style={[
                      styles.balanceValue,

                      {
                        color:
                          jornadaSeleccionada
                            .ganancia >= 0
                            ? '#16A34A'
                            : '#DC2626'
                      }

                    ]}
                  >

                    $
                    {Number(
                      jornadaSeleccionada
                        .ganancia || 0
                    ).toFixed(2)}

                  </Text>

                </View>


                <TouchableOpacity
                  style={styles.deleteButton}
                  onPress={() =>
                    eliminarJornada(
                      jornadaSeleccionada
                    )
                  }
                >

                  <Ionicons
                    name="trash-outline"
                    size={20}
                    color="#DC2626"
                  />

                  <Text style={styles.deleteText}>
                    Eliminar jornada
                  </Text>

                </TouchableOpacity>


                <TouchableOpacity
                  style={styles.acceptButton}
                  onPress={cerrarModal}
                >

                  <Text style={styles.acceptText}>
                    Cerrar
                  </Text>

                </TouchableOpacity>

              </>

            )}

          </View>

        </View>

      </Modal>

    </View>
  );
}


const styles = StyleSheet.create({

  container: {

    flex: 1,

    backgroundColor: '#F1F5F9'
  },


  header: {

    paddingHorizontal: 20,

    paddingTop: 20
  },


  title: {

    fontSize: 30,

    fontWeight: 'bold',

    color: '#0F172A'
  },


  subtitle: {

    fontSize: 15,

    color: '#64748B',

    marginTop: 5,

    marginBottom: 20
  },


  listContent: {

    paddingHorizontal: 20,

    paddingBottom: 40
  },


  card: {

    backgroundColor: '#FFFFFF',

    borderRadius: 18,

    padding: 17,

    marginBottom: 15,

    elevation: 3,

    shadowColor: '#000',

    shadowOpacity: 0.05,

    shadowOffset: {
      width: 0,
      height: 3
    },

    shadowRadius: 5
  },


  cardHeader: {

    flexDirection: 'row',

    alignItems: 'center'
  },


  calendarIcon: {

    width: 48,

    height: 48,

    borderRadius: 14,

    backgroundColor: '#DBEAFE',

    justifyContent: 'center',

    alignItems: 'center'
  },


  dateContainer: {

    flex: 1,

    marginLeft: 12
  },


  date: {

    color: '#0F172A',

    fontSize: 16,

    fontWeight: 'bold'
  },


  time: {

    color: '#64748B',

    marginTop: 2,

    fontSize: 13
  },


  divider: {

    height: 1,

    backgroundColor: '#E2E8F0',

    marginVertical: 15
  },


  infoRow: {

    flexDirection: 'row',

    justifyContent: 'space-between'
  },


  infoLabel: {

    fontSize: 12,

    color: '#64748B'
  },


  income: {

    color: '#16A34A',

    fontWeight: 'bold',

    marginTop: 3
  },


  expense: {

    color: '#DC2626',

    fontWeight: 'bold',

    marginTop: 3
  },


  profit: {

    fontWeight: 'bold',

    marginTop: 3
  },


  ticketInfo: {

    flexDirection: 'row',

    alignItems: 'center',

    marginTop: 15
  },


  ticketText: {

    color: '#64748B',

    fontSize: 13,

    marginLeft: 6
  },


  emptyContainer: {

    flex: 1,

    justifyContent: 'center',

    alignItems: 'center',

    paddingHorizontal: 35
  },


  emptyIcon: {

    width: 100,

    height: 100,

    borderRadius: 50,

    backgroundColor: '#E2E8F0',

    justifyContent: 'center',

    alignItems: 'center',

    marginBottom: 20
  },


  emptyTitle: {

    color: '#0F172A',

    fontSize: 21,

    fontWeight: 'bold'
  },


  emptyText: {

    color: '#64748B',

    textAlign: 'center',

    fontSize: 14,

    marginTop: 8,

    lineHeight: 20
  },


  modalOverlay: {

    flex: 1,

    backgroundColor: 'rgba(15,23,42,0.70)',

    justifyContent: 'center',

    padding: 20
  },


  modalContent: {

    backgroundColor: '#FFFFFF',

    borderRadius: 24,

    padding: 22,

    maxHeight: '90%'
  },


  modalTop: {

    flexDirection: 'row',

    justifyContent: 'space-between',

    alignItems: 'center',

    marginBottom: 20
  },


  modalTitle: {

    color: '#0F172A',

    fontSize: 22,

    fontWeight: 'bold'
  },


  modalDate: {

    color: '#64748B',

    fontSize: 13,

    marginTop: 3
  },


  closeIcon: {

    width: 40,

    height: 40,

    borderRadius: 20,

    backgroundColor: '#F1F5F9',

    justifyContent: 'center',

    alignItems: 'center'
  },


  modalSection: {

    backgroundColor: '#F8FAFC',

    borderRadius: 15,

    padding: 15,

    marginBottom: 12
  },


  modalSectionTitle: {

    color: '#0F172A',

    fontWeight: 'bold',

    fontSize: 16,

    marginBottom: 10
  },


  modalRow: {

    flexDirection: 'row',

    justifyContent: 'space-between',

    marginVertical: 5
  },


  modalLabel: {

    color: '#64748B',

    fontSize: 14
  },


  modalValue: {

    color: '#0F172A',

    fontSize: 14,

    fontWeight: '600'
  },


  greenText: {

    color: '#16A34A',

    fontWeight: 'bold'
  },


  redText: {

    color: '#DC2626',

    fontWeight: 'bold'
  },


  balanceBox: {

    backgroundColor: '#ECFDF5',

    borderRadius: 15,

    padding: 17,

    alignItems: 'center',

    marginTop: 3,

    marginBottom: 15
  },


  balanceLabel: {

    color: '#166534',

    fontSize: 14
  },


  balanceValue: {

    fontSize: 30,

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

    alignItems: 'center'
  },


  deleteText: {

    color: '#DC2626',

    fontWeight: 'bold',

    marginLeft: 7
  },


  acceptButton: {

    backgroundColor: '#2563EB',

    paddingVertical: 14,

    borderRadius: 13,

    alignItems: 'center',

    marginTop: 10
  },


  acceptText: {

    color: '#FFFFFF',

    fontWeight: 'bold',

    fontSize: 16
  }

});