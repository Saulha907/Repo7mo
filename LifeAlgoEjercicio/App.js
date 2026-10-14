import "react-native-gesture-handler";

import React, {
  createContext,
  useContext,
  useEffect,
  useRef,
  useState,
} from "react";

import {
  View,
  Text,
  StyleSheet,
  TouchableOpacity,
  Animated,
  ScrollView,
  Switch,
} from "react-native";

import { NavigationContainer } from "@react-navigation/native";
import { createDrawerNavigator } from "@react-navigation/drawer";
import { createBottomTabNavigator } from "@react-navigation/bottom-tabs";

import { Ionicons } from "@expo/vector-icons";


// ======================================================
// NAVEGADORES
// ======================================================

const Drawer = createDrawerNavigator();
const Tab = createBottomTabNavigator();


// ======================================================
// CONTEXTO DE LA APLICACIÓN
// Sirve para compartir los resultados entre pantallas.
// ======================================================

const GameContext = createContext();

function useGame() {
  return useContext(GameContext);
}


// ======================================================
// LOGOTIPO
// ======================================================

function Logo() {
  return (
    <View style={styles.logoContainer}>

      <View style={styles.logoIcon}>
        <Ionicons
          name="dice-outline"
          size={55}
          color="white"
        />
      </View>

      <Text style={styles.logoText}>
        DICE GAME
      </Text>

    </View>
  );
}


// ======================================================
// SPLASH SCREEN ANIMADO
// ======================================================

function SplashScreen() {

  const escala = useRef(
    new Animated.Value(0)
  ).current;

  const opacidad = useRef(
    new Animated.Value(0)
  ).current;


  useEffect(() => {

    Animated.parallel([

      Animated.spring(escala, {
        toValue: 1,
        friction: 4,
        useNativeDriver: true,
      }),

      Animated.timing(opacidad, {
        toValue: 1,
        duration: 1200,
        useNativeDriver: true,
      }),

    ]).start();

  }, []);


  return (
    <View style={styles.splashContainer}>

      <Animated.View
        style={[
          styles.splashLogo,
          {
            opacity: opacidad,
            transform: [
              { scale: escala }
            ],
          },
        ]}
      >

        <Ionicons
          name="dice"
          size={90}
          color="white"
        />

      </Animated.View>


      <Animated.Text
        style={[
          styles.splashTitle,
          {
            opacity: opacidad,
          },
        ]}
      >
        DICE GAME
      </Animated.Text>


      <Text style={styles.splashSubtitle}>
        Prueba tu suerte
      </Text>

    </View>
  );
}


// ======================================================
// BOTÓN PERSONALIZADO
// ======================================================

function GameButton({
  texto,
  icono,
  onPress,
  secundario = false,
}) {

  return (

    <TouchableOpacity
      style={
        secundario
          ? styles.secondaryButton
          : styles.mainButton
      }
      onPress={onPress}
    >

      <Ionicons
        name={icono}
        size={25}
        color={
          secundario
            ? "#111"
            : "white"
        }
      />

      <Text
        style={
          secundario
            ? styles.secondaryButtonText
            : styles.mainButtonText
        }
      >
        {texto}
      </Text>

    </TouchableOpacity>
  );
}


// ======================================================
// INICIO
// ======================================================

function HomeScreen({ navigation }) {

  return (
    <View style={styles.container}>

      <Logo />

      <Text style={styles.homeTitle}>
        ¡Bienvenido!
      </Text>

      <Text style={styles.description}>
        Lanza dos dados, guarda tus resultados
        y consulta tus estadísticas.
      </Text>


      <GameButton
        texto="Jugar ahora"
        icono="game-controller-outline"
        onPress={() =>
          navigation.navigate("Juego de Dados")
        }
      />

    </View>
  );
}


// ======================================================
// TARJETA DE DADO
// ======================================================

function DiceCard({
  titulo,
  numero,
  simbolo,
  rotacion,
}) {

  return (
    <View style={styles.diceCard}>

      <Text style={styles.diceLabel}>
        {titulo}
      </Text>

      <Animated.Text
        style={[
          styles.dice,
          {
            transform: [
              { rotate: rotacion }
            ],
          },
        ]}
      >
        {simbolo}
      </Animated.Text>

      <Text style={styles.diceNumber}>
        Número: {numero}
      </Text>

    </View>
  );
}


// ======================================================
// RESULTADO
// ======================================================

function ResultCard({
  dado1,
  dado2,
}) {

  const { mostrarSuma } = useGame();

  if (!mostrarSuma) {
    return null;
  }

  return (
    <View style={styles.resultContainer}>

      <Text style={styles.resultTitle}>
        Resultado
      </Text>

      <Text style={styles.resultNumber}>
        {dado1 + dado2}
      </Text>

      <Text style={styles.operation}>
        {dado1} + {dado2} = {dado1 + dado2}
      </Text>

    </View>
  );
}


// ======================================================
// COMPONENTE 1
// JUEGO DE DADOS
// ======================================================

function DiceScreen() {

  const {
    registrarLanzamiento,
    animacionesActivas,
  } = useGame();


  const [dado1, setDado1] =
    useState(1);

  const [dado2, setDado2] =
    useState(1);


  const animacion = useRef(
    new Animated.Value(0)
  ).current;


  const dados = [
    "⚀",
    "⚁",
    "⚂",
    "⚃",
    "⚄",
    "⚅",
  ];


  // ==================================================
  // LANZAR DADOS
  // ==================================================

  function lanzarDados() {

    const numero1 =
      Math.floor(Math.random() * 6) + 1;

    const numero2 =
      Math.floor(Math.random() * 6) + 1;


    setDado1(numero1);
    setDado2(numero2);


    registrarLanzamiento(
      numero1,
      numero2
    );


    if (animacionesActivas) {

      animacion.setValue(0);

      Animated.timing(animacion, {
        toValue: 1,
        duration: 600,
        useNativeDriver: true,
      }).start();

    }

  }


  // ==================================================
  // REINICIAR DADOS
  // ==================================================

  function reiniciarDados() {

    setDado1(1);
    setDado2(1);

    animacion.setValue(0);

  }


  // ==================================================
  // ROTACIÓN
  // ==================================================

  const rotacion =
    animacion.interpolate({

      inputRange: [0, 1],

      outputRange: [
        "0deg",
        "360deg",
      ],

    });


  return (
    <ScrollView
      contentContainerStyle={
        styles.diceScreen
      }
    >

      <Text style={styles.diceTitle}>
        Lanza los dados
      </Text>

      <Text style={styles.diceSubtitle}>
        Presiona el botón para probar tu suerte
      </Text>


      <View style={styles.diceContainer}>

        <DiceCard
          titulo="Dado 1"
          numero={dado1}
          simbolo={
            dados[dado1 - 1]
          }
          rotacion={rotacion}
        />


        <DiceCard
          titulo="Dado 2"
          numero={dado2}
          simbolo={
            dados[dado2 - 1]
          }
          rotacion={rotacion}
        />

      </View>


      <ResultCard
        dado1={dado1}
        dado2={dado2}
      />


      <GameButton
        texto="Lanzar dados"
        icono="dice-outline"
        onPress={lanzarDados}
      />


      <GameButton
        texto="Reiniciar"
        icono="refresh-outline"
        onPress={reiniciarDados}
        secundario={true}
      />

    </ScrollView>
  );
}


// ======================================================
// SEGUNDA PESTAÑA DEL TAB NAVIGATION
// ======================================================

function InstructionsScreen() {

  return (
    <View style={styles.container}>

      <Ionicons
        name="help-circle-outline"
        size={85}
        color="#111"
      />


      <Text style={styles.sectionTitle}>
        ¿Cómo jugar?
      </Text>


      <View style={styles.instructionsCard}>

        <Text style={styles.instruction}>
          1. Presiona "Lanzar dados".
        </Text>

        <Text style={styles.instruction}>
          2. Cada dado obtiene un número del 1 al 6.
        </Text>

        <Text style={styles.instruction}>
          3. Se muestra la suma de los dos dados.
        </Text>

        <Text style={styles.instruction}>
          4. Cada lanzamiento se guarda en el historial.
        </Text>

        <Text style={styles.instruction}>
          5. Las estadísticas se actualizan automáticamente.
        </Text>

      </View>

    </View>
  );
}


// ======================================================
// TAB NAVIGATION
// MÍNIMO 2 BOTONES
// ======================================================

function GameTabs() {

  return (
    <Tab.Navigator

      screenOptions={{

        headerShown: false,

        tabBarActiveTintColor: "#111",

        tabBarInactiveTintColor: "#888",

        tabBarStyle: {
          height: 65,
          paddingTop: 5,
          paddingBottom: 7,
        },

      }}

    >

      <Tab.Screen
        name="Dados"
        component={DiceScreen}

        options={{

          tabBarIcon: ({
            color,
            size,
          }) => (

            <Ionicons
              name="dice-outline"
              color={color}
              size={size}
            />

          ),

        }}
      />


      <Tab.Screen
        name="Cómo jugar"
        component={InstructionsScreen}

        options={{

          tabBarIcon: ({
            color,
            size,
          }) => (

            <Ionicons
              name="help-circle-outline"
              color={color}
              size={size}
            />

          ),

        }}
      />

    </Tab.Navigator>
  );
}


// ======================================================
// COMPONENTE 2
// HISTORIAL
// ======================================================

function HistoryScreen() {

  const {
    historial,
  } = useGame();


  return (
    <View style={styles.pageContainer}>

      <View style={styles.pageHeader}>

        <Ionicons
          name="time-outline"
          size={42}
          color="#111"
        />

        <View style={styles.pageHeaderText}>

          <Text style={styles.pageTitle}>
            Historial
          </Text>

          <Text style={styles.pageSubtitle}>
            Tus lanzamientos recientes
          </Text>

        </View>

      </View>


      {historial.length === 0 ? (

        <View style={styles.emptyContainer}>

          <Ionicons
            name="file-tray-outline"
            size={70}
            color="#aaa"
          />

          <Text style={styles.emptyTitle}>
            Sin lanzamientos
          </Text>

          <Text style={styles.emptyText}>
            Los resultados aparecerán aquí
            cuando lances los dados.
          </Text>

        </View>

      ) : (

        <ScrollView
          showsVerticalScrollIndicator={false}
        >

          {historial.map(
            (item, index) => (

              <View
                key={item.id}
                style={styles.historyCard}
              >

                <View
                  style={
                    styles.historyNumber
                  }
                >

                  <Text
                    style={
                      styles.historyNumberText
                    }
                  >
                    {historial.length - index}
                  </Text>

                </View>


                <View
                  style={
                    styles.historyInfo
                  }
                >

                  <Text
                    style={
                      styles.historyTitle
                    }
                  >
                    Dado 1: {item.dado1}
                    {"   "}
                    Dado 2: {item.dado2}
                  </Text>

                  <Text
                    style={
                      styles.historyTime
                    }
                  >
                    {item.hora}
                  </Text>

                </View>


                <View
                  style={
                    styles.historyResult
                  }
                >

                  <Text
                    style={
                      styles.historyResultLabel
                    }
                  >
                    Suma
                  </Text>

                  <Text
                    style={
                      styles.historyResultNumber
                    }
                  >
                    {item.suma}
                  </Text>

                </View>

              </View>

            )
          )}

        </ScrollView>

      )}

    </View>
  );
}


// ======================================================
// COMPONENTE 3
// ESTADÍSTICAS
// ======================================================

function StatisticsScreen() {

  const {
    historial,
  } = useGame();


  const totalLanzamientos =
    historial.length;


  const totalDobles =
    historial.filter(
      (item) =>
        item.dado1 === item.dado2
    ).length;


  const promedio =
    totalLanzamientos > 0
      ? (
          historial.reduce(
            (total, item) =>
              total + item.suma,
            0
          ) / totalLanzamientos
        ).toFixed(1)
      : "0.0";


  // Cuenta cuántas veces salió cada cara
  // tomando en cuenta ambos dados.

  const frecuencias =
    [1, 2, 3, 4, 5, 6].map(
      (numero) => {

        let cantidad = 0;

        historial.forEach(
          (item) => {

            if (
              item.dado1 === numero
            ) {
              cantidad++;
            }

            if (
              item.dado2 === numero
            ) {
              cantidad++;
            }

          }
        );

        return cantidad;
      }
    );


  const maximo =
    Math.max(
      ...frecuencias,
      1
    );


  let numeroFrecuente = "-";


  if (totalLanzamientos > 0) {

    const mayor =
      Math.max(...frecuencias);

    numeroFrecuente =
      frecuencias.indexOf(mayor) + 1;

  }


  return (
    <ScrollView
      contentContainerStyle={
        styles.statisticsContainer
      }
    >

      <View style={styles.pageHeader}>

        <Ionicons
          name="stats-chart-outline"
          size={42}
          color="#111"
        />

        <View style={styles.pageHeaderText}>

          <Text style={styles.pageTitle}>
            Estadísticas
          </Text>

          <Text style={styles.pageSubtitle}>
            Información de tus partidas
          </Text>

        </View>

      </View>


      <View style={styles.statsGrid}>

        <View style={styles.statCard}>

          <Ionicons
            name="dice-outline"
            size={30}
            color="#111"
          />

          <Text style={styles.statNumber}>
            {totalLanzamientos}
          </Text>

          <Text style={styles.statLabel}>
            Lanzamientos
          </Text>

        </View>


        <View style={styles.statCard}>

          <Ionicons
            name="calculator-outline"
            size={30}
            color="#111"
          />

          <Text style={styles.statNumber}>
            {promedio}
          </Text>

          <Text style={styles.statLabel}>
            Promedio
          </Text>

        </View>


        <View style={styles.statCard}>

          <Ionicons
            name="copy-outline"
            size={30}
            color="#111"
          />

          <Text style={styles.statNumber}>
            {totalDobles}
          </Text>

          <Text style={styles.statLabel}>
            Dobles
          </Text>

        </View>


        <View style={styles.statCard}>

          <Ionicons
            name="trophy-outline"
            size={30}
            color="#111"
          />

          <Text style={styles.statNumber}>
            {numeroFrecuente}
          </Text>

          <Text style={styles.statLabel}>
            Más frecuente
          </Text>

        </View>

      </View>


      <View style={styles.chartCard}>

        <Text style={styles.chartTitle}>
          Frecuencia de números
        </Text>


        {[1, 2, 3, 4, 5, 6].map(
          (numero) => {

            const cantidad =
              frecuencias[numero - 1];

            const porcentaje =
              (cantidad / maximo) * 100;


            return (

              <View
                key={numero}
                style={styles.barRow}
              >

                <View
                  style={styles.barNumber}
                >
                  <Text
                    style={
                      styles.barNumberText
                    }
                  >
                    {numero}
                  </Text>
                </View>


                <View
                  style={
                    styles.barBackground
                  }
                >

                  <View
                    style={[
                      styles.barFill,
                      {
                        width:
                          `${porcentaje}%`,
                      },
                    ]}
                  />

                </View>


                <Text
                  style={styles.barValue}
                >
                  {cantidad}
                </Text>

              </View>

            );

          }
        )}

      </View>

    </ScrollView>
  );
}


// ======================================================
// COMPONENTE 4
// CONFIGURACIÓN
// ======================================================

function SettingsScreen() {

  const {
    animacionesActivas,
    setAnimacionesActivas,

    mostrarSuma,
    setMostrarSuma,

    borrarHistorial,
  } = useGame();


  return (
    <ScrollView
      contentContainerStyle={
        styles.settingsContainer
      }
    >

      <View style={styles.pageHeader}>

        <Ionicons
          name="settings-outline"
          size={42}
          color="#111"
        />

        <View style={styles.pageHeaderText}>

          <Text style={styles.pageTitle}>
            Configuración
          </Text>

          <Text style={styles.pageSubtitle}>
            Personaliza el juego
          </Text>

        </View>

      </View>


      <View style={styles.settingsCard}>

        <View style={styles.settingRow}>

          <View
            style={styles.settingLeft}
          >

            <View
              style={styles.settingIcon}
            >

              <Ionicons
                name="flash-outline"
                size={24}
                color="#111"
              />

            </View>


            <View>

              <Text
                style={
                  styles.settingTitle
                }
              >
                Animaciones
              </Text>

              <Text
                style={
                  styles.settingDescription
                }
              >
                Animar los dados al lanzar
              </Text>

            </View>

          </View>


          <Switch
            value={
              animacionesActivas
            }
            onValueChange={
              setAnimacionesActivas
            }
          />

        </View>


        <View style={styles.separator} />


        <View style={styles.settingRow}>

          <View
            style={styles.settingLeft}
          >

            <View
              style={styles.settingIcon}
            >

              <Ionicons
                name="calculator-outline"
                size={24}
                color="#111"
              />

            </View>


            <View>

              <Text
                style={
                  styles.settingTitle
                }
              >
                Mostrar suma
              </Text>

              <Text
                style={
                  styles.settingDescription
                }
              >
                Mostrar el resultado total
              </Text>

            </View>

          </View>


          <Switch
            value={mostrarSuma}
            onValueChange={
              setMostrarSuma
            }
          />

        </View>

      </View>


      <View style={styles.settingsCard}>

        <Text style={styles.dangerTitle}>
          Datos del juego
        </Text>

        <Text style={styles.dangerText}>
          Esta opción elimina todos los
          lanzamientos y reinicia las estadísticas.
        </Text>


        <TouchableOpacity
          style={styles.deleteButton}
          onPress={borrarHistorial}
        >

          <Ionicons
            name="trash-outline"
            size={23}
            color="white"
          />

          <Text style={styles.deleteButtonText}>
            Borrar historial
          </Text>

        </TouchableOpacity>

      </View>


      <View style={styles.aboutCard}>

        <Logo />

        <Text style={styles.versionText}>
          Versión 1.0
        </Text>

        <Text style={styles.aboutText}>
          Aplicación desarrollada con
          React Native y JavaScript.
        </Text>

      </View>

    </ScrollView>
  );
}


// ======================================================
// NAVIGATION DRAWER
// ======================================================

function NavDrawer() {

  return (

    <Drawer.Navigator

      initialRouteName="Inicio"

      screenOptions={{

        drawerActiveTintColor:
          "#111",

        drawerInactiveTintColor:
          "#666",

        drawerActiveBackgroundColor:
          "#e0e0e0",

        drawerLabelStyle: {
          fontSize: 16,
          fontWeight: "500",
        },

        drawerItemStyle: {
          borderRadius: 20,
        },

        headerStyle: {
          backgroundColor: "#111",
        },

        headerTintColor: "white",

        headerTitleStyle: {
          fontWeight: "bold",
        },

      }}

    >


      {/* ================= INICIO ================= */}

      <Drawer.Screen

        name="Inicio"

        component={HomeScreen}

        options={{

          drawerIcon: ({
            color,
            size,
          }) => (

            <Ionicons
              name="home-outline"
              size={size}
              color={color}
            />

          ),

        }}

      />


      {/* ============ JUEGO DE DADOS ============ */}

      <Drawer.Screen

        name="Juego de Dados"

        component={GameTabs}

        options={{

          drawerIcon: ({
            color,
            size,
          }) => (

            <Ionicons
              name="dice-outline"
              size={size}
              color={color}
            />

          ),

        }}

      />


      {/* ================= HISTORIAL ================= */}

      <Drawer.Screen

        name="Historial"

        component={HistoryScreen}

        options={{

          drawerIcon: ({
            color,
            size,
          }) => (

            <Ionicons
              name="time-outline"
              size={size}
              color={color}
            />

          ),

        }}

      />


      {/* =============== ESTADÍSTICAS =============== */}

      <Drawer.Screen

        name="Estadísticas"

        component={StatisticsScreen}

        options={{

          drawerIcon: ({
            color,
            size,
          }) => (

            <Ionicons
              name="stats-chart-outline"
              size={size}
              color={color}
            />

          ),

        }}

      />


      {/* ============== CONFIGURACIÓN ============== */}

      <Drawer.Screen

        name="Configuración"

        component={SettingsScreen}

        options={{

          drawerIcon: ({
            color,
            size,
          }) => (

            <Ionicons
              name="settings-outline"
              size={size}
              color={color}
            />

          ),

        }}

      />

    </Drawer.Navigator>
  );
}


// ======================================================
// APP PRINCIPAL
// ======================================================

export default function App() {

  const [
    mostrarSplash,
    setMostrarSplash,
  ] = useState(true);


  const [
    historial,
    setHistorial,
  ] = useState([]);


  const [
    animacionesActivas,
    setAnimacionesActivas,
  ] = useState(true);


  const [
    mostrarSuma,
    setMostrarSuma,
  ] = useState(true);


  // ==================================================
  // SPLASH
  // ==================================================

  useEffect(() => {

    const tiempo =
      setTimeout(() => {

        setMostrarSplash(false);

      }, 3000);


    return () =>
      clearTimeout(tiempo);

  }, []);


  // ==================================================
  // GUARDAR LANZAMIENTO
  // ==================================================

  function registrarLanzamiento(
    dado1,
    dado2
  ) {

    const nuevoLanzamiento = {

      id:
        Date.now().toString(),

      dado1,

      dado2,

      suma:
        dado1 + dado2,

      hora:
        new Date()
          .toLocaleTimeString(),

    };


    setHistorial(
      (historialAnterior) => [

        nuevoLanzamiento,

        ...historialAnterior,

      ]
    );

  }


  // ==================================================
  // BORRAR HISTORIAL
  // ==================================================

  function borrarHistorial() {

    setHistorial([]);

  }


  // ==================================================
  // SPLASH SCREEN
  // ==================================================

  if (mostrarSplash) {

    return <SplashScreen />;

  }


  // ==================================================
  // APLICACIÓN
  // ==================================================

  return (

    <GameContext.Provider
      value={{

        historial,

        registrarLanzamiento,

        borrarHistorial,

        animacionesActivas,

        setAnimacionesActivas,

        mostrarSuma,

        setMostrarSuma,

      }}
    >

      <NavigationContainer>

        <NavDrawer />

      </NavigationContainer>

    </GameContext.Provider>

  );
}


// ======================================================
// ESTILOS
// ======================================================

const styles =
  StyleSheet.create({

    // ==================================================
    // GENERAL
    // ==================================================

    container: {

      flex: 1,

      backgroundColor:
        "#f5f5f5",

      justifyContent:
        "center",

      alignItems:
        "center",

      padding: 25,

    },


    pageContainer: {

      flex: 1,

      backgroundColor:
        "#f5f5f5",

      padding: 20,

    },


    pageHeader: {

      flexDirection:
        "row",

      alignItems:
        "center",

      marginBottom: 25,

    },


    pageHeaderText: {

      marginLeft: 15,

    },


    pageTitle: {

      fontSize: 28,

      fontWeight:
        "bold",

      color: "#111",

    },


    pageSubtitle: {

      fontSize: 15,

      color: "#777",

      marginTop: 2,

    },


    sectionTitle: {

      fontSize: 29,

      fontWeight:
        "bold",

      color: "#111",

      marginTop: 15,

      marginBottom: 25,

    },


    // ==================================================
    // LOGOTIPO
    // ==================================================

    logoContainer: {

      alignItems:
        "center",

    },


    logoIcon: {

      width: 100,

      height: 100,

      backgroundColor:
        "#111",

      borderRadius: 25,

      justifyContent:
        "center",

      alignItems:
        "center",

      marginBottom: 10,

    },


    logoText: {

      fontSize: 26,

      fontWeight:
        "bold",

      letterSpacing: 2,

      color: "#111",

    },


    // ==================================================
    // SPLASH
    // ==================================================

    splashContainer: {

      flex: 1,

      backgroundColor:
        "#111",

      justifyContent:
        "center",

      alignItems:
        "center",

    },


    splashLogo: {

      width: 150,

      height: 150,

      borderRadius: 35,

      backgroundColor:
        "#292929",

      justifyContent:
        "center",

      alignItems:
        "center",

      marginBottom: 25,

    },


    splashTitle: {

      color: "white",

      fontSize: 33,

      fontWeight:
        "bold",

      letterSpacing: 4,

    },


    splashSubtitle: {

      color: "#aaa",

      fontSize: 16,

      marginTop: 10,

    },


    // ==================================================
    // HOME
    // ==================================================

    homeTitle: {

      fontSize: 31,

      fontWeight:
        "bold",

      color: "#111",

      marginTop: 40,

      marginBottom: 10,

    },


    description: {

      fontSize: 17,

      textAlign:
        "center",

      color: "#666",

      lineHeight: 25,

      marginBottom: 25,

    },


    // ==================================================
    // BOTONES
    // ==================================================

    mainButton: {

      flexDirection:
        "row",

      backgroundColor:
        "#111",

      paddingVertical:
        15,

      paddingHorizontal:
        30,

      borderRadius: 15,

      alignItems:
        "center",

      justifyContent:
        "center",

      minWidth: 210,

    },


    mainButtonText: {

      color: "white",

      fontSize: 17,

      fontWeight:
        "bold",

      marginLeft: 10,

    },


    secondaryButton: {

      flexDirection:
        "row",

      alignItems:
        "center",

      justifyContent:
        "center",

      padding: 12,

      marginTop: 8,

    },


    secondaryButtonText: {

      color: "#111",

      fontSize: 16,

      fontWeight:
        "bold",

      marginLeft: 7,

    },


    // ==================================================
    // DADOS
    // ==================================================

    diceScreen: {

      flexGrow: 1,

      backgroundColor:
        "#f5f5f5",

      alignItems:
        "center",

      justifyContent:
        "center",

      padding: 20,

    },


    diceTitle: {

      fontSize: 30,

      fontWeight:
        "bold",

      color: "#111",

    },


    diceSubtitle: {

      fontSize: 15,

      color: "#777",

      marginTop: 5,

      marginBottom: 30,

    },


    diceContainer: {

      flexDirection:
        "row",

      justifyContent:
        "center",

    },


    diceCard: {

      width: 145,

      height: 180,

      backgroundColor:
        "white",

      borderRadius: 20,

      justifyContent:
        "center",

      alignItems:
        "center",

      marginHorizontal: 8,

      elevation: 5,

      shadowColor:
        "#000",

      shadowOffset: {
        width: 0,
        height: 3,
      },

      shadowOpacity:
        0.15,

      shadowRadius: 5,

    },


    diceLabel: {

      fontSize: 16,

      fontWeight:
        "bold",

      color: "#555",

    },


    dice: {

      fontSize: 85,

      color: "#111",

    },


    diceNumber: {

      fontSize: 17,

      fontWeight:
        "bold",

      color: "#111",

    },


    // ==================================================
    // RESULTADO
    // ==================================================

    resultContainer: {

      alignItems:
        "center",

      marginVertical: 25,

    },


    resultTitle: {

      fontSize: 17,

      color: "#777",

    },


    resultNumber: {

      fontSize: 50,

      fontWeight:
        "bold",

      color: "#111",

    },


    operation: {

      fontSize: 18,

      color: "#555",

    },


    // ==================================================
    // INSTRUCCIONES
    // ==================================================

    instructionsCard: {

      width: "100%",

      backgroundColor:
        "white",

      borderRadius: 20,

      padding: 25,

      shadowColor:
        "#000",

      shadowOpacity:
        0.08,

      shadowRadius: 8,

      elevation: 3,

    },


    instruction: {

      fontSize: 16,

      color: "#444",

      lineHeight: 25,

      marginBottom: 13,

    },


    // ==================================================
    // HISTORIAL
    // ==================================================

    emptyContainer: {

      flex: 1,

      justifyContent:
        "center",

      alignItems:
        "center",

      paddingBottom: 80,

    },


    emptyTitle: {

      fontSize: 21,

      fontWeight:
        "bold",

      color: "#555",

      marginTop: 15,

    },


    emptyText: {

      fontSize: 15,

      color: "#888",

      textAlign:
        "center",

      marginTop: 8,

    },


    historyCard: {

      flexDirection:
        "row",

      alignItems:
        "center",

      backgroundColor:
        "white",

      borderRadius: 18,

      padding: 15,

      marginBottom: 12,

      shadowColor:
        "#000",

      shadowOpacity:
        0.06,

      shadowRadius: 6,

      elevation: 2,

    },


    historyNumber: {

      width: 42,

      height: 42,

      borderRadius: 12,

      backgroundColor:
        "#111",

      justifyContent:
        "center",

      alignItems:
        "center",

    },


    historyNumberText: {

      color: "white",

      fontWeight:
        "bold",

    },


    historyInfo: {

      flex: 1,

      marginLeft: 13,

    },


    historyTitle: {

      fontSize: 15,

      fontWeight:
        "bold",

      color: "#222",

    },


    historyTime: {

      fontSize: 13,

      color: "#888",

      marginTop: 5,

    },


    historyResult: {

      alignItems:
        "center",

    },


    historyResultLabel: {

      color: "#888",

      fontSize: 12,

    },


    historyResultNumber: {

      color: "#111",

      fontSize: 24,

      fontWeight:
        "bold",

    },


    // ==================================================
    // ESTADÍSTICAS
    // ==================================================

    statisticsContainer: {

      flexGrow: 1,

      backgroundColor:
        "#f5f5f5",

      padding: 20,

    },


    statsGrid: {

      flexDirection:
        "row",

      flexWrap:
        "wrap",

      justifyContent:
        "space-between",

    },


    statCard: {

      width: "48%",

      backgroundColor:
        "white",

      borderRadius: 18,

      padding: 20,

      alignItems:
        "center",

      marginBottom: 15,

      shadowColor:
        "#000",

      shadowOpacity:
        0.06,

      shadowRadius: 6,

      elevation: 2,

    },


    statNumber: {

      fontSize: 32,

      fontWeight:
        "bold",

      color: "#111",

      marginTop: 8,

    },


    statLabel: {

      fontSize: 13,

      color: "#777",

      marginTop: 3,

      textAlign:
        "center",

    },


    chartCard: {

      backgroundColor:
        "white",

      borderRadius: 20,

      padding: 20,

      marginTop: 5,

      marginBottom: 30,

      shadowColor:
        "#000",

      shadowOpacity:
        0.06,

      shadowRadius: 6,

      elevation: 2,

    },


    chartTitle: {

      fontSize: 19,

      fontWeight:
        "bold",

      color: "#111",

      marginBottom: 20,

    },


    barRow: {

      flexDirection:
        "row",

      alignItems:
        "center",

      marginBottom: 13,

    },


    barNumber: {

      width: 30,

      height: 30,

      backgroundColor:
        "#111",

      borderRadius: 8,

      justifyContent:
        "center",

      alignItems:
        "center",

    },


    barNumberText: {

      color: "white",

      fontWeight:
        "bold",

    },


    barBackground: {

      flex: 1,

      height: 12,

      backgroundColor:
        "#e5e5e5",

      borderRadius: 10,

      marginHorizontal: 12,

      overflow:
        "hidden",

    },


    barFill: {

      height: "100%",

      backgroundColor:
        "#111",

      borderRadius: 10,

    },


    barValue: {

      width: 25,

      textAlign:
        "right",

      fontWeight:
        "bold",

      color: "#555",

    },


    // ==================================================
    // CONFIGURACIÓN
    // ==================================================

    settingsContainer: {

      flexGrow: 1,

      backgroundColor:
        "#f5f5f5",

      padding: 20,

    },


    settingsCard: {

      backgroundColor:
        "white",

      borderRadius: 20,

      padding: 18,

      marginBottom: 18,

      shadowColor:
        "#000",

      shadowOpacity:
        0.06,

      shadowRadius: 6,

      elevation: 2,

    },


    settingRow: {

      flexDirection:
        "row",

      alignItems:
        "center",

      justifyContent:
        "space-between",

    },


    settingLeft: {

      flexDirection:
        "row",

      alignItems:
        "center",

      flex: 1,

    },


    settingIcon: {

      width: 45,

      height: 45,

      borderRadius: 13,

      backgroundColor:
        "#eeeeee",

      justifyContent:
        "center",

      alignItems:
        "center",

      marginRight: 13,

    },


    settingTitle: {

      fontSize: 16,

      fontWeight:
        "bold",

      color: "#222",

    },


    settingDescription: {

      fontSize: 13,

      color: "#888",

      marginTop: 3,

    },


    separator: {

      height: 1,

      backgroundColor:
        "#eeeeee",

      marginVertical: 18,

    },


    dangerTitle: {

      fontSize: 18,

      fontWeight:
        "bold",

      color: "#111",

    },


    dangerText: {

      fontSize: 14,

      color: "#777",

      lineHeight: 20,

      marginTop: 7,

      marginBottom: 18,

    },


    deleteButton: {

      backgroundColor:
        "#111",

      borderRadius: 14,

      paddingVertical: 13,

      flexDirection:
        "row",

      justifyContent:
        "center",

      alignItems:
        "center",

    },


    deleteButtonText: {

      color: "white",

      fontWeight:
        "bold",

      fontSize: 15,

      marginLeft: 8,

    },


    aboutCard: {

      alignItems:
        "center",

      backgroundColor:
        "white",

      borderRadius: 20,

      padding: 25,

      marginBottom: 30,

    },


    versionText: {

      fontSize: 14,

      fontWeight:
        "bold",

      color: "#555",

      marginTop: 15,

    },


    aboutText: {

      fontSize: 13,

      color: "#888",

      textAlign:
        "center",

      marginTop: 5,

    },

  });