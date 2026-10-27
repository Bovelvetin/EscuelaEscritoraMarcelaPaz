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

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            EscuelaMarcelaPazTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    var correoSesion by rememberSaveable { mutableStateOf<String?>(null) }
                    var rolSesion by rememberSaveable { mutableStateOf<String?>(null) }

                    if (correoSesion == null || rolSesion == null) {
                        LoginScreen { correo, rol ->
                            correoSesion = correo
                            rolSesion = rol.name
                        }
                    } else {
                        // PantallaInicio
                        PantallaInicio(
                            correo = correoSesion!!,
                            rol = Rol.valueOf(rolSesion!!),
                            onCerrarSesion = {
                                correoSesion = null
                                rolSesion = null
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