import React from 'react';

import {
  createDrawerNavigator
} from '@react-navigation/drawer';

import { Ionicons } from '@expo/vector-icons';

import InicioScreen from '../screens/InicioScreen';
import JornadaScreen from '../screens/JornadaScreen';
import GastosScreen from '../screens/GastosScreen';
import RetirosScreen from '../screens/RetirosScreen';
import HistorialScreen from '../screens/HistorialScreen';
import FinanzasScreen from '../screens/FinanzasScreen';
import UnidadScreen from '../screens/UnidadScreen';
import InformacionScreen from '../screens/InformacionScreen';


const Drawer = createDrawerNavigator();


export default function DrawerNavigator() {

  return (

    <Drawer.Navigator

      screenOptions={{

        headerStyle: {
          backgroundColor: '#0F172A'
        },

        headerTintColor: '#FFFFFF',

        headerTitleStyle: {
          fontWeight: 'bold'
        },

        drawerActiveTintColor: '#2563EB',

        drawerInactiveTintColor: '#64748B',

        drawerActiveBackgroundColor: '#DBEAFE',

        drawerLabelStyle: {
          fontSize: 15,
          fontWeight: '600'
        },

        drawerStyle: {
          backgroundColor: '#F8FAFC',
          width: 280
        },

        drawerItemStyle: {
          borderRadius: 12,
          marginHorizontal: 10,
          marginVertical: 4
        }

      }}

    >

      <Drawer.Screen
        name="Inicio"
        component={InicioScreen}
        options={{
          title: 'Inicio',

          drawerIcon: ({ color, size }) => (
            <Ionicons
              name="home-outline"
              size={size}
              color={color}
            />
          )
        }}
      />


      <Drawer.Screen
        name="Jornada"
        component={JornadaScreen}
        options={{
          title: 'Registrar Jornada',

          drawerIcon: ({ color, size }) => (
            <Ionicons
              name="ticket-outline"
              size={size}
              color={color}
            />
          )
        }}
      />


      <Drawer.Screen
        name="Gastos"
        component={GastosScreen}
        options={{
          title: 'Gastos',

          drawerIcon: ({ color, size }) => (
            <Ionicons
              name="receipt-outline"
              size={size}
              color={color}
            />
          )
        }}
      />


      <Drawer.Screen
        name="Retiros"
        component={RetirosScreen}
        options={{
          title: 'Retiros',

          drawerIcon: ({ color, size }) => (
            <Ionicons
              name="cash-outline"
              size={size}
              color={color}
            />
          )
        }}
      />


      <Drawer.Screen
        name="Historial"
        component={HistorialScreen}
        options={{
          title: 'Historial',

          drawerIcon: ({ color, size }) => (
            <Ionicons
              name="time-outline"
              size={size}
              color={color}
            />
          )
        }}
      />


      <Drawer.Screen
        name="Finanzas"
        component={FinanzasScreen}
        options={{
          title: 'Finanzas',

          drawerIcon: ({ color, size }) => (
            <Ionicons
              name="bar-chart-outline"
              size={size}
              color={color}
            />
          )
        }}
      />


      <Drawer.Screen
        name="Unidad"
        component={UnidadScreen}
        options={{
          title: 'Mis Unidades',

          drawerIcon: ({ color, size }) => (
            <Ionicons
              name="bus-outline"
              size={size}
              color={color}
            />
          )
        }}
      />


      <Drawer.Screen
        name="Informacion"
        component={InformacionScreen}
        options={{
          title: 'Información',

          drawerIcon: ({ color, size }) => (
            <Ionicons
              name="information-circle-outline"
              size={size}
              color={color}
            />
          )
        }}
      />

    </Drawer.Navigator>

  );
}