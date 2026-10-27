package com.example.escuelamarcelapaz

import android.util.Patterns
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp

// Lista cerrada.
enum class Rol(val etiqueta: String) {
    DOCENTE("Docente"),
    INTERNO("Interno")
}

@Composable
fun LoginScreen(onLoginExitoso: (correo: String, rol: Rol) -> Unit) {

    // Variables.
    var correo by rememberSaveable { mutableStateOf("") }
    var contrasena by rememberSaveable { mutableStateOf("") }
    var rol by rememberSaveable { mutableStateOf(Rol.DOCENTE) }
    var mostrarContrasena by rememberSaveable { mutableStateOf(false) }
    var error by rememberSaveable { mutableStateOf<String?>(null) }

    // Modificadores.
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Iniciar sesión", style = MaterialTheme.typography.headlineMedium)
        Spacer(Modifier.height(24.dp))

        // Campo del correo.
        OutlinedTextField(
            value = correo,
            onValueChange = { correo = it; error = null },
            label = { Text("Correo electrónico") },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(12.dp))

        // Campo de la contraseña.
        OutlinedTextField(
            value = contrasena,
            onValueChange = { contrasena = it; error = null },
            label = { Text("Contraseña") },
            singleLine = true,
            visualTransformation = if (mostrarContrasena) VisualTransformation.None
            else PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            trailingIcon = {
                TextButton(onClick = { mostrarContrasena = !mostrarContrasena }) {
                    Text(if (mostrarContrasena) "Ocultar" else "Mostrar")
                }
            },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(16.dp))

        Text("Ingresar como:", style = MaterialTheme.typography.titleMedium)
        // Selección de rol.
        Row(verticalAlignment = Alignment.CenterVertically) {
            Rol.values().forEach { opcion ->
                RadioButton(
                    selected = rol == opcion,
                    onClick = { rol = opcion }
                )
                Text(opcion.etiqueta)
                Spacer(Modifier.width(16.dp))
            }
        }

        // Mensaje de error.
        error?.let {
            Spacer(Modifier.height(8.dp))
            Text(it, color = MaterialTheme.colorScheme.error)
        }

        Spacer(Modifier.height(24.dp))
        // Validación.
        Button(
            onClick = {
                val correoValido = Patterns.EMAIL_ADDRESS
                    .matcher(correo.trim()).matches()
                when {
                    !correoValido -> error = "Ingresa un correo válido"
                    contrasena.isBlank() -> error = "Ingresa tu contraseña"
                    else -> onLoginExitoso(correo.trim(), rol)
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Iniciar sesión")
        }
    }
}

// Función PantallaInicio
@Composable
fun PantallaInicio(correo: String, rol: Rol, onCerrarSesion: () -> Unit) {
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
        Spacer(Modifier.height(24.dp))
        OutlinedButton(onClick = onCerrarSesion) {
            Text("Cerrar sesión")
        }
    }
}