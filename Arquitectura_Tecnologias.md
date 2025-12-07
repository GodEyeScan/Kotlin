# GodEye - Arquitectura y Tecnologías

**Versión:** 5.0  
**Fecha:** 2025-12-07  
**Tipo:** Documentación Técnica

---

## Tabla de Contenidos

1. [Arquitectura General](#arquitectura-general)
2. [Patrones de Diseño](#patrones-de-diseño)
3. [Tecnologías Utilizadas](#tecnologías-utilizadas)
4. [Estructura del Proyecto](#estructura-del-proyecto)
5. [Capa de Datos](#capa-de-datos)
6. [Capa de Dominio](#capa-de-dominio)
7. [Capa de Presentación](#capa-de-presentación)
8. [Flujos de Datos](#flujos-de-datos)
9. [Decisiones de Diseño](#decisiones-de-diseño)

---

## Arquitectura General

### Clean Architecture + MVVM

```

 PRESENTATION LAYER 
 (UI - Jetpack Compose + ViewModels) 

 

 DOMAIN LAYER 
 (Use Cases / Business Logic) 

 

 DATA LAYER 
 (Repository + API + Local Database) 

```

### Principios Aplicados

1. **Separation of Concerns** - Cada capa tiene responsabilidad única
2. **Dependency Inversion** - Capas superiores no dependen de inferiores
3. **Single Responsibility** - Cada clase tiene una sola razón para cambiar
4. **Open/Closed Principle** - Abierto a extensión, cerrado a modificación
5. **Don't Repeat Yourself (DRY)** - Código reutilizable

---

## Patrones de Diseño

### 1. MVVM (Model-View-ViewModel)

**Implementación:**
```
View (Composable) ←→ ViewModel ←→ Repository ←→ Data Source
```

**Ejemplo:**
```kotlin
// View
@Composable
fun CameraScreen(viewModel: CaptureViewModel) {
 val captures by viewModel.captures.observeAsState()
 // UI usando captures
}

// ViewModel
class CaptureViewModel : ViewModel() {
 private val _captures = MutableLiveData<List<Capture>>()
 val captures: LiveData<List<Capture>> = _captures
 
 fun addCapture(capture: Capture) {
 viewModelScope.launch {
 repository.save(capture)
 }
 }
}

// Repository
class CaptureRepository(private val dao: CaptureDao) {
 suspend fun save(capture: Capture) {
 dao.insert(capture.toEntity())
 }
}
```

**Ventajas:**
- Separación UI y lógica de negocio
- Testeable
- Manejo de configuración (rotación)

### 2. Repository Pattern

**Propósito:** Abstracción de fuentes de datos

**Estructura:**
```kotlin
interface CaptureRepository {
 suspend fun save(capture: Capture)
 fun getAll(): Flow<List<Capture>>
 suspend fun sync(): Result<Unit>
}

class CaptureRepositoryImpl(
 private val localDataSource: CaptureDao,
 private val remoteDataSource: ApiService
) : CaptureRepository {
 // Implementación con múltiples fuentes
}
```

**Ventajas:**
- Oculta complejidad de fuentes de datos
- Fácil cambio de implementación
- Permite testing con mocks

### 3. Singleton Pattern

**Implementación:** Kotlin `object`

**Ejemplos:**
```kotlin
// Retrofit client
object RetrofitClient {
 val apiService: GodEyeApiService by lazy {
 // Inicialización
 }
}

// Database
object GodEyeDatabase {
 private var INSTANCE: GodEyeDatabase? = null
 fun getDatabase(context: Context): GodEyeDatabase {
 return INSTANCE ?: synchronized(this) {
 // Crear instancia
 }
 }
}
```

**Ventajas:**
- Una sola instancia
- Acceso global
- Inicialización lazy

### 4. Factory Pattern

**Uso:** ViewModelFactory para inyección de dependencias

```kotlin
class CaptureViewModelFactory(
 private val application: Application
) : ViewModelProvider.Factory {
 override fun <T : ViewModel> create(modelClass: Class<T>): T {
 if (modelClass.isAssignableFrom(CaptureViewModel::class.java)) {
 return CaptureViewModel(application) as T
 }
 throw IllegalArgumentException("Unknown ViewModel class")
 }
}
```

### 5. Observer Pattern

**Implementación:** Flow, LiveData, State

```kotlin
// Flow
val captures: Flow<List<Capture>> = captureDao.getAllCaptures()

// LiveData
val isLoading = MutableLiveData<Boolean>()

// Compose State
var plateNumber by remember { mutableStateOf("") }
```

**Ventajas:**
- Reactividad
- UI se actualiza automáticamente
- Desacoplamiento

### 6. Strategy Pattern

**Uso:** Diferentes estrategias de captura

```kotlin
interface CaptureStrategy {
 suspend fun capture(): CaptureResult
}

class PhotoCaptureStrategy : CaptureStrategy {
 override suspend fun capture(): CaptureResult {
 // Captura de foto
 }
}

class VideoCaptureStrategy : CaptureStrategy {
 override suspend fun capture(): CaptureResult {
 // Captura de video
 }
}
```

### 7. Builder Pattern

**Uso:** Construcción de objetos complejos

```kotlin
// Retrofit Builder
Retrofit.Builder()
 .baseUrl(BASE_URL)
 .client(okHttpClient)
 .addConverterFactory(GsonConverterFactory.create())
 .build()

// Room Database Builder
Room.databaseBuilder(context, GodEyeDatabase::class.java, "godeye_db")
 .fallbackToDestructiveMigration()
 .build()
```

### 8. Adapter Pattern

**Uso:** Conversión entre modelos

```kotlin
// Entity ↔ Data class
fun CaptureEntity.toCaptureData(): CaptureData {
 return CaptureData(
 id = id,
 userEmail = userEmail,
 imageUri = imageUri,
 // ...
 )
}

fun CaptureData.toEntity(): CaptureEntity {
 return CaptureEntity(
 id = id,
 userEmail = userEmail,
 imageUri = imageUri,
 // ...
 )
}
```

---

## Tecnologías Utilizadas

### Lenguajes

- **Kotlin** 1.9+ - Lenguaje principal
- **Java** - Interoperabilidad (ML Kit)
- **XML** - Recursos y configuración
- **Gradle (Kotlin DSL)** - Build system

### Framework UI

- **Jetpack Compose** 2024.02.00
 - Declarative UI
 - Material Design 3
 - Navigation Compose
 - Accompanist (permisos, pager)

### Arquitectura

- **Android Architecture Components**
 - ViewModel
 - LiveData
 - Lifecycle
 - Navigation
 - DataStore (futuro)

### Base de Datos

- **Room** 2.6.1
 - SQLite wrapper
 - Type-safe queries
 - Flow support
 - Coroutines integration

**Configuración:**
```kotlin
@Database(
 entities = [CaptureEntity::class, ReportEntity::class, 
 UserProfileEntity::class, HistoryEntity::class],
 version = 5,
 exportSchema = false
)
abstract class GodEyeDatabase : RoomDatabase()
```

### Networking

- **Retrofit** 2.9.0
 - REST API client
 - Kotlin Coroutines support
 - Gson converter

- **OkHttp** 4.12.0
 - HTTP client
 - Interceptors
 - Logging

**Configuración:**
```kotlin
OkHttpClient.Builder()
 .addInterceptor(loggingInterceptor)
 .connectTimeout(30, TimeUnit.SECONDS)
 .readTimeout(30, TimeUnit.SECONDS)
 .writeTimeout(30, TimeUnit.SECONDS)
 .build()
```

### Inyección de Dependencias

- **Hilt** 2.48 (opcional/futuro)
- **Manual DI** - Actualmente usando constructor injection

### Async/Threading

- **Kotlin Coroutines** 1.7.3
 - Structured concurrency
 - Flow
 - StateFlow
 - Dispatchers (IO, Main, Default)

**Ejemplo:**
```kotlin
viewModelScope.launch {
 withContext(Dispatchers.IO) {
 // Operación en background
 }
 // Resultado en Main thread
}
```

### Machine Learning

- **ML Kit** (Google)
 - Text Recognition V2
 - On-device processing
 - No requiere conexión

**Implementación:**
```kotlin
val recognizer = TextRecognition.getClient(
 TextRecognizerOptions.DEFAULT_OPTIONS
)
val image = InputImage.fromFilePath(context, uri)
recognizer.process(image)
```

### Location Services

- **Google Play Services Location** 21.0.1
 - FusedLocationProviderClient
 - High accuracy mode
 - GPS + Network

### Maps & Geolocation

- **Google Maps Compose** 4.3.0
 - Maps integration for Jetpack Compose
 - Marker API
 - Camera positioning
 - InfoWindow customization

- **Google Maps SDK** 18.2.0
 - Base maps functionality
 - Marker clustering (preparado)
 - Custom map styling

**Implementación:**
```kotlin
// Google Maps en Compose
GoogleMap(
 modifier = Modifier.fillMaxSize(),
 cameraPositionState = cameraPositionState,
 properties = MapProperties(isMyLocationEnabled = false),
 uiSettings = MapUiSettings(zoomControlsEnabled = true)
) {
 Marker(
 state = MarkerState(position = LatLng(lat, lng)),
 title = "Ubicacion",
 snippet = "Detalles",
 icon = BitmapDescriptorFactory.defaultMarker(HUE_RED)
 )
}
```

### Image Processing

- **CameraX** 1.3.0
 - Modern camera API
 - Use cases (Preview, ImageCapture, VideoCapture)
 - Lifecycle aware

- **Coil** 2.5.0
 - Image loading
 - Caching
 - Compose integration

### Serialización

- **Gson** 2.10.1
 - JSON parsing
 - Type adapters
 - Annotations (@SerializedName)

### Permisos

- **Accompanist Permissions** 0.32.0
 - Compose-friendly
 - Multiple permissions
 - Runtime permissions

### Notificaciones y Alertas

- **NotificationCompat** (AndroidX)
 - Push notifications
 - NotificationChannel para Android O+
 - Priority levels
 - Custom actions

- **Vibrator/VibratorManager** (Android)
 - Feedback háptico
 - Patrones personalizados
 - Soporte para Android S+

**Implementación:**
```kotlin
// Crear canal de notificación
val channel = NotificationChannel(
 CHANNEL_ID,
 "Alertas de Placas",
 NotificationManager.IMPORTANCE_HIGH
)
notificationManager.createNotificationChannel(channel)

// Vibración personalizada
val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
vibrator.vibrate(VibrationEffect.createWaveform(longArrayOf(0, 500, 200, 500), -1))
```

### Testing (futuro)

- **JUnit** 4.13.2
- **Mockito** 5.3.1
- **Espresso** 3.5.1
- **Compose UI Testing**

---

## Estructura del Proyecto

```
app/
 src/
 main/
 java/com/example/godeye/
 data/
 api/
 GodEyeApiService.kt
 RetrofitClient.kt
 ReportModels.kt
 AuthModels.kt
 database/
 GodEyeDatabase.kt
 Entities.kt
 Daos.kt
 repository/
 AuthRepository.kt
 ReportRepository.kt
 CaptureRepository.kt
 UserProfileRepository.kt
 CaptureData.kt
 User.kt
 ui/
 auth/
 LoginScreen.kt
 RegisterScreen.kt
 camera/
 CameraScreen.kt
 captures/
 CaptureListScreen.kt
 report/
 ReportScreen.kt
 profile/
 ProfileScreen.kt
 UserProfileScreen.kt
 admin/
 AdminReportsScreen.kt
 map/
 MapScreen.kt
 components/
 BottomNavigationBar.kt
 theme/
 Color.kt
 Theme.kt
 Type.kt
 viewmodel/
 AuthViewModel.kt
 CaptureViewModel.kt
 utils/
 PlateDetector.kt
 ValidationUtils.kt
 MainActivity.kt
 res/
 AndroidManifest.xml
 test/
 androidTest/
 build.gradle.kts
 proguard-rules.pro
```

### Organización por Capas

**Data Layer:**
- `api/` - Networking y modelos API
- `database/` - Room entities y DAOs
- `repository/` - Implementación de repositorios
- Modelos de datos

**UI Layer:**
- `ui/` - Screens Composables
- `theme/` - Theming
- `components/` - Componentes reutilizables

**ViewModel Layer:**
- `viewmodel/` - ViewModels (MVVM)

**Utils:**
- `utils/` - Utilidades y helpers

---

## Capa de Datos

### Data Sources

#### 1. Local Data Source (Room)

**Entities:**
```kotlin
@Entity(tableName = "captures")
data class CaptureEntity(
 @PrimaryKey(autoGenerate = true) val id: Long,
 val userEmail: String,
 val imageUri: String,
 val latitude: Double,
 val longitude: Double,
 val timestamp: Long,
 val extractedText: String,
 val detectedPlate: String,
 val isReported: Boolean = false
)
```

**DAOs:**
```kotlin
@Dao
interface CaptureDao {
 @Query("SELECT * FROM captures WHERE userEmail = :email")
 fun getCapturesByUser(email: String): Flow<List<CaptureEntity>>
 
 @Insert(onConflict = OnConflictStrategy.REPLACE)
 suspend fun insert(capture: CaptureEntity): Long
 
 @Delete
 suspend fun delete(capture: CaptureEntity)
}
```

#### 2. Remote Data Source (API)

**Service Interface:**
```kotlin
interface GodEyeApiService {
 @POST("auth/register")
 suspend fun register(@Body request: RegisterRequest): Response<AuthResponse>
 
 @POST("reports")
 suspend fun createReport(
 @Header("Authorization") auth: String,
 @Body request: CreateReportRequest
 ): Response<ReportResponse>
 
 @GET("reports/check/{placa}")
 suspend fun searchReportByPlate(
 @Header("Authorization") auth: String,
 @Path("placa") placa: String
 ): Response<CheckPlateResponse>
}
```

### Repository Pattern

**Interface:**
```kotlin
interface CaptureRepository {
 fun getAllCaptures(email: String): Flow<List<CaptureData>>
 suspend fun addCapture(capture: CaptureData): Result<Unit>
 suspend fun syncWithServer(token: String): Result<Unit>
}
```

**Implementation:**
```kotlin
class CaptureRepositoryImpl(
 private val captureDao: CaptureDao,
 private val apiService: GodEyeApiService
) : CaptureRepository {
 
 override fun getAllCaptures(email: String): Flow<List<CaptureData>> {
 return captureDao.getCapturesByUser(email).map { entities ->
 entities.map { it.toCaptureData() }
 }
 }
 
 override suspend fun addCapture(capture: CaptureData): Result<Unit> {
 return try {
 // Local first
 captureDao.insert(capture.toEntity())
 Result.success(Unit)
 } catch (e: Exception) {
 Result.failure(e)
 }
 }
}
```

---

## Capa de Dominio

### Use Cases (Business Logic)

Aunque actualmente la lógica está en ViewModels, la arquitectura ideal incluiría:

```kotlin
class DetectPlateUseCase(
 private val plateDetector: PlateDetector
) {
 suspend operator fun invoke(imageUri: Uri): Result<String?> {
 return try {
 val (text, plate) = plateDetector.processImage(imageUri)
 if (plate != null) {
 Result.success(plate)
 } else {
 Result.failure(Exception("No plate detected"))
 }
 } catch (e: Exception) {
 Result.failure(e)
 }
 }
}

class CheckPlateExistsUseCase(
 private val reportRepository: ReportRepository
) {
 suspend operator fun invoke(token: String, plate: String): Result<Boolean> {
 return when (val result = reportRepository.checkPlate(token, plate)) {
 is ApiResult.Success -> Result.success(result.data.exists)
 is ApiResult.Error -> Result.failure(Exception(result.message))
 }
 }
}
```

### Models (Domain Layer)

```kotlin
data class Plate(
 val number: String,
 val confidence: Float,
 val timestamp: Long
)

data class Report(
 val id: String,
 val plate: Plate,
 val location: Location,
 val reporter: User,
 val reason: String
)
```

---

## Capa de Presentación

### Jetpack Compose

**Declarative UI:**
```kotlin
@Composable
fun CameraScreen(
 viewModel: CaptureViewModel,
 authViewModel: AuthViewModel
) {
 val captures by viewModel.captures.collectAsState()
 val currentUser by authViewModel.currentUser.collectAsState()
 
 Scaffold(
 topBar = { TopBar(user = currentUser) },
 bottomBar = { BottomNavigationBar() }
 ) { padding ->
 CameraPreview(
 modifier = Modifier.padding(padding),
 onCapture = { image ->
 viewModel.processCapture(image)
 }
 )
 }
}
```

### State Management

**StateFlow:**
```kotlin
class CaptureViewModel : ViewModel() {
 private val _uiState = MutableStateFlow<UiState>(UiState.Loading)
 val uiState: StateFlow<UiState> = _uiState.asStateFlow()
 
 sealed class UiState {
 object Loading : UiState()
 data class Success(val data: List<Capture>) : UiState()
 data class Error(val message: String) : UiState()
 }
}
```

### Navigation

**NavHost:**
```kotlin
@Composable
fun GodEyeApp() {
 val navController = rememberNavController()
 
 NavHost(
 navController = navController,
 startDestination = "login"
 ) {
 composable("login") { LoginScreen(...) }
 composable("camera") { CameraScreen(...) }
 composable("history") { CaptureListScreen(...) }
 composable("profile") { ProfileScreen(...) }
 }
}
```

---

## Flujos de Datos

### Flujo de Captura

```
Usuario toma foto
 ↓
CameraScreen.onCapture()
 ↓
CaptureViewModel.processCapture(image)
 ↓
PlateDetector.processImage(image)
 ↓ (ML Kit)
Detecta placa
 ↓
CaptureViewModel.addCapture(captureData)
 ↓

 GUARDAR LOCAL ← (SIEMPRE primero)

 
 
¿Hay placa detectada?
 SÍ → Verificar si existe (GET /reports/check/{placa})
 Existe → NO hacer POST
 No existe → POST /reports
 NO → Solo guardar historial
 
 
 UI se actualiza
```

### Flujo de Autenticación

```
Usuario ingresa credenciales
 ↓
LoginScreen.onLogin()
 ↓
AuthViewModel.login(email, password)
 ↓
AuthRepository.login(email, password)
 ↓
ApiService.login(request)
 ↓ (HTTP)
Servidor responde con token
 ↓
AuthViewModel._accessToken.value = token
AuthViewModel._currentUser.value = user
 ↓
UserProfileRepository.getProfile(email)
 ↓
Cargar perfil local
 ↓
CaptureViewModel.setCurrentUser(email)
 ↓
Navegar a pantalla principal
```

### Flujo de Sincronización

```
Captura creada localmente
 ↓
¿Hay token de autenticación?
 NO → Solo local (syncedWithApi = false)
 SÍ → Intentar sincronizar
 ↓
 ¿Hay conexión a internet?
 NO → Solo local
 SÍ → POST a API
 ↓
 ¿Éxito?
 SÍ → Marcar syncedWithApi = true
 NO → Mantener syncedWithApi = false
```

---

## Decisiones de Diseño

### 1. Local-First Architecture

**Decisión:** Guardar siempre en BD local primero, luego sincronizar

**Razones:**
- Funciona offline
- No pierde datos
- Respuesta inmediata al usuario
- Sincronización asíncrona

**Implementación:**
```kotlin
// 1. Guardar local (síncrono)
captureDao.insert(capture)

// 2. Intentar API (asíncrono)
try {
 apiService.createReport(...)
 historyEntity.syncedWithApi = true
} catch (e: Exception) {
 // No importa, ya está guardado localmente
}
```

### 2. Single Activity + Compose Navigation

**Decisión:** Una Activity, múltiples Composables

**Razones:**
- Mejor para Compose
- Transiciones suaves
- Manejo de estado simplificado
- Menos overhead

### 3. Room para Persistencia

**Decisión:** Room en lugar de SQLite directo o DataStore

**Razones:**
- Type-safe
- Menos boilerplate
- Migraciones automáticas
- Flow/LiveData integration
- Queries verificadas en compile-time

### 4. Kotlin Coroutines + Flow

**Decisión:** Coroutines en lugar de RxJava o Callbacks

**Razones:**
- Código más limpio y legible
- Manejo de errores con try-catch
- Cancelación automática
- Integración con Jetpack
- Performance

### 5. Retrofit + OkHttp

**Decisión:** Retrofit en lugar de Ktor o Volley

**Razones:**
- Maduro y estable
- Gran ecosistema
- Fácil configuración
- Interceptores potentes
- Logging built-in

### 6. Manual Dependency Injection

**Decisión:** Constructor injection en lugar de Hilt (por ahora)

**Razones:**
- Simplicidad para MVP
- Menos dependencias
- Más control
- Fácil migración a Hilt después

### 7. Prevención de Duplicados en Cliente

**Decisión:** Verificar antes de POST en lugar de manejar en servidor

**Razones:**
- Reduce tráfico de red
- Feedback inmediato
- Menos carga en servidor
- UX más clara

### 8. Logs Exhaustivos

**Decisión:** android.util.Log en lugar de Timber o Logger

**Razones:**
- No requiere dependencias extra
- Nativo de Android
- Suficiente para debugging
- Fácil filtrado en LogCat

---

## Diagramas de Arquitectura

### Arquitectura de 3 Capas

```

 UI LAYER 
 
 Camera History Profile 
 Screen Screen Screen 
 

 

 VIEWMODEL LAYER 
 
 Capture Auth 
 ViewModel ViewModel 
 

 

 REPOSITORY LAYER 
 
 Capture Auth 
 Repository Repository 
 

 
 
 
 
 Room API Room API 
 DAO Service DAO Service
 
```

### Flujo de Datos Reactivo

```
Database (Room)
 ↓ Flow<List<Entity>>
Repository
 ↓ Flow<List<Data>>
ViewModel
 ↓ StateFlow<UiState>
Composable
 ↓ collectAsState()
UI Render
```

---

## Seguridad

### Autenticación

- **JWT Tokens** almacenados en memoria (ViewModel)
- **Bearer Authentication** en headers HTTP
- **HTTPS** obligatorio en producción

### Datos Sensibles

- **Contraseñas** nunca se guardan localmente
- **Tokens** se pierden al cerrar app (no persisten)
- **Ubicación GPS** con permisos runtime

### API Security

- **HTTPS** con certificado válido
- **Rate limiting** en servidor
- **Token expiration** manejado
- **Input validation** en cliente y servidor

---

## Escalabilidad

### Preparado para Crecer

1. **Modular** - Fácil agregar features
2. **Testeable** - Inyección de dependencias
3. **Cacheable** - Room + Repository pattern
4. **Offline-first** - Funciona sin internet
5. **Lazy loading** - Room paginado (futuro)

### Mejoras Futuras

- **Hilt** para DI
- **WorkManager** para sync en background
- **Paging 3** para listas grandes
- **Compose State Hoisting** mejorado
- **Multi-module** para features

---

## Nuevas Implementaciones (V5.0)

### 1. Sistema de Tracking Global con Google Maps

**Tecnologías:**
- **Google Maps Compose** - Integración nativa con Jetpack Compose
- **Maps SDK for Android** - Renderizado de mapas
- **Marker Clustering** (preparado para implementar)

**Arquitectura:**
```kotlin
// MapScreen composable
@Composable
fun MapScreen(
    capture: CaptureData,
    captureViewModel: CaptureViewModel
) {
    // Estado del mapa
    val cameraPositionState = rememberCameraPositionState()
    var allLocations by remember { mutableStateOf<List<CaptureData>>(emptyList()) }
    
    // Cargar ubicaciones de TODOS los usuarios
    LaunchedEffect(capture.detectedPlate) {
        allLocations = captureViewModel.getAllCapturesByPlateAllUsers(plate)
    }
    
    // Renderizar mapa con marcadores
    GoogleMap(cameraPositionState = cameraPositionState) {
        // Marcador rojo (ubicación actual)
        Marker(
            position = currentPosition,
            icon = BitmapDescriptorFactory.defaultMarker(HUE_RED)
        )
        
        // Marcadores azules (ubicaciones anteriores)
        allLocations.forEach { location ->
            Marker(
                position = LatLng(location.latitude, location.longitude),
                icon = BitmapDescriptorFactory.defaultMarker(HUE_AZURE),
                snippet = "Fecha: ${formatDate(location.timestamp)}\nUsuario: ${location.userEmail}"
            )
        }
    }
}
```

**Query sin filtro de usuario:**
```kotlin
@Query("SELECT * FROM captures WHERE detectedPlate LIKE '%' || :plate || '%' ORDER BY timestamp DESC")
fun getCapturesByPlate(plate: String): Flow<List<CaptureEntity>>
```

**Ventajas:**
- Visualización completa del movimiento de una placa
- Información interactiva en cada marcador
- Útil para análisis de patrones de movimiento
- Colaborativo entre usuarios

### 2. Sistema de Alertas con Vibración y Notificaciones

**Tecnologías:**
- **Vibrator/VibratorManager** - Feedback háptico
- **NotificationManager** - Push notifications
- **NotificationChannel** - Android O+ notifications

**Componentes:**
```kotlin
// Vibración personalizada
val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
val pattern = longArrayOf(0, 500, 200, 500) // Vibrar-Pausa-Vibrar
vibrator.vibrate(VibrationEffect.createWaveform(pattern, -1))

// Notificación con prioridad alta
val notification = NotificationCompat.Builder(context, CHANNEL_ID)
    .setSmallIcon(R.drawable.ic_warning)
    .setContentTitle("ALERTA! Placa Reportada")
    .setContentText("Placa $placa detectada - $cantidad reporte(s)")
    .setPriority(NotificationCompat.PRIORITY_HIGH)
    .setCategory(NotificationCompat.CATEGORY_ALARM)
    .build()

notificationManager.notify(NOTIFICATION_ID, notification)

// AlertDialog en UI
AlertDialog(
    title = { Text("ALERTA! Placa Reportada") },
    text = { Text("La placa $placa ha sido reportada $cantidad veces") },
    confirmButton = { Button(onClick = { ... }) { Text("Entendido") } }
)
```

**Flujo de alerta:**
```
Detección de placa
    ↓
Verificación en API: GET /reports/check/{placa}
    ↓
¿Está reportada?
    ↓ SÍ
Activar sistema de alertas:
    1. Vibración (500ms-200ms-500ms)
    2. Notificación push (alta prioridad)
    3. AlertDialog en pantalla
    4. Marcador visual (isReported = true)
    ↓
Guardar en BD con flag isReported = true
```

### 3. Control de Ciclo de Vida con Kotlin Jobs

**Problema resuelto:** Flow de Room seguía activo después de cambiar de usuario

**Solución:**
```kotlin
class CaptureViewModel : ViewModel() {
    // Job para controlar el Flow
    private var capturesJob: Job? = null
    
    fun setCurrentUser(email: String) {
        // 1. Cancelar Job anterior (detiene Flow)
        capturesJob?.cancel()
        
        // 2. Limpiar estado
        _captures.clear()
        
        // 3. Iniciar nuevo Job
        capturesJob = viewModelScope.launch {
            captureDao.getCapturesByUser(email).collect { entities ->
                _captures.clear()
                _captures.addAll(entities.map { it.toCaptureData() })
            }
        }
    }
    
    fun clearUserData() {
        capturesJob?.cancel()
        capturesJob = null
        _captures.clear()
    }
}
```

**Ventajas:**
- Control explícito del ciclo de vida de Flows
- Previene memory leaks
- Cancela operaciones innecesarias
- Mejora el rendimiento

### 4. Material Design 3 con Color Semántico

**Sistema de colores:**
```kotlin
// Colores semánticos para estados
MaterialTheme.colorScheme.primaryContainer    // Placa normal (azul)
MaterialTheme.colorScheme.errorContainer      // Placa reportada (rojo)
MaterialTheme.colorScheme.secondaryContainer  // Información (gris/verde)

// Implementación en Card
Card(
    colors = CardDefaults.cardColors(
        containerColor = when {
            capture.isReported -> MaterialTheme.colorScheme.errorContainer
            else -> MaterialTheme.colorScheme.primaryContainer
        }
    )
) {
    Text(
        text = if (capture.isReported) "PLACA REPORTADA" else "PLACA DETECTADA",
        color = when {
            capture.isReported -> MaterialTheme.colorScheme.onErrorContainer
            else -> MaterialTheme.colorScheme.onPrimaryContainer
        }
    )
}
```

**Ventajas:**
- Colores adaptativos (light/dark mode)
- Semántica visual clara
- Accesibilidad mejorada
- Consistencia con Material Design

### 5. Debugging con Structured Logging

**Sistema de logs estructurados:**
```kotlin
// Logging en capas
android.util.Log.d("CaptureViewModel", "setCurrentUser llamado para: $email")
android.util.Log.d("CaptureViewModel", "Estado limpiado. Captures size: ${_captures.size}")
android.util.Log.d("CaptureViewModel", "Flow emitió ${entities.size} capturas para: $email")

// Debug de estado de BD
android.util.Log.d("CaptureListScreen", "=== DEBUG: Capturas en BD por usuario ===")
capturesByUser.forEach { (user, count) ->
    android.util.Log.d("CaptureListScreen", "Usuario: $user -> $count capturas")
}
android.util.Log.d("CaptureListScreen", "========================================")
```

**Método de debugging:**
```kotlin
suspend fun debugCaptureCount(): Map<String, Int> {
    val allCaptures = captureDao.getAllCapturesOnce()
    return allCaptures.groupBy { it.userEmail }
        .mapValues { it.value.size }
}
```

**Ventajas:**
- Facilita el debugging
- Trazabilidad completa
- Identificación rápida de problemas
- Útil para auditoría

### 6. LaunchedEffect para Operaciones Asíncronas

**Uso en UI:**
```kotlin
@Composable
fun LoginScreen(authViewModel: AuthViewModel) {
    var loginSuccessful by remember { mutableStateOf(false) }
    var userName by remember { mutableStateOf("") }
    
    // Esperar a que se cargue el perfil
    LaunchedEffect(authViewModel.currentUser.value?.name, loginSuccessful) {
        if (loginSuccessful && authViewModel.currentUser.value != null) {
            delay(200)  // Espera a loadUserProfile()
            userName = authViewModel.currentUser.value?.name ?: "Usuario"
            showWelcomeDialog = true
            loginSuccessful = false
        }
    }
}
```

**Ventajas:**
- Sincronización con ciclo de vida de Composable
- Cancelación automática al salir de composición
- Manejo de operaciones asíncronas en UI
- Evita race conditions

### 7. Room Database Queries Optimizadas

**Queries específicas para diferentes casos de uso:**
```kotlin
@Dao
interface CaptureDao {
    // Solo del usuario actual
    @Query("SELECT * FROM captures WHERE userEmail = :email ORDER BY timestamp DESC")
    fun getCapturesByUser(email: String): Flow<List<CaptureEntity>>
    
    // De todos los usuarios (tracking global)
    @Query("SELECT * FROM captures WHERE detectedPlate LIKE '%' || :plate || '%' ORDER BY timestamp DESC")
    fun getCapturesByPlate(plate: String): Flow<List<CaptureEntity>>
    
    // Combinado: placa + usuario
    @Query("SELECT * FROM captures WHERE detectedPlate LIKE '%' || :plate || '%' AND userEmail = :email ORDER BY timestamp DESC")
    fun getCapturesByPlateAndUser(plate: String, email: String): Flow<List<CaptureEntity>>
    
    // Una sola vez (no Flow)
    @Query("SELECT * FROM captures WHERE userEmail = :email ORDER BY timestamp DESC")
    suspend fun getCapturesByUserOnce(email: String): List<CaptureEntity>
}
```

**Ventajas:**
- Queries específicas para cada necesidad
- Mejor rendimiento
- Menos datos en memoria
- Flexibilidad en filtrado

### 8. Permisos Runtime Mejorados

**Sistema de permisos para alertas:**
```kotlin
// Permiso de notificaciones (Android 13+)
if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
    if (ContextCompat.checkSelfPermission(
            context,
            android.Manifest.permission.POST_NOTIFICATIONS
        ) != PackageManager.PERMISSION_GRANTED
    ) {
        ActivityCompat.requestPermissions(
            activity,
            arrayOf(android.Manifest.permission.POST_NOTIFICATIONS),
            REQUEST_CODE
        )
    }
}

// Permiso de vibración
<uses-permission android:name="android.permission.VIBRATE" />
```

**Manejo en UI:**
```kotlin
@Composable
fun RequestNotificationPermission() {
    val permissionState = rememberPermissionState(
        android.Manifest.permission.POST_NOTIFICATIONS
    )
    
    LaunchedEffect(Unit) {
        if (!permissionState.status.isGranted) {
            permissionState.launchPermissionRequest()
        }
    }
}
```

---

## Conclusión

GodEye está construido con **arquitectura moderna de Android**, siguiendo las **mejores prácticas** recomendadas por Google:

✅ **Clean Architecture** con separación de capas  
✅ **MVVM** para separación UI/lógica  
✅ **Jetpack Compose** para UI declarativa  
✅ **Room** para persistencia  
✅ **Retrofit** para networking  
✅ **Coroutines + Flow** para async  
✅ **Repository Pattern** para abstracción  
✅ **Local-First** para confiabilidad  

**Stack Tecnológico Robusto:**
- Kotlin como lenguaje moderno y conciso
- Jetpack libraries oficiales de Android
- Material Design 3 para UI consistente
- ML Kit para detección on-device
- RESTful API para backend
- Google Maps para visualización geográfica
- Sistema de notificaciones y alertas
- Control avanzado de ciclo de vida con Jobs

**Nuevas Características (V5.0):**
- 🗺️ Tracking global de placas con Google Maps
- 🔔 Sistema de alertas multicanal (vibración, notificación, UI)
- 🎨 UI semántica con Material Design 3
- 🔒 Aislamiento total de datos por usuario
- 🐛 Sistema de debugging estructurado
- ⚡ Control de ciclo de vida optimizado
- 📊 Queries especializadas en Room

**Listo para Producción, Escalable y Mantenible.**

**Documentación actualizada:** Versión 5.0 - Diciembre 2025

---

**Última actualización:** 2025-12-07 
**Versión:** 4.0 
**Estado:** Producción Ready

