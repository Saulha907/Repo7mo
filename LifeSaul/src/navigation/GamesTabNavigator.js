import React from 'react';
import { createBottomTabNavigator } from '@react-navigation/bottom-tabs';
import { Ionicons } from '@expo/vector-icons';

import MemoramaScreen from '../screens/MemoramaScreen';
import TicTacToeScreen from '../screens/TicTacToeScreen';

const Tab = createBottomTabNavigator();

export default function GamesTabNavigator() {
  return (
    <Tab.Navigator
      screenOptions={({ route }) => ({
        headerShown: false,

        tabBarIcon: ({ color, size, focused }) => {
          let iconName;

          if (route.name === 'Memorama') {
            iconName = focused ? 'grid' : 'grid-outline';
          } else if (route.name === 'TicTacToe') {
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

        tabBarActiveTintColor: '#2563EB',
        tabBarInactiveTintColor: '#94A3B8',

        tabBarStyle: {
          height: 65,
          backgroundColor: '#FFFFFF',
          borderTopWidth: 0,
          elevation: 8,
          paddingBottom: 8,
          paddingTop: 8,
        },

        tabBarLabelStyle: {
          fontSize: 13,
          fontWeight: '600',
        },
      })}
    >
      <Tab.Screen
        name="Memorama"
        component={MemoramaScreen}
        options={{
          title: 'Memorama',
        }}
      />

      <Tab.Screen
        name="TicTacToe"
        component={TicTacToeScreen}
        options={{
          title: 'Gato',
        }}
      />
    </Tab.Navigator>
  );
}