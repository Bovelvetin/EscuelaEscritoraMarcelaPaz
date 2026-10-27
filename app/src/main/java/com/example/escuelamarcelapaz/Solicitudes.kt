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
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

// ---------------------------------------------------------------
// ESTADOS POSIBLES DE UN TICKET
// Los cambia el usuario Interno al gestionar la solicitud.
// ---------------------------------------------------------------
enum class EstadoSolicitud(val etiqueta: String) {
    ABIERTA("Abierta"),
    EN_REVISION("En revisión"),
    RESUELTA("Resuelta")
}

// ---------------------------------------------------------------
// MODELO DE DATOS: un ticket
// ---------------------------------------------------------------
data class Solicitud(
    val numero: Int,                  // Número del ticket
    val asunto: String,               // Lo que escribió el Docente
    val fotoUri: Uri?,                // Foto opcional (null si no hay)
    val solicitante: String,          // Correo de quien creó el ticket
    val fecha: String,                // Fecha y hora de creación
    val estado: EstadoSolicitud = EstadoSolicitud.ABIERTA,
    val respuesta: String = ""        // Respuesta escrita por el Interno
)

// ---------------------------------------------------------------
// VIEWMODEL: guarda la lista de tickets FUERA de la sesión.
// Un ViewModel vive mientras la app esté abierta: sobrevive al
// cierre de sesión y a girar el teléfono. Por eso un Docente puede
// crear un ticket, cerrar sesión, y el Interno lo verá al entrar.
// (Si se cierra la app por completo, los tickets se pierden.)
// ---------------------------------------------------------------
class SolicitudesViewModel : ViewModel() {
    // Lista observable: al cambiar, las pantallas se redibujan solas
    val solicitudes = mutableStateListOf<Solicitud>()

    // Crea un ticket nuevo y lo agrega al inicio de la lista
    fun crear(asunto: String, foto: Uri?, solicitante: String) {
        val numero = (solicitudes.maxOfOrNull { it.numero } ?: 0) + 1
        val fecha = SimpleDateFormat("dd-MM-yyyy HH:mm", Locale("es", "CL")).format(Date())
        solicitudes.add(0, Solicitud(numero, asunto, foto, solicitante, fecha))
    }

    // Cambia el estado y la respuesta de un ticket.
    // Como Solicitud es inmutable, se reemplaza por una copia modificada.
    fun actualizar(numero: Int, estado: EstadoSolicitud, respuesta: String) {
        val indice = solicitudes.indexOfFirst { it.numero == numero }
        if (indice >= 0) {
            solicitudes[indice] = solicitudes[indice].copy(estado = estado, respuesta = respuesta)
        }
    }

    // Elimina un ticket por su número
    fun eliminar(numero: Int) {
        solicitudes.removeAll { it.numero == numero }
    }
}

// ---------------------------------------------------------------
// PANTALLA PRINCIPAL DEL MÓDULO
// Según el rol, muestra cosas distintas:
//  - Docente: sus tickets + botón para crear.
//  - Interno: todos los tickets + pantalla para gestionarlos.
// ---------------------------------------------------------------
@Composable
fun PantallaSolicitudes(correo: String, rol: Rol, viewModel: SolicitudesViewModel) {
    val esInterno = rol == Rol.INTERNO

    // true = se muestra el formulario de nueva solicitud (solo Docente)
    var creando by rememberSaveable { mutableStateOf(false) }
    // Número del ticket que el Interno está gestionando (null = ninguno)
    var seleccionada by rememberSaveable { mutableStateOf<Int?>(null) }

    // El Interno ve todos los tickets; el Docente, solo los suyos
    val visibles = if (esInterno) viewModel.solicitudes
    else viewModel.solicitudes.filter { it.solicitante == correo }

    // Ticket abierto en la pantalla de gestión (si hay uno seleccionado)
    val ticket = visibles.firstOrNull { it.numero == seleccionada }

    // Decide qué pantalla mostrar
    when {
        creando -> FormularioSolicitud(
            onCancelar = { creando = false },
            onEnviar = { asunto, foto ->
                viewModel.crear(asunto, foto, correo)
                creando = false
            }
        )
        ticket != null -> GestionSolicitud(
            solicitud = ticket,
            onCancelar = { seleccionada = null },
            onGuardar = { estado, respuesta ->
                viewModel.actualizar(ticket.numero, estado, respuesta)
                seleccionada = null
            }
        )
        else -> BandejaSolicitudes(
            solicitudes = visibles,
            esInterno = esInterno,
            onNueva = { creando = true },
            onGestionar = { seleccionada = it.numero },
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
    esInterno: Boolean,
    onNueva: () -> Unit,
    onGestionar: (Solicitud) -> Unit,
    onEliminar: (Solicitud) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Solo el Docente puede crear solicitudes
        if (!esInterno) {
            Button(onClick = onNueva, modifier = Modifier.fillMaxWidth()) {
                Text("Nueva solicitud")
            }
            Spacer(Modifier.height(16.dp))
        }

        if (solicitudes.isEmpty()) {
            Text(
                if (esInterno) "No hay solicitudes pendientes."
                else "Aún no has creado solicitudes."
            )
        } else {
            // Resumen: el Interno ve cuántas faltan por resolver
            val pendientes = solicitudes.count { it.estado != EstadoSolicitud.RESUELTA }
            Text(
                if (esInterno) "Solicitudes: ${solicitudes.size} (pendientes: $pendientes)"
                else "Mis solicitudes (${solicitudes.size})",
                style = MaterialTheme.typography.titleMedium
            )
            Spacer(Modifier.height(8.dp))

            LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                items(solicitudes, key = { it.numero }) { solicitud ->
                    TarjetaSolicitud(
                        solicitud = solicitud,
                        // El botón "Gestionar" solo existe para el Interno.
                        // (Las llaves dobles: las externas son del if, las internas, la lambda.)
                        onGestionar = if (esInterno) { { onGestionar(solicitud) } } else null,
                        // El Docente solo puede eliminar tickets que siguen abiertos
                        onEliminar = if (!esInterno && solicitud.estado == EstadoSolicitud.ABIERTA) {
                            { onEliminar(solicitud) }
                        } else null
                    )
                }
            }
        }
    }
}

// ---------------------------------------------------------------
// TARJETA: muestra un ticket. Los botones aparecen solo si se
// les pasa una acción (si es null, no se dibujan).
// ---------------------------------------------------------------
@Composable
private fun TarjetaSolicitud(
    solicitud: Solicitud,
    onGestionar: (() -> Unit)?,
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
            Text(solicitud.asunto, style = MaterialTheme.typography.bodyLarge)
            Spacer(Modifier.height(4.dp))
            Text(
                "${solicitud.fecha} · ${solicitud.solicitante}",
                style = MaterialTheme.typography.bodySmall
            )

            // Foto (solo si existe)
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

            // Respuesta del Interno (solo si ya escribió una)
            if (solicitud.respuesta.isNotBlank()) {
                Spacer(Modifier.height(8.dp))
                Text("Respuesta del equipo:", style = MaterialTheme.typography.labelLarge)
                Text(solicitud.respuesta, style = MaterialTheme.typography.bodyMedium)
            }

            // Botones según el rol
            Row {
                onGestionar?.let { TextButton(onClick = it) { Text("Gestionar") } }
                onEliminar?.let { TextButton(onClick = it) { Text("Eliminar") } }
            }
        }
    }
}

// Color del texto de estado, para distinguirlos de un vistazo
@Composable
private fun colorEstado(estado: EstadoSolicitud): Color = when (estado) {
    EstadoSolicitud.ABIERTA -> MaterialTheme.colorScheme.error
    EstadoSolicitud.EN_REVISION -> MaterialTheme.colorScheme.primary
    EstadoSolicitud.RESUELTA -> MaterialTheme.colorScheme.tertiary
}

// ---------------------------------------------------------------
// GESTIÓN (solo Interno): cambiar estado y escribir una respuesta
// ---------------------------------------------------------------
@Composable
private fun GestionSolicitud(
    solicitud: Solicitud,
    onCancelar: () -> Unit,
    onGuardar: (EstadoSolicitud, String) -> Unit
) {
    // Los campos parten con los valores actuales del ticket
    var estado by rememberSaveable { mutableStateOf(solicitud.estado) }
    var respuesta by rememberSaveable { mutableStateOf(solicitud.respuesta) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        Text("Ticket #${solicitud.numero}", style = MaterialTheme.typography.headlineSmall)
        Spacer(Modifier.height(8.dp))

        // Datos del ticket (solo lectura)
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
        // Un botón de radio por cada estado posible
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
                modifier = Modifier.weight(1f)
            ) {
                Text("Guardar")
            }
        }
    }
}

// ---------------------------------------------------------------
// FORMULARIO (solo Docente): asunto obligatorio + foto opcional
// ---------------------------------------------------------------
@Composable
private fun FormularioSolicitud(
    onCancelar: () -> Unit,
    onEnviar: (asunto: String, foto: Uri?) -> Unit
) {
    var asunto by rememberSaveable { mutableStateOf("") }
    var foto by rememberSaveable { mutableStateOf<Uri?>(null) }
    var error by rememberSaveable { mutableStateOf<String?>(null) }

    // Selector de fotos del sistema (no requiere permisos)
    val selectorFoto = rememberLauncherForActivityResult(
        ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) foto = uri // Si cancela, uri es null y no cambia nada
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
                modifier = Modifier.weight(1f)
            ) {
                Text("Enviar")
            }
        }
    }
}

// ---------------------------------------------------------------
// IMAGEN DESDE UN URI
// Carga la foto en segundo plano y la reduce para ahorrar memoria.
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