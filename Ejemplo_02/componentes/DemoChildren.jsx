import {View, Text} from "react-native";
 
export default function DemoChildren({children, titulo}){
    return(
        <View>
            <text>Muestra Children:</text>{children}
        </View>
    );

    
} 