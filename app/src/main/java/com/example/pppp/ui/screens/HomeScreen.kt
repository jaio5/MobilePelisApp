package com.example.pppp.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.pppp.data.remote.dataclass.Movie

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    movies: List<Movie>,
    onMovieClick: (Movie) -> Unit,
    currentPage: Int,
    totalPages: Int,
    onNextPage: () -> Unit,
    onPrevPage: () -> Unit,
    onProfileClick: () -> Unit,
    onSettingsClick: () -> Unit,
    isAdmin: Boolean = false,
    onAdminClick: () -> Unit = {},
    onLogout: () -> Unit = {}
) {
    val backgroundColor = Color(0xFF0A0A0A)
    val accentGreen     = Color(0xFF00C030)
    val surfaceColor    = Color(0xFF141414)
    val cardColor       = Color(0xFF1A1A1A)

    Scaffold(
        containerColor = backgroundColor,
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(28.dp)
                                .background(accentGreen, RoundedCornerShape(6.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Filled.Movie, null, tint = Color.Black, modifier = Modifier.size(16.dp))
                        }
                        Spacer(Modifier.width(10.dp))
                        Text(
                            "CineVerse",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Black,
                            letterSpacing = (-0.5).sp,
                            color = Color.White
                        )
                    }
                },
                actions = {
                    if (isAdmin) {
                        IconButton(onClick = onAdminClick) {
                            Icon(Icons.Filled.AdminPanelSettings, "Admin", tint = Color(0xFFE94560))
                        }
                    }
                    IconButton(onClick = onSettingsClick) {
                        Icon(Icons.Filled.Settings, "Ajustes", tint = Color(0xFF808080))
                    }
                    IconButton(onClick = onProfileClick) {
                        Surface(
                            modifier = Modifier.size(30.dp),
                            shape = CircleShape,
                            color = accentGreen.copy(alpha = 0.2f)
                        ) {
                            Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                                Icon(Icons.Filled.Person, "Perfil", tint = accentGreen, modifier = Modifier.size(18.dp))
                            }
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = backgroundColor,
                    scrolledContainerColor = backgroundColor
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(backgroundColor)
        ) {
            // Section header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        "Explorar",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Black,
                        color = Color.White
                    )
                    Text(
                        "${movies.size} películas disponibles",
                        fontSize = 12.sp,
                        color = Color(0xFF606060)
                    )
                }
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = accentGreen.copy(alpha = 0.1f)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "Pág. $currentPage/$totalPages",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = accentGreen
                        )
                    }
                }
            }

            // Movie grid
            LazyVerticalGrid(
                columns = GridCells.Fixed(3),
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(movies) { movie ->
                    CinePosterCard(movie = movie, onClick = { onMovieClick(movie) })
                }
            }

            // Pagination bar
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = surfaceColor,
                tonalElevation = 0.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    FilledTonalButton(
                        onClick = onPrevPage,
                        enabled = currentPage > 1,
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.filledTonalButtonColors(
                            containerColor = Color(0xFF252525),
                            contentColor   = Color.White,
                            disabledContainerColor = Color(0xFF1A1A1A),
                            disabledContentColor   = Color(0xFF404040)
                        )
                    ) {
                        Icon(Icons.Filled.ChevronLeft, null, modifier = Modifier.size(18.dp))
                        Text("Anterior", fontSize = 13.sp, fontWeight = FontWeight.Medium)
                    }

                    // Page dots
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        val dotsToShow = minOf(totalPages, 5)
                        repeat(dotsToShow) { i ->
                            val pageNum = i + 1
                            Box(
                                modifier = Modifier
                                    .size(if (pageNum == currentPage) 8.dp else 5.dp)
                                    .background(
                                        if (pageNum == currentPage) accentGreen else Color(0xFF303030),
                                        CircleShape
                                    )
                            )
                        }
                        if (totalPages > 5) {
                            Text("…", color = Color(0xFF404040), fontSize = 12.sp)
                        }
                    }

                    FilledTonalButton(
                        onClick = onNextPage,
                        enabled = currentPage < totalPages,
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.filledTonalButtonColors(
                            containerColor = Color(0xFF252525),
                            contentColor   = Color.White,
                            disabledContainerColor = Color(0xFF1A1A1A),
                            disabledContentColor   = Color(0xFF404040)
                        )
                    ) {
                        Text("Siguiente", fontSize = 13.sp, fontWeight = FontWeight.Medium)
                        Icon(Icons.Filled.ChevronRight, null, modifier = Modifier.size(18.dp))
                    }
                }
            }
        }
    }
}

@Composable
private fun CinePosterCard(movie: Movie, onClick: () -> Unit) {
    val accentGreen = Color(0xFF00C030)
    var isPressed by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.94f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMedium),
        label = "cardScale"
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(0.67f)
            .scale(scale)
            .clip(RoundedCornerShape(8.dp))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) {
                isPressed = true
                onClick()
            }
    ) {
        // Poster image
        if (!movie.posterUrl.isNullOrEmpty()) {
            AsyncImage(
                model = movie.posterUrl,
                contentDescription = movie.title,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
        } else {
            // Placeholder with initial
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.linearGradient(
                            colors = listOf(Color(0xFF1C1C1C), Color(0xFF0A0A0A))
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    movie.title.take(1).uppercase(),
                    fontSize = 36.sp,
                    fontWeight = FontWeight.Black,
                    color = Color(0xFF2A2A2A)
                )
            }
        }

        // Gradient overlay at bottom
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color.Transparent,
                            Color.Transparent,
                            Color.Black.copy(alpha = 0.9f)
                        ),
                        startY = 120f
                    )
                )
        )

        // Title and year at bottom
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomStart)
                .padding(8.dp)
        ) {
            Text(
                text = movie.title,
                color = Color.White,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                lineHeight = 14.sp
            )
        }
    }
}