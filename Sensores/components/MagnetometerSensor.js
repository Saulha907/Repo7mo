import { useState, useEffect } from "react";
import { Text, View, StyleSheet } from "react-native"
import { Accelerometer as ExpoAccelerometer } from "expo-sensors";

export default function MagnetometerSensor(){
    const [datos, setDatos] = useState({
        X:0,
        y:0,
        Z:0,
    });

    useEffect(()=>{
        //1.- Suscribirnos al sensor

        const suscripcion = ExpoAccelerometer.addListener(
            mediciones => {
                setDatos({
                    X: mediciones.x,
                    y: mediciones.y,
                    Z: mediciones.z,
                })
            }
        );  

        //2.- Definir al intervalo de mediciones

        ExpoAccelerometer.setUpdateInterval(100);

        //3.- Definir la des-suscripción

        return ()=>{
            suscripcion.remove();
        }
        
    },[]);

    return(
        <View style={styles.container}>

            <Text style={styles.title}>

                Magnetómetro

            </Text>
            <View style={styles.card}>
                    <Text style={styles.axis}>X</Text>
                    <Text style={styles.value}>{datos.X.toFixed(2)}</Text>
            </View>
            <View style={styles.card}>
                    <Text style={styles.axis}>Y</Text>
                    <Text style={styles.value}>{datos.y.toFixed(2)}</Text>
            </View>
            <View>
                    <Text style={styles.axis}>Z</Text>
                    <Text style={styles.value}>{datos.Z.toFixed(2)}</Text>
            </View>
        </View>
    );

}


const styles = StyleSheet.create({
    container: {
        flex:1,
        justifyContent:'center',
        padding: 25,
        backgroundColor:"#f6fefe"
    },
    title:{
        fontSize:28,
        fontWeight:'bold',
        textAlign:'center',
        marginBottom:30,
        color:"#fff"
    },
    card:{
        backgroundColor:"#fff",
        padding:20,
        marginBottom:15,
        borderRadius:20,
        flexDirection:'row',
        justifyContent:"space-between"
,    },
    axis:{
        fontSize:22,
        fonw:'bold',
        color:"#2564eb",
    },
    value:{
        fontSize:22,
        fonw:'bold',
        color:"#345",
    },

})