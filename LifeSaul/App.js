import 'react-native-gesture-handler';
import React, { useState } from 'react';
import { NavigationContainer } from '@react-navigation/native';
import DrawerNavigator from './src/navigation/DrawerNavigator';
import AnimatedSplash from './src/components/AnimatedSplash';

export default function App() {
  const [isSplashDone, setIsSplashDone] = useState(false);

  if (!isSplashDone) {
    return <AnimatedSplash onFinish={() => setIsSplashDone(true)} />;
  }

  return (
    <NavigationContainer>
      <DrawerNavigator />
    </NavigationContainer>
  );
}
