# Guía completa: emulación de sensores en Android Studio

## Objetivo
Este proyecto **sí usa la API oficial Android** (`SensorManager`, `LocationManager`) y recibe tanto lecturas de hardware real como las que inyecta un emulador Android (AVD). No requiere un teléfono físico.

## Requisitos
1. Android Studio con Android Emulator y un AVD (por ejemplo, Pixel con Google APIs).
2. Abrir la carpeta `SensoresAndroid` (contiene `settings.gradle.kts`).
3. Esperar la sincronización Gradle y ejecutar `app`.
4. Para usar los controles virtuales: abrir los tres puntos `⋮` de la barra lateral del emulador (Extended Controls).

## Prácticas que puedes enseñar al profesor

| Pantalla | Prueba con Android Emulator | Resultado |
|---|---|---|
| Acelerómetro | Extended Controls > Virtual Sensors > Device Pose: cambia la postura del teléfono 3D | X/Y/Z en m/s²; incluye gravedad |
| Giroscopio | Virtual Sensors > Device Pose: gira el modelo 3D en movimiento | X/Y/Z en rad/s, puede volver a cero cuando se detiene |
| Magnetómetro | Virtual Sensors > Additional Sensors: modifica Magnetic field (cuando el AVD lo incluya) | X/Y/Z en microteslas |
| GPS | Extended Controls > Location > Single points > Set location | Latitud y longitud simuladas |
| Cámara | Configura cámara virtual en AVD Manager/Device Manager; abre Cámara > Tomar foto | Miniatura de captura de la app de cámara del emulador |
| Podómetro | Si el AVD tiene TYPE_STEP_COUNTER: lecturas de pasos con permiso. Si NO: pulsa 'Activar DEMOSTRACIÓN manual' y '+1 paso' | La pantalla diferencia expresamente datos del sensor frente a pasos simulados manualmente |

**Importante:** Que los controles aparezcan en Extended Controls NO garantiza que todos los AVD publiquen todos los sensores. Es normal que algunos modelos no tengan contador de pasos, sensor magnético o giroscopio. La app indica cuando falta hardware. En Device Manager, elige un perfil que soporte el sensor o prueba otro AVD. No inventamos lecturas.

## Explicación del código
- `SplashActivity`: muestra animación y abre el menú sin dejar el splash en historial.
- `MainActivity`: crea el Navigation Drawer y abre seis `Activity` independientes con `Intent`.
- `BaseSensorActivity`: busca sensores mediante `SensorManager`, registra `SensorEventListener` al reanudar y lo desregistra al pausar; asigna títulos, descripciones y guías de emulación.
- `AcelerometroActivity`: `TYPE_ACCELEROMETER` da aceleración con gravedad en m/s².
- `GiroscopioActivity`: `TYPE_GYROSCOPE` da velocidad angular en rad/s.
- `MagnetometroActivity`: `TYPE_MAGNETIC_FIELD` da componentes del campo en µT.
- `PodometroActivity`: `TYPE_STEP_COUNTER` da total desde reinicio; el modo manual es independiente, visible y etiquetado como demostración.
- `GpsActivity`: solicita permiso en Android; `LocationManager` muestra las coordenadas publicadas por el AVD.
- `CamaraActivity`: abre otra app mediante `ACTION_IMAGE_CAPTURE` y recibe una miniatura de baja resolución.
- `AndroidManifest.xml`: declara pantallas y permisos.
- `activity_sensor.xml`: diseño compartido; incluye instrucciones para emular y controles de podómetro.

## ¿Cómo se comunican Java y XML?
`setContentView(R.layout.activity_sensor)` abre el diseño; `findViewById` obtiene sus controles; `setText` escribe datos recibidos desde los callbacks `onSensorChanged()` y `onLocationChanged()`.

## Solución de problemas
- El valor no cambia: verifica que estás en la pantalla adecuada, que moviste los controles virtuales y que el sensor está disponible en el AVD.
- GPS sin actualización: enciende Location en Android, concede permisos y pulsa Set location; puede ser necesario abrir nuevamente GPS.
- Podómetro sin lecturas: muchos AVD no incluyen contador de pasos. Utiliza el modo manual, explicando claramente su diferencia.
- Cámara no funciona: configura cámara frontal/trasera emulada en las opciones del AVD e instala/abre la app de cámara.
- Error Gradle: sincroniza con internet y usa la versión JDK adecuada para el Android Gradle Plugin de tu proyecto.

No se ha ejecutado una compilación Android ni una prueba de hardware en este entorno de generación; verifica tu AVD en Android Studio.
