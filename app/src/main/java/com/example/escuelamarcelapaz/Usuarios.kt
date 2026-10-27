package com.example.escuelamarcelapaz

import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.ViewModel

// ---------------------------------------------------------------
// MODELO: una cuenta de usuario
// ---------------------------------------------------------------
data class Usuario(
    val nombre: String,
    val correo: String,
    val contrasena: String, // Simulación: en una app real NUNCA se guarda en texto plano
    val rol: Rol
)

// ---------------------------------------------------------------
// VIEWMODEL: guarda las cuentas mientras la app esté abierta.
// Igual que con las solicitudes, sobrevive al cierre de sesión.
// ---------------------------------------------------------------
class UsuariosViewModel : ViewModel() {
    val usuarios = mutableStateListOf<Usuario>()

    // Dos cuentas de prueba para poder entrar sin registrarse
    init {
        usuarios.add(Usuario("Docente de prueba", "docente@escuela.cl", "123456", Rol.DOCENTE))
        usuarios.add(Usuario("Interno de prueba", "interno@escuela.cl", "123456", Rol.INTERNO))
    }

    // Busca una cuenta por correo (sin distinguir mayúsculas)
    fun buscar(correo: String): Usuario? =
        usuarios.firstOrNull { it.correo.equals(correo.trim(), ignoreCase = true) }

    // Crea una cuenta. Devuelve un mensaje de error, o null si todo salió bien.
    fun registrar(nombre: String, correo: String, contrasena: String, rol: Rol): String? {
        if (buscar(correo) != null) return "Ya existe una cuenta con ese correo"
        usuarios.add(Usuario(nombre.trim(), correo.trim().lowercase(), contrasena, rol))
        return null
    }

    // Devuelve el usuario si correo y contraseña coinciden; si no, null
    fun iniciarSesion(correo: String, contrasena: String): Usuario? {
        val usuario = buscar(correo)
        return if (usuario != null && usuario.contrasena == contrasena) usuario else null
    }
}