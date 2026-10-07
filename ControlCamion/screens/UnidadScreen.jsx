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
  Alert
} from 'react-native';

import { Ionicons } from '@expo/vector-icons';

import {
  useFocusEffect
} from '@react-navigation/native';

import {
  obtenerUnidadesStorage,
  obtenerUnidadActivaIdStorage,
  agregarUnidadStorage,
  actualizarUnidadStorage,
  eliminarUnidadStorage,
  establecerUnidadActivaStorage
} from '../storage/storage';


export default function UnidadScreen() {

  const [unidades, setUnidades] =
    useState([]);

  const [unidadActivaId, setUnidadActivaId] =
    useState(null);

  const [modalVisible, setModalVisible] =
    useState(false);

  const [modoEdicion, setModoEdicion] =
    useState(false);

  const [unidadEditandoId, setUnidadEditandoId] =
    useState(null);


  // FORMULARIO

  const [numeroUnidad, setNumeroUnidad] =
    useState('');

  const [ruta, setRuta] =
    useState('');

  const [operador, setOperador] =
    useState('');

  const [marca, setMarca] =
    useState('');

  const [modelo, setModelo] =
    useState('');

  const [anio, setAnio] =
    useState('');

  const [placas, setPlacas] =
    useState('');

  const [kilometraje, setKilometraje] =
    useState('');

  const [tarifaEstudiante, setTarifaEstudiante] =
    useState('7');

  const [tarifaEntero, setTarifaEntero] =
    useState('9');

  const [precioDiesel, setPrecioDiesel] =
    useState('');


  // =====================================================
  // CARGAR UNIDADES
  // =====================================================

  const cargarUnidades = async () => {

    const datos =
      await obtenerUnidadesStorage();

    const activaId =
      await obtenerUnidadActivaIdStorage();

    setUnidades(datos);

    setUnidadActivaId(
      activaId
    );
  };


  useFocusEffect(

    useCallback(() => {

      cargarUnidades();

    }, [])

  );


  // =====================================================
  // LIMPIAR FORMULARIO
  // =====================================================

  const limpiarFormulario = () => {

    setNumeroUnidad('');
    setRuta('');
    setOperador('');
    setMarca('');
    setModelo('');
    setAnio('');
    setPlacas('');
    setKilometraje('');

    setTarifaEstudiante('7');
    setTarifaEntero('9');
    setPrecioDiesel('');

    setModoEdicion(false);

    setUnidadEditandoId(null);
  };


  // =====================================================
  // ABRIR MODAL NUEVA UNIDAD
  // =====================================================

  const abrirNuevaUnidad = () => {

    limpiarFormulario();

    setModalVisible(true);
  };


  // =====================================================
  // EDITAR
  // =====================================================

  const editarUnidad = (unidad) => {

    setModoEdicion(true);

    setUnidadEditandoId(
      unidad.id
    );

    setNumeroUnidad(
      unidad.numeroUnidad || ''
    );

    setRuta(
      unidad.ruta || ''
    );

    setOperador(
      unidad.operador || ''
    );

    setMarca(
      unidad.marca || ''
    );

    setModelo(
      unidad.modelo || ''
    );

    setAnio(
      unidad.anio
        ? String(unidad.anio)
        : ''
    );

    setPlacas(
      unidad.placas || ''
    );

    setKilometraje(
      unidad.kilometraje
        ? String(unidad.kilometraje)
        : ''
    );

    setTarifaEstudiante(
      String(
        unidad.tarifaEstudiante || 7
      )
    );

    setTarifaEntero(
      String(
        unidad.tarifaEntero || 9
      )
    );

    setPrecioDiesel(
      unidad.precioDiesel
        ? String(unidad.precioDiesel)
        : ''
    );

    setModalVisible(true);
  };


  // =====================================================
  // GUARDAR
  // =====================================================

  const guardarUnidad = async () => {

    if (
      numeroUnidad.trim() === '' ||
      ruta.trim() === ''
    ) {

      Alert.alert(
        'Datos incompletos',
        'Ingresa el número de unidad y la ruta.'
      );

      return;
    }


    const tarifaEstudianteNumero =
      parseFloat(
        tarifaEstudiante
      );

    const tarifaEnteroNumero =
      parseFloat(
        tarifaEntero
      );

    const precioDieselNumero =
      parseFloat(
        precioDiesel
      ) || 0;


    if (
      tarifaEstudianteNumero <= 0 ||
      tarifaEnteroNumero <= 0
    ) {

      Alert.alert(
        'Tarifas inválidas',
        'Las tarifas deben ser mayores a cero.'
      );

      return;
    }


    // Evitar números de unidad duplicados

    const duplicada =
      unidades.some(
        (unidad) => {

          const mismoNumero =
            String(
              unidad.numeroUnidad
            ).trim().toLowerCase() ===
            numeroUnidad
              .trim()
              .toLowerCase();

          if (modoEdicion) {

            return (
              mismoNumero &&
              unidad.id !==
                unidadEditandoId
            );
          }

          return mismoNumero;
        }
      );


    if (duplicada) {

      Alert.alert(
        'Unidad existente',
        'Ya existe una unidad registrada con ese número.'
      );

      return;
    }


    const unidad = {

      id:
        modoEdicion
          ? unidadEditandoId
          : undefined,

      numeroUnidad:
        numeroUnidad.trim(),

      ruta:
        ruta.trim(),

      operador:
        operador.trim(),

      marca:
        marca.trim(),

      modelo:
        modelo.trim(),

      anio:
        parseInt(anio) || 0,

      placas:
        placas.trim(),

      kilometraje:
        parseFloat(kilometraje) || 0,

      tarifaEstudiante:
        tarifaEstudianteNumero,

      tarifaEntero:
        tarifaEnteroNumero,

      precioDiesel:
        precioDieselNumero
    };


    let guardado;


    if (modoEdicion) {

      guardado =
        await actualizarUnidadStorage(
          unidad
        );

    } else {

      const nuevaUnidad =
        await agregarUnidadStorage(
          unidad
        );

      guardado =
        nuevaUnidad !== null;
    }


    if (!guardado) {

      Alert.alert(
        'Error',
        'No se pudo guardar la unidad.'
      );

      return;
    }


    setModalVisible(false);

    limpiarFormulario();

    await cargarUnidades();
  };


  // =====================================================
  // SELECCIONAR UNIDAD ACTIVA
  // =====================================================

  const seleccionarUnidad = async (unidad) => {

    if (
      unidad.id === unidadActivaId
    ) {
      return;
    }


    const guardado =
      await establecerUnidadActivaStorage(
        unidad.id
      );


    if (guardado) {

      setUnidadActivaId(
        unidad.id
      );

      Alert.alert(
        'Unidad activa',
        `Ahora estás trabajando con la Unidad ${unidad.numeroUnidad}.`
      );
    }
  };


  // =====================================================
  // ELIMINAR
  // =====================================================

  const confirmarEliminar = (unidad) => {

    Alert.alert(
      'Eliminar unidad',
      `¿Deseas eliminar la Unidad ${unidad.numeroUnidad}?`,
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
              await eliminarUnidadStorage(
                unidad.id
              );


            if (eliminada) {

              await cargarUnidades();
            }
          }
        }

      ]
    );
  };


  return (

    <View style={styles.container}>

      <ScrollView
        contentContainerStyle={
          styles.content
        }
        showsVerticalScrollIndicator={false}
      >

        <View style={styles.headerRow}>

          <View style={styles.headerText}>

            <Text style={styles.title}>
              Mis Unidades
            </Text>

            <Text style={styles.subtitle}>
              Administra los camiones de tu flotilla
            </Text>

          </View>


          <TouchableOpacity
            style={styles.addTopButton}
            onPress={abrirNuevaUnidad}
          >

            <Ionicons
              name="add"
              size={27}
              color="#FFFFFF"
            />

          </TouchableOpacity>

        </View>


        {/* RESUMEN */}

        <View style={styles.summaryCard}>

          <View>

            <Text style={styles.summaryLabel}>
              Unidades registradas
            </Text>

            <Text style={styles.summaryNumber}>
              {unidades.length}
            </Text>

          </View>


          <View style={styles.summaryIcon}>

            <Ionicons
              name="bus"
              size={32}
              color="#FFFFFF"
            />

          </View>

        </View>


        {/* SIN UNIDADES */}

        {unidades.length === 0 ? (

          <View style={styles.emptyCard}>

            <View style={styles.emptyIcon}>

              <Ionicons
                name="bus-outline"
                size={50}
                color="#94A3B8"
              />

            </View>


            <Text style={styles.emptyTitle}>
              No hay unidades registradas
            </Text>

            <Text style={styles.emptyText}>
              Agrega el primer camión para comenzar a utilizar ControlCamión.
            </Text>


            <TouchableOpacity
              style={styles.emptyButton}
              onPress={abrirNuevaUnidad}
            >

              <Ionicons
                name="add-circle-outline"
                size={21}
                color="#FFFFFF"
              />

              <Text style={styles.emptyButtonText}>
                Agregar unidad
              </Text>

            </TouchableOpacity>

          </View>

        ) : (

          <>
            <Text style={styles.sectionTitle}>
              Flotilla
            </Text>


            {unidades.map(
              (unidad) => {

                const activa =
                  unidad.id ===
                  unidadActivaId;


                return (

                  <View
                    key={unidad.id}
                    style={[
                      styles.unitCard,

                      activa &&
                      styles.activeUnitCard
                    ]}
                  >

                    {/* ENCABEZADO */}

                    <View style={styles.unitHeader}>

                      <View
                        style={[
                          styles.busIcon,

                          activa
                            ? styles.busIconActive
                            : null
                        ]}
                      >

                        <Ionicons
                          name="bus"
                          size={29}
                          color={
                            activa
                              ? '#FFFFFF'
                              : '#2563EB'
                          }
                        />

                      </View>


                      <View style={styles.unitMainInfo}>

                        <View style={styles.unitTitleRow}>

                          <Text style={styles.unitNumber}>
                            Unidad {unidad.numeroUnidad}
                          </Text>


                          {activa && (

                            <View style={styles.activeBadge}>

                              <Text style={styles.activeBadgeText}>
                                ACTIVA
                              </Text>

                            </View>

                          )}

                        </View>


                        <Text style={styles.route}>
                          {unidad.ruta}
                        </Text>

                      </View>

                    </View>


                    {/* INFORMACIÓN */}

                    <View style={styles.unitDivider} />


                    <View style={styles.infoGrid}>

                      <View style={styles.infoItem}>

                        <Text style={styles.infoLabel}>
                          Operador
                        </Text>

                        <Text
                          style={styles.infoValue}
                          numberOfLines={1}
                        >
                          {
                            unidad.operador ||
                            'No registrado'
                          }
                        </Text>

                      </View>


                      <View style={styles.infoItem}>

                        <Text style={styles.infoLabel}>
                          Kilometraje
                        </Text>

                        <Text style={styles.infoValue}>
                          {Number(
                            unidad.kilometraje || 0
                          ).toFixed(0)} km
                        </Text>

                      </View>


                      <View style={styles.infoItem}>

                        <Text style={styles.infoLabel}>
                          Estudiante
                        </Text>

                        <Text style={styles.infoValue}>
                          $
                          {Number(
                            unidad.tarifaEstudiante || 0
                          ).toFixed(2)}
                        </Text>

                      </View>


                      <View style={styles.infoItem}>

                        <Text style={styles.infoLabel}>
                          Entero
                        </Text>

                        <Text style={styles.infoValue}>
                          $
                          {Number(
                            unidad.tarifaEntero || 0
                          ).toFixed(2)}
                        </Text>

                      </View>

                    </View>


                    {/* ACCIONES */}

                    {!activa && (

                      <TouchableOpacity
                        style={styles.selectButton}
                        onPress={() =>
                          seleccionarUnidad(
                            unidad
                          )
                        }
                      >

                        <Ionicons
                          name="checkmark-circle-outline"
                          size={21}
                          color="#FFFFFF"
                        />

                        <Text style={styles.selectButtonText}>
                          Usar esta unidad
                        </Text>

                      </TouchableOpacity>

                    )}


                    {activa && (

                      <View style={styles.activeInfo}>

                        <Ionicons
                          name="checkmark-circle"
                          size={20}
                          color="#16A34A"
                        />

                        <Text style={styles.activeInfoText}>
                          Esta es la unidad seleccionada actualmente
                        </Text>

                      </View>

                    )}


                    <View style={styles.actionRow}>

                      <TouchableOpacity
                        style={[
                          styles.secondaryButton,
                          styles.editButton
                        ]}
                        onPress={() =>
                          editarUnidad(
                            unidad
                          )
                        }
                      >

                        <Ionicons
                          name="create-outline"
                          size={19}
                          color="#2563EB"
                        />

                        <Text style={styles.editButtonText}>
                          Editar
                        </Text>

                      </TouchableOpacity>


                      <TouchableOpacity
                        style={[
                          styles.secondaryButton,
                          styles.deleteButton
                        ]}
                        onPress={() =>
                          confirmarEliminar(
                            unidad
                          )
                        }
                      >

                        <Ionicons
                          name="trash-outline"
                          size={19}
                          color="#DC2626"
                        />

                        <Text style={styles.deleteButtonText}>
                          Eliminar
                        </Text>

                      </TouchableOpacity>

                    </View>

                  </View>

                );
              }
            )}


            <TouchableOpacity
              style={styles.addButton}
              onPress={abrirNuevaUnidad}
            >

              <Ionicons
                name="add-circle-outline"
                size={23}
                color="#2563EB"
              />

              <Text style={styles.addButtonText}>
                Agregar otra unidad
              </Text>

            </TouchableOpacity>

          </>

        )}

      </ScrollView>


      {/* ================================================= */}
      {/* MODAL NUEVA / EDITAR UNIDAD */}
      {/* ================================================= */}

      <Modal
        visible={modalVisible}
        transparent={true}
        animationType="slide"
        onRequestClose={() =>
          setModalVisible(false)
        }
      >

        <View style={styles.modalOverlay}>

          <View style={styles.modalContainer}>

            <View style={styles.modalHeader}>

              <View>

                <Text style={styles.modalTitle}>
                  {
                    modoEdicion
                      ? 'Editar Unidad'
                      : 'Nueva Unidad'
                  }
                </Text>

                <Text style={styles.modalSubtitle}>
                  {
                    modoEdicion
                      ? 'Modifica la información del camión'
                      : 'Registra un nuevo camión'
                  }
                </Text>

              </View>


              <TouchableOpacity
                style={styles.closeButton}
                onPress={() => {

                  setModalVisible(false);

                  limpiarFormulario();
                }}
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
              keyboardShouldPersistTaps="handled"
            >

              <Text style={styles.formSectionTitle}>
                Datos generales
              </Text>


              <Text style={styles.label}>
                Número de unidad *
              </Text>

              <TextInput
                style={styles.input}
                placeholder="Ejemplo: 127"
                placeholderTextColor="#94A3B8"
                value={numeroUnidad}
                onChangeText={setNumeroUnidad}
              />


              <Text style={styles.label}>
                Ruta *
              </Text>

              <TextInput
                style={styles.input}
                placeholder="Ejemplo: Centro - Boca del Río"
                placeholderTextColor="#94A3B8"
                value={ruta}
                onChangeText={setRuta}
              />


              <Text style={styles.label}>
                Operador
              </Text>

              <TextInput
                style={styles.input}
                placeholder="Nombre del operador"
                placeholderTextColor="#94A3B8"
                value={operador}
                onChangeText={setOperador}
              />


              <Text style={styles.label}>
                Marca
              </Text>

              <TextInput
                style={styles.input}
                placeholder="Ejemplo: Mercedes-Benz"
                placeholderTextColor="#94A3B8"
                value={marca}
                onChangeText={setMarca}
              />


              <Text style={styles.label}>
                Modelo
              </Text>

              <TextInput
                style={styles.input}
                placeholder="Modelo de la unidad"
                placeholderTextColor="#94A3B8"
                value={modelo}
                onChangeText={setModelo}
              />


              <Text style={styles.label}>
                Año
              </Text>

              <TextInput
                style={styles.input}
                keyboardType="numeric"
                placeholder="Ejemplo: 2020"
                placeholderTextColor="#94A3B8"
                value={anio}
                onChangeText={setAnio}
              />


              <Text style={styles.label}>
                Placas
              </Text>

              <TextInput
                style={styles.input}
                placeholder="Ejemplo: A-12345-X"
                placeholderTextColor="#94A3B8"
                value={placas}
                onChangeText={setPlacas}
                autoCapitalize="characters"
              />


              <Text style={styles.label}>
                Kilometraje actual
              </Text>

              <TextInput
                style={styles.input}
                keyboardType="numeric"
                placeholder="Ejemplo: 120500"
                placeholderTextColor="#94A3B8"
                value={kilometraje}
                onChangeText={setKilometraje}
              />


              <Text style={styles.formSectionTitle}>
                Tarifas
              </Text>


              <Text style={styles.label}>
                Tarifa estudiante
              </Text>

              <View style={styles.moneyInput}>

                <Text style={styles.moneySymbol}>
                  $
                </Text>

                <TextInput
                  style={styles.moneyField}
                  keyboardType="decimal-pad"
                  value={tarifaEstudiante}
                  onChangeText={setTarifaEstudiante}
                />

              </View>


              <Text style={styles.label}>
                Tarifa entero
              </Text>

              <View style={styles.moneyInput}>

                <Text style={styles.moneySymbol}>
                  $
                </Text>

                <TextInput
                  style={styles.moneyField}
                  keyboardType="decimal-pad"
                  value={tarifaEntero}
                  onChangeText={setTarifaEntero}
                />

              </View>


              <Text style={styles.formSectionTitle}>
                Combustible
              </Text>


              <Text style={styles.label}>
                Precio habitual del diésel
              </Text>

              <View style={styles.moneyInput}>

                <Text style={styles.moneySymbol}>
                  $
                </Text>

                <TextInput
                  style={styles.moneyField}
                  keyboardType="decimal-pad"
                  placeholder="Ejemplo: 25.80"
                  placeholderTextColor="#94A3B8"
                  value={precioDiesel}
                  onChangeText={setPrecioDiesel}
                />

              </View>


              <TouchableOpacity
                style={styles.saveButton}
                onPress={guardarUnidad}
              >

                <Ionicons
                  name="save-outline"
                  size={21}
                  color="#FFFFFF"
                />

                <Text style={styles.saveButtonText}>
                  {
                    modoEdicion
                      ? 'Guardar cambios'
                      : 'Registrar unidad'
                  }
                </Text>

              </TouchableOpacity>

            </ScrollView>

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

  content: {
    padding: 20,
    paddingBottom: 50
  },

  headerRow: {
    flexDirection: 'row',
    alignItems: 'center',
    marginBottom: 20
  },

  headerText: {
    flex: 1
  },

  title: {
    fontSize: 30,
    fontWeight: 'bold',
    color: '#0F172A'
  },

  subtitle: {
    color: '#64748B',
    fontSize: 15,
    marginTop: 5
  },

  addTopButton: {
    width: 50,
    height: 50,
    borderRadius: 16,
    backgroundColor: '#2563EB',
    justifyContent: 'center',
    alignItems: 'center'
  },

  summaryCard: {
    backgroundColor: '#0F172A',
    borderRadius: 20,
    padding: 21,
    flexDirection: 'row',
    alignItems: 'center',
    justifyContent: 'space-between'
  },

  summaryLabel: {
    color: '#CBD5E1',
    fontSize: 13
  },

  summaryNumber: {
    color: '#FFFFFF',
    fontSize: 35,
    fontWeight: 'bold',
    marginTop: 3
  },

  summaryIcon: {
    width: 58,
    height: 58,
    borderRadius: 18,
    backgroundColor: '#2563EB',
    alignItems: 'center',
    justifyContent: 'center'
  },

  sectionTitle: {
    color: '#0F172A',
    fontSize: 20,
    fontWeight: 'bold',
    marginTop: 25,
    marginBottom: 13
  },

  unitCard: {
    backgroundColor: '#FFFFFF',
    borderRadius: 19,
    padding: 17,
    marginBottom: 15,
    elevation: 3,
    borderWidth: 1,
    borderColor: 'transparent'
  },

  activeUnitCard: {
    borderColor: '#93C5FD',
    backgroundColor: '#F8FAFF'
  },

  unitHeader: {
    flexDirection: 'row',
    alignItems: 'center'
  },

  busIcon: {
    width: 55,
    height: 55,
    borderRadius: 17,
    backgroundColor: '#DBEAFE',
    justifyContent: 'center',
    alignItems: 'center'
  },

  busIconActive: {
    backgroundColor: '#2563EB'
  },

  unitMainInfo: {
    flex: 1,
    marginLeft: 13
  },

  unitTitleRow: {
    flexDirection: 'row',
    alignItems: 'center',
    flexWrap: 'wrap'
  },

  unitNumber: {
    color: '#0F172A',
    fontSize: 18,
    fontWeight: 'bold'
  },

  activeBadge: {
    backgroundColor: '#DCFCE7',
    borderRadius: 20,
    paddingHorizontal: 8,
    paddingVertical: 4,
    marginLeft: 8
  },

  activeBadgeText: {
    color: '#15803D',
    fontSize: 9,
    fontWeight: 'bold'
  },

  route: {
    color: '#64748B',
    fontSize: 13,
    marginTop: 4
  },

  unitDivider: {
    height: 1,
    backgroundColor: '#E2E8F0',
    marginVertical: 15
  },

  infoGrid: {
    flexDirection: 'row',
    flexWrap: 'wrap'
  },

  infoItem: {
    width: '50%',
    marginBottom: 13
  },

  infoLabel: {
    color: '#94A3B8',
    fontSize: 11
  },

  infoValue: {
    color: '#334155',
    fontSize: 14,
    fontWeight: '600',
    marginTop: 3,
    paddingRight: 8
  },

  selectButton: {
    backgroundColor: '#2563EB',
    borderRadius: 13,
    paddingVertical: 13,
    flexDirection: 'row',
    justifyContent: 'center',
    alignItems: 'center'
  },

  selectButtonText: {
    color: '#FFFFFF',
    fontWeight: 'bold',
    marginLeft: 7
  },

  activeInfo: {
    backgroundColor: '#ECFDF5',
    borderRadius: 12,
    padding: 11,
    flexDirection: 'row',
    alignItems: 'center'
  },

  activeInfoText: {
    color: '#166534',
    fontSize: 12,
    fontWeight: '600',
    marginLeft: 7,
    flex: 1
  },

  actionRow: {
    flexDirection: 'row',
    marginTop: 10
  },

  secondaryButton: {
    flex: 1,
    borderRadius: 12,
    paddingVertical: 11,
    flexDirection: 'row',
    justifyContent: 'center',
    alignItems: 'center'
  },

  editButton: {
    backgroundColor: '#EFF6FF',
    marginRight: 6
  },

  deleteButton: {
    backgroundColor: '#FEF2F2',
    marginLeft: 6
  },

  editButtonText: {
    color: '#2563EB',
    fontWeight: 'bold',
    fontSize: 13,
    marginLeft: 6
  },

  deleteButtonText: {
    color: '#DC2626',
    fontWeight: 'bold',
    fontSize: 13,
    marginLeft: 6
  },

  addButton: {
    borderWidth: 1.5,
    borderStyle: 'dashed',
    borderColor: '#93C5FD',
    backgroundColor: '#EFF6FF',
    borderRadius: 16,
    paddingVertical: 16,
    flexDirection: 'row',
    justifyContent: 'center',
    alignItems: 'center'
  },

  addButtonText: {
    color: '#2563EB',
    fontWeight: 'bold',
    marginLeft: 7
  },

  emptyCard: {
    backgroundColor: '#FFFFFF',
    borderRadius: 20,
    padding: 30,
    marginTop: 20,
    alignItems: 'center'
  },

  emptyIcon: {
    width: 95,
    height: 95,
    borderRadius: 48,
    backgroundColor: '#F1F5F9',
    justifyContent: 'center',
    alignItems: 'center'
  },

  emptyTitle: {
    color: '#0F172A',
    fontSize: 19,
    fontWeight: 'bold',
    marginTop: 17
  },

  emptyText: {
    color: '#64748B',
    fontSize: 13,
    textAlign: 'center',
    lineHeight: 19,
    marginTop: 7
  },

  emptyButton: {
    backgroundColor: '#2563EB',
    borderRadius: 14,
    paddingHorizontal: 18,
    paddingVertical: 13,
    flexDirection: 'row',
    alignItems: 'center',
    marginTop: 20
  },

  emptyButtonText: {
    color: '#FFFFFF',
    fontWeight: 'bold',
    marginLeft: 7
  },

  modalOverlay: {
    flex: 1,
    backgroundColor: 'rgba(15,23,42,0.72)',
    justifyContent: 'flex-end'
  },

  modalContainer: {
    backgroundColor: '#FFFFFF',
    borderTopLeftRadius: 26,
    borderTopRightRadius: 26,
    paddingHorizontal: 20,
    paddingTop: 20,
    paddingBottom: 25,
    maxHeight: '92%'
  },

  modalHeader: {
    flexDirection: 'row',
    alignItems: 'center',
    justifyContent: 'space-between',
    marginBottom: 15
  },

  modalTitle: {
    color: '#0F172A',
    fontSize: 24,
    fontWeight: 'bold'
  },

  modalSubtitle: {
    color: '#64748B',
    fontSize: 13,
    marginTop: 3
  },

  closeButton: {
    width: 42,
    height: 42,
    borderRadius: 21,
    backgroundColor: '#F1F5F9',
    justifyContent: 'center',
    alignItems: 'center'
  },

  formSectionTitle: {
    color: '#0F172A',
    fontSize: 18,
    fontWeight: 'bold',
    marginTop: 17,
    marginBottom: 5
  },

  label: {
    color: '#334155',
    fontSize: 14,
    fontWeight: '600',
    marginTop: 11,
    marginBottom: 7
  },

  input: {
    backgroundColor: '#F8FAFC',
    borderWidth: 1,
    borderColor: '#CBD5E1',
    borderRadius: 12,
    paddingHorizontal: 14,
    paddingVertical: 13,
    color: '#0F172A',
    fontSize: 16
  },

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
    fontSize: 17,
    fontWeight: 'bold'
  },

  moneyField: {
    flex: 1,
    color: '#0F172A',
    fontSize: 16,
    paddingVertical: 13,
    marginLeft: 5
  },

  saveButton: {
    backgroundColor: '#2563EB',
    borderRadius: 15,
    paddingVertical: 15,
    flexDirection: 'row',
    justifyContent: 'center',
    alignItems: 'center',
    marginTop: 25,
    marginBottom: 15
  },

  saveButtonText: {
    color: '#FFFFFF',
    fontSize: 16,
    fontWeight: 'bold',
    marginLeft: 7
  }

});