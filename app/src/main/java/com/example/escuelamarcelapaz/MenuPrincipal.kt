package com.example.escuelamarcelapaz

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch

// Lista cerrada de las pantallas del menú.
enum class Seccion(val titulo: String) {
    INICIO("Inicio"),
    DOCUMENTOS("Carga de documentos")
}

// Función MenuPrincipal.
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MenuPrincipal(correo: String, rol: Rol, onCerrarSesion: () -> Unit) {

    // Variables.
    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    var seccion by rememberSaveable { mutableStateOf(Seccion.INICIO) }
    val documentos = remember { mutableStateListOf<String>() }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet {
                Column(Modifier.padding(16.dp)) {
                    Text("Escuela Escritora Marcela Paz", style = MaterialTheme.typography.titleMedium)
                    Spacer(Modifier.height(4.dp))
                    Text(rol.etiqueta, style = MaterialTheme.typography.bodyMedium)
                    Text(correo, style = MaterialTheme.typography.bodySmall)
                }
                HorizontalDivider()
                Seccion.values().forEach { item ->
                    NavigationDrawerItem(
                        label = { Text(item.titulo) },
                        selected = seccion == item,
                        onClick = {
                            seccion = item
                            scope.launch { drawerState.close() }
                        },
                        modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                    )
                }
                HorizontalDivider()
                NavigationDrawerItem(
                    label = { Text("Cerrar sesión") },
                    selected = false,
                    onClick = {
                        scope.launch { drawerState.close() }
                        onCerrarSesion()
                    },
                    modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                )
            }
        }
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text(seccion.titulo) },
                    navigationIcon = {
                        IconButton(onClick = { scope.launch { drawerState.open() } }) {
                            Icon(Icons.Default.Menu, contentDescription = "Abrir menú")
                        }
                    }
                )
            }
        ) { padding ->
            Box(
                Modifier
                    .padding(padding)
                    .fillMaxSize()
            ) {
                when (seccion) {
                    Seccion.INICIO -> PantallaBienvenida(correo, rol)
                    Seccion.DOCUMENTOS -> PantallaDocumentos(documentos)
                }
            }
        }
    }
}

@Composable
fun PantallaBienvenida(correo: String, rol: Rol) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("¡Bienvenido/a!", style = MaterialTheme.typography.headlineMedium)
        Spacer(Modifier.height(8.dp))
        Text("Sesión iniciada como ${rol.etiqueta}")
        Text(correo)
        Spacer(Modifier.height(16.dp))
        Text("Abre el menú (☰) para cargar documentos o cerrar sesión.")
    }
}

@Composable
fun PantallaDocumentos(documentos: SnapshotStateList<String>) {
    val context = LocalContext.current
    val selector = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenMultipleDocuments()
    ) { uris ->
        uris.forEach { documentos.add(nombreArchivo(context, it)) }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Button(
            onClick = { selector.launch(arrayOf("*/*")) },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Seleccionar documentos")
        }
        Spacer(Modifier.height(16.dp))

        if (documentos.isEmpty()) {
            Text("Aún no has cargado documentos.")
        } else {
            Text("Documentos cargados (${documentos.size})", style = MaterialTheme.typography.titleMedium)
            LazyColumn {
                items(documentos) { nombre ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(nombre, modifier = Modifier.weight(1f))
                        TextButton(onClick = { documentos.remove(nombre) }) {
                            Text("Quitar")
                        }
                    }
                }
            }
        }
    }
}

private fun nombreArchivo(context: Context, uri: Uri): String {
    context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
        val indice = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
        if (indice >= 0 && cursor.moveToFirst()) return cursor.getString(indice)
    }
    return uri.lastPathSegment ?: "documento"
}