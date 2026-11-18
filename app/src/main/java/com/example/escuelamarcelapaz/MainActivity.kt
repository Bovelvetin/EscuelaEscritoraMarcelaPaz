package com.example.escuelamarcelapaz

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.fillMaxSize
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

class MainActivity : ComponentActivity() {

    private val solicitudesViewModel: SolicitudesViewModel by viewModels()
    private val usuariosViewModel: UsuariosViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            EscuelaMarcelaPazTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    var correoSesion by rememberSaveable { mutableStateOf<String?>(null) }
                    var enRegistro by rememberSaveable { mutableStateOf(false) }
                    var mensaje by rememberSaveable { mutableStateOf<String?>(null) }

                    val usuario = correoSesion?.let { usuariosViewModel.buscar(it) }

                    val pantallaActual = when {
                        usuario != null -> "MENU"
                        enRegistro -> "REGISTRO"
                        else -> "LOGIN"
                    }

                    AnimatedContent(
                        targetState = pantallaActual,
                        transitionSpec = {
                            (slideInHorizontally { width -> width / 4 } + fadeIn())
                                .togetherWith(slideOutHorizontally { width -> -width / 4 } + fadeOut())
                        },
                        label = "TransicionPantallaPrincipal"
                    ) { estado ->
                        when (estado) {
                            "MENU" -> usuario?.let {
                                MenuPrincipal(
                                    nombre = it.nombre,
                                    correo = it.correo,
                                    rol = it.rol,
                                    solicitudesViewModel = solicitudesViewModel,
                                    onCerrarSesion = { correoSesion = null }
                                )
                            }

                            "REGISTRO" -> RegistroScreen(
                                onRegistrar = { nombre, correo, contrasena, rol ->
                                    val error = usuariosViewModel.registrar(nombre, correo, contrasena, rol)
                                    if (error == null) {
                                        mensaje = "Cuenta creada. Ya puedes iniciar sesión."
                                        enRegistro = false
                                    }
                                    error
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
                                        correoSesion = u.correo
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
