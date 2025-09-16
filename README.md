# 📱 Pixion Androids

## Aplicación de Información de Películas y Series

Pixion Android es una aplicación móvil moderna desarrollada en Kotlin que proporciona información detallada sobre películas y series de televisión. La aplicación utiliza la API de TMDB para obtener datos actualizados y Firebase para la gestión de usuarios y datos.

## 🌟 Características Principales

### 🎬 Contenido Multimedia
- Integración con la **API de TMDB** para datos en tiempo real
- Información detallada de películas y series
- Imágenes de alta calidad y pósters
- Información de reparto y equipo técnico
- Calificaciones y reseñas

### 👤 Gestión de Usuarios
- Autenticación con **Firebase**
- Registro e inicio de sesión
- Perfiles de usuario
- Sistema de favoritos
- Sincronización en la nube

### 🎨 Interfaz de Usuario
- Diseño Material Design
- Navegación intuitiva
- Soporte para diferentes tamaños de pantalla
- ViewBinding para una mejor gestión de vistas

## 🛠️ Tecnologías y Dependencias

### Core
- **Kotlin** - Lenguaje principal
- **Android SDK 35** - Versión mínima: 27 (Android 8.1)
- **Java 11** - Versión de compilación

### Firebase
- Firebase Authentication
- Firebase Realtime Database
- Firebase BOM (Bill of Materials)

### AndroidX
- Core KTX
- AppCompat
- Material Design
- ConstraintLayout
- Navigation Component

### Networking
- **Retrofit** - Cliente HTTP
- **Gson** - Conversión JSON
- **OkHttp** - Interceptor de logging

### UI/UX
- **Glide** - Carga y caché de imágenes
- **ViewBinding** - Binding de vistas
- **Navigation Component** - Navegación entre pantallas

### Asincronía
- **Coroutines** - Programación asíncrona
- **Play Services** - Integración con servicios de Google

### Testing
- JUnit
- AndroidX Test
- Espresso

## 📱 Requisitos del Sistema

- Android 8.1 (API level 27) o superior
- Conexión a Internet
- Cuenta de Google para autenticación
- 2GB de RAM recomendado

## 🚀 Instalación

1. Clona el repositorio:
```bash
git clone https://github.com/ch0rtas/pixion-android.git
```

2. Abre el proyecto en Android Studio

3. Configura Firebase:
   - Crea un proyecto en Firebase Console
   - Añade la aplicación Android
   - Descarga y coloca `google-services.json` en la carpeta `app/`

4. Configura la API key de TMDB:
   - Obtén una API key en [TMDB](https://www.themoviedb.org/documentation/api)
   - Añade la key en `local.properties`:
   ```
   TMDB_API_KEY=tu_api_key
   ```

5. Sincroniza el proyecto con Gradle

6. Ejecuta la aplicación

## 📦 Estructura del Proyecto

```
app/
├── src/
│   ├── main/
│   │   ├── java/com/chortas/pixion/
│   │   │   ├── data/           // Capa de datos
│   │   │   └── ui/            // Capa de presentación
│   │   ├── res/               // Recursos
│   │   └── AndroidManifest.xml
│   ├── test/                  // Tests unitarios
│   └── androidTest/           // Tests de instrumentación
├── build.gradle.kts          // Configuración de Gradle
├── proguard-rules.pro        // Reglas de ProGuard
└── google-services.json      // Configuración de Firebase
```

## 🎯 Características Técnicas

### Arquitectura
- Arquitectura limpia
- Patrón Repository
- MVVM (Model-View-ViewModel)

### Seguridad
- Autenticación Firebase
- ProGuard para ofuscación
- Validación de datos

### Testing
- Tests unitarios con JUnit
- Tests de UI con Espresso
- Tests de instrumentación

## 📊 Versiones

- Versión actual: 1.0
- Código de versión: 1
- SDK mínimo: 27 (Android 8.1)
- SDK objetivo: 35

## 👥 Contribución

Las contribuciones son bienvenidas. Por favor, sigue estos pasos:

1. Fork el repositorio
2. Crea una rama para tu feature (`git checkout -b feature/AmazingFeature`)
3. Commit tus cambios (`git commit -m 'Add some AmazingFeature'`)
4. Push a la rama (`git push origin feature/AmazingFeature`)
5. Abre un Pull Request

## 📝 Licencia

Este proyecto está bajo la Licencia MIT. Ver el archivo `LICENSE` para más detalles.

## 📬 Contacto

Manuel Martínez - [GitHub](https://github.com/ch0rtas)

Link del proyecto: [https://github.com/ch0rtas/pixion-android](https://github.com/ch0rtas/pixion-android)
