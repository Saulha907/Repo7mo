import AsyncStorage from '@react-native-async-storage/async-storage';

const JORNADAS_KEY = '@controlcamion_jornadas';
const GASTOS_KEY = '@controlcamion_gastos';
const RETIROS_KEY = '@controlcamion_retiros';

// Clave anterior de una sola unidad
const UNIDAD_LEGACY_KEY = '@controlcamion_unidad';

// Nuevas claves para múltiples unidades
const UNIDADES_KEY = '@controlcamion_unidades';
const UNIDAD_ACTIVA_KEY = '@controlcamion_unidad_activa';


// ======================================================
// JORNADAS
// ======================================================

export const guardarJornadaStorage = async (jornada) => {
  try {
    const datosGuardados =
      await AsyncStorage.getItem(JORNADAS_KEY);

    const jornadasActuales =
      datosGuardados
        ? JSON.parse(datosGuardados)
        : [];

    const nuevasJornadas = [
      ...jornadasActuales,
      jornada
    ];

    await AsyncStorage.setItem(
      JORNADAS_KEY,
      JSON.stringify(nuevasJornadas)
    );

    return true;

  } catch (error) {
    console.log(
      'Error al guardar jornada:',
      error
    );

    return false;
  }
};


export const obtenerJornadasStorage = async () => {
  try {
    const datosGuardados =
      await AsyncStorage.getItem(JORNADAS_KEY);

    if (!datosGuardados) {
      return [];
    }

    return JSON.parse(datosGuardados);

  } catch (error) {
    console.log(
      'Error al obtener jornadas:',
      error
    );

    return [];
  }
};


export const eliminarJornadaStorage = async (id) => {
  try {
    const jornadas =
      await obtenerJornadasStorage();

    const nuevasJornadas =
      jornadas.filter(
        (jornada) =>
          jornada.id !== id
      );

    await AsyncStorage.setItem(
      JORNADAS_KEY,
      JSON.stringify(nuevasJornadas)
    );

    return true;

  } catch (error) {
    console.log(
      'Error al eliminar jornada:',
      error
    );

    return false;
  }
};


export const eliminarTodasLasJornadas = async () => {
  try {
    await AsyncStorage.removeItem(
      JORNADAS_KEY
    );

    return true;

  } catch (error) {
    console.log(
      'Error al eliminar jornadas:',
      error
    );

    return false;
  }
};


// ======================================================
// GASTOS
// ======================================================

export const guardarGastoStorage = async (gasto) => {
  try {
    const datosGuardados =
      await AsyncStorage.getItem(GASTOS_KEY);

    const gastosActuales =
      datosGuardados
        ? JSON.parse(datosGuardados)
        : [];

    const nuevosGastos = [
      ...gastosActuales,
      gasto
    ];

    await AsyncStorage.setItem(
      GASTOS_KEY,
      JSON.stringify(nuevosGastos)
    );

    return true;

  } catch (error) {
    console.log(
      'Error al guardar gasto:',
      error
    );

    return false;
  }
};


export const obtenerGastosStorage = async () => {
  try {
    const datosGuardados =
      await AsyncStorage.getItem(GASTOS_KEY);

    if (!datosGuardados) {
      return [];
    }

    return JSON.parse(datosGuardados);

  } catch (error) {
    console.log(
      'Error al obtener gastos:',
      error
    );

    return [];
  }
};


export const eliminarGastoStorage = async (id) => {
  try {
    const gastos =
      await obtenerGastosStorage();

    const nuevosGastos =
      gastos.filter(
        (gasto) =>
          gasto.id !== id
      );

    await AsyncStorage.setItem(
      GASTOS_KEY,
      JSON.stringify(nuevosGastos)
    );

    return true;

  } catch (error) {
    console.log(
      'Error al eliminar gasto:',
      error
    );

    return false;
  }
};


// ======================================================
// RETIROS
// ======================================================

export const guardarRetiroStorage = async (retiro) => {
  try {
    const datosGuardados =
      await AsyncStorage.getItem(RETIROS_KEY);

    const retirosActuales =
      datosGuardados
        ? JSON.parse(datosGuardados)
        : [];

    const nuevosRetiros = [
      ...retirosActuales,
      retiro
    ];

    await AsyncStorage.setItem(
      RETIROS_KEY,
      JSON.stringify(nuevosRetiros)
    );

    return true;

  } catch (error) {
    console.log(
      'Error al guardar retiro:',
      error
    );

    return false;
  }
};


export const obtenerRetirosStorage = async () => {
  try {
    const datosGuardados =
      await AsyncStorage.getItem(RETIROS_KEY);

    if (!datosGuardados) {
      return [];
    }

    return JSON.parse(datosGuardados);

  } catch (error) {
    console.log(
      'Error al obtener retiros:',
      error
    );

    return [];
  }
};


export const eliminarRetiroStorage = async (id) => {
  try {
    const retiros =
      await obtenerRetirosStorage();

    const nuevosRetiros =
      retiros.filter(
        (retiro) =>
          retiro.id !== id
      );

    await AsyncStorage.setItem(
      RETIROS_KEY,
      JSON.stringify(nuevosRetiros)
    );

    return true;

  } catch (error) {
    console.log(
      'Error al eliminar retiro:',
      error
    );

    return false;
  }
};


// ======================================================
// MIGRACIÓN DE LA ANTIGUA UNIDAD
// ======================================================

const migrarUnidadAnterior = async () => {
  try {
    const unidadesGuardadas =
      await AsyncStorage.getItem(UNIDADES_KEY);

    if (unidadesGuardadas) {
      return;
    }

    const unidadAnterior =
      await AsyncStorage.getItem(
        UNIDAD_LEGACY_KEY
      );

    if (!unidadAnterior) {
      return;
    }

    const unidad =
      JSON.parse(unidadAnterior);

    const unidadMigrada = {
      ...unidad,

      id:
        unidad.id ||
        `unidad_${Date.now()}`
    };

    await AsyncStorage.setItem(
      UNIDADES_KEY,
      JSON.stringify([
        unidadMigrada
      ])
    );

    await AsyncStorage.setItem(
      UNIDAD_ACTIVA_KEY,
      unidadMigrada.id
    );

    await AsyncStorage.removeItem(
      UNIDAD_LEGACY_KEY
    );

  } catch (error) {
    console.log(
      'Error migrando unidad anterior:',
      error
    );
  }
};


// ======================================================
// UNIDADES
// ======================================================

export const obtenerUnidadesStorage = async () => {
  try {
    await migrarUnidadAnterior();

    const datosGuardados =
      await AsyncStorage.getItem(
        UNIDADES_KEY
      );

    if (!datosGuardados) {
      return [];
    }

    const unidades =
      JSON.parse(datosGuardados);

    return Array.isArray(unidades)
      ? unidades
      : [];

  } catch (error) {
    console.log(
      'Error al obtener unidades:',
      error
    );

    return [];
  }
};


// ======================================================
// AGREGAR NUEVA UNIDAD
// ======================================================

export const agregarUnidadStorage = async (unidad) => {
  try {
    const unidades =
      await obtenerUnidadesStorage();

    const nuevaUnidad = {
      ...unidad,

      id:
        unidad.id ||
        `unidad_${Date.now()}`
    };

    const nuevasUnidades = [
      ...unidades,
      nuevaUnidad
    ];

    await AsyncStorage.setItem(
      UNIDADES_KEY,
      JSON.stringify(
        nuevasUnidades
      )
    );


    const unidadActiva =
      await AsyncStorage.getItem(
        UNIDAD_ACTIVA_KEY
      );


    // La primera unidad registrada
    // se convierte automáticamente en activa.

    if (!unidadActiva) {
      await AsyncStorage.setItem(
        UNIDAD_ACTIVA_KEY,
        nuevaUnidad.id
      );
    }

    return nuevaUnidad;

  } catch (error) {
    console.log(
      'Error al agregar unidad:',
      error
    );

    return null;
  }
};


// ======================================================
// ACTUALIZAR UNA UNIDAD
// ======================================================

export const actualizarUnidadStorage = async (unidad) => {
  try {
    if (!unidad?.id) {
      return false;
    }

    const unidades =
      await obtenerUnidadesStorage();

    const nuevasUnidades =
      unidades.map(
        (item) =>
          item.id === unidad.id
            ? unidad
            : item
      );

    await AsyncStorage.setItem(
      UNIDADES_KEY,
      JSON.stringify(
        nuevasUnidades
      )
    );

    return true;

  } catch (error) {
    console.log(
      'Error al actualizar unidad:',
      error
    );

    return false;
  }
};


// ======================================================
// ESTABLECER UNIDAD ACTIVA
// ======================================================

export const establecerUnidadActivaStorage = async (id) => {
  try {
    const unidades =
      await obtenerUnidadesStorage();

    const existe =
      unidades.some(
        (unidad) =>
          unidad.id === id
      );

    if (!existe) {
      return false;
    }

    await AsyncStorage.setItem(
      UNIDAD_ACTIVA_KEY,
      id
    );

    return true;

  } catch (error) {
    console.log(
      'Error al establecer unidad activa:',
      error
    );

    return false;
  }
};


// ======================================================
// OBTENER ID DE UNIDAD ACTIVA
// ======================================================

export const obtenerUnidadActivaIdStorage = async () => {
  try {
    await migrarUnidadAnterior();

    return await AsyncStorage.getItem(
      UNIDAD_ACTIVA_KEY
    );

  } catch (error) {
    console.log(
      'Error al obtener unidad activa:',
      error
    );

    return null;
  }
};


// ======================================================
// OBTENER UNIDAD ACTIVA
//
// IMPORTANTE:
// Esta función conserva el mismo nombre que
// ya utiliza InicioScreen y JornadaScreen.
// ======================================================

export const obtenerUnidadStorage = async () => {
  try {
    const unidades =
      await obtenerUnidadesStorage();

    if (unidades.length === 0) {
      return null;
    }

    let unidadActivaId =
      await obtenerUnidadActivaIdStorage();


    // Si por alguna razón no hay unidad activa,
    // usamos la primera.

    if (!unidadActivaId) {

      unidadActivaId =
        unidades[0].id;

      await establecerUnidadActivaStorage(
        unidadActivaId
      );
    }


    const unidadActiva =
      unidades.find(
        (unidad) =>
          unidad.id === unidadActivaId
      );


    if (unidadActiva) {
      return unidadActiva;
    }


    // Si se eliminó la unidad activa,
    // usamos la primera disponible.

    await establecerUnidadActivaStorage(
      unidades[0].id
    );

    return unidades[0];

  } catch (error) {
    console.log(
      'Error al obtener unidad activa:',
      error
    );

    return null;
  }
};


// ======================================================
// GUARDAR UNIDAD
//
// Se conserva para compatibilidad con JornadaScreen.
// Cuando Jornada actualiza kilometraje o precio de diésel,
// esta función actualiza la unidad activa.
// ======================================================

export const guardarUnidadStorage = async (unidad) => {
  try {
    const unidades =
      await obtenerUnidadesStorage();


    // No existen unidades todavía.

    if (unidades.length === 0) {

      const nuevaUnidad =
        await agregarUnidadStorage(
          unidad
        );

      return nuevaUnidad !== null;
    }


    let unidadId =
      unidad.id;


    // Si el objeto no tiene ID,
    // obtenemos el ID de la unidad activa.

    if (!unidadId) {

      unidadId =
        await obtenerUnidadActivaIdStorage();
    }


    const unidadActualizada = {
      ...unidad,
      id: unidadId
    };


    return await actualizarUnidadStorage(
      unidadActualizada
    );

  } catch (error) {
    console.log(
      'Error al guardar unidad:',
      error
    );

    return false;
  }
};


// ======================================================
// ELIMINAR UNIDAD
// ======================================================

export const eliminarUnidadStorage = async (id) => {
  try {
    const unidades =
      await obtenerUnidadesStorage();

    const nuevasUnidades =
      unidades.filter(
        (unidad) =>
          unidad.id !== id
      );

    await AsyncStorage.setItem(
      UNIDADES_KEY,
      JSON.stringify(
        nuevasUnidades
      )
    );


    const unidadActivaId =
      await obtenerUnidadActivaIdStorage();


    if (
      unidadActivaId === id
    ) {

      if (
        nuevasUnidades.length > 0
      ) {

        await AsyncStorage.setItem(
          UNIDAD_ACTIVA_KEY,
          nuevasUnidades[0].id
        );

      } else {

        await AsyncStorage.removeItem(
          UNIDAD_ACTIVA_KEY
        );
      }
    }

    return true;

  } catch (error) {
    console.log(
      'Error al eliminar unidad:',
      error
    );

    return false;
  }
};