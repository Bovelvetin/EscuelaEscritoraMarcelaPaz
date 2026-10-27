package com.example.escuelamarcelapaz

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch

// Secciones del menú lateral
enum class Seccion {
    INICIO,
    SOLICITUDES;

    // El nombre de la sección depende del rol:
    // el Docente ve "Mis solicitudes" y el Interno "Bandeja de solicitudes"
    fun titulo(rol: Rol): String = when (this) {
        INICIO -> "Inicio"
        SOLICITUDES -> if (rol == Rol.INTERNO) "Bandeja de solicitudes" else "Mis solicitudes"
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MenuPrincipal(
    correo: String,
    rol: Rol,
    solicitudesViewModel: SolicitudesViewModel, // Lista compartida, viene de MainActivity
    onCerrarSesion: () -> Unit
) {
    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    var seccion by rememberSaveable { mutableStateOf(Seccion.INICIO) }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet {
                // Encabezado del menú
                Column(Modifier.padding(16.dp)) {
                    Text("Escuela Escritora Marcela Paz", style = MaterialTheme.typography.titleMedium)
                    Spacer(Modifier.height(4.dp))
                    Text(rol.etiqueta, style = MaterialTheme.typography.bodyMedium)
                    Text(correo, style = MaterialTheme.typography.bodySmall)
                }
                HorizontalDivider()

                Seccion.values().forEach { item ->
                    NavigationDrawerItem(
                        label = { Text(item.titulo(rol)) },
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
                    title = { Text(seccion.titulo(rol)) },
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
                    // Se le pasa el rol para que la pantalla muestre lo que corresponde
                    Seccion.SOLICITUDES -> PantallaSolicitudes(correo, rol, solicitudesViewModel)
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
        // El mensaje cambia según el rol
        Text(
            if (rol == Rol.INTERNO) "Abre el menú (☰) para gestionar las solicitudes."
            else "Abre el menú (☰) para crear solicitudes."
        )
    }
}