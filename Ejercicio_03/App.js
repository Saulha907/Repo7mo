import { StatusBar } from 'expo-status-bar';
import { StyleSheet, Text, View } from 'react-native';
import ImagenFondo from './componentes/ImagenFondo';
import DemoFlatList from './componentes/DemoFlatList';
import DemoSectionLIst from './componentes/DemoSectionLIst';

export default function App() {
  return (
    <View style={styles.container}>
      <ImagenFondo/>
      <DemoFlatList/>
      <DemoSectionLIst/>
    </View>
  );
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
    backgroundColor: '#77ff00',
    alignItems: 'center',
    justifyContent: 'center',
  },
});
