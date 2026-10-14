import { View, Text, StyleSheet } from "react-native";
const MiComponente = () =>{
    return(
        <view>
            <text style={styles.color_texto}>Hola desde mi componente</text>
        </view>
    );
}

const styles=StyleSheet.create({
    color_texto:{
        color:'red',
    },
});

export default MiComponente;