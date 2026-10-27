package com.example.escuelamarcelapaz

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp

// Roles disponibles. "etiqueta" es el texto que se muestra al usuario.
enum class Rol(val etiqueta: String) {
    DOCENTE("Docente"),
    INTERNO("Interno")
}

// ---------------------------------------------------------------
// LOGIN
// - mensaje: aviso opcional (por ejemplo, "Cuenta creada")
// - onIniciarSesion: recibe correo y contraseña y devuelve un
//   mensaje de error, o null si el inicio de sesión fue correcto
// - onIrARegistro: se llama al pulsar "Crear cuenta"
// ---------------------------------------------------------------
@Composable
fun LoginScreen(
    mensaje: String?,
    onIniciarSesion: (correo: String, contrasena: String) -> String?,
    onIrARegistro: () -> Unit
) {
    var correo by rememberSaveable { mutableStateOf("") }
    var contrasena by rememberSaveable { mutableStateOf("") }
    var mostrarContrasena by rememberSaveable { mutableStateOf(false) }
    var error by rememberSaveable { mutableStateOf<String?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Iniciar sesión", style = MaterialTheme.typography.headlineMedium)
        Spacer(Modifier.height(16.dp))

        // Aviso positivo (por ejemplo, tras registrarse)
        mensaje?.let {
            Text(it, color = MaterialTheme.colorScheme.primary)
            Spacer(Modifier.height(12.dp))
        }

        OutlinedTextField(
            value = correo,
            onValueChange = { correo = it; error = null },
            label = { Text("Correo electrónico") },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(12.dp))

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

        error?.let {
            Spacer(Modifier.height(8.dp))
            Text(it, color = MaterialTheme.colorScheme.error)
        }

        Spacer(Modifier.height(24.dp))
        Button(
            onClick = {
                if (correo.isBlank() || contrasena.isBlank()) {
                    error = "Ingresa tu correo y contraseña"
                } else {
                    // Si el login falla, MainActivity devuelve el mensaje de error
                    error = onIniciarSesion(correo.trim(), contrasena)
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Iniciar sesión")
        }

        Spacer(Modifier.height(8.dp))
        TextButton(onClick = onIrARegistro) {
            Text("¿No tienes cuenta? Crear cuenta")
        }
    }
}