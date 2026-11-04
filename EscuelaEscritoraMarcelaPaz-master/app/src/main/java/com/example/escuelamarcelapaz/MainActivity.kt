package com.example.escuelamarcelapaz

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.example.escuelamarcelapaz.ui.theme.EscuelaMarcelaPazTheme
import androidx.activity.viewModels

class MainActivity : ComponentActivity() {

    // Ambos ViewModels viven a nivel de clase, fuera de onCreate
    private val solicitudesViewModel: SolicitudesViewModel by viewModels()
    private val usuariosViewModel: UsuariosViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            EscuelaMarcelaPazTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    // Sesión: solo guardamos el correo; el resto se busca en las cuentas
                    var correoSesion by rememberSaveable { mutableStateOf<String?>(null) }
                    // true = se muestra el registro en vez del login
                    var enRegistro by rememberSaveable { mutableStateOf(false) }
                    // Aviso que se muestra en el login (por ejemplo, "Cuenta creada")
                    var mensaje by rememberSaveable { mutableStateOf<String?>(null) }

                    // Usuario de la sesión actual (null si no hay sesión o no existe)
                    val usuario = correoSesion?.let { usuariosViewModel.buscar(it) }

                    // Elige la pantalla según el estado
                    when {
                        usuario != null -> MenuPrincipal(
                            nombre = usuario.nombre,
                            correo = usuario.correo,
                            rol = usuario.rol,
                            solicitudesViewModel = solicitudesViewModel,
                            onCerrarSesion = { correoSesion = null }
                        )

                        enRegistro -> RegistroScreen(
                            onRegistrar = { nombre, correo, contrasena, rol ->
                                val error = usuariosViewModel.registrar(nombre, correo, contrasena, rol)
                                if (error == null) {
                                    // Cuenta creada: vuelve al login con un aviso
                                    mensaje = "Cuenta creada. Ya puedes iniciar sesión."
                                    enRegistro = false
                                }
                                error // Devuelve el error (o null) a la pantalla
                            },
                            onVolver = { enRegistro = false }
                        )

                        else -> LoginScreen(
                            mensaje = mensaje,
                            onIniciarSesion = { correo, contrasena ->
                                val u = usuariosViewModel.iniciarSesion(correo, contrasena)
                                if (u == null) {
                                    "Correo o contraseña incorrectos"
                                } else {
                                    mensaje = null
                                    correoSesion = u.correo // Esto abre el menú principal
                                    null
                                }
                            },
                            onIrARegistro = {
                                mensaje = null
                                enRegistro = true
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    EscuelaMarcelaPazTheme {
        Greeting("Android")
    }
}