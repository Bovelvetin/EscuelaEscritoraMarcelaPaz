package com.example.escuelamarcelapaz.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// Tema claro:
//  - primary / secondary: tonos oscuros (botones, enlaces, selección)
//  - primaryContainer / secondaryContainer: los colores OFICIALES (barras y fondos)
private val EsquemaClaro = lightColorScheme(
    primary = AzulOscuro,
    onPrimary = Color.White,
    primaryContainer = AzulOficial,
    onPrimaryContainer = TextoSobreAzulOficial,
    secondary = AmarilloOscuro,
    onSecondary = Color.White,
    secondaryContainer = AmarilloOficial,
    onSecondaryContainer = TextoSobreAmarilloOficial,
    tertiary = VerdeResuelto,
    onTertiary = Color.White,
    background = Color(0xFFFAFDFC),
    surface = Color(0xFFFAFDFC)
)

// Tema oscuro: aquí los colores oficiales sí funcionan como color principal,
// porque se leen bien sobre fondo oscuro
private val EsquemaOscuro = darkColorScheme(
    primary = AzulOficial,
    onPrimary = Color(0xFF003733),
    primaryContainer = AzulContenedorOscuro,
    onPrimaryContainer = AzulClaroSobreOscuro,
    secondary = AmarilloOficial,
    onSecondary = Color(0xFF353100),
    secondaryContainer = AmarilloContenedorOscuro,
    onSecondaryContainer = AmarilloOficial,
    tertiary = VerdeSobreOscuro,
    onTertiary = Color(0xFF003910),
    background = Color(0xFF0F1514),
    surface = Color(0xFF0F1514)
)

@Composable
fun EscuelaMarcelaPazTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    // Sin "color dinámico" de Android 12+, para que mande nuestra paleta
    val colores = if (darkTheme) EsquemaOscuro else EsquemaClaro

    MaterialTheme(
        colorScheme = colores,
        typography = Typography, // Viene de Type.kt
        content = content
    )
}