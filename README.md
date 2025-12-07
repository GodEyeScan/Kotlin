# GEODE - Documentación Completa Unificada

**Versión:** 5.0  
**Fecha:** 2025-12-07  
**Proyecto:** Sistema de Detección y Reporte de Placas Vehiculares con Tracking Global

---

## Tabla de Contenidos

1. [Introducción](#introducción)
2. [Inicio Rápido](#inicio-rápido)
3. [Características Principales](#características-principales)
4. [Funcionalidades Implementadas](#funcionalidades-implementadas)
5. [Soluciones a Problemas Críticos](#soluciones-a-problemas-críticos)
6. [Integración con API](#integración-con-api)
7. [Base de Datos Local](#base-de-datos-local)
8. [Guía de Uso](#guía-de-uso)
9. [Guía de Pruebas](#guía-de-pruebas)
10. [Solución de Problemas](#solución-de-problemas)

---

## Introducción

**GEODE** es una aplicación Android que permite detectar automáticamente placas vehiculares mediante la cámara, verificar si están reportadas, y crear reportes de vehículos sospechosos o infractores.

### Características Destacadas

- **Detección automática de placas** con ML Kit OCR
- **Verificación en tiempo real** contra base de datos
- **Sistema local-first** - funciona sin internet
- **Prevención de duplicados** - una placa = un reporte
- **Autenticación JWT** con roles (usuario/admin)
- **Panel de administrador** para gestionar reportes
- **Historial completo** con GPS y timestamp
- **Perfil de usuario local** persistente
- **Tracking global de placas** - visualización de rutas en mapa
- **Marcadores interactivos** con información detallada
- **Alertas de placas reportadas** con vibración y notificación
- **Historial aislado por usuario** con seguridad mejorada
- **Sistema de colores** para identificar placas reportadas

---

## Inicio Rápido

### Requisitos Previos

- **Android Studio** Arctic Fox o superior
- **SDK Android** 24+ (Android 7.0)
- **Gradle** 8.0+
- **Kotlin** 1.9+
- **Conexión a Internet** (para sincronización con API)

### Instalación

1. **Clonar el repositorio:**
 ```bash
 git clone https://github.com/tu-usuario/godeye.git
 cd godeye
 ```

2. **Abrir en Android Studio:**
 - File → Open → Seleccionar carpeta del proyecto

3. **Sincronizar Gradle:**
 ```bash
 ./gradlew clean build
 ```

4. **Ejecutar:**
 - Run → Run 'app'
 - O usar: `./gradlew installDebug`

### Primera Ejecución

1. **Registrar cuenta:**
 - Email: tu-email@ejemplo.com
 - Contraseña: mínimo 6 caracteres
 - Nombre completo
 - Teléfono: +57 3001234567
 - NIT: 123456789

2. **Otorgar permisos:**
 - Cámara
 - Ubicación GPS
 - Almacenamiento

3. **Tomar primera foto:**
 - Ir a pestaña "Cámara"
 - Apuntar a una placa
 - Capturar foto o video
 - Esperar detección automática

---

## Características Principales

### 1. Detección de Placas con ML Kit

**Tecnología:** Google ML Kit Text Recognition V2

**Funcionamiento:**
- Captura de imagen o video
- Procesamiento con OCR
- Extracción de texto
- Detección de patrón de placa (regex)
- Validación y guardado

**Códigos de ejemplo:**
```kotlin
// Procesamiento de imagen
val (extractedText, detectedPlate) = PlateDetector.processImage(imageUri)

// Validación de placa
if (PlateDetector.shouldSaveCapture(extractedText)) {
 // Guardar captura
}
```

### 2. Sistema Local-First

**Filosofía:** Guardar primero localmente, sincronizar después

**Flujo:**
```
Captura
 ↓
Guardar en BD Local (SIEMPRE)
 ↓
¿Hay Internet?
 SÍ → Sincronizar con API 
 NO → Solo local 
 ↓
Éxito (datos seguros)
```

**Ventajas:**
- Funciona offline
- No pierde datos
- Respuesta inmediata
- Sincronización transparente

### 3. Prevención de Duplicados

**Problema resuelto:** Evita múltiples reportes de la misma placa

**Solución:**
```kotlin
// Verificar antes de crear
GET /reports/check/{placa}

if (exists) {
 // Ya existe - NO crear duplicado
 Log.w("Placa ya reportada")
} else {
 // No existe - Crear reporte
 POST /reports
}
```

**Resultado:**
- Una placa = Un reporte en servidor
- Múltiples fotos = Múltiples entradas locales
- Sin duplicados en API

### 4. Autenticación y Roles

**Sistema:** JWT (JSON Web Tokens)

**Roles disponibles:**
- **Usuario normal:** Puede crear reportes y ver su historial
- **Administrador:** Puede ver todos los reportes del sistema

**Endpoints:**
- `POST /auth/register` - Registro de usuario
- `POST /auth/login` - Inicio de sesión
- `POST /auth/register-admin` - Registro de admin

### 5. Perfil de Usuario Local

**Datos almacenados localmente:**
- Nombre completo
- Teléfono (con prefijo)
- NIT

**Persistencia:**
- Se guarda al registrarse
- Se carga al hacer login
- Se usa para autocompletar reportes
- Persiste entre sesiones

---

## Funcionalidades Implementadas

### Detección Automática de Placas

**Modos de captura:**
1. **Foto estática** - Toma una foto y analiza
2. **Video continuo** - Graba 2 segundos y analiza múltiples frames

**Proceso:**
```
Captura → OCR → Regex → Validación → Guardado
```

**Patrones de placa soportados:**
- ABC123 (3 letras, 3 números)
- ABC12D (3 letras, 2 números, 1 letra)
- Variaciones con espacios o guiones

### Verificación en Tiempo Real

**Endpoint:** `GET /reports/check/{placa}`

**Respuesta:**
```json
{
 "status": 200,
 "exists": true,
 "data": {
 "placa": "ABC123",
 "type": "vehiculo",
 "color": "rojo"
 }
}
```

**Logs en LogCat:**
```
 GET /reports/check/ABC123
 ¡PLACA ENCONTRADA! ABC123 - 1 reporte(s)
 ¡ALERTA! Placa reportada
```

### Creación de Reportes

**Modos:**
1. **Automático** - Al detectar placa con cámara
2. **Manual** - Formulario en pestaña "Reportar"

**Datos incluidos:**
- Placa detectada/ingresada
- Ubicación GPS
- Timestamp
- Tipo de vehículo
- Color (si se conoce)
- Razón del reporte
- Datos del reportante (nombre, teléfono, NIT)

**Endpoints:**
- `POST /reports` - Crear reporte
- `POST /history` - Guardar historial con foto

### Historial de Usuario

**Ubicación:** Pestaña "Historial"

**Contenido:**
- Todas las capturas del usuario
- Fotos tomadas
- Placas detectadas
- Ubicación GPS de cada captura
- Fecha y hora
- Estado de sincronización

**Filtrado:**
- Por usuario (cada uno ve solo lo suyo)
- Ordenado por fecha (más reciente primero)

### Panel de Administrador

**Acceso:** Solo usuarios con rol admin

**Funciones:**
- Ver todos los reportes del sistema
- Filtrar y buscar reportes
- Ver estadísticas
- Botón de refrescar

**Endpoint:** `GET /admin/reports`

---

## Funcionalidades Nuevas (V5.0)

### 1. Tracking Global de Placas en Mapa

**Descripción:** Visualización completa del historial de movimiento de una placa detectada por TODOS los usuarios del sistema.

**Características:**
- **Marcadores de colores:**
  - 🔴 **Rojo:** Ubicación desde donde se abrió el mapa (actual)
  - 🔵 **Azul:** Todas las ubicaciones anteriores donde se vio esa placa
- **Información interactiva:** Al hacer clic en cada marcador se muestra:
  - Placa detectada
  - Fecha y hora (formato: dd/MM/yyyy HH:mm)
  - Usuario que la detectó (primeros caracteres del email)
- **Card informativo:** Muestra total de avistamientos y leyenda de colores

**Cómo usar:**
1. Ve al historial
2. Haz clic en una captura con placa detectada
3. Se abre el mapa mostrando:
   - Tu ubicación en rojo
   - Todas las otras ubicaciones en azul (de cualquier usuario)
   - Total de avistamientos en el card inferior

**Implementación técnica:**
```kotlin
// Obtener TODAS las capturas de una placa (todos los usuarios)
suspend fun getAllCapturesByPlateAllUsers(plate: String): List<CaptureData>

// Query sin filtro de usuario
@Query("SELECT * FROM captures WHERE detectedPlate LIKE '%' || :plate || '%' ORDER BY timestamp DESC")
fun getCapturesByPlate(plate: String): Flow<List<CaptureEntity>>
```

### 2. Sistema de Alertas de Placas Reportadas

**Descripción:** Alerta visual, sonora y háptica cuando se detecta una placa que está reportada en el sistema.

**Componentes de la alerta:**
- **Vibración:** Patrón personalizado (500ms vibrar, 200ms pausa, 500ms vibrar)
- **Notificación:** Push notification con prioridad alta
- **Diálogo en pantalla:** AlertDialog con información de la placa
- **Indicador visual:** Recuadro rojo en lugar de azul en el historial

**Flujo de alerta:**
```
1. Placa detectada → Verificación en API
   ↓
2. ¿Está reportada?
   ↓
3. SÍ → Activar alertas:
   - Vibración
   - Notificación
   - Diálogo "ALERTA! Placa reportada"
   - Marcar isReported = true
   ↓
4. Guardar con indicador visual rojo
```

**Ejemplo de uso:**
```kotlin
// En VideoProcessor
if (encontrada && cantidadReportes > 0) {
    // Activar vibración
    vibrator.vibrate(VibrationEffect.createWaveform(longArrayOf(0, 500, 200, 500), -1))
    
    // Mostrar notificación
    notificationManager.notify(NOTIFICATION_ID, notification)
    
    // Mostrar diálogo de alerta
    _plateAlert.value = PlateAlert(placa, cantidadReportes, timestamp)
}
```

### 3. Identificación Visual de Placas Reportadas

**Descripción:** Sistema de colores para distinguir placas reportadas en el historial.

**Colores implementados:**
- **Azul (Primary):** Placa normal, sin reportes
- **Rojo (Error):** Placa reportada en el sistema

**Componentes visuales:**
```kotlin
// En CaptureCard
Card(
    colors = CardDefaults.cardColors(
        containerColor = if (capture.isReported) {
            MaterialTheme.colorScheme.errorContainer  // ROJO
        } else {
            MaterialTheme.colorScheme.primaryContainer  // AZUL
        }
    )
) {
    Text(
        text = if (capture.isReported) 
            "PLACA REPORTADA"  // Con warning icon
        else 
            "PLACA DETECTADA"
    )
}
```

**Campo agregado a la base de datos:**
```kotlin
@Entity(tableName = "captures")
data class CaptureEntity(
    // ...campos existentes...
    val isReported: Boolean = false  // NUEVO
)
```

### 4. Aislamiento Total de Historial por Usuario

**Descripción:** Sistema mejorado que garantiza que cada usuario vea SOLO su propio historial.

**Problema resuelto:** Usuarios veían historial de otros usuarios al cambiar de sesión.

**Solución implementada:**
```kotlin
// Control de Job para cancelar Flow anterior
private var capturesJob: Job? = null

fun setCurrentUser(email: String) {
    // 1. Cancelar Flow anterior
    capturesJob?.cancel()
    
    // 2. Limpiar datos
    _captures.clear()
    
    // 3. Cargar datos del nuevo usuario
    capturesJob = viewModelScope.launch {
        captureDao.getCapturesByUser(email).collect { entities ->
            _captures.clear()
            _captures.addAll(entities.map { it.toCaptureData() })
        }
    }
}
```

**Características:**
- Cancelación automática del Flow anterior
- Limpieza completa del estado
- Recarga forzada al abrir historial
- Logs detallados para debugging

### 5. Diálogos de Bienvenida Mejorados

**Descripción:** Mensajes de bienvenida personalizados al iniciar sesión o registrarse.

**Características:**
- **Sin emojis:** Diseño limpio y profesional
- **Personalizado:** Muestra el nombre real del usuario
- **Corrección de bug:** Ahora muestra el nombre correcto en login (antes mostraba el email)

**Implementación:**
```kotlin
// Login - Espera a que se cargue el perfil
LaunchedEffect(authViewModel.currentUser.value?.name, loginSuccessful) {
    if (loginSuccessful && authViewModel.currentUser.value != null) {
        delay(200)  // Espera a que loadUserProfile() termine
        userName = authViewModel.currentUser.value?.name ?: "Usuario"
        showWelcomeDialog = true
    }
}

// Diálogo
AlertDialog(
    title = { Text("Bienvenido a GEODE") },
    text = { Text("Hola $userName, has iniciado sesion exitosamente.") },
    confirmButton = { Button(onClick = { ... }) { Text("Continuar") } }
)
```

### 6. Sistema de Debugging Completo

**Descripción:** Logs exhaustivos para diagnosticar problemas de aislamiento de historial.

**Logs implementados:**
```kotlin
// Al abrir historial
D/CaptureListScreen: === DEBUG: Capturas en BD por usuario ===
D/CaptureListScreen: Usuario: userA@email.com -> 5 capturas
D/CaptureListScreen: Usuario: userB@email.com -> 2 capturas
D/CaptureListScreen: ========================================

// Al cambiar de usuario
D/CaptureViewModel: setCurrentUser llamado para: userB@email.com
D/CaptureViewModel: Estado limpiado. Captures size: 0
D/CaptureViewModel: Flow emitió 2 capturas para: userB@email.com
```

**Método de debugging:**
```kotlin
suspend fun debugCaptureCount(): Map<String, Int> {
    val allCaptures = captureDao.getAllCapturesOnce()
    return allCaptures.groupBy { it.userEmail }
        .mapValues { it.value.size }
}
```

---

## Soluciones a Problemas Críticos

### 1. Perfil se Perdía al Hacer Login 

**Problema:** Al registrarse se guardaba nombre/teléfono/NIT, pero al salir y hacer login se perdía.

**Causa:** `loadUserProfile()` cargaba de BD pero no actualizaba `currentUser`.

**Solución:**
```kotlin
private fun loadUserProfile(email: String) {
 val profile = userProfileRepository.getProfile(email)
 if (profile != null) {
 _userProfile.value = profile
 // Actualizar currentUser con datos del perfil
 _currentUser.value = currentUser.copy(
 name = profile.name,
 phonePrefix = profile.phone.substringBefore(" "),
 phoneNumber = profile.phone.substringAfter(" "),
 nit = profile.nit
 )
 }
}
```

### 2. Historial Compartido Entre Usuarios 

**Problema:** Todos veían las mismas capturas.

**Causa:** Las capturas no tenían identificación de usuario.

**Solución:**
- Agregado campo `userEmail` a `CaptureEntity` y `CaptureData`
- Queries filtrados por usuario
- `setCurrentUser(email)` al hacer login

```kotlin
@Query("SELECT * FROM captures WHERE userEmail = :email ORDER BY timestamp DESC")
fun getCapturesByUser(email: String): Flow<List<CaptureEntity>>
```

### 3. Error JSON Parse (BEGIN_ARRAY vs BEGIN_OBJECT) 

**Problema:** Error "Expected BEGIN_ARRAY but was BEGIN_OBJECT"

**Causa:** El modelo esperaba array `[]` pero servidor devolvía objeto `{}`

**Solución:** Modelo actualizado
```kotlin
data class CheckPlateResponse(
 val status: Int,
 val exists: Boolean,
 val data: ReportResponse? // Objeto, no lista
)
```

### 4. Endpoint Incorrecto 

**Problema:** Usaba `/reportscheck/{placa}`

**Solución:** Actualizado a `/reports/check/{placa}`

### 5. POST de Reportes No Funcionaba 

**Problema:** Reportes no se subían a la API

**Solución:** Logs exhaustivos para debugging
```kotlin
Log.d(" POST /reports - Placa: $placa")
Log.d(" Respuesta: ${response.code()}")
if (!response.isSuccessful) {
 Log.e(" Error: ${response.errorBody()?.string()}")
}
```

### 6. Reportes Duplicados 

**Problema:** Múltiples fotos de la misma placa creaban múltiples reportes

**Solución:** Verificación antes de POST
```kotlin
// Verificar si existe
val checkResult = searchReportByPlate(token, placa)
if (checkResult.data.isNotEmpty()) {
 // Ya existe - NO crear duplicado
} else {
 // No existe - Crear reporte
 POST /reports
}
```

---

## Integración con API

### Base URL

```
https://gateway.helmer-pardo.com
```

### Endpoints Implementados

#### Autenticación

**Registro:**
```http
POST /auth/register
Content-Type: application/json

{
 "email": "user@example.com",
 "password": "password123"
}

Response 200:
{
 "token": "eyJhbGci...",
 "user": { ... }
}
```

**Login:**
```http
POST /auth/login
Content-Type: application/json

{
 "email": "user@example.com",
 "password": "password123"
}
```

#### Reportes

**Crear reporte:**
```http
POST /reports
Authorization: Bearer {token}
Content-Type: application/json

{
 "placa": "ABC123",
 "timestamp": "2025-12-07T15:30:00-05:00",
 "type": "vehiculo",
 "color": "desconocido"
}

Response 201:
{
 "id": "uuid",
 "placa": "ABC123",
 ...
}
```

**Verificar placa:**
```http
GET /reports/check/{placa}
Authorization: Bearer {token}

Response 200:
{
 "status": 200,
 "exists": true,
 "data": { ... }
}
```

**Obtener reportes:**
```http
GET /reports
Authorization: Bearer {token}

Response 200:
[
 { "id": "...", "placa": "ABC123", ... },
 ...
]
```

**Admin - Todos los reportes:**
```http
GET /admin/reports
Authorization: Bearer {token}

Response 200:
[
 { "id": "...", "placa": "ABC123", "userId": "...", ... },
 ...
]
```

#### Historial

**Crear historial:**
```http
POST /history
Authorization: Bearer {token}
Content-Type: application/json

{
 "photo": "file:///path/to/photo.jpg",
 "timestamp": "2025-12-07T15:30:00Z",
 "latitude": 4.6097,
 "longitude": -74.0817
}
```

**Obtener historial:**
```http
GET /history
Authorization: Bearer {token}

Response 200:
[
 { "id": "...", "photo": "...", "latitude": 4.6097, ... },
 ...
]
```

---

## Base de Datos Local

### Tecnología

**Room Database** - Versión 5

### Entidades

#### 1. CaptureEntity
```kotlin
@Entity(tableName = "captures")
data class CaptureEntity(
 val id: Long,
 val userEmail: String, // Filtro por usuario
 val imageUri: String,
 val latitude: Double,
 val longitude: Double,
 val timestamp: Long,
 val extractedText: String,
 val detectedPlate: String,
 val isReported: Boolean = false // Indica si la placa está reportada
)
```

#### 2. ReportEntity
```kotlin
@Entity(tableName = "reports")
data class ReportEntity(
 val id: Long,
 val userEmail: String,
 val userName: String,
 val userPhone: String,
 val userNit: String,
 val plateNumber: String,
 val reportReason: String,
 val timestamp: Long
)
```

#### 3. UserProfileEntity
```kotlin
@Entity(tableName = "user_profile")
data class UserProfileEntity(
 val email: String, // PK
 val name: String,
 val phone: String,
 val nit: String
)
```

#### 4. HistoryEntity
```kotlin
@Entity(tableName = "history")
data class HistoryEntity(
 val id: Long,
 val userEmail: String,
 val photoUri: String,
 val latitude: Double,
 val longitude: Double,
 val timestamp: Long,
 val syncedWithApi: Boolean
)
```

### Migraciones

**Versión 3 → 4:**
- Agregado campo `userEmail` a `captures`
- Agregada tabla `history`

**Versión 4 → 5:**
- Agregado campo `isReported` a `captures`
- Implementado sistema de marcado de placas reportadas

**Estrategia:** `fallbackToDestructiveMigration()` (desarrollo)

---

## Guía de Uso

### Registro e Inicio de Sesión

1. **Abrir la app**
2. **Click en "Regístrate"**
3. **Llenar formulario:**
 - Nombre: Juan Pérez
 - Email: juan@gmail.com
 - Contraseña: test123
 - Teléfono: +57 3001234567
 - NIT: 123456789
4. **Click en "Registrarse"**
5. **Login automático**

### Tomar Foto de Placa

1. **Ir a pestaña "Cámara"**
2. **Apuntar a una placa vehicular**
3. **Opciones:**
 - **Foto:** Click en botón cámara
 - **Video:** Mantener presionado 2 segundos
4. **Esperar procesamiento OCR**
5. **Ver resultado:**
 - "Placa detectada: ABC123"
 - "Placa ABC123 REPORTADA" (si existe)
 - "Placa ABC123 sin reportes" (si limpia)

### Crear Reporte Manual

1. **Ir a pestaña "Reportar"**
2. **Ingresar placa:** ABC123
3. **Ingresar razón:** "Vehículo estacionado en lugar prohibido"
4. **Verificar datos prellenados:**
 - Nombre (del perfil)
 - Email
 - Teléfono
 - NIT
5. **Click en "Enviar Reporte"**
6. **Confirmar en diálogo de éxito**

### Ver Historial

1. **Ir a pestaña "Historial"**
2. **Ver lista de capturas:**
 - Foto (si hay)
 - Placa detectada
 - Fecha y hora
 - Ubicación GPS
3. **Click en captura para ver detalles**
4. **Ver en mapa (si tiene ubicación)**

### Editar Perfil

1. **Ir a pestaña "Perfil"**
2. **Click en "Editar Perfil"**
3. **Modificar datos:**
 - Nombre
 - Teléfono
 - NIT
4. **Click en "Guardar Cambios"**
5. **Confirmar actualización**

### Panel de Administrador

**Solo para usuarios admin**

1. **Ir a "Perfil"**
2. **Click en "Panel de Administrador"**
3. **Ver todos los reportes del sistema**
4. **Usar botón para refrescar**

---

## Guía de Pruebas

### Prueba 1: Registro y Persistencia de Perfil

```
1. Registrar usuario:
 - Email: test@gmail.com
 - Nombre: Test Usuario
 - Tel: +57 3001234567
 - NIT: 123456789

2. Verificar en "Editar Perfil":
 Todos los datos aparecen

3. Logout

4. Login con test@gmail.com

5. Verificar en "Editar Perfil":
 Datos siguen ahí (persistieron)

LogCat:
I/AuthViewModel: Perfil cargado desde BD local: Test Usuario
```

### Prueba 2: Separación de Historial

```
Usuario A:
1. Login como userA@test.com
2. Tomar foto de ABC123
3. Ver historial → ABC123 aparece
4. Logout

Usuario B:
1. Login como userB@test.com
2. Ver historial → VACÍO (no ve ABC123)
3. Tomar foto de XYZ789
4. Ver historial → Solo XYZ789

Usuario A:
1. Login como userA@test.com
2. Ver historial → Solo ABC123 (no ve XYZ789)
```

### Prueba 3: Verificación de Placa

```
1. Tomar foto de placa reportada (ABC123)

LogCat:
 GET /reports/check/ABC123
 ¡PLACA ENCONTRADA! ABC123 - 1 reporte(s)
 ¡ALERTA! Placa reportada

UI:
 Placa ABC123 REPORTADA (1 veces)
```

### Prueba 4: Prevención de Duplicados

```
1. Tomar foto de TEST999

LogCat:
 Placa TEST999 no existe en servidor
 POST /reports exitoso

2. Tomar otra foto de TEST999

LogCat:
 Placa encontrada: TEST999
 Placa TEST999 ya tiene 1 reporte(s)
 NO se creará reporte duplicado

Postman:
GET /reports/check/TEST999
→ Solo 1 reporte (no duplicó) 
```

### Prueba 5: Funcionamiento Offline

```
1. Activar modo avión (sin internet)

2. Tomar foto de placa

3. Verificar:
 Se guarda en BD local
 "Guardado localmente"
 No sincroniza con API

4. Desactivar modo avión

5. (Futuro) Sincronización automática
```

---

## Solución de Problemas

### Error: App se Cierra al Abrir

**Causa:** Base de datos desactualizada

**Solución:**
```
Configuración → Apps → GEODE → Almacenamiento → Limpiar datos
```

O reinstalar la app.

### Error: "Token no disponible"

**Causa:** Token expiró o sesión perdida

**Solución:**
1. Logout
2. Login de nuevo
3. Token se renovará

### Error: "Expected BEGIN_ARRAY but was BEGIN_OBJECT"

**Causa:** Modelo desactualizado (ya corregido)

**Solución:** Instalar última versión de la app

### Error: Perfil se Pierde

**Causa:** No se guardó correctamente (ya corregido)

**Verificar:**
```
LogCat filtro: AuthViewModel
Buscar: "Perfil cargado"
```

Si no aparece, el perfil no está en BD.

### Error: Reportes Duplicados

**Causa:** Ya corregido con verificación previa

**Verificar en LogCat:**
```
 NO se creará reporte duplicado
```

### Error: Sin Permisos

**Solución:**
```
Configuración → Apps → GEODE → Permisos
Activar:
- Cámara
- Ubicación
- Almacenamiento
```

---

## Logs Importantes

### Logs de Autenticación
```
I/AuthViewModel: Perfil cargado desde BD local: [nombre]
W/AuthViewModel: No hay perfil local guardado para: [email]
```

### Logs de Verificación de Placa
```
D/CaptureViewModel: GET /reports/check/ABC123
I/CaptureViewModel: ¡PLACA ENCONTRADA! ABC123 - X reporte(s)
I/CaptureViewModel: Placa NO encontrada: ABC123
```

### Logs de Creación de Reporte
```
D/ReportRepository: Preparando POST /reports
D/ReportRepository: Placa: ABC123
D/ReportRepository: Respuesta recibida: 201
I/CaptureViewModel: POST /reports exitoso - ID: xxx
```

### Logs de Duplicados
```
W/CaptureViewModel: Placa ABC123 ya tiene 1 reporte(s)
W/CaptureViewModel: NO se creará reporte duplicado
```

---

## Resumen de Estado Actual

### Funcionalidades Completadas

- [x] Detección automática de placas con ML Kit
- [x] Verificación en tiempo real contra API
- [x] Sistema local-first con sincronización
- [x] Prevención de duplicados
- [x] Autenticación JWT con roles
- [x] Perfil de usuario persistente
- [x] Historial separado por usuario
- [x] Panel de administrador
- [x] Creación de reportes (automático/manual)
- [x] Ubicación GPS en capturas
- [x] Logs exhaustivos para debugging

### Base de Datos

- **Versión actual:** 4
- **Estrategia migración:** Destructiva (desarrollo)
- **Tablas:** captures, reports, user_profile, history

### API

- **Base URL:** https://gateway.helmer-pardo.com
- **Autenticación:** Bearer JWT
- **Endpoints:** 10+ implementados
- **Logging:** HttpLoggingInterceptor.Level.BODY

### Compatibilidad

- **Android:** 7.0+ (API 24+)
- **Gradle:** 8.0+
- **Kotlin:** 1.9+
- **Compose:** 2024.02.00

---

## Información de Contacto

**Proyecto:** GEODE 
**Versión:** 4.0 
**Base de Datos:** Versión 4 
**API:** v2 
**Última actualización:** 2025-12-07

---

## Notas Finales

### Buenas Prácticas Implementadas

1. **Local-first architecture** - Datos siempre seguros
2. **Prevención de duplicados** - Integridad de datos
3. **Logs exhaustivos** - Fácil debugging
4. **Separación de datos por usuario** - Privacidad
5. **Manejo de errores robusto** - Mejor UX
6. **Persistencia de perfil** - Datos no se pierden
7. **Verificación antes de POST** - Evita conflictos

### Mejoras Futuras Sugeridas

1. Sincronización automática de datos pendientes
2. Dashboard con estadísticas
3. Mapa de calor de reportes
4. Notificaciones push
5. Mejora en detección de placas
6. Personalización de tema
7. Exportar reportes a PDF

---

** Documentación Completa y Actualizada** 
**Última revisión:** 2025-12-07 
**Estado:** Producción - Ready

