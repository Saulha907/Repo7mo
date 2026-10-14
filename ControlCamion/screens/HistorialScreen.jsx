import React, {
  useCallback,
  useState
} from 'react';

import {
  View,
  Text,
  StyleSheet,
  ScrollView,
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
  obtenerUnidadesStorage,
  eliminarJornadaStorage
} from '../storage/storage';


export default function HistorialScreen() {

  const [jornadas, setJornadas] =
    useState([]);

  const [unidades, setUnidades] =
    useState([]);

  const [unidadFiltro, setUnidadFiltro] =
    useState('TODAS');

  const [
    jornadaSeleccionada,
    setJornadaSeleccionada
  ] = useState(null);


  // =====================================================
  // CARGAR DATOS
  // =====================================================

  const cargarDatos = async () => {

    try {

      const jornadasGuardadas =
        await obtenerJornadasStorage();

      const unidadesGuardadas =
        await obtenerUnidadesStorage();


      setJornadas(
        Array.isArray(jornadasGuardadas)
          ? jornadasGuardadas
          : []
      );


      setUnidades(
        Array.isArray(unidadesGuardadas)
          ? unidadesGuardadas
          : []
      );

    } catch (error) {

      console.log(
        'Error al cargar historial:',
        error
      );
    }
  };


  useFocusEffect(

    useCallback(() => {

      cargarDatos();

    }, [])

  );


  // =====================================================
  // FILTRADO POR UNIDAD
  // =====================================================

  const perteneceUnidad = (
    jornada
  ) => {

    if (
      unidadFiltro === 'TODAS'
    ) {

      return true;
    }


    if (
      jornada.unidadId ===
      unidadFiltro
    ) {

      return true;
    }


    /*
      Compatibilidad con registros antiguos
      que todavía no tenían unidadId.
    */

    const unidadSeleccionada =
      unidades.find(
        (unidad) =>
          unidad.id ===
          unidadFiltro
      );


    if (
      !jornada.unidadId &&
      unidadSeleccionada &&
      jornada.numeroUnidad ===
        unidadSeleccionada.numeroUnidad
    ) {

      return true;
    }


    return false;
  };


  const jornadasFiltradas =
    jornadas
      .filter(
        perteneceUnidad
      )
      .sort(
        (a, b) =>
          new Date(b.fecha) -
          new Date(a.fecha)
      );


  // =====================================================
  // UNIDAD SELECCIONADA
  // =====================================================

  const unidadSeleccionada =
    unidades.find(
      (unidad) =>
        unidad.id ===
        unidadFiltro
    );


  // =====================================================
  // TOTALES DEL HISTORIAL FILTRADO
  // =====================================================

  const ingresosFiltrados =
    jornadasFiltradas.reduce(
      (total, jornada) =>

        total +
        Number(
          jornada.ingresoTotal || 0
        ),

      0
    );


  const dieselFiltrado =
    jornadasFiltradas.reduce(
      (total, jornada) =>

        total +
        Number(
          jornada.gastoDiesel || 0
        ),

      0
    );


  const gananciaFiltrada =
    jornadasFiltradas.reduce(
      (total, jornada) =>

        total +
        Number(
          jornada.ganancia || 0
        ),

      0
    );


  const boletosFiltrados =
    jornadasFiltradas.reduce(
      (total, jornada) =>

        total +

        Number(
          jornada.boletosEstudiante || 0
        ) +

        Number(
          jornada.boletosEnteros || 0
        ),

      0
    );


  // =====================================================
  // FORMATO FECHA
  // =====================================================

  const formatearFecha = (
    fecha
  ) => {

    if (!fecha) {

      return 'Sin fecha';
    }


    return new Date(fecha)
      .toLocaleDateString(
        'es-MX',
        {
          weekday: 'short',
          day: '2-digit',
          month: 'short',
          year: 'numeric'
        }
      );
  };


  const formatearHora = (
    fecha
  ) => {

    if (!fecha) {

      return '';
    }


    return new Date(fecha)
      .toLocaleTimeString(
        'es-MX',
        {
          hour: '2-digit',
          minute: '2-digit'
        }
      );
  };


  // =====================================================
  // ELIMINAR JORNADA
  // =====================================================

  const confirmarEliminar = (
    jornada
  ) => {

    Alert.alert(
      'Eliminar jornada',
      `¿Deseas eliminar la jornada de la Unidad ${jornada.numeroUnidad || ''}?`,
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
              await eliminarJornadaStorage(
                jornada.id
              );


            if (eliminado) {

              setJornadaSeleccionada(
                null
              );

              await cargarDatos();

            } else {

              Alert.alert(
                'Error',
                'No se pudo eliminar la jornada.'
              );
            }
          }

        }

      ]
    );
  };


  return (

    <ScrollView
      style={styles.container}
      contentContainerStyle={
        styles.content
      }
      showsVerticalScrollIndicator={false}
    >

      {/* ================================================= */}
      {/* ENCABEZADO */}
      {/* ================================================= */}

      <Text style={styles.title}>
        Historial
      </Text>

      <Text style={styles.subtitle}>
        Consulta las jornadas registradas por unidad
      </Text>


      {/* ================================================= */}
      {/* SELECTOR DE UNIDAD */}
      {/* ================================================= */}

      <Text style={styles.sectionTitle}>
        Mostrar jornadas de
      </Text>


      <ScrollView
        horizontal={true}
        showsHorizontalScrollIndicator={false}
        contentContainerStyle={
          styles.unitSelector
        }
      >

        <TouchableOpacity
          style={[
            styles.unitButton,

            unidadFiltro ===
              'TODAS' &&
            styles.unitButtonActive
          ]}
          onPress={() =>
            setUnidadFiltro(
              'TODAS'
            )
          }
        >

          <Ionicons
            name="business-outline"
            size={19}
            color={
              unidadFiltro ===
              'TODAS'
                ? '#FFFFFF'
                : '#475569'
            }
          />


          <Text
            style={[
              styles.unitButtonText,

              unidadFiltro ===
                'TODAS' &&
              styles.unitButtonTextActive
            ]}
          >
            Toda la flotilla
          </Text>

        </TouchableOpacity>


        {unidades.map(
          (unidad) => (

            <TouchableOpacity
              key={unidad.id}
              style={[
                styles.unitButton,

                unidadFiltro ===
                  unidad.id &&
                styles.unitButtonActive
              ]}
              onPress={() =>
                setUnidadFiltro(
                  unidad.id
                )
              }
            >

              <Ionicons
                name="bus-outline"
                size={19}
                color={
                  unidadFiltro ===
                    unidad.id
                    ? '#FFFFFF'
                    : '#475569'
                }
              />


              <Text
                style={[
                  styles.unitButtonText,

                  unidadFiltro ===
                    unidad.id &&
                  styles.unitButtonTextActive
                ]}
              >
                Unidad {unidad.numeroUnidad}
              </Text>

            </TouchableOpacity>

          )
        )}

      </ScrollView>


      {/* ================================================= */}
      {/* VISTA ACTUAL */}
      {/* ================================================= */}

      <View style={styles.selectedCard}>

        <View style={styles.selectedIcon}>

          <Ionicons
            name={
              unidadFiltro === 'TODAS'
                ? 'business'
                : 'bus'
            }
            size={27}
            color="#FFFFFF"
          />

        </View>


        <View style={styles.selectedInfo}>

          <Text style={styles.selectedLabel}>
            CONSULTANDO
          </Text>


          <Text style={styles.selectedTitle}>

            {
              unidadFiltro === 'TODAS'

                ? 'Toda la flotilla'

                : `Unidad ${
                    unidadSeleccionada
                      ?.numeroUnidad || ''
                  }`
            }

          </Text>


          {
            unidadSeleccionada &&
            unidadFiltro !==
              'TODAS' && (

              <Text style={styles.selectedRoute}>
                {unidadSeleccionada.ruta}
              </Text>

            )
          }

        </View>


        <View style={styles.countBadge}>

          <Text style={styles.countNumber}>
            {jornadasFiltradas.length}
          </Text>

          <Text style={styles.countLabel}>
            jornadas
          </Text>

        </View>

      </View>


      {/* ================================================= */}
      {/* RESUMEN */}
      {/* ================================================= */}

      <Text style={styles.sectionTitle}>
        Resumen
      </Text>


      <View style={styles.summaryGrid}>

        <View
          style={[
            styles.summaryCard,
            styles.cardMargin
          ]}
        >

          <View
            style={[
              styles.summaryIcon,
              {
                backgroundColor:
                  '#DCFCE7'
              }
            ]}
          >

            <Ionicons
              name="arrow-up-outline"
              size={21}
              color="#16A34A"
            />

          </View>


          <Text style={styles.summaryLabel}>
            Ingresos
          </Text>

          <Text style={styles.incomeValue}>
            ${ingresosFiltrados.toFixed(2)}
          </Text>

        </View>


        <View style={styles.summaryCard}>

          <View
            style={[
              styles.summaryIcon,
              {
                backgroundColor:
                  '#FEF3C7'
              }
            ]}
          >

            <Ionicons
              name="water-outline"
              size={21}
              color="#D97706"
            />

          </View>


          <Text style={styles.summaryLabel}>
            Diésel
          </Text>

          <Text style={styles.expenseValue}>
            ${dieselFiltrado.toFixed(2)}
          </Text>

        </View>

      </View>


      <View style={styles.summaryGrid}>

        <View
          style={[
            styles.summaryCard,
            styles.cardMargin
          ]}
        >

          <View
            style={[
              styles.summaryIcon,
              {
                backgroundColor:
                  '#DBEAFE'
              }
            ]}
          >

            <Ionicons
              name="ticket-outline"
              size={21}
              color="#2563EB"
            />

          </View>


          <Text style={styles.summaryLabel}>
            Boletos
          </Text>

          <Text style={styles.normalValue}>
            {boletosFiltrados}
          </Text>

        </View>


        <View style={styles.summaryCard}>

          <View
            style={[
              styles.summaryIcon,
              {
                backgroundColor:
                  gananciaFiltrada >= 0
                    ? '#DCFCE7'
                    : '#FEE2E2'
              }
            ]}
          >

            <Ionicons
              name={
                gananciaFiltrada >= 0
                  ? 'trending-up-outline'
                  : 'trending-down-outline'
              }
              size={21}
              color={
                gananciaFiltrada >= 0
                  ? '#16A34A'
                  : '#DC2626'
              }
            />

          </View>


          <Text style={styles.summaryLabel}>
            Ganancia
          </Text>

          <Text
            style={[
              styles.normalValue,

              {
                color:
                  gananciaFiltrada >= 0
                    ? '#16A34A'
                    : '#DC2626'
              }
            ]}
          >
            ${gananciaFiltrada.toFixed(2)}
          </Text>

        </View>

      </View>


      {/* ================================================= */}
      {/* LISTADO */}
      {/* ================================================= */}

      <Text style={styles.sectionTitle}>
        Jornadas registradas
      </Text>


      {jornadasFiltradas.length === 0 ? (

        <View style={styles.emptyCard}>

          <View style={styles.emptyIcon}>

            <Ionicons
              name="time-outline"
              size={46}
              color="#94A3B8"
            />

          </View>


          <Text style={styles.emptyTitle}>
            Sin jornadas registradas
          </Text>


          <Text style={styles.emptyText}>

            {
              unidadFiltro === 'TODAS'

                ? 'Todavía no existen jornadas registradas.'

                : `La Unidad ${
                    unidadSeleccionada
                      ?.numeroUnidad || ''
                  } todavía no tiene jornadas registradas.`
            }

          </Text>

        </View>

      ) : (

        jornadasFiltradas.map(
          (jornada) => {

            const totalBoletos =

              Number(
                jornada.boletosEstudiante || 0
              ) +

              Number(
                jornada.boletosEnteros || 0
              );


            return (

              <TouchableOpacity
                key={jornada.id}
                style={styles.jornadaCard}
                activeOpacity={0.82}
                onPress={() =>
                  setJornadaSeleccionada(
                    jornada
                  )
                }
              >

                <View style={styles.jornadaHeader}>

                  <View style={styles.jornadaBusIcon}>

                    <Ionicons
                      name="bus"
                      size={24}
                      color="#2563EB"
                    />

                  </View>


                  <View style={styles.jornadaMainInfo}>

                    <Text style={styles.jornadaUnit}>
                      Unidad {jornada.numeroUnidad || 'N/D'}
                    </Text>

                    <Text style={styles.jornadaRoute}>
                      {jornada.ruta || 'Sin ruta'}
                    </Text>

                  </View>


                  <Ionicons
                    name="chevron-forward"
                    size={22}
                    color="#94A3B8"
                  />

                </View>


                <View style={styles.jornadaDivider} />


                <View style={styles.dateRow}>

                  <Ionicons
                    name="calendar-outline"
                    size={16}
                    color="#64748B"
                  />

                  <Text style={styles.dateText}>
                    {formatearFecha(
                      jornada.fecha
                    )}
                  </Text>


                  <Text style={styles.timeText}>
                    {formatearHora(
                      jornada.fecha
                    )}
                  </Text>

                </View>


                <View style={styles.jornadaNumbers}>

                  <View style={styles.numberItem}>

                    <Text style={styles.numberLabel}>
                      Ingreso
                    </Text>

                    <Text style={styles.numberIncome}>
                      $
                      {Number(
                        jornada.ingresoTotal || 0
                      ).toFixed(2)}
                    </Text>

                  </View>


                  <View style={styles.numberItem}>

                    <Text style={styles.numberLabel}>
                      Boletos
                    </Text>

                    <Text style={styles.numberValue}>
                      {totalBoletos}
                    </Text>

                  </View>


                  <View style={styles.numberItem}>

                    <Text style={styles.numberLabel}>
                      Ganancia
                    </Text>

                    <Text
                      style={[
                        styles.numberValue,

                        {
                          color:
                            Number(
                              jornada.ganancia || 0
                            ) >= 0
                              ? '#16A34A'
                              : '#DC2626'
                        }
                      ]}
                    >
                      $
                      {Number(
                        jornada.ganancia || 0
                      ).toFixed(2)}
                    </Text>

                  </View>

                </View>

              </TouchableOpacity>

            );
          }
        )

      )}


      {/* ================================================= */}
      {/* MODAL DETALLE */}
      {/* ================================================= */}

      <Modal
        transparent={true}
        visible={
          jornadaSeleccionada !== null
        }
        animationType="slide"
        onRequestClose={() =>
          setJornadaSeleccionada(
            null
          )
        }
      >

        <View style={styles.modalOverlay}>

          <View style={styles.modalContainer}>

            {jornadaSeleccionada && (

              <ScrollView
                showsVerticalScrollIndicator={false}
              >

                <View style={styles.modalHeader}>

                  <View>

                    <Text style={styles.modalTitle}>
                      Detalle de jornada
                    </Text>

                    <Text style={styles.modalSubtitle}>
                      Unidad {jornadaSeleccionada.numeroUnidad}
                    </Text>

                  </View>


                  <TouchableOpacity
                    style={styles.closeButton}
                    onPress={() =>
                      setJornadaSeleccionada(
                        null
                      )
                    }
                  >

                    <Ionicons
                      name="close"
                      size={25}
                      color="#475569"
                    />

                  </TouchableOpacity>

                </View>


                <View style={styles.modalUnitCard}>

                  <View style={styles.modalUnitIcon}>

                    <Ionicons
                      name="bus"
                      size={28}
                      color="#FFFFFF"
                    />

                  </View>


                  <View>

                    <Text style={styles.modalUnitTitle}>
                      Unidad {jornadaSeleccionada.numeroUnidad}
                    </Text>

                    <Text style={styles.modalUnitRoute}>
                      {jornadaSeleccionada.ruta}
                    </Text>

                  </View>

                </View>


                <DetailRow
                  label="Fecha"
                  value={
                    formatearFecha(
                      jornadaSeleccionada.fecha
                    )
                  }
                />


                <DetailRow
                  label="Hora de registro"
                  value={
                    formatearHora(
                      jornadaSeleccionada.fecha
                    )
                  }
                />


                <Text style={styles.modalSectionTitle}>
                  Boletos
                </Text>


                <DetailRow
                  label="Estudiante"
                  value={
                    String(
                      jornadaSeleccionada.boletosEstudiante || 0
                    )
                  }
                />


                <DetailRow
                  label="Enteros"
                  value={
                    String(
                      jornadaSeleccionada.boletosEnteros || 0
                    )
                  }
                />


                <DetailRow
                  label="Ingreso estudiantes"
                  value={
                    `$${Number(
                      jornadaSeleccionada.ingresoEstudiantes || 0
                    ).toFixed(2)}`
                  }
                  color="#16A34A"
                />


                <DetailRow
                  label="Ingreso enteros"
                  value={
                    `$${Number(
                      jornadaSeleccionada.ingresoEnteros || 0
                    ).toFixed(2)}`
                  }
                  color="#16A34A"
                />


                <Text style={styles.modalSectionTitle}>
                  Combustible
                </Text>


                <DetailRow
                  label="Litros"
                  value={
                    `${Number(
                      jornadaSeleccionada.litrosDiesel || 0
                    ).toFixed(2)} L`
                  }
                />


                <DetailRow
                  label="Precio por litro"
                  value={
                    `$${Number(
                      jornadaSeleccionada.precioDiesel || 0
                    ).toFixed(2)}`
                  }
                />


                <DetailRow
                  label="Gasto diésel"
                  value={
                    `$${Number(
                      jornadaSeleccionada.gastoDiesel || 0
                    ).toFixed(2)}`
                  }
                  color="#DC2626"
                />


                <Text style={styles.modalSectionTitle}>
                  Kilometraje
                </Text>


                <DetailRow
                  label="Km inicial"
                  value={
                    `${Number(
                      jornadaSeleccionada.kmInicial || 0
                    ).toFixed(0)} km`
                  }
                />


                <DetailRow
                  label="Km final"
                  value={
                    `${Number(
                      jornadaSeleccionada.kmFinal || 0
                    ).toFixed(0)} km`
                  }
                />


                <DetailRow
                  label="Recorrido"
                  value={
                    `${Number(
                      jornadaSeleccionada.kilometrosRecorridos || 0
                    ).toFixed(0)} km`
                  }
                />


                <DetailRow
                  label="Rendimiento"
                  value={
                    `${Number(
                      jornadaSeleccionada.rendimiento || 0
                    ).toFixed(2)} km/L`
                  }
                  color="#7C3AED"
                />


                <View style={styles.resultCard}>

                  <Text style={styles.resultLabel}>
                    Ganancia de la jornada
                  </Text>


                  <Text
                    style={[
                      styles.resultValue,

                      {
                        color:
                          Number(
                            jornadaSeleccionada.ganancia || 0
                          ) >= 0
                            ? '#16A34A'
                            : '#DC2626'
                      }
                    ]}
                  >
                    $
                    {Number(
                      jornadaSeleccionada.ganancia || 0
                    ).toFixed(2)}
                  </Text>

                </View>


                <TouchableOpacity
                  style={styles.deleteButton}
                  onPress={() =>
                    confirmarEliminar(
                      jornadaSeleccionada
                    )
                  }
                >

                  <Ionicons
                    name="trash-outline"
                    size={20}
                    color="#DC2626"
                  />

                  <Text style={styles.deleteButtonText}>
                    Eliminar jornada
                  </Text>

                </TouchableOpacity>


                <TouchableOpacity
                  style={styles.doneButton}
                  onPress={() =>
                    setJornadaSeleccionada(
                      null
                    )
                  }
                >

                  <Text style={styles.doneButtonText}>
                    Cerrar
                  </Text>

                </TouchableOpacity>

              </ScrollView>

            )}

          </View>

        </View>

      </Modal>

    </ScrollView>
  );
}


// =====================================================
// FILA DETALLE
// =====================================================

function DetailRow({
  label,
  value,
  color = '#0F172A'
}) {

  return (

    <View style={styles.detailRow}>

      <Text style={styles.detailLabel}>
        {label}
      </Text>

      <Text
        style={[
          styles.detailValue,
          {
            color: color
          }
        ]}
      >
        {value}
      </Text>

    </View>

  );
}


// =====================================================
// ESTILOS
// =====================================================

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
    color: '#0F172A',
    fontSize: 30,
    fontWeight: 'bold'
  },

  subtitle: {
    color: '#64748B',
    fontSize: 15,
    marginTop: 5,
    marginBottom: 10
  },

  sectionTitle: {
    color: '#0F172A',
    fontSize: 19,
    fontWeight: 'bold',
    marginTop: 25,
    marginBottom: 12
  },


  // SELECTOR

  unitSelector: {
    paddingRight: 20
  },

  unitButton: {
    backgroundColor: '#FFFFFF',
    borderRadius: 22,
    paddingHorizontal: 14,
    paddingVertical: 10,
    flexDirection: 'row',
    alignItems: 'center',
    marginRight: 8,
    borderWidth: 1,
    borderColor: '#E2E8F0'
  },

  unitButtonActive: {
    backgroundColor: '#2563EB',
    borderColor: '#2563EB'
  },

  unitButtonText: {
    color: '#475569',
    fontWeight: '600',
    marginLeft: 6
  },

  unitButtonTextActive: {
    color: '#FFFFFF'
  },


  // SELECCIÓN

  selectedCard: {
    backgroundColor: '#FFFFFF',
    borderRadius: 18,
    padding: 15,
    marginTop: 15,
    flexDirection: 'row',
    alignItems: 'center',
    elevation: 2
  },

  selectedIcon: {
    width: 51,
    height: 51,
    borderRadius: 15,
    backgroundColor: '#0F172A',
    alignItems: 'center',
    justifyContent: 'center'
  },

  selectedInfo: {
    flex: 1,
    marginLeft: 12
  },

  selectedLabel: {
    color: '#94A3B8',
    fontSize: 9,
    fontWeight: 'bold'
  },

  selectedTitle: {
    color: '#0F172A',
    fontSize: 17,
    fontWeight: 'bold',
    marginTop: 2
  },

  selectedRoute: {
    color: '#64748B',
    fontSize: 11,
    marginTop: 2
  },

  countBadge: {
    alignItems: 'center'
  },

  countNumber: {
    color: '#2563EB',
    fontWeight: 'bold',
    fontSize: 20
  },

  countLabel: {
    color: '#94A3B8',
    fontSize: 9
  },


  // RESUMEN

  summaryGrid: {
    flexDirection: 'row',
    marginBottom: 10
  },

  summaryCard: {
    flex: 1,
    backgroundColor: '#FFFFFF',
    borderRadius: 17,
    padding: 15,
    elevation: 2
  },

  cardMargin: {
    marginRight: 10
  },

  summaryIcon: {
    width: 40,
    height: 40,
    borderRadius: 12,
    alignItems: 'center',
    justifyContent: 'center'
  },

  summaryLabel: {
    color: '#64748B',
    fontSize: 12,
    marginTop: 10
  },

  incomeValue: {
    color: '#16A34A',
    fontSize: 18,
    fontWeight: 'bold',
    marginTop: 2
  },

  expenseValue: {
    color: '#D97706',
    fontSize: 18,
    fontWeight: 'bold',
    marginTop: 2
  },

  normalValue: {
    color: '#0F172A',
    fontSize: 18,
    fontWeight: 'bold',
    marginTop: 2
  },


  // JORNADAS

  jornadaCard: {
    backgroundColor: '#FFFFFF',
    borderRadius: 18,
    padding: 16,
    marginBottom: 12,
    elevation: 2
  },

  jornadaHeader: {
    flexDirection: 'row',
    alignItems: 'center'
  },

  jornadaBusIcon: {
    width: 46,
    height: 46,
    borderRadius: 14,
    backgroundColor: '#DBEAFE',
    alignItems: 'center',
    justifyContent: 'center'
  },

  jornadaMainInfo: {
    flex: 1,
    marginLeft: 11
  },

  jornadaUnit: {
    color: '#0F172A',
    fontSize: 16,
    fontWeight: 'bold'
  },

  jornadaRoute: {
    color: '#64748B',
    fontSize: 11,
    marginTop: 2
  },

  jornadaDivider: {
    height: 1,
    backgroundColor: '#E2E8F0',
    marginVertical: 13
  },

  dateRow: {
    flexDirection: 'row',
    alignItems: 'center'
  },

  dateText: {
    color: '#475569',
    fontSize: 12,
    marginLeft: 6,
    textTransform: 'capitalize'
  },

  timeText: {
    color: '#94A3B8',
    fontSize: 11,
    marginLeft: 'auto'
  },

  jornadaNumbers: {
    flexDirection: 'row',
    marginTop: 14
  },

  numberItem: {
    flex: 1
  },

  numberLabel: {
    color: '#94A3B8',
    fontSize: 10
  },

  numberIncome: {
    color: '#16A34A',
    fontWeight: 'bold',
    marginTop: 3
  },

  numberValue: {
    color: '#0F172A',
    fontWeight: 'bold',
    marginTop: 3
  },


  // VACÍO

  emptyCard: {
    backgroundColor: '#FFFFFF',
    borderRadius: 18,
    padding: 30,
    alignItems: 'center'
  },

  emptyIcon: {
    width: 85,
    height: 85,
    borderRadius: 43,
    backgroundColor: '#F1F5F9',
    alignItems: 'center',
    justifyContent: 'center'
  },

  emptyTitle: {
    color: '#334155',
    fontSize: 17,
    fontWeight: 'bold',
    marginTop: 12
  },

  emptyText: {
    color: '#94A3B8',
    textAlign: 'center',
    fontSize: 12,
    lineHeight: 18,
    marginTop: 5
  },


  // MODAL

  modalOverlay: {
    flex: 1,
    backgroundColor: 'rgba(15,23,42,0.72)',
    justifyContent: 'flex-end'
  },

  modalContainer: {
    backgroundColor: '#F8FAFC',
    borderTopLeftRadius: 27,
    borderTopRightRadius: 27,
    padding: 20,
    maxHeight: '92%'
  },

  modalHeader: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'center'
  },

  modalTitle: {
    color: '#0F172A',
    fontSize: 24,
    fontWeight: 'bold'
  },

  modalSubtitle: {
    color: '#64748B',
    fontSize: 13,
    marginTop: 2
  },

  closeButton: {
    width: 42,
    height: 42,
    borderRadius: 21,
    backgroundColor: '#E2E8F0',
    alignItems: 'center',
    justifyContent: 'center'
  },

  modalUnitCard: {
    backgroundColor: '#0F172A',
    borderRadius: 17,
    padding: 15,
    flexDirection: 'row',
    alignItems: 'center',
    marginTop: 18,
    marginBottom: 15
  },

  modalUnitIcon: {
    width: 50,
    height: 50,
    borderRadius: 15,
    backgroundColor: '#2563EB',
    alignItems: 'center',
    justifyContent: 'center'
  },

  modalUnitTitle: {
    color: '#FFFFFF',
    fontSize: 17,
    fontWeight: 'bold',
    marginLeft: 12
  },

  modalUnitRoute: {
    color: '#94A3B8',
    fontSize: 11,
    marginLeft: 12,
    marginTop: 2
  },

  modalSectionTitle: {
    color: '#0F172A',
    fontSize: 17,
    fontWeight: 'bold',
    marginTop: 20,
    marginBottom: 7
  },

  detailRow: {
    backgroundColor: '#FFFFFF',
    paddingHorizontal: 14,
    paddingVertical: 12,
    flexDirection: 'row',
    justifyContent: 'space-between',
    borderBottomWidth: 1,
    borderBottomColor: '#F1F5F9'
  },

  detailLabel: {
    color: '#64748B',
    fontSize: 13
  },

  detailValue: {
    fontWeight: 'bold',
    fontSize: 13,
    maxWidth: '55%',
    textAlign: 'right'
  },

  resultCard: {
    backgroundColor: '#ECFDF5',
    borderRadius: 16,
    padding: 17,
    marginTop: 18,
    alignItems: 'center'
  },

  resultLabel: {
    color: '#166534',
    fontSize: 13,
    fontWeight: '600'
  },

  resultValue: {
    fontSize: 28,
    fontWeight: 'bold',
    marginTop: 3
  },

  deleteButton: {
    backgroundColor: '#FEF2F2',
    borderWidth: 1,
    borderColor: '#FECACA',
    borderRadius: 14,
    paddingVertical: 13,
    flexDirection: 'row',
    justifyContent: 'center',
    alignItems: 'center',
    marginTop: 18
  },

  deleteButtonText: {
    color: '#DC2626',
    fontWeight: 'bold',
    marginLeft: 7
  },

  doneButton: {
    backgroundColor: '#2563EB',
    borderRadius: 14,
    paddingVertical: 14,
    alignItems: 'center',
    marginTop: 10,
    marginBottom: 15
  },

  doneButtonText: {
    color: '#FFFFFF',
    fontWeight: 'bold'
  }

});