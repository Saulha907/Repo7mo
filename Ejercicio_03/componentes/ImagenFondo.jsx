import { StyleSheet, View, ImageBackground, Dimensions, Image, Text } from "react-native";

export default function ImagenFondo(){
    return(
        <View>
            
            <ImageBackground
                style={styles.fondo} 
                source={require('../assets/Gatito.jpg')}
            >
                <View style={styles.container}>
                <Text style={styles.titulo}>Hola</Text>
                <Image
                    source={{uri:'https://http.cat/510'}}
                    style={styles.foto}
                />
                </View>
            </ImageBackground>
        </View>
    );
}


const styles = StyleSheet.create({
    fondo:{
        /*flex:1,
        alignItems:"center",
        justifyContent:"center",*/
        width: Dimensions.get("window").width,
        height: Dimensions.get("window").height,
    },
    foto:{
        //margin:20,
        opacity:0.8,
        width:160,
        height:160,
        borderRadius:16,
        borderWidth:10,
        borderColor:'#b5f',
        shadowColor:'#000',
        shadowOffset:{width:0, height:6 },
        shadowOpacity:0.5,
        shadowRadius: 10,
        elevation: 8,
    },
        titulo:{
        color:"#fff",
        fontSize:80,
        fontWeight:"bold",
        textAlign:"center",
        position:"absolute",
        top:60,
        width:"100%",
        borderWidth:10,
        borderColor:'#b5f',
        
    },  
    container:{
        flex:1,
        justifyContent:"center",
        alignItems:"center",
        backgroundColor:"rgb(0,0,0,0.5)", 
    }, 

});