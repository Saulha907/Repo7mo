# SensoresAndroid — Guía del proyecto documentado

## Alcance
Proyecto Java + XML con seis Activities individuales: AcelerometroActivity,
PodometroActivity, GiroscopioActivity, CamaraActivity, GpsActivity y MagnetometroActivity.
También incorpora MainActivity (Navigation Drawer) y SplashActivity (animación de inicio).

## Cómo abrirlo
1. Descomprime el ZIP.
2. Abre la carpeta `SensoresAndroid` desde Android Studio (File > Open).
3. Sincroniza Gradle (requiere internet para descargar dependencias si no están en caché).
4. Selecciona dispositivo/emulador y presiona Run.
5. Concede los permisos cuando la app los solicite.

## Ubicación de archivos
- `app/src/main/java/com/example/sensoresandroid/`: archivos Java, comentados.
- `app/src/main/res/layout/`: diseños XML, comentados.
- `app/src/main/AndroidManifest.xml`: registro de pantallas y permisos.
- `app/build.gradle.kts`: SDK y dependencias.

## Descripción para exponer
- **Acelerómetro**: mide aceleración incluyendo gravedad (m/s²), tres ejes.
- **Giroscopio**: velocidad angular alrededor de cada eje (rad/s).
- **Magnetómetro**: intensidad del campo magnético (µT).
- **Podómetro**: TYPE_STEP_COUNTER reporta pasos acumulados desde el reinicio;
  la app descuenta la primera lectura para calcular pasos de la sesión.
- **Cámara**: envía un Intent a una app de cámara y presenta su miniatura;
  **no guarda una imagen de resolución completa**.
- **GPS**: usa LocationManager; solicita ubicación precisa o aproximada.
  Puede usar proveedor de red como respaldo; no muestra un mapa.
- **Navigation Drawer**: DrawerLayout abre una lista lateral con seis botones;
  cada botón inicia su Activity mediante Intent.
- **Splash**: anima un TextView con alpha y escala, y navega al menú.

## Ciclo de vida de los sensores
- `onCreate` prepara el diseño y obtiene el sensor.
- `onResume` registra el sensor cuando hay permiso.
- `onSensorChanged` muestra nuevas mediciones.
- `onPause` deja de escuchar y reduce consumo de batería.

## Limitaciones de prueba
- Algunos emuladores carecen de podómetro, giroscopio o magnetómetro.
- Para podómetro real lo ideal es un Android físico compatible.
- El GPS requiere ubicación activada y permisos; un emulador puede simular ubicación.
- Las fotos se muestran como miniaturas; no se almacenan en galería por esta app.
- Los comentarios son didácticos; no alteran el código ejecutable.
- La compilación de Gradle no se ejecutó automáticamente en este entorno.
