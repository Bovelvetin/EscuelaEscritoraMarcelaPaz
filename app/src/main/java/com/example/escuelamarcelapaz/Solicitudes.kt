package com.example.escuelamarcelapaz

import android.graphics.BitmapFactory
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

// ---------------------------------------------------------------
// ESTADOS POSIBLES DE UN TICKET (gestionados por Interno)
// ---------------------------------------------------------------
enum class EstadoSolicitud(val etiqueta: String) {
    ABIERTA("Abierta"),
    EN_REVISION("En revisión"),
    RESUELTA("Resuelta")
}

// ---------------------------------------------------------------
// PRIORIDADES DE UN TICKET (clasificadas por Administrador)
// El orden define la urgencia para ordenar la bandeja del Interno.
// ---------------------------------------------------------------
enum class PrioridadSolicitud(val etiqueta: String, val nivelOrden: Int) {
    CRITICA("Crítica", 1),
    ALTA("Alta", 2),
    MEDIA("Media", 3),
    BAJA("Baja", 4),
    SIN_ASIGNAR("Sin asignar", 5)
}

// ---------------------------------------------------------------
// MODELO DE DATOS: un ticket
// ---------------------------------------------------------------
data class Solicitud(
    val numero: Int,
    val asunto: String,
    val fotoUri: Uri?,
    val solicitante: String,
    val fecha: String,
    val estado: EstadoSolicitud = EstadoSolicitud.ABIERTA,
    val prioridad: PrioridadSolicitud = PrioridadSolicitud.SIN_ASIGNAR,
    val respuesta: String = ""
)

// ---------------------------------------------------------------
// VIEWMODEL
// ---------------------------------------------------------------
class SolicitudesViewModel : ViewModel() {
    val solicitudes = mutableStateListOf<Solicitud>()

    fun crear(asunto: String, foto: Uri?, solicitante: String) {
        val numero = (solicitudes.maxOfOrNull { it.numero } ?: 0) + 1
        val fecha = SimpleDateFormat("dd-MM-yyyy HH:mm", Locale("es", "CL")).format(Date())
        solicitudes.add(0, Solicitud(numero, asunto, foto, solicitante, fecha))
    }

    // Usado por el Interno para cambiar estado y respuesta
    fun actualizar(numero: Int, estado: EstadoSolicitud, respuesta: String) {
        val indice = solicitudes.indexOfFirst { it.numero == numero }
        if (indice >= 0) {
            solicitudes[indice] = solicitudes[indice].copy(estado = estado, respuesta = respuesta)
        }
    }

    // Usado por el Administrador para clasificar la prioridad
    fun actualizarPrioridad(numero: Int, prioridad: PrioridadSolicitud) {
        val indice = solicitudes.indexOfFirst { it.numero == numero }
        if (indice >= 0) {
            solicitudes[indice] = solicitudes[indice].copy(prioridad = prioridad)
        }
    }

    fun eliminar(numero: Int) {
        solicitudes.removeAll { it.numero == numero }
    }
}

// ---------------------------------------------------------------
// PANTALLA PRINCIPAL DEL MÓDULO
// ---------------------------------------------------------------
@Composable
fun PantallaSolicitudes(correo: String, rol: Rol, viewModel: SolicitudesViewModel) {
    var creando by rememberSaveable { mutableStateOf(false) }
    var seleccionada by rememberSaveable { mutableStateOf<Int?>(null) }

    // Lista filtrada y ordenada según el rol
    val visibles = when (rol) {
        Rol.DOCENTE -> viewModel.solicitudes.filter { it.solicitante == correo }
        // El Interno ve todos ordenados por prioridad (Crítica -> Alta -> Media -> Baja -> Sin asignar)
        Rol.INTERNO -> viewModel.solicitudes.sortedBy { it.prioridad.nivelOrden }
        // El Administrador ve todos poniendo primero los que faltan por clasificar
        Rol.ADMINISTRADOR -> viewModel.solicitudes.sortedBy {
            if (it.prioridad == PrioridadSolicitud.SIN_ASIGNAR) 0 else 1
        }
    }

    val ticket = visibles.firstOrNull { it.numero == seleccionada }

    when {
        creando && rol == Rol.DOCENTE -> FormularioSolicitud(
            onCancelar = { creando = false },
            onEnviar = { asunto, foto ->
                viewModel.crear(asunto, foto, correo)
                creando = false
            }
        )
        ticket != null && rol == Rol.INTERNO -> GestionSolicitud(
            solicitud = ticket,
            onCancelar = { seleccionada = null },
            onGuardar = { estado, respuesta ->
                viewModel.actualizar(ticket.numero, estado, respuesta)
                seleccionada = null
            }
        )
        ticket != null && rol == Rol.ADMINISTRADOR -> ClasificarPrioridadSolicitud(
            solicitud = ticket,
            onCancelar = { seleccionada = null },
            onGuardar = { nuevaPrioridad ->
                viewModel.actualizarPrioridad(ticket.numero, nuevaPrioridad)
                seleccionada = null
            }
        )
        else -> BandejaSolicitudes(
            solicitudes = visibles,
            rol = rol,
            onNueva = { creando = true },
            onAccionPrincipal = { seleccionada = it.numero },
            onEliminar = { viewModel.eliminar(it.numero) }
        )
    }
}

// ---------------------------------------------------------------
// BANDEJA: lista de tickets
// ---------------------------------------------------------------
@Composable
private fun BandejaSolicitudes(
    solicitudes: List<Solicitud>,
    rol: Rol,
    onNueva: () -> Unit,
    onAccionPrincipal: (Solicitud) -> Unit,
    onEliminar: (Solicitud) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        if (rol == Rol.DOCENTE) {
            Button(
                onClick = onNueva,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            ) {
                Text("Nueva solicitud")
            }
            Spacer(Modifier.height(16.dp))
        }

        if (solicitudes.isEmpty()) {
            Text(
                when (rol) {
                    Rol.DOCENTE -> "Aún no has creado solicitudes."
                    Rol.INTERNO -> "No hay solicitudes pendientes."
                    Rol.ADMINISTRADOR -> "No hay tickets registrados para clasificar."
                }
            )
        } else {
            val encabezado = when (rol) {
                Rol.DOCENTE -> "Mis solicitudes (${solicitudes.size})"
                Rol.INTERNO -> {
                    val pendientes = solicitudes.count { it.estado != EstadoSolicitud.RESUELTA }
                    "Solicitudes: ${solicitudes.size} (pendientes: $pendientes)"
                }
                Rol.ADMINISTRADOR -> {
                    val sinClasificar = solicitudes.count { it.prioridad == PrioridadSolicitud.SIN_ASIGNAR }
                    "Total tickets: ${solicitudes.size} (sin clasificar: $sinClasificar)"
                }
            }
            Text(encabezado, style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(8.dp))

            LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                items(solicitudes, key = { it.numero }) { solicitud ->
                    TarjetaSolicitud(
                        solicitud = solicitud,
                        textoAccionPrincipal = when (rol) {
                            Rol.INTERNO -> "Gestionar"
                            Rol.ADMINISTRADOR -> "Clasificar prioridad"
                            Rol.DOCENTE -> null
                        },
                        onAccionPrincipal = if (rol != Rol.DOCENTE) {
                            { onAccionPrincipal(solicitud) }
                        } else null,
                        onEliminar = if (rol == Rol.DOCENTE && solicitud.estado == EstadoSolicitud.ABIERTA) {
                            { onEliminar(solicitud) }
                        } else null
                    )
                }
            }
        }
    }
}

// ---------------------------------------------------------------
// TARJETA DE TICKET
// ---------------------------------------------------------------
@Composable
private fun TarjetaSolicitud(
    solicitud: Solicitud,
    textoAccionPrincipal: String?,
    onAccionPrincipal: (() -> Unit)?,
    onEliminar: (() -> Unit)?
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Ticket #${solicitud.numero}", style = MaterialTheme.typography.titleSmall)
                Text(
                    solicitud.estado.etiqueta,
                    color = colorEstado(solicitud.estado),
                    style = MaterialTheme.typography.labelLarge
                )
            }
            Spacer(Modifier.height(4.dp))
            Text(
                text = "Prioridad: ${solicitud.prioridad.etiqueta}",
                color = colorPrioridad(solicitud.prioridad),
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(4.dp))
            Text(solicitud.asunto, style = MaterialTheme.typography.bodyLarge)
            Spacer(Modifier.height(4.dp))
            Text(
                "${solicitud.fecha} · ${solicitud.solicitante}",
                style = MaterialTheme.typography.bodySmall
            )

            solicitud.fotoUri?.let { uri ->
                Spacer(Modifier.height(8.dp))
                ImagenDesdeUri(
                    uri = uri,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(160.dp)
                        .clip(RoundedCornerShape(8.dp))
                )
            }

            if (solicitud.respuesta.isNotBlank()) {
                Spacer(Modifier.height(8.dp))
                Text("Respuesta del equipo:", style = MaterialTheme.typography.labelLarge)
                Text(solicitud.respuesta, style = MaterialTheme.typography.bodyMedium)
            }

            Row {
                if (onAccionPrincipal != null && textoAccionPrincipal != null) {
                    TextButton(onClick = onAccionPrincipal) { Text(textoAccionPrincipal) }
                }
                onEliminar?.let { TextButton(onClick = it) { Text("Eliminar") } }
            }
        }
    }
}

@Composable
private fun colorEstado(estado: EstadoSolicitud): Color = when (estado) {
    EstadoSolicitud.ABIERTA -> MaterialTheme.colorScheme.error
    EstadoSolicitud.EN_REVISION -> MaterialTheme.colorScheme.primary
    EstadoSolicitud.RESUELTA -> MaterialTheme.colorScheme.tertiary
}

@Composable
private fun colorPrioridad(prioridad: PrioridadSolicitud): Color = when (prioridad) {
    PrioridadSolicitud.CRITICA -> MaterialTheme.colorScheme.error
    PrioridadSolicitud.ALTA -> Color(0xFFD84315)
    PrioridadSolicitud.MEDIA -> MaterialTheme.colorScheme.primary
    PrioridadSolicitud.BAJA -> MaterialTheme.colorScheme.tertiary
    PrioridadSolicitud.SIN_ASIGNAR -> MaterialTheme.colorScheme.outline
}

// ---------------------------------------------------------------
// PANTALLA DE CLASIFICACIÓN (solo Administrador)
// ---------------------------------------------------------------
@Composable
private fun ClasificarPrioridadSolicitud(
    solicitud: Solicitud,
    onCancelar: () -> Unit,
    onGuardar: (PrioridadSolicitud) -> Unit
) {
    var prioridadSeleccionada by rememberSaveable { mutableStateOf(solicitud.prioridad) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        Text("Clasificar Ticket #${solicitud.numero}", style = MaterialTheme.typography.headlineSmall)
        Spacer(Modifier.height(8.dp))

        Text(solicitud.asunto, style = MaterialTheme.typography.bodyLarge)
        Spacer(Modifier.height(4.dp))
        Text(
            "${solicitud.fecha} · ${solicitud.solicitante}",
            style = MaterialTheme.typography.bodySmall
        )
        Spacer(Modifier.height(4.dp))
        Text(
            "Estado actual: ${solicitud.estado.etiqueta}",
            style = MaterialTheme.typography.labelLarge,
            color = colorEstado(solicitud.estado)
        )

        solicitud.fotoUri?.let { uri ->
            Spacer(Modifier.height(8.dp))
            ImagenDesdeUri(
                uri = uri,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(220.dp)
                    .clip(RoundedCornerShape(8.dp))
            )
        }

        Spacer(Modifier.height(16.dp))
        Text("Selecciona el nivel de prioridad:", style = MaterialTheme.typography.titleMedium)
        Spacer(Modifier.height(4.dp))

        PrioridadSolicitud.values().forEach { opcion ->
            Row(verticalAlignment = Alignment.CenterVertically) {
                RadioButton(
                    selected = prioridadSeleccionada == opcion,
                    onClick = { prioridadSeleccionada = opcion }
                )
                Text(
                    text = opcion.etiqueta,
                    color = colorPrioridad(opcion),
                    fontWeight = if (prioridadSeleccionada == opcion) FontWeight.Bold else FontWeight.Normal
                )
            }
        }

        Spacer(Modifier.height(24.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            OutlinedButton(onClick = onCancelar, modifier = Modifier.weight(1f)) {
                Text("Cancelar")
            }
            Button(
                onClick = { onGuardar(prioridadSeleccionada) },
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            ) {
                Text("Guardar prioridad")
            }
        }
    }
}

// ---------------------------------------------------------------
// GESTIÓN (solo Interno)
// ---------------------------------------------------------------
@Composable
private fun GestionSolicitud(
    solicitud: Solicitud,
    onCancelar: () -> Unit,
    onGuardar: (EstadoSolicitud, String) -> Unit
) {
    var estado by rememberSaveable { mutableStateOf(solicitud.estado) }
    var respuesta by rememberSaveable { mutableStateOf(solicitud.respuesta) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        Text("Ticket #${solicitud.numero}", style = MaterialTheme.typography.headlineSmall)
        Spacer(Modifier.height(4.dp))
        Text(
            "Prioridad asignada: ${solicitud.prioridad.etiqueta}",
            color = colorPrioridad(solicitud.prioridad),
            style = MaterialTheme.typography.titleSmall
        )
        Spacer(Modifier.height(8.dp))

        Text(solicitud.asunto, style = MaterialTheme.typography.bodyLarge)
        Spacer(Modifier.height(4.dp))
        Text(
            "${solicitud.fecha} · ${solicitud.solicitante}",
            style = MaterialTheme.typography.bodySmall
        )
        solicitud.fotoUri?.let { uri ->
            Spacer(Modifier.height(8.dp))
            ImagenDesdeUri(
                uri = uri,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(220.dp)
                    .clip(RoundedCornerShape(8.dp))
            )
        }

        Spacer(Modifier.height(16.dp))
        Text("Estado", style = MaterialTheme.typography.titleMedium)
        EstadoSolicitud.values().forEach { opcion ->
            Row(verticalAlignment = Alignment.CenterVertically) {
                RadioButton(selected = estado == opcion, onClick = { estado = opcion })
                Text(opcion.etiqueta)
            }
        }

        Spacer(Modifier.height(16.dp))
        OutlinedTextField(
            value = respuesta,
            onValueChange = { respuesta = it },
            label = { Text("Respuesta o comentario") },
            minLines = 3,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.height(24.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            OutlinedButton(onClick = onCancelar, modifier = Modifier.weight(1f)) {
                Text("Cancelar")
            }
            Button(
                onClick = { onGuardar(estado, respuesta.trim()) },
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            ) {
                Text("Guardar")
            }
        }
    }
}

// ---------------------------------------------------------------
// FORMULARIO (solo Docente)
// ---------------------------------------------------------------
@Composable
private fun FormularioSolicitud(
    onCancelar: () -> Unit,
    onEnviar: (asunto: String, foto: Uri?) -> Unit
) {
    var asunto by rememberSaveable { mutableStateOf("") }
    var foto by rememberSaveable { mutableStateOf<Uri?>(null) }
    var error by rememberSaveable { mutableStateOf<String?>(null) }

    val selectorFoto = rememberLauncherForActivityResult(
        ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) foto = uri
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        Text("Nueva solicitud", style = MaterialTheme.typography.headlineSmall)
        Spacer(Modifier.height(16.dp))

        OutlinedTextField(
            value = asunto,
            onValueChange = {
                if (it.length <= 100) asunto = it
                error = null
            },
            label = { Text("Asunto") },
            supportingText = { Text("${asunto.length}/100") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(16.dp))

        Text("Foto (opcional)", style = MaterialTheme.typography.titleMedium)
        Spacer(Modifier.height(8.dp))
        OutlinedButton(
            onClick = {
                selectorFoto.launch(
                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                )
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(if (foto == null) "Adjuntar foto" else "Cambiar foto")
        }

        foto?.let { uri ->
            Spacer(Modifier.height(8.dp))
            ImagenDesdeUri(
                uri = uri,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .clip(RoundedCornerShape(8.dp))
            )
            TextButton(onClick = { foto = null }) { Text("Quitar foto") }
        }

        error?.let {
            Spacer(Modifier.height(8.dp))
            Text(it, color = MaterialTheme.colorScheme.error)
        }

        Spacer(Modifier.height(24.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            OutlinedButton(onClick = onCancelar, modifier = Modifier.weight(1f)) {
                Text("Cancelar")
            }
            Button(
                onClick = {
                    if (asunto.isBlank()) {
                        error = "Escribe el asunto de la solicitud"
                    } else {
                        onEnviar(asunto.trim(), foto)
                    }
                },
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            ) {
                Text("Enviar")
            }
        }
    }
}

// ---------------------------------------------------------------
// IMAGEN DESDE UN URI
// ---------------------------------------------------------------
@Composable
private fun ImagenDesdeUri(uri: Uri, modifier: Modifier = Modifier) {
    val context = LocalContext.current

    val imagen by produceState<ImageBitmap?>(initialValue = null, key1 = uri) {
        value = withContext(Dispatchers.IO) {
            try {
                val opciones = BitmapFactory.Options().apply { inSampleSize = 4 }
                context.contentResolver.openInputStream(uri)?.use { flujo ->
                    BitmapFactory.decodeStream(flujo, null, opciones)
                }?.asImageBitmap()
            } catch (e: Exception) {
                null
            }
        }
    }

    val bitmap = imagen
    if (bitmap != null) {
        Image(
            bitmap = bitmap,
            contentDescription = "Foto adjunta",
            modifier = modifier,
            contentScale = ContentScale.Crop
        )
    } else {
        Box(modifier, contentAlignment = Alignment.Center) {
            Text("Cargando foto...")
        }
    }
}