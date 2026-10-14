import React from 'react';
import { createDrawerNavigator } from '@react-navigation/drawer';
import { Ionicons } from '@expo/vector-icons';

import HomeScreen from '../screens/HomeScreen';
import PropinasScreen from '../screens/PropinasScreen';
import IMCScreen from '../screens/IMCScreen';
import GamesTabNavigator from './GamesTabNavigator';

const Drawer = createDrawerNavigator();

export default function DrawerNavigator() {
  return (
    <Drawer.Navigator
      screenOptions={({ route }) => ({
        headerStyle: {
          backgroundColor: '#1E293B',
        },

        headerTintColor: '#FFFFFF',

        headerTitleStyle: {
          fontWeight: 'bold',
        },

        drawerActiveTintColor: '#2563EB',
        drawerInactiveTintColor: '#64748B',

        drawerActiveBackgroundColor: '#E0E7FF',

        drawerLabelStyle: {
          fontSize: 15,
          fontWeight: '600',
          marginLeft: -5,
        },

        drawerStyle: {
          backgroundColor: '#F8FAFC',
          width: 280,
        },

        drawerItemStyle: {
          borderRadius: 12,
          marginHorizontal: 10,
          marginVertical: 4,
        },

        drawerIcon: ({ color, size, focused }) => {
          let iconName;

          if (route.name === 'Home') {
            iconName = focused ? 'home' : 'home-outline';
          }

          else if (route.name === 'Propinas') {
            iconName = focused ? 'wallet' : 'wallet-outline';
          }

          else if (route.name === 'IMC') {
            iconName = focused ? 'body' : 'body-outline';
          }

          else if (route.name === 'JuegosTab') {
            iconName = focused
              ? 'game-controller'
              : 'game-controller-outline';
          }

          return (
            <Ionicons
              name={iconName}
              size={size}
              color={color}
            />
          );
        },
      })}
    >

      <Drawer.Screen
        name="Home"
        component={HomeScreen}
        options={{
          title: 'Inicio',
        }}
      />

      <Drawer.Screen
        name="Propinas"
        component={PropinasScreen}
        options={{
          title: 'Calculadora de Propinas',
        }}
      />

      <Drawer.Screen
        name="IMC"
        component={IMCScreen}
        options={{
          title: 'Calculadora IMC',
        }}
      />

      <Drawer.Screen
        name="JuegosTab"
        component={GamesTabNavigator}
        options={{
          title: 'Juegos',
        }}
      />

    </Drawer.Navigator>
  );
}