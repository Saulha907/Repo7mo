import AsyncStorage from '@react-native-async-storage/async-storage';


// ======================================================
// CLAVES
// ======================================================

const JORNADAS_KEY = '@controlcamion_jornadas';
const GASTOS_KEY = '@controlcamion_gastos';
const RETIROS_KEY = '@controlcamion_retiros';

const UNIDAD_LEGACY_KEY = '@controlcamion_unidad';

const UNIDADES_KEY = '@controlcamion_unidades';
const UNIDAD_ACTIVA_KEY = '@controlcamion_unidad_activa';


// ======================================================
// JORNADAS
// ======================================================

export const guardarJornadaStorage = async (jornada) => {

  try {

    const datos =
      await AsyncStorage.getItem(
        JORNADAS_KEY
      );


    const jornadas =
      datos
        ? JSON.parse(datos)
        : [];


    const lista =
      Array.isArray(jornadas)
        ? jornadas
        : [];


    lista.push(jornada);


    await AsyncStorage.setItem(
      JORNADAS_KEY,
      JSON.stringify(lista)
    );


    return true;

  } catch (error) {

    console.log(
      'Error guardarJornadaStorage:',
      error
    );

    return false;
  }
};


export const obtenerJornadasStorage = async () => {

  try {

    const datos =
      await AsyncStorage.getItem(
        JORNADAS_KEY
      );


    if (!datos) {
      return [];
    }


    const jornadas =
      JSON.parse(datos);


    return Array.isArray(jornadas)
      ? jornadas
      : [];

  } catch (error) {

    console.log(
      'Error obtenerJornadasStorage:',
      error
    );

    return [];
  }
};


export const eliminarJornadaStorage = async (id) => {

  try {

    const jornadas =
      await obtenerJornadasStorage();


    const nuevas =
      jornadas.filter(
        (jornada) =>
          jornada.id !== id
      );


    await AsyncStorage.setItem(
      JORNADAS_KEY,
      JSON.stringify(nuevas)
    );


    return true;

  } catch (error) {

    console.log(
      'Error eliminarJornadaStorage:',
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
      'Error eliminarTodasLasJornadas:',
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

    const datos =
      await AsyncStorage.getItem(
        GASTOS_KEY
      );


    const gastos =
      datos
        ? JSON.parse(datos)
        : [];


    const lista =
      Array.isArray(gastos)
        ? gastos
        : [];


    lista.push(gasto);


    await AsyncStorage.setItem(
      GASTOS_KEY,
      JSON.stringify(lista)
    );


    return true;

  } catch (error) {

    console.log(
      'Error guardarGastoStorage:',
      error
    );

    return false;
  }
};


export const obtenerGastosStorage = async () => {

  try {

    const datos =
      await AsyncStorage.getItem(
        GASTOS_KEY
      );


    if (!datos) {
      return [];
    }


    const gastos =
      JSON.parse(datos);


    return Array.isArray(gastos)
      ? gastos
      : [];

  } catch (error) {

    console.log(
      'Error obtenerGastosStorage:',
      error
    );

    return [];
  }
};


export const eliminarGastoStorage = async (id) => {

  try {

    const gastos =
      await obtenerGastosStorage();


    const nuevos =
      gastos.filter(
        (gasto) =>
          gasto.id !== id
      );


    await AsyncStorage.setItem(
      GASTOS_KEY,
      JSON.stringify(nuevos)
    );


    return true;

  } catch (error) {

    console.log(
      'Error eliminarGastoStorage:',
      error
    );

    return false;
  }
};


export const eliminarTodosLosGastosStorage = async () => {

  try {

    await AsyncStorage.removeItem(
      GASTOS_KEY
    );

    return true;

  } catch (error) {

    console.log(
      'Error eliminarTodosLosGastosStorage:',
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

    const datos =
      await AsyncStorage.getItem(
        RETIROS_KEY
      );


    const retiros =
      datos
        ? JSON.parse(datos)
        : [];


    const lista =
      Array.isArray(retiros)
        ? retiros
        : [];


    lista.push(retiro);


    await AsyncStorage.setItem(
      RETIROS_KEY,
      JSON.stringify(lista)
    );


    return true;

  } catch (error) {

    console.log(
      'Error guardarRetiroStorage:',
      error
    );

    return false;
  }
};


export const obtenerRetirosStorage = async () => {

  try {

    const datos =
      await AsyncStorage.getItem(
        RETIROS_KEY
      );


    if (!datos) {
      return [];
    }


    const retiros =
      JSON.parse(datos);


    return Array.isArray(retiros)
      ? retiros
      : [];

  } catch (error) {

    console.log(
      'Error obtenerRetirosStorage:',
      error
    );

    return [];
  }
};


export const eliminarRetiroStorage = async (id) => {

  try {

    const retiros =
      await obtenerRetirosStorage();


    const nuevos =
      retiros.filter(
        (retiro) =>
          retiro.id !== id
      );


    await AsyncStorage.setItem(
      RETIROS_KEY,
      JSON.stringify(nuevos)
    );


    return true;

  } catch (error) {

    console.log(
      'Error eliminarRetiroStorage:',
      error
    );

    return false;
  }
};


export const eliminarTodosLosRetirosStorage = async () => {

  try {

    await AsyncStorage.removeItem(
      RETIROS_KEY
    );

    return true;

  } catch (error) {

    console.log(
      'Error eliminarTodosLosRetirosStorage:',
      error
    );

    return false;
  }
};


// ======================================================
// MIGRACIÓN DE UNIDAD ANTIGUA
// ======================================================

const migrarUnidadAnterior = async () => {

  try {

    const unidadesNuevas =
      await AsyncStorage.getItem(
        UNIDADES_KEY
      );


    if (unidadesNuevas) {
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
      JSON.parse(
        unidadAnterior
      );


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
      'Error migrarUnidadAnterior:',
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


    const datos =
      await AsyncStorage.getItem(
        UNIDADES_KEY
      );


    if (!datos) {
      return [];
    }


    const unidades =
      JSON.parse(datos);


    return Array.isArray(unidades)
      ? unidades
      : [];

  } catch (error) {

    console.log(
      'Error obtenerUnidadesStorage:',
      error
    );

    return [];
  }
};


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


    const nuevas = [
      ...unidades,
      nuevaUnidad
    ];


    await AsyncStorage.setItem(
      UNIDADES_KEY,
      JSON.stringify(nuevas)
    );


    const unidadActiva =
      await AsyncStorage.getItem(
        UNIDAD_ACTIVA_KEY
      );


    if (!unidadActiva) {

      await AsyncStorage.setItem(
        UNIDAD_ACTIVA_KEY,
        nuevaUnidad.id
      );
    }


    return nuevaUnidad;

  } catch (error) {

    console.log(
      'Error agregarUnidadStorage:',
      error
    );

    return null;
  }
};


export const actualizarUnidadStorage = async (unidad) => {

  try {

    if (!unidad?.id) {
      return false;
    }


    const unidades =
      await obtenerUnidadesStorage();


    const existe =
      unidades.some(
        (item) =>
          item.id === unidad.id
      );


    if (!existe) {
      return false;
    }


    const nuevas =
      unidades.map(
        (item) =>

          item.id === unidad.id
            ? {
                ...item,
                ...unidad
              }
            : item
      );


    await AsyncStorage.setItem(
      UNIDADES_KEY,
      JSON.stringify(nuevas)
    );


    return true;

  } catch (error) {

    console.log(
      'Error actualizarUnidadStorage:',
      error
    );

    return false;
  }
};


export const establecerUnidadActivaStorage = async (id) => {

  try {

    if (!id) {
      return false;
    }


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
      'Error establecerUnidadActivaStorage:',
      error
    );

    return false;
  }
};


export const obtenerUnidadActivaIdStorage = async () => {

  try {

    await migrarUnidadAnterior();


    return await AsyncStorage.getItem(
      UNIDAD_ACTIVA_KEY
    );

  } catch (error) {

    console.log(
      'Error obtenerUnidadActivaIdStorage:',
      error
    );

    return null;
  }
};


export const obtenerUnidadStorage = async () => {

  try {

    const unidades =
      await obtenerUnidadesStorage();


    if (unidades.length === 0) {
      return null;
    }


    let activaId =
      await AsyncStorage.getItem(
        UNIDAD_ACTIVA_KEY
      );


    if (!activaId) {

      activaId =
        unidades[0].id;


      await AsyncStorage.setItem(
        UNIDAD_ACTIVA_KEY,
        activaId
      );
    }


    const activa =
      unidades.find(
        (unidad) =>
          unidad.id === activaId
      );


    if (activa) {
      return activa;
    }


    await AsyncStorage.setItem(
      UNIDAD_ACTIVA_KEY,
      unidades[0].id
    );


    return unidades[0];

  } catch (error) {

    console.log(
      'Error obtenerUnidadStorage:',
      error
    );

    return null;
  }
};


export const guardarUnidadStorage = async (unidad) => {

  try {

    const unidades =
      await obtenerUnidadesStorage();


    if (unidades.length === 0) {

      const nueva =
        await agregarUnidadStorage(
          unidad
        );


      return nueva !== null;
    }


    let unidadId =
      unidad?.id;


    if (!unidadId) {

      unidadId =
        await AsyncStorage.getItem(
          UNIDAD_ACTIVA_KEY
        );
    }


    if (!unidadId) {
      return false;
    }


    return await actualizarUnidadStorage({

      ...unidad,

      id:
        unidadId
    });

  } catch (error) {

    console.log(
      'Error guardarUnidadStorage:',
      error
    );

    return false;
  }
};


export const eliminarUnidadStorage = async (id) => {

  try {

    if (!id) {
      return false;
    }


    const unidades =
      await obtenerUnidadesStorage();


    const nuevas =
      unidades.filter(
        (unidad) =>
          unidad.id !== id
      );


    await AsyncStorage.setItem(
      UNIDADES_KEY,
      JSON.stringify(nuevas)
    );


    const activaId =
      await AsyncStorage.getItem(
        UNIDAD_ACTIVA_KEY
      );


    if (activaId === id) {

      if (nuevas.length > 0) {

        await AsyncStorage.setItem(
          UNIDAD_ACTIVA_KEY,
          nuevas[0].id
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
      'Error eliminarUnidadStorage:',
      error
    );

    return false;
  }
};


export const eliminarTodasLasUnidadesStorage = async () => {

  try {

    await AsyncStorage.multiRemove([
      UNIDADES_KEY,
      UNIDAD_ACTIVA_KEY,
      UNIDAD_LEGACY_KEY
    ]);


    return true;

  } catch (error) {

    console.log(
      'Error eliminarTodasLasUnidadesStorage:',
      error
    );

    return false;
  }
};


// ======================================================
// RESTABLECER MOVIMIENTOS
// ======================================================

export const restablecerMovimientosStorage = async () => {

  try {

    await AsyncStorage.multiRemove([
      JORNADAS_KEY,
      GASTOS_KEY,
      RETIROS_KEY
    ]);


    return true;

  } catch (error) {

    console.log(
      'Error restablecerMovimientosStorage:',
      error
    );

    return false;
  }
};


// ======================================================
// RESTABLECER TODA LA APP
// ======================================================

export const restablecerTodoStorage = async () => {

  try {

    await AsyncStorage.multiRemove([
      JORNADAS_KEY,
      GASTOS_KEY,
      RETIROS_KEY,
      UNIDADES_KEY,
      UNIDAD_ACTIVA_KEY,
      UNIDAD_LEGACY_KEY
    ]);


    return true;

  } catch (error) {

    console.log(
      'Error restablecerTodoStorage:',
      error
    );

    return false;
  }
};


// ======================================================
// RESUMEN FINANCIERO GENERAL
// ======================================================

export const obtenerResumenFinancieroStorage = async () => {

  try {

    const jornadas =
      await obtenerJornadasStorage();

    const gastos =
      await obtenerGastosStorage();

    const retiros =
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
      retiros.reduce(
        (total, retiro) =>

          total +
          Number(
            retiro.monto || 0
          ),

        0
      );


    const gastosTotales =
      diesel +
      otrosGastos;


    const gananciaOperativa =
      ingresos -
      gastosTotales;


    const saldoDisponible =
      gananciaOperativa -
      totalRetiros;


    return {

      ingresos,

      diesel,

      otrosGastos,

      gastosTotales,

      retiros:
        totalRetiros,

      gananciaOperativa,

      saldoDisponible
    };

  } catch (error) {

    console.log(
      'Error obtenerResumenFinancieroStorage:',
      error
    );


    return {

      ingresos: 0,

      diesel: 0,

      otrosGastos: 0,

      gastosTotales: 0,

      retiros: 0,

      gananciaOperativa: 0,

      saldoDisponible: 0
    };
  }
};