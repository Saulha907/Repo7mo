import React, {
  useCallback,
  useRef,
  useState
} from 'react';

import {
  Text,
  StyleSheet,
  ScrollView,
  TouchableOpacity,
  Animated,
  View
} from 'react-native';

import { Ionicons } from '@expo/vector-icons';

import {
  useFocusEffect
} from '@react-navigation/native';

import {
  obtenerJornadasStorage,
  obtenerGastosStorage,
  obtenerRetirosStorage,
  obtenerUnidadStorage
} from '../storage/storage';


export default function InicioScreen({ navigation }) {

  const [jornadas, setJornadas] =
    useState([]);

  const [gastos, setGastos] =
    useState([]);

  const [retiros, setRetiros] =
    useState([]);

  const [unidad, setUnidad] =
    useState(null);


  const fadeAnim =
    useRef(
      new Animated.Value(0)
    ).current;


  const translateAnim =
    useRef(
      new Animated.Value(25)
    ).current;


  // =====================================================
  // CARGAR DATOS
  // =====================================================

  const cargarDatos = async () => {

    try {

      const jornadasGuardadas =
        await obtenerJornadasStorage();

      const gastosGuardados =
        await obtenerGastosStorage();

      const retirosGuardados =
        await obtenerRetirosStorage();

      const unidadGuardada =
        await obtenerUnidadStorage();


      setJornadas(
        Array.isArray(jornadasGuardadas)
          ? jornadasGuardadas
          : []
      );


      setGastos(
        Array.isArray(gastosGuardados)
          ? gastosGuardados
          : []
      );


      setRetiros(
        Array.isArray(retirosGuardados)
          ? retirosGuardados
          : []
      );


      setUnidad(
        unidadGuardada || null
      );


      fadeAnim.setValue(0);

      translateAnim.setValue(25);


      Animated.parallel([

        Animated.timing(
          fadeAnim,
          {
            toValue: 1,
            duration: 450,
            useNativeDriver: true
          }
        ),

        Animated.timing(
          translateAnim,
          {
            toValue: 0,
            duration: 450,
            useNativeDriver: true
          }
        )

      ]).start();

    } catch (error) {

      console.log(
        'Error cargando Inicio:',
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
  // FECHAS
  // =====================================================

  const esHoy = (fechaISO) => {

    if (!fechaISO) {
      return false;
    }


    const fecha =
      new Date(fechaISO);

    const hoy =
      new Date();


    return (

      fecha.getDate() ===
        hoy.getDate() &&

      fecha.getMonth() ===
        hoy.getMonth() &&

      fecha.getFullYear() ===
        hoy.getFullYear()

    );
  };


  const formatearFecha = (fechaISO) => {

    if (!fechaISO) {
      return 'Sin fecha';
    }


    return new Date(fechaISO)
      .toLocaleDateString(
        'es-MX',
        {
          day: '2-digit',
          month: 'short',
          year: 'numeric'
        }
      );
  };


  // =====================================================
  // MOVIMIENTOS DE HOY
  // =====================================================

  const jornadasHoy =
    jornadas.filter(
      (jornada) =>
        esHoy(jornada.fecha)
    );


  const gastosHoy =
    gastos.filter(
      (gasto) =>
        esHoy(gasto.fecha)
    );


  const retirosHoy =
    retiros.filter(
      (retiro) =>
        esHoy(retiro.fecha)
    );


  // =====================================================
  // INGRESOS HOY
  // =====================================================

  const ingresosHoy =
    jornadasHoy.reduce(
      (total, jornada) => {

        return (
          total +
          Number(
            jornada.ingresoTotal || 0
          )
        );

      },
      0
    );


  // =====================================================
  // DIÉSEL HOY
  // =====================================================

  const dieselHoy =
    jornadasHoy.reduce(
      (total, jornada) => {

        return (
          total +
          Number(
            jornada.gastoDiesel || 0
          )
        );

      },
      0
    );


  // =====================================================
  // OTROS GASTOS HOY
  // =====================================================

  const otrosGastosHoy =
    gastosHoy.reduce(
      (total, gasto) => {

        return (
          total +
          Number(
            gasto.monto || 0
          )
        );

      },
      0
    );


  const gastosTotalesHoy =
    dieselHoy +
    otrosGastosHoy;


  // =====================================================
  // GANANCIA HOY
  // =====================================================

  const gananciaHoy =
    ingresosHoy -
    gastosTotalesHoy;


  // =====================================================
  // RETIROS HOY
  // =====================================================

  const retirosTotalesHoy =
    retirosHoy.reduce(
      (total, retiro) => {

        return (
          total +
          Number(
            retiro.monto || 0
          )
        );

      },
      0
    );


  // =====================================================
  // SALDO HISTÓRICO
  // =====================================================

  const ingresosTotales =
    jornadas.reduce(
      (total, jornada) => {

        return (
          total +
          Number(
            jornada.ingresoTotal || 0
          )
        );

      },
      0
    );


  const dieselTotal =
    jornadas.reduce(
      (total, jornada) => {

        return (
          total +
          Number(
            jornada.gastoDiesel || 0
          )
        );

      },
      0
    );


  const otrosGastosTotal =
    gastos.reduce(
      (total, gasto) => {

        return (
          total +
          Number(
            gasto.monto || 0
          )
        );

      },
      0
    );


  const retirosTotales =
    retiros.reduce(
      (total, retiro) => {

        return (
          total +
          Number(
            retiro.monto || 0
          )
        );

      },
      0
    );


  const saldoDisponible =
    ingresosTotales -
    dieselTotal -
    otrosGastosTotal -
    retirosTotales;


  // =====================================================
  // BOLETOS
  // =====================================================

  const boletosEstudianteHoy =
    jornadasHoy.reduce(
      (total, jornada) => {

        return (
          total +
          Number(
            jornada.boletosEstudiante || 0
          )
        );

      },
      0
    );


  const boletosEnterosHoy =
    jornadasHoy.reduce(
      (total, jornada) => {

        return (
          total +
          Number(
            jornada.boletosEnteros || 0
          )
        );

      },
      0
    );


  const boletosHoy =
    boletosEstudianteHoy +
    boletosEnterosHoy;


  // =====================================================
  // ÚLTIMA JORNADA
  // =====================================================

  const jornadasOrdenadas =
    [...jornadas].sort(
      (a, b) => {

        return (
          new Date(b.fecha) -
          new Date(a.fecha)
        );

      }
    );


  const ultimaJornada =
    jornadasOrdenadas.length > 0
      ? jornadasOrdenadas[0]
      : null;


  return (

    <ScrollView
      style={styles.container}
      contentContainerStyle={
        styles.content
      }
      showsVerticalScrollIndicator={false}
    >

      <Animated.View
        style={{
          opacity: fadeAnim,

          transform: [
            {
              translateY:
                translateAnim
            }
          ]
        }}
      >

        <Text style={styles.welcome}>
          Bienvenido 👋
        </Text>

        <Text style={styles.subtitle}>
          Resumen general de tu unidad
        </Text>


        {/* MI UNIDAD */}

        <TouchableOpacity
          style={styles.unitCard}
          activeOpacity={0.85}
          onPress={() =>
            navigation.navigate(
              'Unidad'
            )
          }
        >

          <View style={styles.unitIcon}>

            <Ionicons
              name="bus-outline"
              size={29}
              color="#FFFFFF"
            />

          </View>


          <View style={styles.unitInfo}>

            <Text style={styles.unitLabel}>
              Mi unidad
            </Text>


            <Text style={styles.unitName}>

              {
                unidad
                  ? `Unidad ${unidad.numeroUnidad || ''}`
                  : 'Sin configurar'
              }

            </Text>


            <Text style={styles.unitRoute}>

              {
                unidad
                  ? unidad.ruta || 'Sin ruta'
                  : 'Toca aquí para configurar'
              }

            </Text>

          </View>


          <Ionicons
            name="chevron-forward"
            size={23}
            color="#94A3B8"
          />

        </TouchableOpacity>


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


            <Text style={styles.balanceHelper}>
              Después de gastos y retiros
            </Text>

          </View>


          <View style={styles.balanceIcon}>

            <Ionicons
              name="wallet-outline"
              size={31}
              color="#FFFFFF"
            />

          </View>

        </View>


        {/* RESUMEN HOY */}

        <Text style={styles.sectionTitle}>
          Resumen de hoy
        </Text>


        <View style={styles.cardsRow}>

          <View
            style={[
              styles.smallCard,
              styles.cardRightMargin
            ]}
          >

            <View
              style={[
                styles.smallIcon,
                {
                  backgroundColor:
                    '#DCFCE7'
                }
              ]}
            >

              <Ionicons
                name="arrow-up-outline"
                size={22}
                color="#16A34A"
              />

            </View>


            <Text style={styles.smallLabel}>
              Ingresos
            </Text>


            <Text
              style={[
                styles.smallValue,
                {
                  color: '#16A34A'
                }
              ]}
            >
              ${ingresosHoy.toFixed(2)}
            </Text>

          </View>


          <View style={styles.smallCard}>

            <View
              style={[
                styles.smallIcon,
                {
                  backgroundColor:
                    '#FEE2E2'
                }
              ]}
            >

              <Ionicons
                name="arrow-down-outline"
                size={22}
                color="#DC2626"
              />

            </View>


            <Text style={styles.smallLabel}>
              Gastos
            </Text>


            <Text
              style={[
                styles.smallValue,
                {
                  color: '#DC2626'
                }
              ]}
            >
              ${gastosTotalesHoy.toFixed(2)}
            </Text>

          </View>

        </View>


        {/* GANANCIA HOY */}

        <View
          style={[
            styles.profitCard,

            {
              backgroundColor:
                gananciaHoy >= 0
                  ? '#ECFDF5'
                  : '#FEF2F2'
            }
          ]}
        >

          <View>

            <Text
              style={[
                styles.profitLabel,

                {
                  color:
                    gananciaHoy >= 0
                      ? '#166534'
                      : '#991B1B'
                }
              ]}
            >
              Ganancia del día
            </Text>


            <Text
              style={[
                styles.profitValue,

                {
                  color:
                    gananciaHoy >= 0
                      ? '#16A34A'
                      : '#DC2626'
                }
              ]}
            >
              ${gananciaHoy.toFixed(2)}
            </Text>

          </View>


          <Ionicons
            name={
              gananciaHoy >= 0
                ? 'trending-up-outline'
                : 'trending-down-outline'
            }
            size={43}
            color={
              gananciaHoy >= 0
                ? '#16A34A'
                : '#DC2626'
            }
          />

        </View>


        {/* ESTADÍSTICAS */}

        <View style={styles.statsCard}>

          <View style={styles.statItem}>

            <Ionicons
              name="ticket-outline"
              size={25}
              color="#2563EB"
            />

            <Text style={styles.statValue}>
              {boletosHoy}
            </Text>

            <Text style={styles.statLabel}>
              Boletos hoy
            </Text>

          </View>


          <View style={styles.statDivider} />


          <View style={styles.statItem}>

            <Ionicons
              name="cash-outline"
              size={25}
              color="#7C3AED"
            />

            <Text style={styles.statValue}>
              ${retirosTotalesHoy.toFixed(0)}
            </Text>

            <Text style={styles.statLabel}>
              Retiros hoy
            </Text>

          </View>


          <View style={styles.statDivider} />


          <View style={styles.statItem}>

            <Ionicons
              name="calendar-outline"
              size={25}
              color="#D97706"
            />

            <Text style={styles.statValue}>
              {jornadasHoy.length}
            </Text>

            <Text style={styles.statLabel}>
              Jornadas
            </Text>

          </View>

        </View>


        {/* ACCESOS */}

        <Text style={styles.sectionTitle}>
          Accesos rápidos
        </Text>


        <View style={styles.actionsGrid}>

          <TouchableOpacity
            style={styles.actionCard}
            onPress={() =>
              navigation.navigate(
                'Jornada'
              )
            }
          >

            <View
              style={[
                styles.actionIcon,
                {
                  backgroundColor:
                    '#DBEAFE'
                }
              ]}
            >

              <Ionicons
                name="ticket-outline"
                size={27}
                color="#2563EB"
              />

            </View>

            <Text style={styles.actionTitle}>
              Jornada
            </Text>

            <Text style={styles.actionSubtitle}>
              Registrar operación
            </Text>

          </TouchableOpacity>


          <TouchableOpacity
            style={styles.actionCard}
            onPress={() =>
              navigation.navigate(
                'Gastos'
              )
            }
          >

            <View
              style={[
                styles.actionIcon,
                {
                  backgroundColor:
                    '#FEE2E2'
                }
              ]}
            >

              <Ionicons
                name="receipt-outline"
                size={27}
                color="#DC2626"
              />

            </View>

            <Text style={styles.actionTitle}>
              Gastos
            </Text>

            <Text style={styles.actionSubtitle}>
              Registrar gasto
            </Text>

          </TouchableOpacity>


          <TouchableOpacity
            style={styles.actionCard}
            onPress={() =>
              navigation.navigate(
                'Retiros'
              )
            }
          >

            <View
              style={[
                styles.actionIcon,
                {
                  backgroundColor:
                    '#F3E8FF'
                }
              ]}
            >

              <Ionicons
                name="cash-outline"
                size={27}
                color="#9333EA"
              />

            </View>

            <Text style={styles.actionTitle}>
              Retiros
            </Text>

            <Text style={styles.actionSubtitle}>
              Retirar efectivo
            </Text>

          </TouchableOpacity>


          <TouchableOpacity
            style={styles.actionCard}
            onPress={() =>
              navigation.navigate(
                'Finanzas'
              )
            }
          >

            <View
              style={[
                styles.actionIcon,
                {
                  backgroundColor:
                    '#DCFCE7'
                }
              ]}
            >

              <Ionicons
                name="bar-chart-outline"
                size={27}
                color="#16A34A"
              />

            </View>

            <Text style={styles.actionTitle}>
              Finanzas
            </Text>

            <Text style={styles.actionSubtitle}>
              Ver estadísticas
            </Text>

          </TouchableOpacity>

        </View>


        {/* ÚLTIMA JORNADA */}

        <Text style={styles.sectionTitle}>
          Última jornada
        </Text>


        {
          ultimaJornada
            ? (

              <TouchableOpacity
                style={styles.lastCard}
                onPress={() =>
                  navigation.navigate(
                    'Historial'
                  )
                }
              >

                <View style={styles.lastTop}>

                  <View>

                    <Text style={styles.lastDate}>
                      {
                        formatearFecha(
                          ultimaJornada.fecha
                        )
                      }
                    </Text>


                    <Text style={styles.lastRoute}>
                      {
                        ultimaJornada.ruta ||
                        unidad?.ruta ||
                        'Sin ruta'
                      }
                    </Text>

                  </View>


                  <Ionicons
                    name="chevron-forward"
                    size={24}
                    color="#94A3B8"
                  />

                </View>


                <View style={styles.lastDivider} />


                <View style={styles.lastValues}>

                  <View>

                    <Text style={styles.lastLabel}>
                      Ingreso
                    </Text>

                    <Text style={styles.lastIncome}>
                      $
                      {
                        Number(
                          ultimaJornada.ingresoTotal || 0
                        ).toFixed(2)
                      }
                    </Text>

                  </View>


                  <View>

                    <Text style={styles.lastLabel}>
                      Diésel
                    </Text>

                    <Text style={styles.lastExpense}>
                      $
                      {
                        Number(
                          ultimaJornada.gastoDiesel || 0
                        ).toFixed(2)
                      }
                    </Text>

                  </View>


                  <View>

                    <Text style={styles.lastLabel}>
                      Ganancia
                    </Text>

                    <Text
                      style={[
                        styles.lastProfit,

                        {
                          color:
                            Number(
                              ultimaJornada.ganancia || 0
                            ) >= 0
                              ? '#16A34A'
                              : '#DC2626'
                        }
                      ]}
                    >
                      $
                      {
                        Number(
                          ultimaJornada.ganancia || 0
                        ).toFixed(2)
                      }
                    </Text>

                  </View>

                </View>

              </TouchableOpacity>

            )
            : (

              <View style={styles.emptyCard}>

                <Ionicons
                  name="document-text-outline"
                  size={42}
                  color="#94A3B8"
                />

                <Text style={styles.emptyTitle}>
                  Aún no hay jornadas
                </Text>

                <Text style={styles.emptyText}>
                  Registra tu primera jornada para comenzar.
                </Text>

              </View>

            )
        }

      </Animated.View>

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

  welcome: {
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

  unitCard: {
    backgroundColor: '#FFFFFF',
    borderRadius: 18,
    padding: 15,
    flexDirection: 'row',
    alignItems: 'center',
    elevation: 3,
    marginBottom: 15
  },

  unitIcon: {
    width: 52,
    height: 52,
    borderRadius: 16,
    backgroundColor: '#2563EB',
    justifyContent: 'center',
    alignItems: 'center'
  },

  unitInfo: {
    flex: 1,
    marginLeft: 13
  },

  unitLabel: {
    color: '#94A3B8',
    fontSize: 11
  },

  unitName: {
    color: '#0F172A',
    fontSize: 17,
    fontWeight: 'bold',
    marginTop: 2
  },

  unitRoute: {
    color: '#64748B',
    fontSize: 12,
    marginTop: 2
  },

  balanceCard: {
    backgroundColor: '#0F172A',
    borderRadius: 20,
    padding: 22,
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'center',
    elevation: 5
  },

  balanceLabel: {
    color: '#CBD5E1',
    fontSize: 14
  },

  balanceValue: {
    fontSize: 33,
    fontWeight: 'bold',
    marginTop: 4
  },

  balanceHelper: {
    color: '#64748B',
    fontSize: 12,
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

  sectionTitle: {
    fontSize: 20,
    fontWeight: 'bold',
    color: '#0F172A',
    marginTop: 26,
    marginBottom: 13
  },

  cardsRow: {
    flexDirection: 'row'
  },

  smallCard: {
    flex: 1,
    backgroundColor: '#FFFFFF',
    borderRadius: 17,
    padding: 16,
    elevation: 2
  },

  cardRightMargin: {
    marginRight: 10
  },

  smallIcon: {
    width: 42,
    height: 42,
    borderRadius: 13,
    justifyContent: 'center',
    alignItems: 'center'
  },

  smallLabel: {
    color: '#64748B',
    fontSize: 13,
    marginTop: 11
  },

  smallValue: {
    fontSize: 20,
    fontWeight: 'bold',
    marginTop: 3
  },

  profitCard: {
    borderRadius: 18,
    padding: 19,
    marginTop: 14,
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'center'
  },

  profitLabel: {
    fontSize: 14,
    fontWeight: '600'
  },

  profitValue: {
    fontSize: 29,
    fontWeight: 'bold',
    marginTop: 4
  },

  statsCard: {
    backgroundColor: '#FFFFFF',
    borderRadius: 18,
    paddingVertical: 18,
    flexDirection: 'row',
    marginTop: 14,
    elevation: 2
  },

  statItem: {
    flex: 1,
    alignItems: 'center'
  },

  statDivider: {
    width: 1,
    backgroundColor: '#E2E8F0'
  },

  statValue: {
    color: '#0F172A',
    fontWeight: 'bold',
    fontSize: 19,
    marginTop: 6
  },

  statLabel: {
    color: '#94A3B8',
    fontSize: 11,
    marginTop: 2,
    textAlign: 'center'
  },

  actionsGrid: {
    flexDirection: 'row',
    flexWrap: 'wrap',
    justifyContent: 'space-between'
  },

  actionCard: {
    width: '48%',
    backgroundColor: '#FFFFFF',
    borderRadius: 17,
    padding: 16,
    marginBottom: 12,
    elevation: 2
  },

  actionIcon: {
    width: 48,
    height: 48,
    borderRadius: 14,
    justifyContent: 'center',
    alignItems: 'center'
  },

  actionTitle: {
    color: '#0F172A',
    fontWeight: 'bold',
    fontSize: 16,
    marginTop: 11
  },

  actionSubtitle: {
    color: '#94A3B8',
    fontSize: 11,
    marginTop: 3
  },

  lastCard: {
    backgroundColor: '#FFFFFF',
    borderRadius: 18,
    padding: 17,
    elevation: 3
  },

  lastTop: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'center'
  },

  lastDate: {
    color: '#0F172A',
    fontSize: 16,
    fontWeight: 'bold'
  },

  lastRoute: {
    color: '#64748B',
    fontSize: 12,
    marginTop: 3
  },

  lastDivider: {
    height: 1,
    backgroundColor: '#E2E8F0',
    marginVertical: 14
  },

  lastValues: {
    flexDirection: 'row',
    justifyContent: 'space-between'
  },

  lastLabel: {
    color: '#94A3B8',
    fontSize: 11
  },

  lastIncome: {
    color: '#16A34A',
    fontWeight: 'bold',
    marginTop: 3
  },

  lastExpense: {
    color: '#DC2626',
    fontWeight: 'bold',
    marginTop: 3
  },

  lastProfit: {
    fontWeight: 'bold',
    marginTop: 3
  },

  emptyCard: {
    backgroundColor: '#FFFFFF',
    borderRadius: 18,
    padding: 28,
    alignItems: 'center'
  },

  emptyTitle: {
    color: '#334155',
    fontSize: 17,
    fontWeight: 'bold',
    marginTop: 10
  },

  emptyText: {
    color: '#94A3B8',
    fontSize: 12,
    textAlign: 'center',
    lineHeight: 18,
    marginTop: 5
  }

});