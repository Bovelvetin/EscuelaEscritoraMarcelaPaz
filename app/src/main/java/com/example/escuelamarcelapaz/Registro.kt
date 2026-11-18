package com.example.escuelamarcelapaz

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
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

@Composable
fun RegistroScreen(
    onRegistrar: (nombre: String, correo: String, contrasena: String, rol: Rol) -> String?,
    onVolver: () -> Unit
) {
    var nombre by rememberSaveable { mutableStateOf("") }
    var correo by rememberSaveable { mutableStateOf("") }
    var contrasena by rememberSaveable { mutableStateOf("") }
    var confirmar by rememberSaveable { mutableStateOf("") }
    var rol by rememberSaveable { mutableStateOf(Rol.DOCENTE) }
    var mostrarContrasena by rememberSaveable { mutableStateOf(false) }
    var error by rememberSaveable { mutableStateOf<String?>(null) }

    val transformacion = if (mostrarContrasena) VisualTransformation.None
    else PasswordVisualTransformation()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Crear cuenta", style = MaterialTheme.typography.headlineMedium)
        Spacer(Modifier.height(16.dp))

        OutlinedTextField(
            value = nombre,
            onValueChange = { nombre = it; error = null },
            label = { Text("Nombre completo") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(12.dp))

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
            label = { Text("Contraseña (mínimo 6 caracteres)") },
            singleLine = true,
            visualTransformation = transformacion,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            trailingIcon = {
                TextButton(onClick = { mostrarContrasena = !mostrarContrasena }) {
                    Text(if (mostrarContrasena) "Ocultar" else "Mostrar")
                }
            },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(12.dp))

        OutlinedTextField(
            value = confirmar,
            onValueChange = { confirmar = it; error = null },
            label = { Text("Confirmar contraseña") },
            singleLine = true,
            visualTransformation = transformacion,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(16.dp))

        // Selección de rol en columna para evitar desbordes con 3 opciones
        Column(modifier = Modifier.fillMaxWidth()) {
            Text("Registrarme como:", style = MaterialTheme.typography.titleMedium)
            Rol.values().forEach { opcion ->
                Row(verticalAlignment = Alignment.CenterVertically) {
                    RadioButton(selected = rol == opcion, onClick = { rol = opcion })
                    Text(opcion.etiqueta)
                }
            }
        }

        AnimatedVisibility(
            visible = error != null,
            enter = expandVertically() + fadeIn(),
            exit = shrinkVertically() + fadeOut()
        ) {
            error?.let {
                Column {
                    Spacer(Modifier.height(8.dp))
                    Text(it, color = MaterialTheme.colorScheme.error)
                }
            }
        }

        Spacer(Modifier.height(24.dp))
        Button(
            onClick = {
                val correoValido = android.util.Patterns.EMAIL_ADDRESS
                    .matcher(correo.trim()).matches()
                error = when {
                    nombre.isBlank() -> "Ingresa tu nombre"
                    !correoValido -> "Ingresa un correo válido"
                    contrasena.length < 6 -> "La contraseña debe tener al menos 6 caracteres"
                    contrasena != confirmar -> "Las contraseñas no coinciden"
                    else -> onRegistrar(nombre, correo, contrasena, rol)
                }
            },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                contentColor = MaterialTheme.colorScheme.onPrimaryContainer
            )
        ) {
            Text("Crear cuenta")
        }

        Spacer(Modifier.height(8.dp))
        TextButton(onClick = onVolver) {
            Text("Ya tengo cuenta")
        }
    }
}
