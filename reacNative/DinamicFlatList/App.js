import { StyleSheet, Text, View, FlatList, TouchableOpacity } from 'react-native';
import CustomModal from './componentes/CustomModal';
import { useState } from 'react';
const cursos = [
  {id:'1', titulo:'Aplicaciones Moviles', duracion:'20', reating:'4.5'},
  {id:'2', titulo:'Bases de Datos', duracion:'30', reating:'5.0'},
  {id:'3', titulo:'Redes de computadora', duracion:'40', reating:'4.0'},
  {id:'4', titulo:'Desarrollo de aplicaciones web', duracion:'20', reating:'5.0'},
  {id:'5', titulo:'Ciberseguridad', duracion:'30', reating:'4.3'},
];


export default function App() {

const [modalVisble, setModalVisible]= useState(false);  
const [cursoSeleccionado, setCursoSeleccionado]= useState(null);

const manejaPresionCurso = (tituloCurso)=>{
  setCursoSeleccionado({valor:tituloCurso});
  setModalVisible(true);
}
const renderCard = ({item}) =>(
  <TouchableOpacity
    style={styles.card}
    onPress={()=>manejaPresionCurso(item.titulo)}
    activeOpacity={0.7}
  >
    <View>
        <Text style={styles.title}>{item.titulo}</Text>
        <Text styles={styles.subtitulo}>{item.duracion} | {item.reating}</Text>
    </View>
  </TouchableOpacity>
);

  return (
    <View style={styles.container}>
      <Text style={styles.header}>Mi Lista de cursos</Text>
      <FlatList
        data={cursos}
        renderItem= {renderCard}s
        keyExtractor={item=>item.id}
        contentContainerStyle={styles.listContaines}
      />
      <CustomModal
        visible = {modalVisble}
        onClose={()=>setModalVisible(false)}
        contenido={cursoSeleccionado}
      />
    </View>
  );
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
    backgroundColor: '#fff',
  },

  listContaines:{
    paddingHorizontal:16,
    paddingBottom:16,
  },
  header:{
    fontSize:24,
    fontWeight:'bold',
    color:'#000'
  },
  card:{
    backgroundColor:'#ddd',
    padding: 16,
    marginVertical:8,
    borderdRadius: 12,
  },
  title:{
    fontSize: 15,
    fontWeight: 'bold',
    color:'#333',
    marginBottom: 4,
  },
  subtitulo:{
    fontSize:14,
    color:'#777',
  },

});
