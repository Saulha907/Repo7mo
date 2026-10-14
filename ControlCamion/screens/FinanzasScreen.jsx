import React, {
  useCallback,
  useState
} from 'react';

import {
  View,
  Text,
  StyleSheet,
  ScrollView,
  TouchableOpacity
} from 'react-native';

import { Ionicons } from '@expo/vector-icons';

import {
  useFocusEffect
} from '@react-navigation/native';

import {
  obtenerJornadasStorage,
  obtenerGastosStorage,
  obtenerRetirosStorage,
  obtenerUnidadesStorage
} from '../storage/storage';


export default function FinanzasScreen() {

  const [jornadas, setJornadas] =
    useState([]);

  const [gastos, setGastos] =
    useState([]);

  const [retiros, setRetiros] =
    useState([]);

  const [unidades, setUnidades] =
    useState([]);

  const [unidadFiltro, setUnidadFiltro] =
    useState('TODAS');

  const [periodo, setPeriodo] =
    useState('Mes');


  const cargarDatos = async () => {

    const jornadasGuardadas =
      await obtenerJornadasStorage();

    const gastosGuardados =
      await obtenerGastosStorage();

    const retirosGuardados =
      await obtenerRetirosStorage();

    const unidadesGuardadas =
      await obtenerUnidadesStorage();


    setJornadas(jornadasGuardadas);
    setGastos(gastosGuardados);
    setRetiros(retirosGuardados);
    setUnidades(unidadesGuardadas);
  };


  useFocusEffect(

    useCallback(() => {

      cargarDatos();

    }, [])

  );


  // ==========================================
  // FECHAS
  // ==========================================

  const esMismoDia = (
    fecha1,
    fecha2
  ) => {

    return (
      fecha1.getDate() === fecha2.getDate() &&
      fecha1.getMonth() === fecha2.getMonth() &&
      fecha1.getFullYear() === fecha2.getFullYear()
    );
  };


  const inicioSemana = (fecha) => {

    const resultado =
      new Date(fecha);

    const dia =
      resultado.getDay();

    const diferencia =
      dia === 0
        ? -6
        : 1 - dia;

    resultado.setDate(
      resultado.getDate() +
      diferencia
    );

    resultado.setHours(
      0,
      0,
      0,
      0
    );

    return resultado;
  };


  const pertenecePeriodo = (fechaISO) => {

    if (!fechaISO) {
      return false;
    }

    const fecha =
      new Date(fechaISO);

    const hoy =
      new Date();


    if (periodo === 'Hoy') {

      return esMismoDia(
        fecha,
        hoy
      );
    }


    if (periodo === 'Semana') {

      const inicio =
        inicioSemana(hoy);

      const fin =
        new Date(inicio);

      fin.setDate(
        fin.getDate() + 7
      );

      return (
        fecha >= inicio &&
        fecha < fin
      );
    }


    if (periodo === 'Mes') {

      return (
        fecha.getMonth() ===
          hoy.getMonth() &&

        fecha.getFullYear() ===
          hoy.getFullYear()
      );
    }


    if (periodo === 'Año') {

      return (
        fecha.getFullYear() ===
        hoy.getFullYear()
      );
    }


    return true;
  };


  // ==========================================
  // FILTRADO POR UNIDAD
  // ==========================================

  const perteneceUnidad = (registro) => {

    if (
      unidadFiltro === 'TODAS'
    ) {
      return true;
    }


    if (
      registro.unidadId ===
      unidadFiltro
    ) {
      return true;
    }


    /*
      Compatibilidad con jornadas antiguas
      que todavía no tenían unidadId.
    */

    const unidadSeleccionada =
      unidades.find(
        (unidad) =>
          unidad.id ===
          unidadFiltro
      );


    if (
      !registro.unidadId &&
      unidadSeleccionada &&
      registro.numeroUnidad ===
        unidadSeleccionada.numeroUnidad
    ) {
      return true;
    }


    return false;
  };


  const jornadasFiltradas =
    jornadas.filter(
      (jornada) =>
        pertenecePeriodo(
          jornada.fecha
        ) &&
        perteneceUnidad(
          jornada
        )
    );


  const gastosFiltrados =
    gastos.filter(
      (gasto) =>
        pertenecePeriodo(
          gasto.fecha
        ) &&
        perteneceUnidad(
          gasto
        )
    );


  const retirosFiltrados =
    retiros.filter(
      (retiro) =>
        pertenecePeriodo(
          retiro.fecha
        )
    );


  // ==========================================
  // CÁLCULOS
  // ==========================================

  const ingresos =
    jornadasFiltradas.reduce(
      (total, jornada) =>
        total +
        Number(
          jornada.ingresoTotal || 0
        ),
      0
    );


  const diesel =
    jornadasFiltradas.reduce(
      (total, jornada) =>
        total +
        Number(
          jornada.gastoDiesel || 0
        ),
      0
    );


  const otrosGastos =
    gastosFiltrados.reduce(
      (total, gasto) =>
        total +
        Number(
          gasto.monto || 0
        ),
      0
    );


  const gastosTotales =
    diesel +
    otrosGastos;


  const ganancia =
    ingresos -
    gastosTotales;


  const boletos =
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


  const kilometros =
    jornadasFiltradas.reduce(
      (total, jornada) =>
        total +
        Number(
          jornada.kilometrosRecorridos || 0
        ),
      0
    );


  const litros =
    jornadasFiltradas.reduce(
      (total, jornada) =>
        total +
        Number(
          jornada.litrosDiesel || 0
        ),
      0
    );


  const rendimiento =
    litros > 0
      ? kilometros / litros
      : 0;


  const totalRetiros =
    retirosFiltrados.reduce(
      (total, retiro) =>
        total +
        Number(
          retiro.monto || 0
        ),
      0
    );


  /*
    Los retiros son generales.
    Solo afectan el saldo cuando estamos
    viendo toda la flotilla.
  */

  const saldoMostrado =
    unidadFiltro === 'TODAS'

      ? ganancia -
        totalRetiros

      : ganancia;


  const unidadSeleccionada =
    unidades.find(
      (unidad) =>
        unidad.id ===
        unidadFiltro
    );


  const tituloVista =
    unidadFiltro === 'TODAS'

      ? 'Toda la flotilla'

      : `Unidad ${
          unidadSeleccionada
            ?.numeroUnidad || ''
        }`;


  const periodos = [
    'Hoy',
    'Semana',
    'Mes',
    'Año'
  ];


  return (

    <ScrollView
      style={styles.container}
      contentContainerStyle={styles.content}
      showsVerticalScrollIndicator={false}
    >

      <Text style={styles.title}>
        Finanzas
      </Text>

      <Text style={styles.subtitle}>
        Consulta el rendimiento de toda la flotilla o de cada unidad
      </Text>


      {/* ================================= */}
      {/* SELECTOR DE UNIDAD */}
      {/* ================================= */}

      <Text style={styles.sectionTitle}>
        Mostrar información de
      </Text>


      <ScrollView
        horizontal
        showsHorizontalScrollIndicator={false}
        contentContainerStyle={
          styles.unitSelector
        }
      >

        <TouchableOpacity
          style={[
            styles.unitButton,

            unidadFiltro === 'TODAS' &&
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
              unidadFiltro === 'TODAS'
                ? '#FFFFFF'
                : '#475569'
            }
          />

          <Text
            style={[
              styles.unitButtonText,

              unidadFiltro === 'TODAS' &&
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

                unidadFiltro === unidad.id &&
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
                  unidadFiltro === unidad.id
                    ? '#FFFFFF'
                    : '#475569'
                }
              />

              <Text
                style={[
                  styles.unitButtonText,

                  unidadFiltro === unidad.id &&
                  styles.unitButtonTextActive
                ]}
              >
                Unidad {unidad.numeroUnidad}
              </Text>

            </TouchableOpacity>

          )
        )}

      </ScrollView>


      {/* ================================= */}
      {/* VISTA ACTUAL */}
      {/* ================================= */}

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


        <View>

          <Text style={styles.selectedLabel}>
            CONSULTANDO
          </Text>

          <Text style={styles.selectedTitle}>
            {tituloVista}
          </Text>

          {
            unidadSeleccionada &&
            unidadFiltro !== 'TODAS' && (

              <Text style={styles.selectedRoute}>
                {unidadSeleccionada.ruta}
              </Text>

            )
          }

        </View>

      </View>


      {/* ================================= */}
      {/* PERIODO */}
      {/* ================================= */}

      <Text style={styles.sectionTitle}>
        Periodo
      </Text>


      <View style={styles.periodContainer}>

        {periodos.map(
          (item) => (

            <TouchableOpacity
              key={item}
              style={[
                styles.periodButton,

                periodo === item &&
                styles.periodActive
              ]}
              onPress={() =>
                setPeriodo(item)
              }
            >

              <Text
                style={[
                  styles.periodText,

                  periodo === item &&
                  styles.periodTextActive
                ]}
              >
                {item}
              </Text>

            </TouchableOpacity>

          )
        )}

      </View>


      {/* ================================= */}
      {/* RESUMEN PRINCIPAL */}
      {/* ================================= */}

      <View style={styles.summaryCard}>

        <Text style={styles.summaryLabel}>
          {
            unidadFiltro === 'TODAS'
              ? 'Saldo disponible'
              : 'Ganancia de la unidad'
          }
        </Text>

        <Text
          style={[
            styles.summaryValue,

            {
              color:
                saldoMostrado >= 0
                  ? '#FFFFFF'
                  : '#FCA5A5'
            }
          ]}
        >
          ${saldoMostrado.toFixed(2)}
        </Text>


        <Text style={styles.summaryHelper}>

          {
            unidadFiltro === 'TODAS'

              ? 'Ganancia total menos retiros generales'

              : 'Ingresos menos gastos de esta unidad'
          }

        </Text>

      </View>


      {/* INGRESOS / GASTOS */}

      <View style={styles.moneyRow}>

        <View
          style={[
            styles.moneyCard,
            styles.moneyMargin
          ]}
        >

          <View
            style={[
              styles.moneyIcon,
              {
                backgroundColor: '#DCFCE7'
              }
            ]}
          >

            <Ionicons
              name="arrow-up"
              size={22}
              color="#16A34A"
            />

          </View>

          <Text style={styles.moneyLabel}>
            Ingresos
          </Text>

          <Text style={styles.incomeValue}>
            ${ingresos.toFixed(2)}
          </Text>

        </View>


        <View style={styles.moneyCard}>

          <View
            style={[
              styles.moneyIcon,
              {
                backgroundColor: '#FEE2E2'
              }
            ]}
          >

            <Ionicons
              name="arrow-down"
              size={22}
              color="#DC2626"
            />

          </View>

          <Text style={styles.moneyLabel}>
            Gastos
          </Text>

          <Text style={styles.expenseValue}>
            ${gastosTotales.toFixed(2)}
          </Text>

        </View>

      </View>


      {/* GANANCIA */}

      <View
        style={[
          styles.profitCard,

          {
            backgroundColor:
              ganancia >= 0
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
                  ganancia >= 0
                    ? '#166534'
                    : '#991B1B'
              }
            ]}
          >
            Ganancia operativa
          </Text>

          <Text
            style={[
              styles.profitValue,

              {
                color:
                  ganancia >= 0
                    ? '#16A34A'
                    : '#DC2626'
              }
            ]}
          >
            ${ganancia.toFixed(2)}
          </Text>

        </View>


        <Ionicons
          name={
            ganancia >= 0
              ? 'trending-up'
              : 'trending-down'
          }
          size={40}
          color={
            ganancia >= 0
              ? '#16A34A'
              : '#DC2626'
          }
        />

      </View>


      {/* ================================= */}
      {/* DESGLOSE */}
      {/* ================================= */}

      <Text style={styles.sectionTitle}>
        Desglose
      </Text>


      <View style={styles.detailCard}>

        <DetailRow
          icon="water-outline"
          background="#FEF3C7"
          color="#D97706"
          label="Diésel"
          value={`-$${diesel.toFixed(2)}`}
          valueColor="#DC2626"
        />


        <View style={styles.divider} />


        <DetailRow
          icon="construct-outline"
          background="#FEE2E2"
          color="#DC2626"
          label="Otros gastos"
          value={`-$${otrosGastos.toFixed(2)}`}
          valueColor="#DC2626"
        />


        {
          unidadFiltro === 'TODAS' && (

            <>

              <View style={styles.divider} />

              <DetailRow
                icon="cash-outline"
                background="#F3E8FF"
                color="#9333EA"
                label="Retiros generales"
                value={`-$${totalRetiros.toFixed(2)}`}
                valueColor="#9333EA"
              />

            </>

          )
        }


        <View style={styles.divider} />


        <DetailRow
          icon="ticket-outline"
          background="#DBEAFE"
          color="#2563EB"
          label="Boletos vendidos"
          value={String(boletos)}
          valueColor="#0F172A"
        />


        <View style={styles.divider} />


        <DetailRow
          icon="speedometer-outline"
          background="#EDE9FE"
          color="#7C3AED"
          label="Kilómetros"
          value={`${kilometros.toFixed(0)} km`}
          valueColor="#0F172A"
        />

      </View>


      {/* RENDIMIENTO */}

      <Text style={styles.sectionTitle}>
        Rendimiento
      </Text>


      <View style={styles.performanceCard}>

        <View style={styles.performanceRow}>

          <Text style={styles.performanceLabel}>
            Jornadas
          </Text>

          <Text style={styles.performanceValue}>
            {jornadasFiltradas.length}
          </Text>

        </View>


        <View style={styles.performanceRow}>

          <Text style={styles.performanceLabel}>
            Litros utilizados
          </Text>

          <Text style={styles.performanceValue}>
            {litros.toFixed(2)} L
          </Text>

        </View>


        <View style={styles.performanceRow}>

          <Text style={styles.performanceLabel}>
            Kilómetros recorridos
          </Text>

          <Text style={styles.performanceValue}>
            {kilometros.toFixed(0)} km
          </Text>

        </View>


        <View style={styles.performanceRow}>

          <Text style={styles.performanceLabel}>
            Rendimiento promedio
          </Text>

          <Text style={styles.performanceHighlight}>
            {rendimiento.toFixed(2)} km/L
          </Text>

        </View>

      </View>


      {/* ================================= */}
      {/* RESUMEN POR CAMIÓN */}
      {/* Solo aparece en Toda la flotilla */}
      {/* ================================= */}

      {
        unidadFiltro === 'TODAS' &&
        unidades.length > 0 && (

          <>

            <Text style={styles.sectionTitle}>
              Resumen por unidad
            </Text>


            {unidades.map(
              (unidad) => {

                const jornadasUnidad =
                  jornadas.filter(
                    (jornada) =>

                      pertenecePeriodo(
                        jornada.fecha
                      ) &&

                      (
                        jornada.unidadId ===
                          unidad.id ||

                        (
                          !jornada.unidadId &&
                          jornada.numeroUnidad ===
                            unidad.numeroUnidad
                        )
                      )
                  );


                const gastosUnidad =
                  gastos.filter(
                    (gasto) =>

                      pertenecePeriodo(
                        gasto.fecha
                      ) &&

                      gasto.unidadId ===
                        unidad.id
                  );


                const ingresosUnidad =
                  jornadasUnidad.reduce(
                    (total, jornada) =>
                      total +
                      Number(
                        jornada.ingresoTotal || 0
                      ),
                    0
                  );


                const dieselUnidad =
                  jornadasUnidad.reduce(
                    (total, jornada) =>
                      total +
                      Number(
                        jornada.gastoDiesel || 0
                      ),
                    0
                  );


                const extrasUnidad =
                  gastosUnidad.reduce(
                    (total, gasto) =>
                      total +
                      Number(
                        gasto.monto || 0
                      ),
                    0
                  );


                const gastoUnidad =
                  dieselUnidad +
                  extrasUnidad;


                const gananciaUnidad =
                  ingresosUnidad -
                  gastoUnidad;


                return (

                  <TouchableOpacity
                    key={unidad.id}
                    style={styles.unitSummaryCard}
                    onPress={() =>
                      setUnidadFiltro(
                        unidad.id
                      )
                    }
                  >

                    <View style={styles.unitSummaryHeader}>

                      <View style={styles.unitSummaryIcon}>

                        <Ionicons
                          name="bus"
                          size={25}
                          color="#2563EB"
                        />

                      </View>


                      <View style={styles.unitSummaryInfo}>

                        <Text style={styles.unitSummaryTitle}>
                          Unidad {unidad.numeroUnidad}
                        </Text>

                        <Text style={styles.unitSummaryRoute}>
                          {unidad.ruta}
                        </Text>

                      </View>


                      <Ionicons
                        name="chevron-forward"
                        size={22}
                        color="#94A3B8"
                      />

                    </View>


                    <View style={styles.unitSummaryDivider} />


                    <View style={styles.unitNumbers}>

                      <View>

                        <Text style={styles.unitNumberLabel}>
                          Ingresos
                        </Text>

                        <Text style={styles.unitIncome}>
                          ${ingresosUnidad.toFixed(2)}
                        </Text>

                      </View>


                      <View>

                        <Text style={styles.unitNumberLabel}>
                          Gastos
                        </Text>

                        <Text style={styles.unitExpense}>
                          ${gastoUnidad.toFixed(2)}
                        </Text>

                      </View>


                      <View>

                        <Text style={styles.unitNumberLabel}>
                          Ganancia
                        </Text>

                        <Text
                          style={[
                            styles.unitProfit,

                            {
                              color:
                                gananciaUnidad >= 0
                                  ? '#16A34A'
                                  : '#DC2626'
                            }
                          ]}
                        >
                          ${gananciaUnidad.toFixed(2)}
                        </Text>

                      </View>

                    </View>

                  </TouchableOpacity>

                );
              }
            )}

          </>

        )
      }

    </ScrollView>
  );
}


function DetailRow({
  icon,
  background,
  color,
  label,
  value,
  valueColor
}) {

  return (

    <View style={styles.detailRow}>

      <View style={styles.detailLeft}>

        <View
          style={[
            styles.detailIcon,

            {
              backgroundColor:
                background
            }
          ]}
        >

          <Ionicons
            name={icon}
            size={21}
            color={color}
          />

        </View>


        <Text style={styles.detailLabel}>
          {label}
        </Text>

      </View>


      <Text
        style={[
          styles.detailValue,

          {
            color:
              valueColor
          }
        ]}
      >
        {value}
      </Text>

    </View>

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
    color: '#0F172A',
    fontSize: 30,
    fontWeight: 'bold'
  },

  subtitle: {
    color: '#64748B',
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

  selectedCard: {
    backgroundColor: '#FFFFFF',
    borderRadius: 17,
    padding: 15,
    flexDirection: 'row',
    alignItems: 'center',
    marginTop: 15
  },

  selectedIcon: {
    width: 50,
    height: 50,
    borderRadius: 15,
    backgroundColor: '#0F172A',
    justifyContent: 'center',
    alignItems: 'center',
    marginRight: 12
  },

  selectedLabel: {
    color: '#94A3B8',
    fontSize: 10,
    fontWeight: 'bold'
  },

  selectedTitle: {
    color: '#0F172A',
    fontSize: 17,
    fontWeight: 'bold'
  },

  selectedRoute: {
    color: '#64748B',
    fontSize: 12,
    marginTop: 2
  },

  periodContainer: {
    backgroundColor: '#E2E8F0',
    borderRadius: 14,
    padding: 4,
    flexDirection: 'row'
  },

  periodButton: {
    flex: 1,
    alignItems: 'center',
    paddingVertical: 10,
    borderRadius: 11
  },

  periodActive: {
    backgroundColor: '#FFFFFF'
  },

  periodText: {
    color: '#64748B',
    fontWeight: '600',
    fontSize: 12
  },

  periodTextActive: {
    color: '#2563EB'
  },

  summaryCard: {
    backgroundColor: '#0F172A',
    borderRadius: 20,
    padding: 22,
    marginTop: 18
  },

  summaryLabel: {
    color: '#CBD5E1'
  },

  summaryValue: {
    fontSize: 33,
    fontWeight: 'bold',
    marginTop: 4
  },

  summaryHelper: {
    color: '#64748B',
    fontSize: 12,
    marginTop: 4
  },

  moneyRow: {
    flexDirection: 'row',
    marginTop: 14
  },

  moneyCard: {
    flex: 1,
    backgroundColor: '#FFFFFF',
    borderRadius: 17,
    padding: 16
  },

  moneyMargin: {
    marginRight: 10
  },

  moneyIcon: {
    width: 42,
    height: 42,
    borderRadius: 13,
    alignItems: 'center',
    justifyContent: 'center'
  },

  moneyLabel: {
    color: '#64748B',
    marginTop: 10,
    fontSize: 12
  },

  incomeValue: {
    color: '#16A34A',
    fontWeight: 'bold',
    fontSize: 20
  },

  expenseValue: {
    color: '#DC2626',
    fontWeight: 'bold',
    fontSize: 20
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
    fontWeight: '600'
  },

  profitValue: {
    fontSize: 29,
    fontWeight: 'bold'
  },

  detailCard: {
    backgroundColor: '#FFFFFF',
    borderRadius: 18,
    padding: 17
  },

  detailRow: {
    flexDirection: 'row',
    alignItems: 'center',
    justifyContent: 'space-between'
  },

  detailLeft: {
    flexDirection: 'row',
    alignItems: 'center'
  },

  detailIcon: {
    width: 40,
    height: 40,
    borderRadius: 12,
    justifyContent: 'center',
    alignItems: 'center',
    marginRight: 10
  },

  detailLabel: {
    color: '#334155',
    fontWeight: '600'
  },

  detailValue: {
    fontWeight: 'bold'
  },

  divider: {
    height: 1,
    backgroundColor: '#E2E8F0',
    marginVertical: 11
  },

  performanceCard: {
    backgroundColor: '#FFFFFF',
    borderRadius: 18,
    padding: 18
  },

  performanceRow: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    marginVertical: 7
  },

  performanceLabel: {
    color: '#64748B'
  },

  performanceValue: {
    color: '#0F172A',
    fontWeight: '600'
  },

  performanceHighlight: {
    color: '#7C3AED',
    fontWeight: 'bold'
  },

  unitSummaryCard: {
    backgroundColor: '#FFFFFF',
    borderRadius: 18,
    padding: 16,
    marginBottom: 12
  },

  unitSummaryHeader: {
    flexDirection: 'row',
    alignItems: 'center'
  },

  unitSummaryIcon: {
    width: 47,
    height: 47,
    borderRadius: 14,
    backgroundColor: '#DBEAFE',
    justifyContent: 'center',
    alignItems: 'center'
  },

  unitSummaryInfo: {
    flex: 1,
    marginLeft: 11
  },

  unitSummaryTitle: {
    color: '#0F172A',
    fontWeight: 'bold',
    fontSize: 16
  },

  unitSummaryRoute: {
    color: '#64748B',
    fontSize: 11,
    marginTop: 2
  },

  unitSummaryDivider: {
    height: 1,
    backgroundColor: '#E2E8F0',
    marginVertical: 13
  },

  unitNumbers: {
    flexDirection: 'row',
    justifyContent: 'space-between'
  },

  unitNumberLabel: {
    color: '#94A3B8',
    fontSize: 10
  },

  unitIncome: {
    color: '#16A34A',
    fontWeight: 'bold',
    marginTop: 3
  },

  unitExpense: {
    color: '#DC2626',
    fontWeight: 'bold',
    marginTop: 3
  },

  unitProfit: {
    fontWeight: 'bold',
    marginTop: 3
  }

});