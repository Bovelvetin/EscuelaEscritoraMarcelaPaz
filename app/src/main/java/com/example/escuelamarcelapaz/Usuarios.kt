package com.example.escuelamarcelapaz

import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.ViewModel

data class Usuario(
    val nombre: String,
    val correo: String,
    val contrasena: String,
    val rol: Rol
)

class UsuariosViewModel : ViewModel() {
    val usuarios = mutableStateListOf<Usuario>()

    // Tres cuentas de prueba para entrar sin registrarse
    init {
        usuarios.add(Usuario("Docente de prueba", "docente@escuela.cl", "123456", Rol.DOCENTE))
        usuarios.add(Usuario("Interno de prueba", "interno@escuela.cl", "123456", Rol.INTERNO))
        usuarios.add(Usuario("Administrador de prueba", "admin@escuela.cl", "123456", Rol.ADMINISTRADOR))
    }

    fun buscar(correo: String): Usuario? =
        usuarios.firstOrNull { it.correo.equals(correo.trim(), ignoreCase = true) }

    fun registrar(nombre: String, correo: String, contrasena: String, rol: Rol): String? {
        if (buscar(correo) != null) return "Ya existe una cuenta con ese correo"
        usuarios.add(Usuario(nombre.trim(), correo.trim().lowercase(), contrasena, rol))
        return null
    }

    fun iniciarSesion(correo: String, contrasena: String): Usuario? {
        val usuario = buscar(correo)
        return if (usuario != null && usuario.contrasena == contrasena) usuario else null
    }
}
