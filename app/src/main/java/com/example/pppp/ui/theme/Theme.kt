package com.example.pppp.ui.theme

import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// Si existe otra definición de CinematicColors en el proyecto, elimina este objeto y usa la definición centralizada.
// Si solo se usa aquí, mantén la definición y asegúrate de que no haya duplicados.
// Paleta principal para la app (Cinematic)
object CinematicColors {
    val Primary = Color(0xFFE94560)           // Rojo vibrante
    val PrimaryVariant = Color(0xFFFF6B6B)    // Rojo claro
    val Secondary = Color(0xFF667EEA)         // Azul/Púrpura
    val SecondaryVariant = Color(0xFF764BA2)  // Púrpura
    val Accent = Color(0xFFFFE66D)            // Amarillo dorado
    val Background = Color(0xFF1A1A2E)        // Azul oscuro profundo
    val BackgroundVariant = Color(0xFF16213E) // Azul oscuro medio
    val Surface = Color(0xFF0F3460)           // Azul oscuro
}

@Composable
fun PelisAppTheme(
    darkTheme: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) {
        darkColorScheme(
            primary = CinematicColors.Primary,
            secondary = CinematicColors.Secondary,
            background = CinematicColors.Background,
            surface = CinematicColors.Surface,
            error = Color(0xFFFF6B6B),
            onPrimary = Color.White,
            onSecondary = Color.White,
            onBackground = Color.White,
            onSurface = Color.White,
            onError = Color.White
        )
    } else {
        lightColorScheme(
            primary = CinematicColors.Primary,
            secondary = CinematicColors.Secondary,
            background = CinematicColors.Background,
            surface = CinematicColors.Surface,
            error = Color(0xFFFF6B6B),
            onPrimary = Color.White,
            onSecondary = Color.White,
            onBackground = Color.White,
            onSurface = Color.White,
            onError = Color.White
        )
    }
    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
