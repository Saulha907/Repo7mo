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
  guardarGastoStorage,
  obtenerGastosStorage,
  eliminarGastoStorage,
  obtenerUnidadStorage,
  obtenerUnidadesStorage,
  establecerUnidadActivaStorage
} from '../storage/storage';


export default function GastosScreen() {

  // =====================================================
  // UNIDADES
  // =====================================================

  const [unidades, setUnidades] =
    useState([]);

  const [unidad, setUnidad] =
    useState(null);

  const [selectorVisible, setSelectorVisible] =
    useState(false);


  // =====================================================
  // FECHA
  // =====================================================

  const [fechaGasto, setFechaGasto] =
    useState(new Date());

  const [mostrarFecha, setMostrarFecha] =
    useState(false);


  // =====================================================
  // FORMULARIO
  // =====================================================

  const [categoria, setCategoria] =
    useState('Combustible');

  const [monto, setMonto] =
    useState('');

  const [descripcion, setDescripcion] =
    useState('');

  const [gastos, setGastos] =
    useState([]);

  const [modalVisible, setModalVisible] =
    useState(false);


  // =====================================================
  // CATEGORÍAS
  // =====================================================

  const categorias = [

    {
      nombre: 'Combustible',
      icono: 'water-outline',
      color: '#2563EB'
    },

    {
      nombre: 'Ponchadura',
      icono: 'radio-button-on-outline',
      color: '#DC2626'
    },

    {
      nombre: 'Llantas',
      icono: 'ellipse-outline',
      color: '#7C3AED'
    },

    {
      nombre: 'Aceite',
      icono: 'water-outline',
      color: '#0891B2'
    },

    {
      nombre: 'Reparación',
      icono: 'construct-outline',
      color: '#EA580C'
    },

    {
      nombre: 'Lavado',
      icono: 'sparkles-outline',
      color: '#2563EB'
    },

    {
      nombre: 'Refacciones',
      icono: 'build-outline',
      color: '#475569'
    },

    {
      nombre: 'Otro',
      icono: 'ellipsis-horizontal',
      color: '#64748B'
    }

  ];


  // =====================================================
  // CARGAR DATOS
  // =====================================================

  const cargarDatos = async () => {

    try {

      const unidadesGuardadas =
        await obtenerUnidadesStorage();

      const unidadActiva =
        await obtenerUnidadStorage();

      const gastosGuardados =
        await obtenerGastosStorage();


      setUnidades(
        Array.isArray(unidadesGuardadas)
          ? unidadesGuardadas
          : []
      );


      setUnidad(
        unidadActiva || null
      );


      setGastos(
        Array.isArray(gastosGuardados)
          ? gastosGuardados
          : []
      );

    } catch (error) {

      console.log(
        'Error al cargar gastos:',
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
  // SELECCIONAR UNIDAD
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


    setUnidad(
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


    setFechaGasto(
      fechaSeleccionada
    );
  };


  // =====================================================
  // FORMATEAR FECHA
  // =====================================================

  const formatearFechaCompleta = (fecha) => {

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


  const formatearFechaCorta = (fecha) => {

    if (!fecha) {
      return '';
    }


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
  // SABER SI ES HOY
  // =====================================================

  const esFechaHoy = () => {

    const hoy =
      new Date();


    return (

      fechaGasto.getDate() ===
        hoy.getDate() &&

      fechaGasto.getMonth() ===
        hoy.getMonth() &&

      fechaGasto.getFullYear() ===
        hoy.getFullYear()

    );
  };


  // =====================================================
  // REGISTRAR GASTO
  // =====================================================

  const registrarGasto = async () => {

    if (!unidad) {

      Alert.alert(
        'Unidad no seleccionada',
        'Selecciona el camión al que pertenece este gasto.'
      );

      return;
    }


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


    /*
      Guardamos el día seleccionado,
      pero conservamos la hora actual.
    */

    const ahora =
      new Date();

    const fechaRegistro =
      new Date(fechaGasto);


    fechaRegistro.setHours(
      ahora.getHours(),
      ahora.getMinutes(),
      ahora.getSeconds(),
      ahora.getMilliseconds()
    );


    const nuevoGasto = {

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

      categoria:
        categoria,

      monto:
        montoNumero,

      descripcion:
        descripcion.trim() === ''
          ? 'Sin descripción'
          : descripcion.trim()

    };


    const guardado =
      await guardarGastoStorage(
        nuevoGasto
      );


    if (!guardado) {

      Alert.alert(
        'Error',
        'No se pudo registrar el gasto.'
      );

      return;
    }


    setMonto('');

    setDescripcion('');


    // Volvemos a la fecha actual
    // para el próximo gasto.

    setFechaGasto(
      new Date()
    );


    await cargarDatos();

    setModalVisible(true);
  };


  // =====================================================
  // GASTOS DE LA UNIDAD
  // =====================================================

  const gastosUnidad =
    unidad

      ? gastos.filter(
          (gasto) => {

            if (
              gasto.unidadId ===
              unidad.id
            ) {

              return true;
            }


            if (
              !gasto.unidadId &&
              gasto.numeroUnidad ===
              unidad.numeroUnidad
            ) {

              return true;
            }


            return false;
          }
        )

      : [];


  const gastosOrdenados =
    [...gastosUnidad].sort(
      (a, b) =>
        new Date(b.fecha) -
        new Date(a.fecha)
    );


  const totalGastosUnidad =
    gastosUnidad.reduce(
      (total, gasto) =>

        total +
        Number(
          gasto.monto || 0
        ),

      0
    );


  // =====================================================
  // ELIMINAR
  // =====================================================

  const confirmarEliminar = (gasto) => {

    Alert.alert(
      'Eliminar gasto',
      '¿Deseas eliminar este gasto?',
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
              await eliminarGastoStorage(
                gasto.id
              );


            if (eliminado) {

              await cargarDatos();

            } else {

              Alert.alert(
                'Error',
                'No se pudo eliminar el gasto.'
              );
            }
          }

        }

      ]
    );
  };


  // =====================================================
  // DATOS VISUALES DE UNA CATEGORÍA
  // =====================================================

  const obtenerCategoria = (
    nombreCategoria
  ) => {

    const encontrada =
      categorias.find(
        (item) =>
          item.nombre ===
          nombreCategoria
      );


    return (
      encontrada || {
        nombre: 'Otro',
        icono: 'ellipsis-horizontal',
        color: '#64748B'
      }
    );
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
        Gastos
      </Text>

      <Text style={styles.subtitle}>
        Registra los gastos operativos de cada unidad
      </Text>


      {/* ================================================= */}
      {/* FECHA DEL GASTO */}
      {/* ================================================= */}

      <Text style={styles.sectionHeaderTitle}>
        Fecha del gasto
      </Text>


      <TouchableOpacity
        style={styles.dateCard}
        activeOpacity={0.85}
        onPress={() =>
          setMostrarFecha(true)
        }
      >

        <View style={styles.dateIcon}>

          <Ionicons
            name="calendar-outline"
            size={27}
            color="#2563EB"
          />

        </View>


        <View style={styles.dateInfo}>

          <View style={styles.dateLabelRow}>

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
            {formatearFechaCompleta(
              fechaGasto
            )}
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
          value={fechaGasto}
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
      {/* UNIDAD */}
      {/* ================================================= */}

      <Text style={styles.sectionHeaderTitle}>
        Unidad
      </Text>


      <TouchableOpacity
        style={styles.unitSelector}
        activeOpacity={0.85}
        onPress={() =>
          setSelectorVisible(true)
        }
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
      {/* TOTAL */}
      {/* ================================================= */}

      <View style={styles.totalCard}>

        <View>

          <Text style={styles.totalLabel}>
            Gastos acumulados
          </Text>

          <Text style={styles.totalUnit}>
            {
              unidad
                ? `Unidad ${unidad.numeroUnidad}`
                : 'Sin unidad'
            }
          </Text>

        </View>


        <Text style={styles.totalValue}>
          ${totalGastosUnidad.toFixed(2)}
        </Text>

      </View>


      {/* ================================================= */}
      {/* NUEVO GASTO */}
      {/* ================================================= */}

      <View style={styles.formCard}>

        <Text style={styles.sectionTitle}>
          Nuevo gasto
        </Text>


        <Text style={styles.label}>
          Categoría
        </Text>


        {/* CATEGORÍAS CON ICONOS */}

        <View style={styles.categoryContainer}>

          {categorias.map(
            (item) => {

              const activa =
                categoria ===
                item.nombre;


              return (

                <TouchableOpacity
                  key={item.nombre}
                  style={[
                    styles.categoryButton,

                    activa &&
                    styles.categoryActive
                  ]}
                  onPress={() =>
                    setCategoria(
                      item.nombre
                    )
                  }
                >

                  <Ionicons
                    name={item.icono}
                    size={22}
                    color={
                      activa
                        ? '#FFFFFF'
                        : item.color
                    }
                  />


                  <Text
                    style={[
                      styles.categoryText,

                      activa &&
                      styles.categoryTextActive
                    ]}
                  >
                    {item.nombre}
                  </Text>

                </TouchableOpacity>

              );
            }
          )}

        </View>


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
            placeholder="Ejemplo: 350"
            placeholderTextColor="#94A3B8"
            value={monto}
            onChangeText={
              setMonto
            }
          />

        </View>


        <Text style={styles.label}>
          Descripción
        </Text>


        <TextInput
          style={[
            styles.input,
            styles.descriptionInput
          ]}
          placeholder="Ejemplo: Reparación de llanta trasera"
          placeholderTextColor="#94A3B8"
          value={descripcion}
          onChangeText={
            setDescripcion
          }
          multiline={true}
          numberOfLines={4}
        />


        {/* RESUMEN ANTES DE GUARDAR */}

        <View style={styles.previewCard}>

          <View style={styles.previewRow}>

            <Text style={styles.previewLabel}>
              Unidad
            </Text>

            <Text style={styles.previewValue}>
              {
                unidad
                  ? `Unidad ${unidad.numeroUnidad}`
                  : 'Sin seleccionar'
              }
            </Text>

          </View>


          <View style={styles.previewRow}>

            <Text style={styles.previewLabel}>
              Fecha
            </Text>

            <Text style={styles.previewValue}>
              {fechaGasto.toLocaleDateString(
                'es-MX'
              )}
            </Text>

          </View>


          <View style={styles.previewRow}>

            <Text style={styles.previewLabel}>
              Categoría
            </Text>

            <Text style={styles.previewValue}>
              {categoria}
            </Text>

          </View>


          <View style={styles.previewDivider} />


          <View style={styles.previewRow}>

            <Text style={styles.previewTotalLabel}>
              Gasto
            </Text>

            <Text style={styles.previewTotalValue}>
              -$
              {(
                parseFloat(monto) || 0
              ).toFixed(2)}
            </Text>

          </View>

        </View>


        <TouchableOpacity
          style={[
            styles.saveButton,

            !unidad &&
            styles.disabledButton
          ]}
          disabled={!unidad}
          onPress={registrarGasto}
        >

          <Ionicons
            name="add-circle-outline"
            size={22}
            color="#FFFFFF"
          />

          <Text style={styles.saveButtonText}>
            Registrar gasto
          </Text>

        </TouchableOpacity>

      </View>


      {/* ================================================= */}
      {/* HISTORIAL DE LA UNIDAD */}
      {/* ================================================= */}

      <View style={styles.historyHeader}>

        <View>

          <Text style={styles.historyTitle}>
            Gastos registrados
          </Text>

          <Text style={styles.historySubtitle}>
            {
              unidad
                ? `Unidad ${unidad.numeroUnidad}`
                : 'Selecciona una unidad'
            }
          </Text>

        </View>


        <View style={styles.countBadge}>

          <Text style={styles.countText}>
            {gastosUnidad.length}
          </Text>

        </View>

      </View>


      {gastosOrdenados.length === 0 ? (

        <View style={styles.emptyCard}>

          <Ionicons
            name="receipt-outline"
            size={45}
            color="#94A3B8"
          />

          <Text style={styles.emptyTitle}>
            Sin gastos registrados
          </Text>

          <Text style={styles.emptyText}>
            {
              unidad
                ? 'Esta unidad todavía no tiene gastos registrados.'
                : 'Selecciona una unidad para consultar sus gastos.'
            }
          </Text>

        </View>

      ) : (

        gastosOrdenados.map(
          (gasto) => {

            const datosCategoria =
              obtenerCategoria(
                gasto.categoria
              );


            return (

              <View
                key={gasto.id}
                style={styles.gastoCard}
              >

                <View
                  style={[
                    styles.gastoIcon,
                    {
                      backgroundColor:
                        `${datosCategoria.color}15`
                    }
                  ]}
                >

                  <Ionicons
                    name={
                      datosCategoria.icono
                    }
                    size={23}
                    color={
                      datosCategoria.color
                    }
                  />

                </View>


                <View style={styles.gastoInfo}>

                  <Text style={styles.gastoCategoria}>
                    {gasto.categoria}
                  </Text>


                  <Text
                    style={styles.gastoDescripcion}
                    numberOfLines={2}
                  >
                    {gasto.descripcion}
                  </Text>


                  <Text style={styles.gastoFecha}>
                    {formatearFechaCorta(
                      gasto.fecha
                    )}
                    {' • '}
                    {formatearHora(
                      gasto.fecha
                    )}
                  </Text>

                </View>


                <View style={styles.gastoRight}>

                  <Text style={styles.gastoMonto}>
                    -$
                    {Number(
                      gasto.monto || 0
                    ).toFixed(2)}
                  </Text>


                  <TouchableOpacity
                    style={styles.deleteIcon}
                    onPress={() =>
                      confirmarEliminar(
                        gasto
                      )
                    }
                  >

                    <Ionicons
                      name="trash-outline"
                      size={20}
                      color="#DC2626"
                    />

                  </TouchableOpacity>

                </View>

              </View>

            );
          }
        )

      )}


      {/* ================================================= */}
      {/* SELECTOR DE UNIDAD */}
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
                  Elige el camión del gasto
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

                          <Text style={styles.selectorUnitExtra}>
                            {
                              item.operador
                                ? `Operador: ${item.operador}`
                                : 'Sin operador registrado'
                            }
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
      {/* MODAL GUARDADO */}
      {/* ================================================= */}

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
                size={43}
                color="#FFFFFF"
              />

            </View>


            <Text style={styles.modalTitle}>
              Gasto registrado
            </Text>


            <Text style={styles.modalText}>
              El gasto fue asignado correctamente a la Unidad {unidad?.numeroUnidad}.
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

    </ScrollView>
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


  // FECHA

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

  dateLabelRow: {
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


  // UNIDAD

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


  // TOTAL

  totalCard: {
    backgroundColor: '#FEF2F2',
    borderRadius: 18,
    padding: 18,
    marginTop: 14,
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'center'
  },

  totalLabel: {
    color: '#991B1B',
    fontSize: 12
  },

  totalUnit: {
    color: '#7F1D1D',
    fontSize: 15,
    fontWeight: 'bold',
    marginTop: 3
  },

  totalValue: {
    color: '#DC2626',
    fontSize: 26,
    fontWeight: 'bold'
  },


  // FORMULARIO

  formCard: {
    backgroundColor: '#FFFFFF',
    borderRadius: 18,
    padding: 18,
    marginTop: 18,
    elevation: 2
  },

  sectionTitle: {
    color: '#0F172A',
    fontSize: 20,
    fontWeight: 'bold'
  },

  label: {
    color: '#334155',
    fontWeight: '600',
    fontSize: 14,
    marginTop: 14,
    marginBottom: 8
  },


  // CATEGORÍAS

  categoryContainer: {
    flexDirection: 'row',
    flexWrap: 'wrap'
  },

  categoryButton: {
    backgroundColor: '#F1F5F9',
    borderRadius: 25,
    paddingHorizontal: 14,
    paddingVertical: 11,
    marginRight: 8,
    marginBottom: 9,
    flexDirection: 'row',
    alignItems: 'center'
  },

  categoryActive: {
    backgroundColor: '#2563EB'
  },

  categoryText: {
    color: '#475569',
    fontWeight: '700',
    fontSize: 13,
    marginLeft: 7
  },

  categoryTextActive: {
    color: '#FFFFFF'
  },


  // INPUTS

  moneyInput: {
    backgroundColor: '#F8FAFC',
    borderWidth: 1,
    borderColor: '#CBD5E1',
    borderRadius: 12,
    paddingHorizontal: 14,
    flexDirection: 'row',
    alignItems: 'center'
  },

  moneySymbol: {
    color: '#475569',
    fontSize: 18,
    fontWeight: 'bold'
  },

  moneyField: {
    flex: 1,
    color: '#0F172A',
    fontSize: 16,
    paddingVertical: 13,
    marginLeft: 5
  },

  input: {
    backgroundColor: '#F8FAFC',
    borderWidth: 1,
    borderColor: '#CBD5E1',
    borderRadius: 12,
    paddingHorizontal: 13,
    paddingVertical: 13,
    color: '#0F172A',
    fontSize: 16
  },

  descriptionInput: {
    minHeight: 90,
    textAlignVertical: 'top'
  },


  // PREVIEW

  previewCard: {
    backgroundColor: '#F8FAFC',
    borderRadius: 15,
    padding: 15,
    marginTop: 18
  },

  previewRow: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    marginVertical: 5
  },

  previewLabel: {
    color: '#64748B'
  },

  previewValue: {
    color: '#0F172A',
    fontWeight: '600'
  },

  previewDivider: {
    height: 1,
    backgroundColor: '#E2E8F0',
    marginVertical: 8
  },

  previewTotalLabel: {
    color: '#0F172A',
    fontWeight: 'bold'
  },

  previewTotalValue: {
    color: '#DC2626',
    fontWeight: 'bold',
    fontSize: 18
  },


  // BOTÓN

  saveButton: {
    backgroundColor: '#2563EB',
    borderRadius: 14,
    paddingVertical: 15,
    flexDirection: 'row',
    justifyContent: 'center',
    alignItems: 'center',
    marginTop: 18
  },

  disabledButton: {
    backgroundColor: '#94A3B8'
  },

  saveButtonText: {
    color: '#FFFFFF',
    fontWeight: 'bold',
    fontSize: 16,
    marginLeft: 7
  },


  // HISTORIAL

  historyHeader: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'center',
    marginTop: 28,
    marginBottom: 13
  },

  historyTitle: {
    color: '#0F172A',
    fontSize: 20,
    fontWeight: 'bold'
  },

  historySubtitle: {
    color: '#64748B',
    fontSize: 12,
    marginTop: 2
  },

  countBadge: {
    backgroundColor: '#DBEAFE',
    minWidth: 36,
    height: 36,
    borderRadius: 18,
    justifyContent: 'center',
    alignItems: 'center',
    paddingHorizontal: 8
  },

  countText: {
    color: '#2563EB',
    fontWeight: 'bold'
  },

  gastoCard: {
    backgroundColor: '#FFFFFF',
    borderRadius: 16,
    padding: 15,
    marginBottom: 10,
    flexDirection: 'row',
    alignItems: 'center',
    elevation: 2
  },

  gastoIcon: {
    width: 46,
    height: 46,
    borderRadius: 14,
    justifyContent: 'center',
    alignItems: 'center'
  },

  gastoInfo: {
    flex: 1,
    marginLeft: 11
  },

  gastoCategoria: {
    color: '#0F172A',
    fontSize: 15,
    fontWeight: 'bold'
  },

  gastoDescripcion: {
    color: '#64748B',
    fontSize: 12,
    marginTop: 2
  },

  gastoFecha: {
    color: '#94A3B8',
    fontSize: 10,
    marginTop: 4
  },

  gastoRight: {
    alignItems: 'flex-end',
    marginLeft: 8
  },

  gastoMonto: {
    color: '#DC2626',
    fontWeight: 'bold',
    fontSize: 16
  },

  deleteIcon: {
    width: 34,
    height: 34,
    borderRadius: 10,
    backgroundColor: '#FEF2F2',
    justifyContent: 'center',
    alignItems: 'center',
    marginTop: 6
  },


  // VACÍO

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
  },


  // SELECTOR

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

  selectorUnitExtra: {
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


  // MODAL

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

  successCircle: {
    width: 75,
    height: 75,
    borderRadius: 38,
    backgroundColor: '#16A34A',
    justifyContent: 'center',
    alignItems: 'center'
  },

  modalTitle: {
    color: '#0F172A',
    fontSize: 23,
    fontWeight: 'bold',
    marginTop: 14
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
    marginTop: 20
  },

  modalButtonText: {
    color: '#FFFFFF',
    fontWeight: 'bold'
  }

});