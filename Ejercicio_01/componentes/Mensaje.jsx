import { View, Text, StyleSheet } from "react-native";

//const mitexto="Mensaje desde un objeto"
//const num=22

//const double = (n) =>n*2 

export default function Mensaje( props ){
    return(
        <View>
            <text style={styles.color_texto}>{ props.titulo }</text>
            <text style={styles.color_texto}>{ props.numero }</text>
        </View>
    );
}

const styles=StyleSheet.create({
    color_texto:{
        color:'blue',
        fontSize:25,
        backgroundColor:'yellow',
    },
});

