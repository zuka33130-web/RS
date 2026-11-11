# Retro State RS

Aplicación Android para gestionar un taller de reparación de consolas: órdenes y clientes, presupuestos en PDF, finanzas, tareas, inventario, estadísticas y copias de seguridad locales.

## Requisitos

- Android Studio con JDK 21.
- Android SDK 36.1 (el proyecto usa `minSdk 24` y `targetSdk 36`).
- No hace falta configurar Gemini ni Firebase para ejecutar la versión actual.

## Abrir y ejecutar

1. Abrí este directorio en Android Studio y esperá la sincronización de Gradle.
2. Ejecutá la configuración **app** en un emulador o dispositivo con Android 7.0 o superior.

Desde una terminal también podés compilar y probar el proyecto:

```bash
./gradlew testDebugUnitTest
./gradlew assembleDebug
```

El APK de depuración queda en `app/build/outputs/apk/debug/app-debug.apk`.

## Firma de una versión release

La compilación release puede generarse sin firma. Para firmarla, definí estas variables de entorno antes de ejecutar `./gradlew assembleRelease`:

- `KEYSTORE_PATH`: ruta al archivo JKS.
- `STORE_PASSWORD`: contraseña del almacén.
- `KEY_PASSWORD`: contraseña de la clave.
- `KEY_ALIAS` (opcional): alias; por defecto se usa `upload`.

Las credenciales no deben agregarse al repositorio.

## Datos y respaldos

La información se guarda localmente con Room. Desde **Ajustes** se puede exportar, compartir y restaurar una copia JSON. La restauración reemplaza los datos actuales solo después de validar el archivo y se ejecuta dentro de una transacción para evitar restauraciones parciales.

## Verificación continua

El workflow **Android CI** ejecuta las pruebas unitarias y compila el APK de depuración en cada push y pull request.
