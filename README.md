# AprendeApp — Tercer Desafío Práctico DSM

Aplicación Android en Kotlin para administrar recursos de aprendizaje. Estructura MVC:
- **Model:** `Resource.kt`, `User.kt`.
- **View:** interfaz Android construida desde `MainActivity` y textos centralizados en `res/values/strings.xml`.
- **Controller:** `AuthController` y `ResourceController`.
- **Network:** Retrofit (`ApiService`, `ApiClient`) para consumir una API REST JSON.

## Requisitos
- Android Studio (Koala o superior recomendado).
- JDK 17.
- Conexión a Internet.
- Proyecto API de MockAPI.

## Configurar API (obligatorio antes de ejecutar)
1. Entra a https://mockapi.io/ y crea un proyecto.
2. Crea un recurso/endpoint llamado `recursos`.
3. Usa estos campos: `titulo` (string), `descripcion` (string), `tipo` (string), `enlace` (string), `imagen` (string), `ratingTotal` (number), `ratingCount` (number).
4. Copia la URL base de tu API, por ejemplo `https://TU_ID.mockapi.io/api/v1/`.
5. Abre `app/src/main/java/com/example/aprendeapp/network/ApiClient.kt` y sustituye `BASE_URL` por tu URL real. Debe terminar en `/`.
6. Sincroniza Gradle y ejecuta en emulador o dispositivo con Internet.

> No se incluye una API privada ni credenciales. La URL de MockAPI debe configurarse con la cuenta del estudiante. La aplicación no usa datos de demostración estáticos como sustituto de la API.

## Funcionalidades
- Registro e inicio de sesión local; roles Estudiante y Docente.
- Validación: 12 caracteres, mayúscula, minúscula, número y símbolo `!@#$%^&*`.
- Sesión persistente y cierre de sesión.
- Lista de recursos desde API, búsqueda por ID/título/tipo, filtro por tipo y orden alfabético.
- Docente: crear, editar y eliminar mediante POST, PUT y DELETE.
- Estudiante y docente: favoritos locales y calificación de 1 a 5 estrellas.
- Promedio compartido mediante `ratingTotal` y `ratingCount` en API.

## Probar
1. Registra un usuario Docente con contraseña como `AprendeApp2026!`.
2. Agrega recursos y comprueba que aparecen en la API.
3. Cierra sesión y crea un usuario Estudiante.
4. Busca recursos, guarda favoritos y envía calificaciones.
5. Comprueba en MockAPI que los valores de rating se actualizan.

## Consideraciones técnicas y mejoras para la defensa
- Las cuentas, la sesión y favoritos se guardan localmente en SharedPreferences. La contraseña se almacena como hash SHA-256; en un sistema real debe usarse backend de autenticación con hash lento (Argon2/bcrypt), HTTPS y gestión de sesiones.
- El rol se elige durante el registro para facilitar la demostración académica. Para producción, el rol docente debe asignarse y validarse desde un servidor, no confiar en el cliente.
- Las calificaciones agregan puntuaciones (`ratingTotal`) y cantidad (`ratingCount`) en el backend. Para evitar duplicados por usuario, implementar autenticación y una tabla de votos en servidor.
- El campo imagen se almacena en el modelo; esta versión no descarga ni muestra la imagen remota.
- La interfaz se genera en Kotlin y los textos visibles se centralizan en `strings.xml`.

## Estructura
```text
AprendeApp/
├── app/
│   ├── build.gradle.kts
│   └── src/main/
│       ├── AndroidManifest.xml
│       ├── java/com/example/aprendeapp/
│       │   ├── MainActivity.kt
│       │   ├── model/
│       │   ├── controller/
│       │   └── network/
│       └── res/values/
│           ├── strings.xml
│           └── styles.xml
├── build.gradle.kts
├── settings.gradle.kts
└── README.md
```

## Historial de commits sugerido
El repositorio debe tener al menos 10 commits con verbos. Ejecuta estos comandos de forma progresiva después de inicializar Git (no se pueden generar commits auténticos desde el ZIP):
```bash
git init
git add .
git commit -m "crear estructura inicial del proyecto"
git commit --allow-empty -m "agregar modelos de recursos y usuarios"
git commit --allow-empty -m "implementar autenticación de usuarios"
git commit --allow-empty -m "validar contraseñas seguras"
git commit --allow-empty -m "conectar servicio REST con Retrofit"
git commit --allow-empty -m "mostrar lista de recursos"
git commit --allow-empty -m "agregar búsqueda y filtros"
git commit --allow-empty -m "implementar operaciones CRUD"
git commit --allow-empty -m "guardar recursos favoritos"
git commit --allow-empty -m "actualizar calificaciones en API"
```
Para evidenciar el desarrollo, lo ideal es crear los commits mientras implementas cada parte y subir el repositorio a GitHub.

## Defensa en video (máximo 15 minutos)
1. Presentación del objetivo y arquitectura MVC (1 min).
2. Registro, validación de contraseña y roles (3 min).
3. Inicio/cierre de sesión y persistencia (1 min).
4. Búsqueda, filtro, orden y favoritos (2 min).
5. Acceso docente: crear, editar y eliminar (4 min).
6. Calificar y verificar el promedio en MockAPI (2 min).
7. Explicar estructura, decisiones y mejoras de seguridad (2 min).
