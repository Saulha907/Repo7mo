import { useState } from "react";
import { View, Text, TextInput, Button, ScrollView, StyleSheet, Dimensions } from "react-native";

export default function MyInpuntText(){
    const [texto, setTexto]= useState()
    const [enviar, setEnviar] = useState()

    return (
        <View>
            <ScrollView style={styles.input}>
                <Text>Inicial{enviar}</Text>
                <Text>{enviar}</Text>
                <Text>{enviar}</Text>
                <Text>{enviar}</Text>
                <Text>{enviar}</Text>
                <Text>{enviar}</Text>
                <Text>{enviar}</Text>
                <Text>{enviar}</Text>
                <Text>{enviar}</Text>
                <Text>{enviar}</Text>
                <Text>{enviar}</Text>
                <Text>{enviar}</Text>
                <Text>{enviar}</Text>
                <Text>{enviar}</Text>
                <Text>{enviar}</Text>
                <Text>{enviar}</Text>
                <Text>{enviar}</Text>
                <Text>{enviar}</Text>
                <Text>{enviar}</Text>
                <Text>{enviar}</Text>
                <Text>{enviar}</Text>
                <Text>{enviar}</Text>
                <Text>{enviar}</Text>
                <Text>{enviar}</Text>
                <Text>{enviar}</Text>
                <Text>{enviar}</Text>
                <Text>{enviar}</Text>
                <Text>{enviar}</Text>
                <Text>{enviar}</Text>
                <Text>{enviar}</Text>
                <Text>{enviar}</Text>
                <Text>{enviar}</Text>
                <Text>{enviar}</Text>
                <Text>{enviar}</Text>
                <Text>{enviar}</Text>
                <Text>{enviar}</Text>
                <Text>{enviar}</Text>
                <Text>{enviar}</Text>
                <Text>{enviar}</Text>
                <Text>{enviar}</Text>
                <Text>{enviar}</Text>
                <Text>{enviar}</Text>
                <Text>{enviar}</Text>
                <Text>{enviar}</Text>
                <Text>{enviar}</Text>
                <Text>{enviar}</Text>
                <Text>{enviar}</Text>
                <Text>{enviar}</Text>
                <Text>{enviar}</Text>
                <Text>{enviar}</Text>
                <Text>{enviar}</Text>
                <Text>{enviar}</Text>
                <Text>{enviar}</Text>
                <Text>{enviar}</Text>
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

const styles= StyleSheet.create({
    input :{
        width:Dimensions.get("window").width,

    },  
})