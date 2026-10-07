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
  Alert,
  Platform
} from 'react-native';

import { Ionicons } from '@expo/vector-icons';

import DateTimePicker from '@react-native-community/datetimepicker';

import {
  useFocusEffect
} from '@react-navigation/native';

import {
  guardarJornadaStorage,
  obtenerUnidadesStorage,
  obtenerUnidadStorage,
  guardarUnidadStorage,
  establecerUnidadActivaStorage
} from '../storage/storage';


export default function JornadaScreen() {

  const [unidades, setUnidades] =
    useState([]);

  const [unidad, setUnidad] =
    useState(null);

  const [selectorVisible, setSelectorVisible] =
    useState(false);

  const [modalVisible, setModalVisible] =
    useState(false);


  // =====================================================
  // FECHA
  // =====================================================

  const [fechaJornada, setFechaJornada] =
    useState(new Date());

  const [mostrarFecha, setMostrarFecha] =
    useState(false);


  // =====================================================
  // FORMULARIO
  // =====================================================

  const [boletosEstudiante, setBoletosEstudiante] =
    useState('');

  const [boletosEnteros, setBoletosEnteros] =
    useState('');

  const [litrosDiesel, setLitrosDiesel] =
    useState('');

  const [precioDiesel, setPrecioDiesel] =
    useState('');

  const [kmInicial, setKmInicial] =
    useState('');

  const [kmFinal, setKmFinal] =
    useState('');

  const [tarifaEstudiante, setTarifaEstudiante] =
    useState(7);

  const [tarifaEntero, setTarifaEntero] =
    useState(9);


  // =====================================================
  // CARGAR UNIDADES
  // =====================================================

  const cargarDatos = async () => {

    const unidadesGuardadas =
      await obtenerUnidadesStorage();

    const unidadActiva =
      await obtenerUnidadStorage();


    setUnidades(
      Array.isArray(unidadesGuardadas)
        ? unidadesGuardadas
        : []
    );


    if (unidadActiva) {

      cargarUnidadEnFormulario(
        unidadActiva
      );

    } else {

      setUnidad(null);
    }
  };


  useFocusEffect(

    useCallback(() => {

      cargarDatos();

    }, [])

  );


  // =====================================================
  // CARGAR DATOS DE UNA UNIDAD
  // =====================================================

  const cargarUnidadEnFormulario = (
    unidadSeleccionada
  ) => {

    setUnidad(
      unidadSeleccionada
    );


    setTarifaEstudiante(
      Number(
        unidadSeleccionada.tarifaEstudiante || 7
      )
    );


    setTarifaEntero(
      Number(
        unidadSeleccionada.tarifaEntero || 9
      )
    );


    setPrecioDiesel(
      unidadSeleccionada.precioDiesel
        ? String(
            unidadSeleccionada.precioDiesel
          )
        : ''
    );


    setKmInicial(
      unidadSeleccionada.kilometraje
        ? String(
            unidadSeleccionada.kilometraje
          )
        : ''
    );


    setKmFinal('');
  };


  // =====================================================
  // SELECCIONAR CAMIÓN
  // =====================================================

  const seleccionarUnidad = async (
    unidadSeleccionada
  ) => {

    const guardado =
      await establecerUnidadActivaStorage(
        unidadSeleccionada.id
      );


    if (!guardado) {

      Alert.alert(
        'Error',
        'No se pudo seleccionar la unidad.'
      );

      return;
    }


    cargarUnidadEnFormulario(
      unidadSeleccionada
    );


    setSelectorVisible(false);
  };


  // =====================================================
  // CAMBIAR FECHA
  // =====================================================

  const cambiarFecha = (
    event,
    fechaSeleccionada
  ) => {

    if (Platform.OS === 'android') {
      setMostrarFecha(false);
    }


    if (
      event.type === 'dismissed' ||
      !fechaSeleccionada
    ) {
      return;
    }


    setFechaJornada(
      fechaSeleccionada
    );
  };


  // =====================================================
  // FORMATEAR FECHA
  // =====================================================

  const formatearFecha = (fecha) => {

    return fecha.toLocaleDateString(
      'es-MX',
      {
        weekday: 'long',
        day: '2-digit',
        month: 'long',
        year: 'numeric'
      }
    );
  };


  // =====================================================
  // COMPROBAR SI ES HOY
  // =====================================================

  const esFechaHoy = () => {

    const hoy =
      new Date();

    return (
      fechaJornada.getDate() ===
        hoy.getDate() &&

      fechaJornada.getMonth() ===
        hoy.getMonth() &&

      fechaJornada.getFullYear() ===
        hoy.getFullYear()
    );
  };


  // =====================================================
  // CONVERSIONES
  // =====================================================

  const estudiantes =
    parseInt(
      boletosEstudiante
    ) || 0;


  const enteros =
    parseInt(
      boletosEnteros
    ) || 0;


  const litros =
    parseFloat(
      litrosDiesel
    ) || 0;


  const precioLitro =
    parseFloat(
      precioDiesel
    ) || 0;


  const kilometrajeInicial =
    parseFloat(
      kmInicial
    ) || 0;


  const kilometrajeFinal =
    parseFloat(
      kmFinal
    ) || 0;


  // =====================================================
  // CÁLCULOS
  // =====================================================

  const ingresoEstudiantes =
    estudiantes *
    tarifaEstudiante;


  const ingresoEnteros =
    enteros *
    tarifaEntero;


  const ingresoTotal =
    ingresoEstudiantes +
    ingresoEnteros;


  const gastoDiesel =
    litros *
    precioLitro;


  const kilometrosRecorridos =
    kilometrajeFinal >
    kilometrajeInicial

      ? kilometrajeFinal -
        kilometrajeInicial

      : 0;


  const rendimiento =
    litros > 0

      ? kilometrosRecorridos /
        litros

      : 0;


  const gananciaPreliminar =
    ingresoTotal -
    gastoDiesel;


  // =====================================================
  // GUARDAR JORNADA
  // =====================================================

  const guardarJornada = async () => {

    if (!unidad) {

      Alert.alert(
        'Unidad no seleccionada',
        'Selecciona el camión al que pertenece esta jornada.'
      );

      return;
    }


    if (
      boletosEstudiante.trim() === '' &&
      boletosEnteros.trim() === ''
    ) {

      Alert.alert(
        'Datos incompletos',
        'Ingresa al menos la cantidad de boletos vendidos.'
      );

      return;
    }


    if (
      kilometrajeInicial <= 0 ||
      kilometrajeFinal <= 0
    ) {

      Alert.alert(
        'Kilometraje incompleto',
        'Ingresa correctamente el kilometraje inicial y final.'
      );

      return;
    }


    if (
      kilometrajeFinal <
      kilometrajeInicial
    ) {

      Alert.alert(
        'Kilometraje incorrecto',
        'El kilometraje final no puede ser menor al inicial.'
      );

      return;
    }


    if (
      litros > 0 &&
      precioLitro <= 0
    ) {

      Alert.alert(
        'Precio incorrecto',
        'Ingresa correctamente el precio del diésel.'
      );

      return;
    }


    /*
      Guardamos el día seleccionado,
      pero conservamos la hora actual.
    */

    const ahora =
      new Date();

    const fechaRegistro =
      new Date(fechaJornada);

    fechaRegistro.setHours(
      ahora.getHours(),
      ahora.getMinutes(),
      ahora.getSeconds(),
      ahora.getMilliseconds()
    );


    const nuevaJornada = {

      id:
        Date.now().toString(),

      fecha:
        fechaRegistro.toISOString(),

      unidadId:
        unidad.id,

      numeroUnidad:
        unidad.numeroUnidad,

      ruta:
        unidad.ruta,

      boletosEstudiante:
        estudiantes,

      boletosEnteros:
        enteros,

      precioEstudiante:
        tarifaEstudiante,

      precioEntero:
        tarifaEntero,

      ingresoEstudiantes:
        ingresoEstudiantes,

      ingresoEnteros:
        ingresoEnteros,

      ingresoTotal:
        ingresoTotal,

      litrosDiesel:
        litros,

      precioDiesel:
        precioLitro,

      gastoDiesel:
        gastoDiesel,

      kmInicial:
        kilometrajeInicial,

      kmFinal:
        kilometrajeFinal,

      kilometrosRecorridos:
        kilometrosRecorridos,

      rendimiento:
        rendimiento,

      ganancia:
        gananciaPreliminar
    };


    const guardado =
      await guardarJornadaStorage(
        nuevaJornada
      );


    if (!guardado) {

      Alert.alert(
        'Error',
        'No se pudo guardar la jornada.'
      );

      return;
    }


    // =====================================================
    // ACTUALIZAR UNIDAD
    // =====================================================

    const unidadActualizada = {

      ...unidad,

      kilometraje:
        kilometrajeFinal,

      precioDiesel:
        precioLitro
    };


    const actualizada =
      await guardarUnidadStorage(
        unidadActualizada
      );


    if (!actualizada) {

      Alert.alert(
        'Advertencia',
        'La jornada se guardó, pero no se pudo actualizar la información del camión.'
      );
    }


    setUnidad(
      unidadActualizada
    );


    setUnidades(
      unidades.map(
        (item) =>
          item.id ===
          unidadActualizada.id

            ? unidadActualizada
            : item
      )
    );


    setModalVisible(true);
  };


  // =====================================================
  // LIMPIAR FORMULARIO
  // =====================================================

  const limpiarFormulario = () => {

    setBoletosEstudiante('');

    setBoletosEnteros('');

    setLitrosDiesel('');


    setPrecioDiesel(
      String(
        precioLitro
      )
    );


    setKmInicial(
      String(
        kilometrajeFinal
      )
    );


    setKmFinal('');


    /*
      Para la siguiente jornada
      volvemos automáticamente al día actual.
    */

    setFechaJornada(
      new Date()
    );


    setModalVisible(false);
  };


  return (

    <ScrollView
      style={styles.container}
      contentContainerStyle={
        styles.content
      }
      keyboardShouldPersistTaps="handled"
      showsVerticalScrollIndicator={false}
    >

      <Text style={styles.title}>
        Registrar Jornada
      </Text>

      <Text style={styles.subtitle}>
        Selecciona una unidad, fecha y registra su operación
      </Text>


      {/* ================================================= */}
      {/* FECHA */}
      {/* ================================================= */}

      <Text style={styles.sectionHeaderTitle}>
        Fecha de la jornada
      </Text>


      <TouchableOpacity
        style={styles.dateCard}
        onPress={() =>
          setMostrarFecha(true)
        }
        activeOpacity={0.85}
      >

        <View style={styles.dateIcon}>

          <Ionicons
            name="calendar-outline"
            size={27}
            color="#2563EB"
          />

        </View>


        <View style={styles.dateInfo}>

          <View style={styles.dateTitleRow}>

            <Text style={styles.dateLabel}>
              FECHA
            </Text>


            {esFechaHoy() && (

              <View style={styles.todayBadge}>

                <Text style={styles.todayBadgeText}>
                  HOY
                </Text>

              </View>

            )}

          </View>


          <Text style={styles.dateValue}>
            {formatearFecha(fechaJornada)}
          </Text>

        </View>


        <View style={styles.editDateButton}>

          <Ionicons
            name="create-outline"
            size={20}
            color="#2563EB"
          />

        </View>

      </TouchableOpacity>


      {mostrarFecha && (

        <DateTimePicker
          value={fechaJornada}
          mode="date"
          display={
            Platform.OS === 'ios'
              ? 'spinner'
              : 'default'
          }
          maximumDate={new Date()}
          onChange={cambiarFecha}
        />

      )}


      {Platform.OS === 'ios' &&
        mostrarFecha && (

          <TouchableOpacity
            style={styles.confirmDateButton}
            onPress={() =>
              setMostrarFecha(false)
            }
          >

            <Text style={styles.confirmDateText}>
              Confirmar fecha
            </Text>

          </TouchableOpacity>

        )}


      {/* ================================================= */}
      {/* SELECTOR DE CAMIÓN */}
      {/* ================================================= */}

      <Text style={styles.sectionHeaderTitle}>
        Unidad
      </Text>


      <TouchableOpacity
        style={styles.unitSelector}
        onPress={() =>
          setSelectorVisible(true)
        }
        activeOpacity={0.85}
      >

        <View style={styles.unitSelectorIcon}>

          <Ionicons
            name="bus"
            size={29}
            color="#FFFFFF"
          />

        </View>


        <View style={styles.unitSelectorInfo}>

          <Text style={styles.unitSelectorLabel}>
            CAMIÓN SELECCIONADO
          </Text>


          <Text style={styles.unitSelectorTitle}>

            {
              unidad
                ? `Unidad ${unidad.numeroUnidad}`
                : 'Seleccionar unidad'
            }

          </Text>


          <Text style={styles.unitSelectorRoute}>

            {
              unidad
                ? unidad.ruta
                : 'Toca para elegir un camión'
            }

          </Text>

        </View>


        <View style={styles.changeButton}>

          <Text style={styles.changeText}>
            Cambiar
          </Text>

          <Ionicons
            name="chevron-down"
            size={20}
            color="#2563EB"
          />

        </View>

      </TouchableOpacity>


      {/* ================================================= */}
      {/* TARIFAS */}
      {/* ================================================= */}

      {unidad && (

        <View style={styles.ratesCard}>

          <View style={styles.rateBox}>

            <Ionicons
              name="school-outline"
              size={23}
              color="#2563EB"
            />

            <Text style={styles.rateLabel}>
              Estudiante
            </Text>

            <Text style={styles.rateValue}>
              ${tarifaEstudiante.toFixed(2)}
            </Text>

          </View>


          <View style={styles.rateDivider} />


          <View style={styles.rateBox}>

            <Ionicons
              name="person-outline"
              size={23}
              color="#7C3AED"
            />

            <Text style={styles.rateLabel}>
              Entero
            </Text>

            <Text style={styles.rateValue}>
              ${tarifaEntero.toFixed(2)}
            </Text>

          </View>


          <View style={styles.rateDivider} />


          <View style={styles.rateBox}>

            <Ionicons
              name="speedometer-outline"
              size={23}
              color="#D97706"
            />

            <Text style={styles.rateLabel}>
              Kilometraje
            </Text>

            <Text style={styles.rateValue}>
              {Number(
                unidad.kilometraje || 0
              ).toFixed(0)}
            </Text>

          </View>

        </View>

      )}


      {/* ================================================= */}
      {/* BOLETOS */}
      {/* ================================================= */}

      <View style={styles.sectionCard}>

        <View style={styles.sectionTitleRow}>

          <View
            style={[
              styles.sectionIcon,
              {
                backgroundColor:
                  '#DBEAFE'
              }
            ]}
          >

            <Ionicons
              name="ticket-outline"
              size={24}
              color="#2563EB"
            />

          </View>


          <Text style={styles.sectionTitle}>
            Boletos vendidos
          </Text>

        </View>


        <Text style={styles.label}>
          Boletos estudiante
        </Text>


        <TextInput
          style={styles.input}
          keyboardType="numeric"
          placeholder="Ejemplo: 120"
          placeholderTextColor="#94A3B8"
          value={boletosEstudiante}
          onChangeText={
            setBoletosEstudiante
          }
        />


        <Text style={styles.helper}>
          {estudiantes} × ${tarifaEstudiante.toFixed(2)}
          {' = '}
          ${ingresoEstudiantes.toFixed(2)}
        </Text>


        <Text style={styles.label}>
          Boletos enteros
        </Text>


        <TextInput
          style={styles.input}
          keyboardType="numeric"
          placeholder="Ejemplo: 250"
          placeholderTextColor="#94A3B8"
          value={boletosEnteros}
          onChangeText={
            setBoletosEnteros
          }
        />


        <Text style={styles.helper}>
          {enteros} × ${tarifaEntero.toFixed(2)}
          {' = '}
          ${ingresoEnteros.toFixed(2)}
        </Text>

      </View>


      {/* ================================================= */}
      {/* DIÉSEL */}
      {/* ================================================= */}

      <View style={styles.sectionCard}>

        <View style={styles.sectionTitleRow}>

          <View
            style={[
              styles.sectionIcon,
              {
                backgroundColor:
                  '#FEF3C7'
              }
            ]}
          >

            <Ionicons
              name="water-outline"
              size={24}
              color="#D97706"
            />

          </View>


          <Text style={styles.sectionTitle}>
            Diésel
          </Text>

        </View>


        <Text style={styles.label}>
          Litros cargados
        </Text>


        <TextInput
          style={styles.input}
          keyboardType="decimal-pad"
          placeholder="Ejemplo: 45"
          placeholderTextColor="#94A3B8"
          value={litrosDiesel}
          onChangeText={
            setLitrosDiesel
          }
        />


        <Text style={styles.label}>
          Precio por litro
        </Text>


        <TextInput
          style={styles.input}
          keyboardType="decimal-pad"
          placeholder="Ejemplo: 25.80"
          placeholderTextColor="#94A3B8"
          value={precioDiesel}
          onChangeText={
            setPrecioDiesel
          }
        />


        <View style={styles.calculationBox}>

          <Text style={styles.calculationLabel}>
            Gasto en diésel
          </Text>

          <Text style={styles.calculationValue}>
            ${gastoDiesel.toFixed(2)}
          </Text>

        </View>

      </View>


      {/* ================================================= */}
      {/* KILOMETRAJE */}
      {/* ================================================= */}

      <View style={styles.sectionCard}>

        <View style={styles.sectionTitleRow}>

          <View
            style={[
              styles.sectionIcon,
              {
                backgroundColor:
                  '#EDE9FE'
              }
            ]}
          >

            <Ionicons
              name="speedometer-outline"
              size={24}
              color="#7C3AED"
            />

          </View>


          <Text style={styles.sectionTitle}>
            Kilometraje
          </Text>

        </View>


        <Text style={styles.label}>
          Kilometraje inicial
        </Text>


        <TextInput
          style={styles.input}
          keyboardType="numeric"
          placeholder="Ejemplo: 120500"
          placeholderTextColor="#94A3B8"
          value={kmInicial}
          onChangeText={
            setKmInicial
          }
        />


        <Text style={styles.helper}>
          Se carga desde la unidad seleccionada, pero puedes modificarlo.
        </Text>


        <Text style={styles.label}>
          Kilometraje final
        </Text>


        <TextInput
          style={styles.input}
          keyboardType="numeric"
          placeholder="Ejemplo: 120720"
          placeholderTextColor="#94A3B8"
          value={kmFinal}
          onChangeText={
            setKmFinal
          }
        />


        <View style={styles.metricsRow}>

          <View style={styles.metric}>

            <Text style={styles.metricLabel}>
              Recorrido
            </Text>

            <Text style={styles.metricValue}>
              {kilometrosRecorridos.toFixed(0)}
            </Text>

            <Text style={styles.metricUnit}>
              km
            </Text>

          </View>


          <View style={styles.metricDivider} />


          <View style={styles.metric}>

            <Text style={styles.metricLabel}>
              Rendimiento
            </Text>

            <Text style={styles.metricValue}>
              {rendimiento.toFixed(2)}
            </Text>

            <Text style={styles.metricUnit}>
              km/L
            </Text>

          </View>

        </View>

      </View>


      {/* ================================================= */}
      {/* RESUMEN */}
      {/* ================================================= */}

      <View style={styles.summaryCard}>

        <View style={styles.summaryHeader}>

          <Text style={styles.summaryTitle}>
            Resumen
          </Text>


          {unidad && (

            <Text style={styles.summaryUnit}>
              Unidad {unidad.numeroUnidad}
            </Text>

          )}

        </View>


        <View style={styles.summaryDate}>

          <Ionicons
            name="calendar-outline"
            size={16}
            color="#60A5FA"
          />

          <Text style={styles.summaryDateText}>
            {formatearFecha(fechaJornada)}
          </Text>

        </View>


        <View style={styles.summaryRow}>

          <Text style={styles.summaryLabel}>
            Ingresos
          </Text>

          <Text style={styles.greenText}>
            +${ingresoTotal.toFixed(2)}
          </Text>

        </View>


        <View style={styles.summaryRow}>

          <Text style={styles.summaryLabel}>
            Diésel
          </Text>

          <Text style={styles.redText}>
            -${gastoDiesel.toFixed(2)}
          </Text>

        </View>


        <View style={styles.summaryDivider} />


        <Text style={styles.profitLabel}>
          Ganancia preliminar
        </Text>


        <Text
          style={[
            styles.profitValue,

            {
              color:
                gananciaPreliminar >= 0
                  ? '#4ADE80'
                  : '#F87171'
            }
          ]}
        >
          ${gananciaPreliminar.toFixed(2)}
        </Text>

      </View>


      {/* GUARDAR */}

      <TouchableOpacity
        style={[
          styles.saveButton,

          !unidad &&
          styles.disabledButton
        ]}
        disabled={!unidad}
        onPress={guardarJornada}
      >

        <Ionicons
          name="save-outline"
          size={22}
          color="#FFFFFF"
        />

        <Text style={styles.saveButtonText}>
          Guardar jornada
        </Text>

      </TouchableOpacity>


      {/* ================================================= */}
      {/* MODAL SELECTOR UNIDAD */}
      {/* ================================================= */}

      <Modal
        visible={selectorVisible}
        transparent={true}
        animationType="slide"
        onRequestClose={() =>
          setSelectorVisible(false)
        }
      >

        <View style={styles.selectorOverlay}>

          <View style={styles.selectorModal}>

            <View style={styles.selectorHeader}>

              <View>

                <Text style={styles.selectorTitle}>
                  Seleccionar unidad
                </Text>

                <Text style={styles.selectorSubtitle}>
                  Elige el camión de esta jornada
                </Text>

              </View>


              <TouchableOpacity
                style={styles.closeSelector}
                onPress={() =>
                  setSelectorVisible(false)
                }
              >

                <Ionicons
                  name="close"
                  size={25}
                  color="#475569"
                />

              </TouchableOpacity>

            </View>


            <ScrollView
              showsVerticalScrollIndicator={false}
            >

              {unidades.length === 0 ? (

                <View style={styles.noUnits}>

                  <Ionicons
                    name="bus-outline"
                    size={50}
                    color="#94A3B8"
                  />

                  <Text style={styles.noUnitsTitle}>
                    Sin unidades
                  </Text>

                  <Text style={styles.noUnitsText}>
                    Primero registra un camión desde Mis Unidades.
                  </Text>

                </View>

              ) : (

                unidades.map(
                  (item) => {

                    const seleccionada =
                      unidad?.id ===
                      item.id;


                    return (

                      <TouchableOpacity
                        key={item.id}
                        style={[
                          styles.selectorUnitCard,

                          seleccionada &&
                          styles.selectorUnitActive
                        ]}
                        onPress={() =>
                          seleccionarUnidad(
                            item
                          )
                        }
                      >

                        <View
                          style={[
                            styles.selectorBusIcon,

                            seleccionada &&
                            styles.selectorBusIconActive
                          ]}
                        >

                          <Ionicons
                            name="bus"
                            size={27}
                            color={
                              seleccionada
                                ? '#FFFFFF'
                                : '#2563EB'
                            }
                          />

                        </View>


                        <View style={styles.selectorUnitInfo}>

                          <Text style={styles.selectorUnitTitle}>
                            Unidad {item.numeroUnidad}
                          </Text>

                          <Text style={styles.selectorUnitRoute}>
                            {item.ruta}
                          </Text>

                          <Text style={styles.selectorUnitKm}>
                            {Number(
                              item.kilometraje || 0
                            ).toFixed(0)}
                            {' km'}
                          </Text>

                        </View>


                        {seleccionada ? (

                          <Ionicons
                            name="checkmark-circle"
                            size={27}
                            color="#16A34A"
                          />

                        ) : (

                          <Ionicons
                            name="chevron-forward"
                            size={22}
                            color="#94A3B8"
                          />

                        )}

                      </TouchableOpacity>

                    );
                  }
                )

              )}

            </ScrollView>

          </View>

        </View>

      </Modal>


      {/* ================================================= */}
      {/* MODAL JORNADA GUARDADA */}
      {/* ================================================= */}

      <Modal
        visible={modalVisible}
        transparent={true}
        animationType="fade"
        onRequestClose={() =>
          setModalVisible(false)
        }
      >

        <View style={styles.modalOverlay}>

          <View style={styles.modalContent}>

            <Ionicons
              name="checkmark-circle"
              size={72}
              color="#16A34A"
            />


            <Text style={styles.modalTitle}>
              Jornada registrada
            </Text>


            <Text style={styles.modalText}>
              La jornada de la Unidad {unidad?.numeroUnidad} fue registrada correctamente.
            </Text>


            <View style={styles.modalDateCard}>

              <Ionicons
                name="calendar-outline"
                size={21}
                color="#2563EB"
              />

              <Text style={styles.modalDateText}>
                {formatearFecha(fechaJornada)}
              </Text>

            </View>


            <TouchableOpacity
              style={styles.modalButton}
              onPress={limpiarFormulario}
            >

              <Text style={styles.modalButtonText}>
                Aceptar
              </Text>

            </TouchableOpacity>

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
    marginBottom: 23
  },

  sectionHeaderTitle: {
    color: '#0F172A',
    fontSize: 18,
    fontWeight: 'bold',
    marginBottom: 10,
    marginTop: 8
  },

  dateCard: {
    backgroundColor: '#FFFFFF',
    borderRadius: 18,
    padding: 15,
    flexDirection: 'row',
    alignItems: 'center',
    marginBottom: 16,
    elevation: 2
  },

  dateIcon: {
    width: 50,
    height: 50,
    borderRadius: 15,
    backgroundColor: '#DBEAFE',
    justifyContent: 'center',
    alignItems: 'center'
  },

  dateInfo: {
    flex: 1,
    marginLeft: 12
  },

  dateTitleRow: {
    flexDirection: 'row',
    alignItems: 'center'
  },

  dateLabel: {
    color: '#94A3B8',
    fontSize: 10,
    fontWeight: 'bold'
  },

  todayBadge: {
    backgroundColor: '#DCFCE7',
    paddingHorizontal: 7,
    paddingVertical: 3,
    borderRadius: 10,
    marginLeft: 7
  },

  todayBadgeText: {
    color: '#15803D',
    fontSize: 9,
    fontWeight: 'bold'
  },

  dateValue: {
    color: '#0F172A',
    fontSize: 15,
    fontWeight: 'bold',
    marginTop: 3,
    textTransform: 'capitalize'
  },

  editDateButton: {
    width: 40,
    height: 40,
    borderRadius: 12,
    backgroundColor: '#EFF6FF',
    justifyContent: 'center',
    alignItems: 'center'
  },

  confirmDateButton: {
    backgroundColor: '#2563EB',
    borderRadius: 12,
    paddingVertical: 12,
    alignItems: 'center',
    marginBottom: 15
  },

  confirmDateText: {
    color: '#FFFFFF',
    fontWeight: 'bold'
  },

  unitSelector: {
    backgroundColor: '#0F172A',
    borderRadius: 19,
    padding: 16,
    flexDirection: 'row',
    alignItems: 'center'
  },

  unitSelectorIcon: {
    width: 53,
    height: 53,
    borderRadius: 16,
    backgroundColor: '#2563EB',
    justifyContent: 'center',
    alignItems: 'center'
  },

  unitSelectorInfo: {
    flex: 1,
    marginLeft: 13
  },

  unitSelectorLabel: {
    color: '#60A5FA',
    fontSize: 9,
    fontWeight: 'bold'
  },

  unitSelectorTitle: {
    color: '#FFFFFF',
    fontSize: 18,
    fontWeight: 'bold',
    marginTop: 2
  },

  unitSelectorRoute: {
    color: '#94A3B8',
    fontSize: 12,
    marginTop: 3
  },

  changeButton: {
    backgroundColor: '#EFF6FF',
    borderRadius: 12,
    paddingHorizontal: 10,
    paddingVertical: 7,
    flexDirection: 'row',
    alignItems: 'center'
  },

  changeText: {
    color: '#2563EB',
    fontWeight: 'bold',
    fontSize: 11,
    marginRight: 3
  },

  ratesCard: {
    backgroundColor: '#FFFFFF',
    borderRadius: 17,
    paddingVertical: 16,
    flexDirection: 'row',
    marginTop: 13,
    marginBottom: 18,
    elevation: 2
  },

  rateBox: {
    flex: 1,
    alignItems: 'center'
  },

  rateDivider: {
    width: 1,
    backgroundColor: '#E2E8F0'
  },

  rateLabel: {
    color: '#64748B',
    fontSize: 10,
    marginTop: 5
  },

  rateValue: {
    color: '#0F172A',
    fontWeight: 'bold',
    fontSize: 15,
    marginTop: 2
  },

  sectionCard: {
    backgroundColor: '#FFFFFF',
    borderRadius: 18,
    padding: 18,
    marginBottom: 18,
    elevation: 2
  },

  sectionTitleRow: {
    flexDirection: 'row',
    alignItems: 'center',
    marginBottom: 13
  },

  sectionIcon: {
    width: 44,
    height: 44,
    borderRadius: 13,
    justifyContent: 'center',
    alignItems: 'center',
    marginRight: 11
  },

  sectionTitle: {
    color: '#0F172A',
    fontSize: 19,
    fontWeight: 'bold'
  },

  label: {
    color: '#334155',
    fontWeight: '600',
    fontSize: 14,
    marginTop: 8,
    marginBottom: 7
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
    fontSize: 11,
    marginTop: 5,
    marginBottom: 7
  },

  calculationBox: {
    backgroundColor: '#FFF7ED',
    borderRadius: 13,
    padding: 14,
    marginTop: 15,
    flexDirection: 'row',
    justifyContent: 'space-between'
  },

  calculationLabel: {
    color: '#9A3412',
    fontWeight: '600'
  },

  calculationValue: {
    color: '#C2410C',
    fontWeight: 'bold',
    fontSize: 18
  },

  metricsRow: {
    backgroundColor: '#F8FAFC',
    borderRadius: 14,
    paddingVertical: 15,
    flexDirection: 'row',
    marginTop: 16
  },

  metric: {
    flex: 1,
    alignItems: 'center'
  },

  metricDivider: {
    width: 1,
    backgroundColor: '#CBD5E1'
  },

  metricLabel: {
    color: '#64748B',
    fontSize: 11
  },

  metricValue: {
    color: '#0F172A',
    fontWeight: 'bold',
    fontSize: 22
  },

  metricUnit: {
    color: '#94A3B8',
    fontSize: 11
  },

  summaryCard: {
    backgroundColor: '#0F172A',
    borderRadius: 20,
    padding: 20,
    marginBottom: 20
  },

  summaryHeader: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'center',
    marginBottom: 10
  },

  summaryTitle: {
    color: '#FFFFFF',
    fontSize: 20,
    fontWeight: 'bold'
  },

  summaryUnit: {
    color: '#60A5FA',
    fontSize: 12,
    fontWeight: 'bold'
  },

  summaryDate: {
    flexDirection: 'row',
    alignItems: 'center',
    backgroundColor: '#1E293B',
    borderRadius: 10,
    paddingHorizontal: 10,
    paddingVertical: 8,
    marginBottom: 15
  },

  summaryDateText: {
    color: '#CBD5E1',
    fontSize: 11,
    marginLeft: 6,
    textTransform: 'capitalize'
  },

  summaryRow: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    marginBottom: 10
  },

  summaryLabel: {
    color: '#CBD5E1'
  },

  greenText: {
    color: '#4ADE80',
    fontWeight: 'bold'
  },

  redText: {
    color: '#F87171',
    fontWeight: 'bold'
  },

  summaryDivider: {
    height: 1,
    backgroundColor: '#334155',
    marginVertical: 10
  },

  profitLabel: {
    color: '#94A3B8',
    textAlign: 'center'
  },

  profitValue: {
    textAlign: 'center',
    fontSize: 34,
    fontWeight: 'bold',
    marginTop: 4
  },

  saveButton: {
    backgroundColor: '#2563EB',
    borderRadius: 15,
    paddingVertical: 16,
    flexDirection: 'row',
    justifyContent: 'center',
    alignItems: 'center'
  },

  disabledButton: {
    backgroundColor: '#94A3B8'
  },

  saveButtonText: {
    color: '#FFFFFF',
    fontWeight: 'bold',
    fontSize: 17,
    marginLeft: 7
  },

  selectorOverlay: {
    flex: 1,
    backgroundColor: 'rgba(15,23,42,0.72)',
    justifyContent: 'flex-end'
  },

  selectorModal: {
    backgroundColor: '#F8FAFC',
    borderTopLeftRadius: 27,
    borderTopRightRadius: 27,
    padding: 20,
    maxHeight: '78%'
  },

  selectorHeader: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'center',
    marginBottom: 18
  },

  selectorTitle: {
    color: '#0F172A',
    fontSize: 24,
    fontWeight: 'bold'
  },

  selectorSubtitle: {
    color: '#64748B',
    fontSize: 13,
    marginTop: 3
  },

  closeSelector: {
    width: 42,
    height: 42,
    borderRadius: 21,
    backgroundColor: '#E2E8F0',
    justifyContent: 'center',
    alignItems: 'center'
  },

  selectorUnitCard: {
    backgroundColor: '#FFFFFF',
    borderRadius: 17,
    padding: 15,
    flexDirection: 'row',
    alignItems: 'center',
    marginBottom: 10,
    borderWidth: 1,
    borderColor: '#E2E8F0'
  },

  selectorUnitActive: {
    borderColor: '#86EFAC',
    backgroundColor: '#F0FDF4'
  },

  selectorBusIcon: {
    width: 50,
    height: 50,
    borderRadius: 15,
    backgroundColor: '#DBEAFE',
    justifyContent: 'center',
    alignItems: 'center'
  },

  selectorBusIconActive: {
    backgroundColor: '#2563EB'
  },

  selectorUnitInfo: {
    flex: 1,
    marginLeft: 12
  },

  selectorUnitTitle: {
    color: '#0F172A',
    fontSize: 16,
    fontWeight: 'bold'
  },

  selectorUnitRoute: {
    color: '#64748B',
    fontSize: 12,
    marginTop: 2
  },

  selectorUnitKm: {
    color: '#94A3B8',
    fontSize: 11,
    marginTop: 3
  },

  noUnits: {
    alignItems: 'center',
    paddingVertical: 35
  },

  noUnitsTitle: {
    color: '#0F172A',
    fontSize: 18,
    fontWeight: 'bold',
    marginTop: 10
  },

  noUnitsText: {
    color: '#64748B',
    textAlign: 'center',
    marginTop: 5
  },

  modalOverlay: {
    flex: 1,
    backgroundColor: 'rgba(15,23,42,0.72)',
    justifyContent: 'center',
    padding: 25
  },

  modalContent: {
    backgroundColor: '#FFFFFF',
    borderRadius: 24,
    padding: 25,
    alignItems: 'center'
  },

  modalTitle: {
    color: '#0F172A',
    fontSize: 23,
    fontWeight: 'bold',
    marginTop: 12
  },

  modalText: {
    color: '#64748B',
    textAlign: 'center',
    marginTop: 7
  },

  modalDateCard: {
    backgroundColor: '#EFF6FF',
    borderRadius: 12,
    paddingHorizontal: 12,
    paddingVertical: 10,
    flexDirection: 'row',
    alignItems: 'center',
    marginTop: 15
  },

  modalDateText: {
    color: '#1E40AF',
    fontWeight: '600',
    fontSize: 12,
    marginLeft: 7,
    textTransform: 'capitalize'
  },

  modalButton: {
    width: '100%',
    backgroundColor: '#2563EB',
    borderRadius: 14,
    paddingVertical: 14,
    alignItems: 'center',
    marginTop: 20
  },

  modalButtonText: {
    color: '#FFFFFF',
    fontWeight: 'bold'
  }

});