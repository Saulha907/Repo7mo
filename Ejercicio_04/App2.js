/*import { StatusBar } from 'expo-status-bar';
import { useState } from 'react';
import { StyleSheet, Text, View, Modal, Button } from 'react-native';


export default function App2() {
  const [modal, setModal]=useState(false);
  return (
    <View style={styles.container}>
      <Modal
       animationType='slide'
       transparent={true}
       visible={modal}
      //configuración del modal
      >
        <View style={styles.containerModal}>
          <View Style={styles.viewModal}>
            <Text>Esto esta dentro del modal</Text>
            <Button
              title="Cerrar modal"
              onPress={()=>setModal(!modal)}
            />

          </View>
        </View>

      </Modal>
      <Text>Este testo esta fuera de modal  </Text>
      <Text>Este testo esta fuera de modal  </Text>
      <Text>Este testo esta fuera de modal  </Text>
      <Text>Este testo esta fuera de modal  </Text>
      <Text>Este testo esta fuera de modal  </Text>
      <Text>Este testo esta fuera de modal  </Text>
      <Text>Este testo esta fuera de modal  </Text>
      <Text>Este testo esta fuera de modal  </Text>
      <Text>Este testo esta fuera de modal  </Text>
      <Text>Este testo esta fuera de modal  </Text>
      <Text>Este testo esta fuera de modal  </Text>
      <Text>Este testo esta fuera de modal  </Text>
      <Text>Este testo esta fuera de modal  </Text>
      <Text>Este testo esta fuera de modal  </Text>
      <Text>Este testo esta fuera de modal  </Text>
      <Text>Este testo esta fuera de modal  </Text>
      <Text>Este testo esta fuera de modal  </Text>
      <Text>Este testo esta fuera de modal  </Text>
      <Text>Este testo esta fuera de modal  </Text>
      <Text>Este testo esta fuera de modal  </Text>
      <Text>Este testo esta fuera de modal  </Text>
      <Text>Este testo esta fuera de modal  </Text>
      <Text>Este testo esta fuera de modal  </Text>
      <Text>Este testo esta fuera de modal  </Text>
      <Text>Este testo esta fuera de modal  </Text>
      <Text>Este testo esta fuera de modal  </Text>
      <Text>Este testo esta fuera de modal  </Text>
      <Text>Este testo esta fuera de modal  </Text>
      <Text>Este testo esta fuera de modal  </Text>
      <Text>Este testo esta fuera de modal  </Text>
      <Text>Este testo esta fuera de modal  </Text>
      <Text>Este testo esta fuera de modal  </Text>
      <Text>Este testo esta fuera de modal  </Text>
      <Text>Este testo esta fuera de modal  </Text>
      <Text>Este testo esta fuera de modal  </Text>
      <Text>Este testo esta fuera de modal  </Text>
      <Text>Este testo esta fuera de modal  </Text>
      <Text>Este testo esta fuera de modal  </Text>
      <Text>Este testo esta fuera de modal  </Text>
      <Text>Este testo esta fuera de modal  </Text>
      <Text>Este testo esta fuera de modal  </Text>
      <Text>Este testo esta fuera de modal  </Text>
      <Text>Este testo esta fuera de modal  </Text>
      <Text>Este testo esta fuera de modal  </Text>
      <Text>Este testo esta fuera de modal  </Text>
      <Text>Este testo esta fuera de modal  </Text>
      <Text>Este testo esta fuera de modal  </Text>
      <Text>Este testo esta fuera de modal  </Text>
      <Text>Este testo esta fuera de modal  </Text>
      <Text>Este testo esta fuera de modal  </Text>
      <Text>Este testo esta fuera de modal  </Text>
      <Text>Este testo esta fuera de modal  </Text>

      <Button title="Mostrar Modal"
        onPress={()=>setModal(!modal)}
      />
    
    </View>
  );
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
    backgroundColor: '#fff',
    alignItems: 'center',
    justifyContent: 'center',
  },
  containerModal:{
    flex:1,
    alignItems:'stretch',
    justifyContent:'center',
    backgroundColor:'rgba(0,0,0,0.5)'
  },  
  viewModal:{
    flex:1,
    backgroundColor:'#fff',
    alignItems:'center',
    justifyContent:'center',
    marginHorizontal: 400,
    marginVertical: 300,
    },
});
*/