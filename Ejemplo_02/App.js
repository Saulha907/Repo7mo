import { StyleSheet, Text, View, TextInput, Button, ScrollView } from 'react-native';
//import MyInpuntText from './componentes/DemoInputText';
import FlagComponent from './componentes/FlagComponent';
import { useState } from "react";

export default function App() {
    const [texto, setTexto]= useState()
    const [enviar, setEnviar] = useState()
  return (
    <View style={styles.container}>
      <View style={styles.panel1}></View>
      <View style={styles.panel2}></View>
      <View style={styles.panel3}></View>
       <ScrollView style={styles.input}>
          <Text>Inicial{enviar}</Text>
                      
          <Text>Final{enviar}</Text>
          <TextInput placeholder="Escribe aqui..."
            onChangeText={t=>setTexto(t)}
          />
          <Button
            title="Enviar"
            onPress={()=>setEnviar(texto)}
            />
        </ScrollView>
    </View>
    
  );
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
    flexDirection: 'column',
    //backgroundColor: '#66ff00',
    
  },
  panel1:{
    flex:1,
    backgroundColor:'#fbff00'
  },
  panel2:{
    flex:1,
    backgroundColor:'#000dff'
  },
  panel3:{
    flex:1,
    backgroundColor:'#ff0202',
  },
});
