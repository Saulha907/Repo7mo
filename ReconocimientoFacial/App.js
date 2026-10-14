import React, {
  useCallback,
  useEffect,
  useRef,
  useState,
} from 'react';

import {
  ActivityIndicator,
  StyleSheet,
  Text,
  View,
} from 'react-native';

import {
  Camera as VisionCamera,
  useCameraDevice,
} from 'react-native-vision-camera';

import {
  Camera,
} from 'react-native-vision-camera-face-detector';


export default function App() {

  const device = useCameraDevice('front');

  const [hasPermission, setHasPermission] = useState(false);

  const [permissionChecked, setPermissionChecked] =
    useState(false);

  const [faceDetected, setFaceDetected] =
    useState(false);

  const previousFaceDetected = useRef(false);


  // Solicitar permiso para utilizar la cámara
  useEffect(() => {

    async function checkPermission() {

      try {

        let status =
          VisionCamera.getCameraPermissionStatus();

        if (status !== 'granted') {

          status =
            await VisionCamera.requestCameraPermission();

        }

        setHasPermission(status === 'granted');

      } catch (error) {

        console.log(
          'Error solicitando permiso:',
          error
        );

        setHasPermission(false);

      } finally {

        setPermissionChecked(true);

      }

    }


    checkPermission();

  }, []);


  // Función que recibe los rostros encontrados
  const handleFacesDetected = useCallback((faces) => {

    const detected = faces.length > 0;


    // Solo actualizamos si cambia el estado
    if (
      previousFaceDetected.current !== detected
    ) {

      previousFaceDetected.current = detected;

      setFaceDetected(detected);

    }

  }, []);


  // Mientras revisa permisos
  if (!permissionChecked) {

    return (

      <View style={styles.center}>

        <ActivityIndicator
          size="large"
          color="#00FF88"
        />

        <Text style={styles.loadingText}>
          Cargando cámara...
        </Text>

      </View>

    );

  }


  // Si no dio permiso
  if (!hasPermission) {

    return (

      <View style={styles.center}>

        <Text style={styles.permissionTitle}>
          Permiso de cámara requerido
        </Text>

        <Text style={styles.permissionText}>
          La aplicación necesita permiso para
          utilizar la cámara.
        </Text>

      </View>

    );

  }


  // Si todavía no encuentra la cámara frontal
  if (!device) {

    return (

      <View style={styles.center}>

        <ActivityIndicator
          size="large"
          color="#00FF88"
        />

        <Text style={styles.loadingText}>
          Buscando cámara frontal...
        </Text>

      </View>

    );

  }


  return (

    <View style={styles.container}>


      {/* Cámara frontal en tiempo real */}

      <Camera
        style={StyleSheet.absoluteFill}

        device={device}

        isActive={true}

        performanceMode="fast"

        runLandmarks={false}

        runContours={false}

        runClassifications={false}

        minFaceSize={0.15}

        onFacesDetected={handleFacesDetected}
      />


      {/* Título */}

      <View style={styles.titleContainer}>

        <Text style={styles.title}>
          Detección Facial
        </Text>

      </View>


      {/* Marco para colocar el rostro */}

      <View
        style={[
          styles.faceFrame,

          faceDetected
            ? styles.faceFrameDetected
            : styles.faceFrameSearching,
        ]}
      />


      {/* Mensaje */}

      <View
        style={[
          styles.statusContainer,

          faceDetected
            ? styles.detectedBackground
            : styles.searchingBackground,
        ]}
      >

        <Text style={styles.statusText}>

          {
            faceDetected
              ? 'ROSTRO DETECTADO'
              : 'Buscando rostro...'
          }

        </Text>

      </View>


    </View>

  );

}


const styles = StyleSheet.create({

  container: {

    flex: 1,

    backgroundColor: '#000000',

  },


  center: {

    flex: 1,

    justifyContent: 'center',

    alignItems: 'center',

    backgroundColor: '#000000',

    padding: 30,

  },


  loadingText: {

    color: '#ffffff',

    fontSize: 18,

    marginTop: 20,

  },


  permissionTitle: {

    color: '#ffffff',

    fontSize: 22,

    fontWeight: 'bold',

    textAlign: 'center',

    marginBottom: 15,

  },


  permissionText: {

    color: '#ffffff',

    fontSize: 17,

    textAlign: 'center',

  },


  titleContainer: {

    position: 'absolute',

    top: 60,

    left: 0,

    right: 0,

    alignItems: 'center',

  },


  title: {

    color: '#ffffff',

    fontSize: 24,

    fontWeight: 'bold',

    backgroundColor: 'rgba(0,0,0,0.55)',

    paddingVertical: 10,

    paddingHorizontal: 22,

    borderRadius: 15,

  },


  faceFrame: {

    position: 'absolute',

    width: 260,

    height: 350,

    top: '24%',

    alignSelf: 'center',

    borderWidth: 4,

    borderRadius: 140,

  },


  faceFrameSearching: {

    borderColor: '#ffffff',

  },


  faceFrameDetected: {

    borderColor: '#00FF66',

  },


  statusContainer: {

    position: 'absolute',

    bottom: 80,

    alignSelf: 'center',

    paddingVertical: 15,

    paddingHorizontal: 30,

    borderRadius: 15,

  },


  searchingBackground: {

    backgroundColor: 'rgba(0,0,0,0.70)',

  },


  detectedBackground: {

    backgroundColor: '#00A852',

  },


  statusText: {

    color: '#ffffff',

    fontSize: 20,

    fontWeight: 'bold',

    textAlign: 'center',

  },

});