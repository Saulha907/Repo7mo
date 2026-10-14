import React from "react";
import { View, Text, Modal, StyleSheet, Button,} from "react-native";

const CustomModal = ({ visible, onClose, contenido }) => {
  return (
    <Modal
      animationType="fade"
      transparent={true}
      visible={visible}
      onRequestClose={onClose}
    >
      <View style={styles.centeredView}>

        <View style={styles.modalView}>

          <Text style={styles.titulo}>
            Resultado IMC
          </Text>

          <Text style={styles.imc}>
            {contenido ? contenido.imc : ""}
          </Text>

          <Text style={styles.clasificacion}>
            {contenido ? contenido.clasificacion : ""}
          </Text>

          <View style={styles.boton}>
            <Button
              title="Aceptar"
              onPress={onClose}
            />
          </View>

        </View>

      </View>
    </Modal>
  );
};

const styles = StyleSheet.create({
  centeredView: {
    flex: 1,
    justifyContent: "center",
    alignItems: "center",
    backgroundColor: "rgba(0,0,0,0.5)",
  },

  modalView: {
    width: "80%",
    backgroundColor: "white",
    borderRadius: 15,
    padding: 35,
    alignItems: "center",

    shadowColor: "#000",
    shadowOffset: {
      width: 0,
      height: 2,
    },
    shadowOpacity: 0.3,
    shadowRadius: 6,
    elevation: 5,
  },

  titulo: {
    fontSize: 18,
    fontWeight: "600",
    marginBottom: 15,
  },

  imc: {
    fontSize: 40,
    fontWeight: "bold",
    marginBottom: 10,
  },

  clasificacion: {
    fontSize: 18,
    marginBottom: 25,
    textAlign: "center",
  },

  boton: {
    width: "100%",
  },
});

export default CustomModal;