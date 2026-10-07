import React, { useState } from 'react';

import {
  NavigationContainer
} from '@react-navigation/native';

import AnimatedSplash from './componentes/AnimatedSplash';

import DrawerNavigator from './navigation/DrawerNavigator';

export default function App() {

  const [showSplash, setShowSplash] =
    useState(true);

  if (showSplash) {

    return (

      <AnimatedSplash
        onFinish={() =>
          setShowSplash(false)
        }
      />

    );
  }

  return (

    <NavigationContainer>

      <DrawerNavigator />

    </NavigationContainer>

  );
}