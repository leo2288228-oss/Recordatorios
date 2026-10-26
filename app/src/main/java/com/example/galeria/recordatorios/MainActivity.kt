package com.example.galeria.recordatorios

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp



// Sealed Class para Prioridades (restringe a un conjunto cerrado de opciones)
sealed class Prioridad(val nombre: String, val nivel: Int) {
    object Alta : Prioridad("Alta", 3)
    object Media : Prioridad("Media", 2)
    object Baja : Prioridad("Baja", 1)
}

// Sealed Class para el Estado del Recordatorio
sealed class EstadoRecordatorio(val descripcion: String) {
    object Pendiente : EstadoRecordatorio("Pendiente")
    object EnProgreso : EstadoRecordatorio("En progreso")
    object Completado : EstadoRecordatorio("Completado")
}

// Sealed Class para el Estado General de la Aplicación (UI State)
sealed class EstadoPantalla {
    object Cargando : EstadoPantalla()
    data class Exito(val recordatorios: List<Recordatorio>) : EstadoPantalla()
    data class Error(val mensaje: String) : EstadoPantalla()
}

// Data Class inmutable para la Categoría
data class Categoria(
    val id: String,
    val nombre: String
)

// Data Class inmutable para el Recordatorio (incluye datos opcionales con '?')
data class Recordatorio(
    val id: Int,
    val titulo: String,
    val categoria: Categoria,
    val prioridad: Prioridad,
    val estado: EstadoRecordatorio,
    val ubicacionOpcional: String? = null, // Dato opcional (puede ser null)
    val avisoHoraOpcional: String? = null  // Dato opcional (puede ser null)
) {
    // Regla de validación en el bloque init para evitar estados inválidos
    init {
        require(id > 0) { "El ID del recordatorio debe ser mayor a 0." }
        require(titulo.isNotBlank()) { "El título del recordatorio no puede estar vacío." }
        require(categoria.nombre.isNotBlank()) { "La categoría debe tener un nombre válido." }
    }
}


object RepositorioSimulado {

    // Definición de categorías base
    private val catTrabajo = Categoria("CAT_01", "Trabajo")
    private val catSalud = Categoria("CAT_02", "Salud")
    private val catHogar = Categoria("CAT_03", "Hogar")
    private val catEstudio = Categoria("CAT_04", "Estudio")

    //SET
    val categoriasUnicas: Set<Categoria> = setOf(
        catTrabajo,
        catSalud,
        catHogar,
        catEstudio,
        catTrabajo // Duplicado intencional: el Set lo ignora y conserva solo 4
    )

    //MAP
    val metadatosCategoria: Map<Categoria, Pair<String, Color>> = mapOf(
        catTrabajo to Pair("Laboral / Oficina", Color(0xFF1E88E5)), // Azul
        catSalud to Pair("Bienestar personal", Color(0xFF43A047)),  // Verde
        catHogar to Pair("Compras y recados", Color(0xFFFB8C00)),   // Naranja
        catEstudio to Pair("Universidad / Cursos", Color(0xFF8E24AA)) // Violeta
    )

    //LIST
    val listaRecordatorios: List<Recordatorio> = listOf(
        Recordatorio(
            id = 1,
            titulo = "Entregar informe de avance mensual",
            categoria = catTrabajo,
            prioridad = Prioridad.Alta,
            estado = EstadoRecordatorio.Pendiente,
            ubicacionOpcional = "Oficina Central - Piso 2",
            avisoHoraOpcional = "09:00 hs"
        ),
        Recordatorio(
            id = 2,
            titulo = "Turno odontológico de control",
            categoria = catSalud,
            prioridad = Prioridad.Media,
            estado = EstadoRecordatorio.Pendiente,
            ubicacionOpcional = "Clínica del Sur",
            avisoHoraOpcional = "16:30 hs"
        ),
        Recordatorio(
            id = 3,
            titulo = "Comprar repuestos de luminaria",
            categoria = catHogar,
            prioridad = Prioridad.Baja,
            estado = EstadoRecordatorio.Completado,
            ubicacionOpcional = null, // Sin ubicación
            avisoHoraOpcional = null  // Sin aviso
        ),
        Recordatorio(
            id = 4,
            titulo = "Subir Actividad 1 de Programación Mobile",
            categoria = catEstudio,
            prioridad = Prioridad.Alta,
            estado = EstadoRecordatorio.EnProgreso,
            ubicacionOpcional = "Campus Virtual",
            avisoHoraOpcional = "23:59 hs"
        ),
        Recordatorio(
            id = 5,
            titulo = "Sincronizar servidor de respaldo local",
            categoria = catTrabajo,
            prioridad = Prioridad.Alta,
            estado = EstadoRecordatorio.Pendiente,
            ubicacionOpcional = null,
            avisoHoraOpcional = "18:00 hs"
        )
    )

    // Funciones de filtrado sobre la List
    fun filtrarPorPrioridad(prioridad: Prioridad): List<Recordatorio> {
        return listaRecordatorios.filter { it.prioridad == prioridad }
    }

    fun filtrarPorEstado(estado: EstadoRecordatorio): List<Recordatorio> {
        return listaRecordatorios.filter { it.estado == estado }
    }
}

// INTERFAZ CON JETPACK COMPOSE Y MATERIAL 3

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    PantallaInicialRecordatorios()
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantallaInicialRecordatorios() {
    // Estado para controlar el filtro seleccionado en la pantalla
    var filtroActual by remember { mutableStateOf("Todos") }

    // Aplicamos el filtrado según el botón seleccionado
    val recordatoriosMostrados = remember(filtroActual) {
        when (filtroActual) {
            "Prioridad Alta" -> RepositorioSimulado.filtrarPorPrioridad(Prioridad.Alta)
            "Pendientes" -> RepositorioSimulado.filtrarPorEstado(EstadoRecordatorio.Pendiente)
            "En Progreso" -> RepositorioSimulado.filtrarPorEstado(EstadoRecordatorio.EnProgreso)
            "Completados" -> RepositorioSimulado.filtrarPorEstado(EstadoRecordatorio.Completado)
            else -> RepositorioSimulado.listaRecordatorios
        }
    }

    // Modelamos el estado de la pantalla mediante nuestra Sealed Class
    val estadoPantalla: EstadoPantalla = if (recordatoriosMostrados.isEmpty()) {
        EstadoPantalla.Error("No hay recordatorios para el filtro seleccionado.")
    } else {
        EstadoPantalla.Exito(recordatoriosMostrados)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Recordatorios Contextuales",
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Categorías activas (Set): ${RepositorioSimulado.categoriasUnicas.size}",
                            style = MaterialTheme.typography.labelMedium
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            // Barra de filtros interactiva (demuestra el uso de filter en la List)
            Text(
                text = "Filtrar por estado o prioridad:",
                style = MaterialTheme.typography.labelLarge,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            val opcionesFiltro = listOf("Todos", "Prioridad Alta", "Pendientes", "En Progreso", "Completados")
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.padding(bottom = 16.dp)
            ) {
                items(opcionesFiltro) { opcion ->
                    FilterChip(
                        selected = (filtroActual == opcion),
                        onClick = { filtroActual = opcion },
                        label = { Text(opcion) }
                    )
                }
            }

            // Renderizado según el EstadoPantalla (Sealed Class)
            when (estadoPantalla) {
                is EstadoPantalla.Cargando -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
                }
                is EstadoPantalla.Error -> {
                    Text(
                        text = estadoPantalla.mensaje,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
                is EstadoPantalla.Exito -> {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        items(
                            items = estadoPantalla.recordatorios,
                            key = { it.id }
                        ) { recordatorio ->
                            TarjetaRecordatorio(recordatorio = recordatorio)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun TarjetaRecordatorio(recordatorio: Recordatorio) {
    // etiqueta y el color asociado a la categoría desde el MAP
    val infoCategoria = RepositorioSimulado.metadatosCategoria[recordatorio.categoria]
    val etiquetaCategoria = infoCategoria?.first ?: "General"
    val colorCategoria = infoCategoria?.second ?: Color.Gray

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Fila superior: Categoría
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    color = colorCategoria.copy(alpha = 0.15f),
                    shape = MaterialTheme.shapes.small
                ) {
                    Text(
                        text = "${recordatorio.categoria.nombre} • $etiquetaCategoria",
                        color = colorCategoria,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }

                Text(
                    text = recordatorio.estado.descripcion,
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Título del recordatorio
            Text(
                text = recordatorio.titulo,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(6.dp))

            // Prioridad
            val colorPrioridad = when (recordatorio.prioridad) {
                is Prioridad.Alta -> Color(0xFFD32F2F)
                is Prioridad.Media -> Color(0xFFF57C00)
                is Prioridad.Baja -> Color(0xFF388E3C)
            }
            Text(
                text = "Prioridad: ${recordatorio.prioridad.nombre}",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                color = colorPrioridad
            )

            // Datos opcionales (solo se muestran si no son null)
            if (recordatorio.ubicacionOpcional != null || recordatorio.avisoHoraOpcional != null) {
                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    recordatorio.ubicacionOpcional?.let { ubicacion ->
                        Text(
                            text = "📍 $ubicacion",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                    recordatorio.avisoHoraOpcional?.let { hora ->
                        Text(
                            text = "⏰ $hora",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }
        }
    }
}


@androidx.compose.ui.tooling.preview.Preview(showBackground = true, showSystemUi = true)
@Composable
fun VistaPreviaRecordatorios() {
    MaterialTheme {
        PantallaInicialRecordatorios()
    }
}