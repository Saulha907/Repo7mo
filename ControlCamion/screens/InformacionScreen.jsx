import React from 'react';

import {
  View,
  Text,
  StyleSheet,
  ScrollView
} from 'react-native';

import { Ionicons } from '@expo/vector-icons';


export default function InformacionScreen() {

  return (

    <ScrollView
      style={styles.container}
      contentContainerStyle={styles.content}
      showsVerticalScrollIndicator={false}
    >

      {/* ENCABEZADO */}

      <View style={styles.heroCard}>

        <View style={styles.logoContainer}>

          <Ionicons
            name="bus"
            size={55}
            color="#FFFFFF"
          />

        </View>


        <Text style={styles.appName}>
          ControlCamión
        </Text>

        <Text style={styles.appSubtitle}>
          Administración inteligente de una unidad de transporte
        </Text>

        <View style={styles.versionBadge}>

          <Text style={styles.versionText}>
            Versión 1.0
          </Text>

        </View>

      </View>


      {/* DESCRIPCIÓN */}

      <Text style={styles.sectionTitle}>
        Acerca de la aplicación
      </Text>


      <View style={styles.card}>

        <Text style={styles.description}>
          ControlCamión es una aplicación móvil desarrollada para apoyar
          la administración diaria de una unidad de transporte urbano.
        </Text>

        <Text style={styles.description}>
          Permite registrar jornadas de trabajo, boletos vendidos,
          consumo de diésel, gastos operativos, retiros de efectivo y
          consultar información financiera de la unidad.
        </Text>

        <Text style={styles.description}>
          Toda la información se almacena localmente en el dispositivo,
          por lo que la aplicación puede utilizarse sin conexión a
          internet.
        </Text>

      </View>


      {/* FUNCIONES */}

      <Text style={styles.sectionTitle}>
        Funciones principales
      </Text>


      <View style={styles.card}>

        <FeatureItem
          icon="ticket-outline"
          color="#2563EB"
          background="#DBEAFE"
          title="Registro de jornadas"
          text="Control de boletos, ingresos, diésel, kilometraje y rendimiento."
        />


        <Divider />


        <FeatureItem
          icon="receipt-outline"
          color="#DC2626"
          background="#FEE2E2"
          title="Control de gastos"
          text="Registro de reparaciones, llantas, aceite, lavado, refacciones y otros gastos."
        />


        <Divider />


        <FeatureItem
          icon="cash-outline"
          color="#9333EA"
          background="#F3E8FF"
          title="Retiros"
          text="Control del dinero retirado sin considerarlo como gasto operativo."
        />


        <Divider />


        <FeatureItem
          icon="bar-chart-outline"
          color="#16A34A"
          background="#DCFCE7"
          title="Finanzas"
          text="Consulta ingresos, gastos, ganancias y saldo disponible."
        />


        <Divider />


        <FeatureItem
          icon="time-outline"
          color="#D97706"
          background="#FEF3C7"
          title="Historial"
          text="Consulta las jornadas registradas y sus detalles."
        />


        <Divider />


        <FeatureItem
          icon="bus-outline"
          color="#0891B2"
          background="#CFFAFE"
          title="Mi Unidad"
          text="Configuración de unidad, ruta, operador, tarifas, kilometraje y diésel."
        />

      </View>


      {/* TECNOLOGÍAS */}

      <Text style={styles.sectionTitle}>
        Tecnologías utilizadas
      </Text>


      <View style={styles.card}>

        <Technology
          icon="logo-react"
          title="React Native"
          text="Desarrollo de la interfaz móvil."
        />

        <Divider />

        <Technology
          icon="phone-portrait-outline"
          title="Expo"
          text="Entorno utilizado para desarrollar y ejecutar la aplicación."
        />

        <Divider />

        <Technology
          icon="folder-outline"
          title="AsyncStorage"
          text="Almacenamiento local de información en el dispositivo."
        />

        <Divider />

        <Technology
          icon="menu-outline"
          title="React Navigation"
          text="Navegación mediante menú lateral Drawer."
        />

      </View>


      {/* CONCEPTOS DE CLASE */}

      <Text style={styles.sectionTitle}>
        Elementos implementados
      </Text>


      <View style={styles.requirementsCard}>

        <Requirement
          text="Splash Screen animado"
        />

        <Requirement
          text="Drawer Navigation"
        />

        <Requirement
          text="Modales"
        />

        <Requirement
          text="Animaciones"
        />

        <Requirement
          text="ScrollView"
        />

        <Requirement
          text="FlatList"
        />

        <Requirement
          text="TextInput"
        />

        <Requirement
          text="TouchableOpacity"
        />

        <Requirement
          text="Hooks de React"
        />

        <Requirement
          text="Almacenamiento local"
        />

      </View>


      {/* FUNCIONAMIENTO */}

      <Text style={styles.sectionTitle}>
        Funcionamiento general
      </Text>


      <View style={styles.flowCard}>

        <FlowStep
          number="1"
          title="Configurar unidad"
          text="Se registran los datos principales del camión."
        />


        <FlowLine />


        <FlowStep
          number="2"
          title="Registrar jornada"
          text="Se capturan boletos, combustible y kilometraje."
        />


        <FlowLine />


        <FlowStep
          number="3"
          title="Registrar gastos"
          text="Se agregan los gastos adicionales de operación."
        />


        <FlowLine />


        <FlowStep
          number="4"
          title="Consultar finanzas"
          text="La aplicación calcula ingresos, gastos, ganancias y saldo."
        />

      </View>


      {/* ALMACENAMIENTO */}

      <View style={styles.localCard}>

        <View style={styles.localIcon}>

          <Ionicons
            name="shield-checkmark-outline"
            size={29}
            color="#16A34A"
          />

        </View>


        <View style={styles.localContent}>

          <Text style={styles.localTitle}>
            Aplicación completamente local
          </Text>

          <Text style={styles.localText}>
            ControlCamión no necesita servidores, bases de datos externas
            ni servicios web para funcionar.
          </Text>

        </View>

      </View>


      {/* PIE */}

      <View style={styles.footer}>

        <Ionicons
          name="bus-outline"
          size={30}
          color="#2563EB"
        />

        <Text style={styles.footerTitle}>
          ControlCamión
        </Text>

        <Text style={styles.footerText}>
          Proyecto académico desarrollado con React Native
        </Text>

      </View>

    </ScrollView>
  );
}


// ======================================================
// COMPONENTE DE FUNCIÓN
// ======================================================

function FeatureItem({
  icon,
  color,
  background,
  title,
  text
}) {

  return (

    <View style={styles.featureRow}>

      <View
        style={[
          styles.featureIcon,

          {
            backgroundColor:
              background
          }
        ]}
      >

        <Ionicons
          name={icon}
          size={24}
          color={color}
        />

      </View>


      <View style={styles.featureContent}>

        <Text style={styles.featureTitle}>
          {title}
        </Text>

        <Text style={styles.featureText}>
          {text}
        </Text>

      </View>

    </View>

  );
}


// ======================================================
// TECNOLOGÍA
// ======================================================

function Technology({
  icon,
  title,
  text
}) {

  return (

    <View style={styles.technologyRow}>

      <View style={styles.techIcon}>

        <Ionicons
          name={icon}
          size={23}
          color="#2563EB"
        />

      </View>


      <View style={styles.techContent}>

        <Text style={styles.techTitle}>
          {title}
        </Text>

        <Text style={styles.techText}>
          {text}
        </Text>

      </View>

    </View>

  );
}


// ======================================================
// REQUISITO
// ======================================================

function Requirement({ text }) {

  return (

    <View style={styles.requirementItem}>

      <Ionicons
        name="checkmark-circle"
        size={21}
        color="#16A34A"
      />

      <Text style={styles.requirementText}>
        {text}
      </Text>

    </View>

  );
}


// ======================================================
// FLUJO
// ======================================================

function FlowStep({
  number,
  title,
  text
}) {

  return (

    <View style={styles.flowStep}>

      <View style={styles.stepNumber}>

        <Text style={styles.stepNumberText}>
          {number}
        </Text>

      </View>


      <View style={styles.stepContent}>

        <Text style={styles.stepTitle}>
          {title}
        </Text>

        <Text style={styles.stepText}>
          {text}
        </Text>

      </View>

    </View>

  );
}


function FlowLine() {

  return (
    <View style={styles.flowLine} />
  );
}


function Divider() {

  return (
    <View style={styles.divider} />
  );
}


// ======================================================
// ESTILOS
// ======================================================

const styles = StyleSheet.create({

  container: {
    flex: 1,
    backgroundColor: '#F1F5F9'
  },


  content: {
    padding: 20,
    paddingBottom: 50
  },


  heroCard: {
    backgroundColor: '#0F172A',
    borderRadius: 24,
    padding: 30,
    alignItems: 'center',
    elevation: 5
  },


  logoContainer: {
    width: 95,
    height: 95,
    borderRadius: 28,
    backgroundColor: '#2563EB',
    justifyContent: 'center',
    alignItems: 'center',
    marginBottom: 17
  },


  appName: {
    color: '#FFFFFF',
    fontSize: 30,
    fontWeight: 'bold'
  },


  appSubtitle: {
    color: '#CBD5E1',
    textAlign: 'center',
    fontSize: 14,
    lineHeight: 20,
    marginTop: 7
  },


  versionBadge: {
    backgroundColor: '#1E293B',
    paddingHorizontal: 13,
    paddingVertical: 6,
    borderRadius: 20,
    marginTop: 15
  },


  versionText: {
    color: '#94A3B8',
    fontSize: 12,
    fontWeight: '600'
  },


  sectionTitle: {
    color: '#0F172A',
    fontSize: 20,
    fontWeight: 'bold',
    marginTop: 27,
    marginBottom: 13
  },


  card: {
    backgroundColor: '#FFFFFF',
    borderRadius: 18,
    padding: 18,
    elevation: 2
  },


  description: {
    color: '#475569',
    fontSize: 14,
    lineHeight: 22,
    marginBottom: 10,
    textAlign: 'justify'
  },


  featureRow: {
    flexDirection: 'row',
    alignItems: 'center'
  },


  featureIcon: {
    width: 48,
    height: 48,
    borderRadius: 14,
    justifyContent: 'center',
    alignItems: 'center',
    marginRight: 13
  },


  featureContent: {
    flex: 1
  },


  featureTitle: {
    color: '#0F172A',
    fontSize: 15,
    fontWeight: 'bold'
  },


  featureText: {
    color: '#64748B',
    fontSize: 12,
    lineHeight: 17,
    marginTop: 3
  },


  divider: {
    height: 1,
    backgroundColor: '#E2E8F0',
    marginVertical: 14
  },


  technologyRow: {
    flexDirection: 'row',
    alignItems: 'center'
  },


  techIcon: {
    width: 43,
    height: 43,
    borderRadius: 13,
    backgroundColor: '#DBEAFE',
    justifyContent: 'center',
    alignItems: 'center',
    marginRight: 12
  },


  techContent: {
    flex: 1
  },


  techTitle: {
    color: '#0F172A',
    fontWeight: 'bold',
    fontSize: 15
  },


  techText: {
    color: '#64748B',
    fontSize: 12,
    marginTop: 2
  },


  requirementsCard: {
    backgroundColor: '#FFFFFF',
    borderRadius: 18,
    padding: 18,
    flexDirection: 'row',
    flexWrap: 'wrap',
    elevation: 2
  },


  requirementItem: {
    width: '50%',
    flexDirection: 'row',
    alignItems: 'center',
    marginVertical: 8
  },


  requirementText: {
    color: '#475569',
    fontSize: 12,
    fontWeight: '600',
    marginLeft: 6,
    paddingRight: 5
  },


  flowCard: {
    backgroundColor: '#FFFFFF',
    borderRadius: 18,
    padding: 18,
    elevation: 2
  },


  flowStep: {
    flexDirection: 'row',
    alignItems: 'center'
  },


  stepNumber: {
    width: 42,
    height: 42,
    borderRadius: 21,
    backgroundColor: '#2563EB',
    justifyContent: 'center',
    alignItems: 'center'
  },


  stepNumberText: {
    color: '#FFFFFF',
    fontSize: 16,
    fontWeight: 'bold'
  },


  stepContent: {
    flex: 1,
    marginLeft: 13
  },


  stepTitle: {
    color: '#0F172A',
    fontSize: 15,
    fontWeight: 'bold'
  },


  stepText: {
    color: '#64748B',
    fontSize: 12,
    lineHeight: 17,
    marginTop: 2
  },


  flowLine: {
    width: 2,
    height: 25,
    backgroundColor: '#BFDBFE',
    marginLeft: 20
  },


  localCard: {
    backgroundColor: '#ECFDF5',
    borderWidth: 1,
    borderColor: '#BBF7D0',
    borderRadius: 18,
    padding: 17,
    flexDirection: 'row',
    alignItems: 'center',
    marginTop: 27
  },


  localIcon: {
    width: 50,
    height: 50,
    borderRadius: 15,
    backgroundColor: '#DCFCE7',
    justifyContent: 'center',
    alignItems: 'center',
    marginRight: 12
  },


  localContent: {
    flex: 1
  },


  localTitle: {
    color: '#166534',
    fontSize: 15,
    fontWeight: 'bold'
  },


  localText: {
    color: '#15803D',
    fontSize: 12,
    lineHeight: 17,
    marginTop: 3
  },


  footer: {
    alignItems: 'center',
    marginTop: 35,
    paddingBottom: 10
  },


  footerTitle: {
    color: '#0F172A',
    fontSize: 17,
    fontWeight: 'bold',
    marginTop: 7
  },


  footerText: {
    color: '#94A3B8',
    fontSize: 11,
    marginTop: 4,
    textAlign: 'center'
  }

});