package com.example.pppp.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.pppp.data.remote.Retrofit
import com.example.pppp.data.remote.dataclass.Movie
import kotlinx.coroutines.launch

private val MovBg     = Color(0xFF0A0A0A)
private val MovSurf   = Color(0xFF141414)
private val MovCard   = Color(0xFF1A1A1A)
private val MovGreen  = Color(0xFF00C030)
private val MovRed    = Color(0xFFE94560)
private val MovTextPr = Color(0xFFF0F0F0)
private val MovTextSc = Color(0xFF909090)
private val MovTextMt = Color(0xFF505050)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminMoviesScreen(
    token: String,
    onNavigateBack: () -> Unit,
    onMovieClick: (Long) -> Unit = {}
) {
    val scope = rememberCoroutineScope()

    var movies by remember { mutableStateOf<List<Movie>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var currentPage by remember { mutableIntStateOf(0) }
    var totalPages by remember { mutableIntStateOf(1) }
    var errorMsg by remember { mutableStateOf("") }
    var successMsg by remember { mutableStateOf("") }

    // TMDB dialog state
    var showTmdbDialog by remember { mutableStateOf(false) }
    var tmdbIdInput by remember { mutableStateOf("") }
    var bulkPageInput by remember { mutableStateOf("1") }
    var isTmdbLoading by remember { mutableStateOf(false) }
    var tmdbDialogError by remember { mutableStateOf("") }

    // The token from AppNavigation is RAW (no Bearer prefix).
    // AdminApi endpoints receive the full "Bearer $token" string.
    val bearerToken = "Bearer $token"

    fun loadMovies(page: Int) {
        scope.launch {
            isLoading = true
            errorMsg = ""
            try {
                // GET /api/movies?page=N&size=12 – no auth required per API docs
                val response = Retrofit.Movies.getMovies(page, 12)
                if (response.isSuccessful) {
                    val body = response.body()
                    movies = body?.content ?: emptyList()
                    totalPages = body?.totalPages?.coerceAtLeast(1) ?: 1
                    currentPage = page
                } else {
                    errorMsg = "Error ${response.code()}: ${response.message()}"
                }
            } catch (e: Exception) {
                errorMsg = "Error de conexión: ${e.message}"
            } finally {
                isLoading = false
            }
        }
    }

    LaunchedEffect(Unit) { loadMovies(0) }

    // Auto-dismiss banners
    LaunchedEffect(successMsg) {
        if (successMsg.isNotEmpty()) { kotlinx.coroutines.delay(3000); successMsg = "" }
    }
    LaunchedEffect(errorMsg) {
        if (errorMsg.isNotEmpty() && !isLoading) { kotlinx.coroutines.delay(4000); errorMsg = "" }
    }
    LaunchedEffect(tmdbDialogError) {
        if (tmdbDialogError.isNotEmpty()) { kotlinx.coroutines.delay(4000); tmdbDialogError = "" }
    }

    // ── TMDB dialog ──────────────────────────────────────────────────────────
    if (showTmdbDialog) {
        AlertDialog(
            onDismissRequest = { if (!isTmdbLoading) { showTmdbDialog = false; tmdbDialogError = "" } },
            containerColor = MovCard,
            icon = {
                Icon(Icons.Filled.CloudDownload, null, tint = MovGreen, modifier = Modifier.size(32.dp))
            },
            title = {
                Text("Cargar desde TMDB", fontWeight = FontWeight.Bold, color = MovTextPr)
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {

                    if (tmdbDialogError.isNotEmpty()) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = MovRed.copy(alpha = 0.15f),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                tmdbDialogError,
                                color = MovRed,
                                fontSize = 12.sp,
                                modifier = Modifier.padding(10.dp)
                            )
                        }
                    }

                    // ── Individual load ──────────────────────────────────
                    Text(
                        "Película individual (por ID de TMDB)",
                        fontSize = 13.sp,
                        color = MovTextSc,
                        fontWeight = FontWeight.SemiBold
                    )
                    OutlinedTextField(
                        value = tmdbIdInput,
                        onValueChange = { tmdbIdInput = it.filter { c -> c.isDigit() } },
                        label = { Text("ID de TMDB", color = MovTextSc, fontSize = 13.sp) },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        shape = RoundedCornerShape(8.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MovGreen,
                            unfocusedBorderColor = Color(0xFF2A2A2A),
                            focusedTextColor = MovTextPr,
                            unfocusedTextColor = MovTextPr,
                            cursorColor = MovGreen,
                            focusedContainerColor = Color(0xFF222222),
                            unfocusedContainerColor = Color(0xFF1E1E1E)
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                    Button(
                        onClick = {
                            val tmdbId = tmdbIdInput.toLongOrNull()
                            if (tmdbId == null || tmdbId <= 0) {
                                tmdbDialogError = "Introduce un ID de TMDB válido."
                                return@Button
                            }
                            scope.launch {
                                isTmdbLoading = true
                                tmdbDialogError = ""
                                try {
                                    // POST /api/admin/tmdb/load-movie/{tmdbId}
                                    val response = Retrofit.Admin.loadMovieFromTmdb(tmdbId, bearerToken)
                                    when {
                                        response.isSuccessful -> {
                                            successMsg = "Película TMDB #$tmdbId cargada."
                                            showTmdbDialog = false
                                            tmdbIdInput = ""
                                            loadMovies(currentPage)
                                        }
                                        response.code() == 401 -> tmdbDialogError = "Sin autorización. Verifica tu sesión."
                                        response.code() == 403 -> tmdbDialogError = "Acceso denegado. Se requiere rol ADMIN."
                                        response.code() == 404 -> tmdbDialogError = "Película no encontrada en TMDB (ID: $tmdbId)."
                                        else -> tmdbDialogError = "Error ${response.code()}: ${response.message()}"
                                    }
                                } catch (e: Exception) {
                                    tmdbDialogError = "Error de conexión: ${e.message}"
                                } finally {
                                    isTmdbLoading = false
                                }
                            }
                        },
                        enabled = tmdbIdInput.isNotBlank() && !isTmdbLoading,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MovGreen)
                    ) {
                        if (isTmdbLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                color = Color.Black,
                                strokeWidth = 2.dp
                            )
                        } else {
                            Icon(Icons.Filled.Download, null, tint = Color.Black, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(6.dp))
                            Text("Cargar película", color = Color.Black, fontWeight = FontWeight.Bold)
                        }
                    }

                    HorizontalDivider(color = Color(0xFF2A2A2A))

                    // ── Bulk load ────────────────────────────────────────
                    Text(
                        "Carga masiva (página TMDB popular, 1-500)",
                        fontSize = 13.sp,
                        color = MovTextSc,
                        fontWeight = FontWeight.SemiBold
                    )
                    OutlinedTextField(
                        value = bulkPageInput,
                        onValueChange = { bulkPageInput = it.filter { c -> c.isDigit() } },
                        label = { Text("Nº de página TMDB", color = MovTextSc, fontSize = 13.sp) },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        shape = RoundedCornerShape(8.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MovGreen,
                            unfocusedBorderColor = Color(0xFF2A2A2A),
                            focusedTextColor = MovTextPr,
                            unfocusedTextColor = MovTextPr,
                            cursorColor = MovGreen,
                            focusedContainerColor = Color(0xFF222222),
                            unfocusedContainerColor = Color(0xFF1E1E1E)
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                    Button(
                        onClick = {
                            val page = bulkPageInput.toIntOrNull()?.coerceIn(1, 500)
                            if (page == null) {
                                tmdbDialogError = "Introduce un número de página entre 1 y 500."
                                return@Button
                            }
                            scope.launch {
                                isTmdbLoading = true
                                tmdbDialogError = ""
                                try {
                                    // POST /api/admin/tmdb/bulk-load?page=N
                                    val response = Retrofit.Admin.bulkLoadMovies(page, bearerToken)
                                    when {
                                        response.isSuccessful -> {
                                            successMsg = "Carga masiva página $page completada."
                                            showTmdbDialog = false
                                            bulkPageInput = "1"
                                            loadMovies(currentPage)
                                        }
                                        response.code() == 401 -> tmdbDialogError = "Sin autorización. Verifica tu sesión."
                                        response.code() == 403 -> tmdbDialogError = "Acceso denegado. Se requiere rol ADMIN."
                                        else -> tmdbDialogError = "Error ${response.code()}: ${response.message()}"
                                    }
                                } catch (e: Exception) {
                                    tmdbDialogError = "Error de conexión: ${e.message}"
                                } finally {
                                    isTmdbLoading = false
                                }
                            }
                        },
                        enabled = bulkPageInput.isNotBlank() && !isTmdbLoading,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF333333))
                    ) {
                        if (isTmdbLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                color = Color.White,
                                strokeWidth = 2.dp
                            )
                        } else {
                            Icon(Icons.Filled.CloudSync, null, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(6.dp))
                            Text("Carga masiva", color = Color.White)
                        }
                    }

                    HorizontalDivider(color = Color(0xFF2A2A2A))

                    // ── Reload posters ───────────────────────────────────
                    Text("Recargar posters desde TMDB", fontSize = 13.sp, color = MovTextSc, fontWeight = FontWeight.SemiBold)
                    Button(
                        onClick = {
                            scope.launch {
                                isTmdbLoading = true
                                tmdbDialogError = ""
                                try {
                                    // POST /api/admin/images/reload
                                    val response = Retrofit.Admin.reloadPosters(bearerToken)
                                    if (response.isSuccessful) {
                                        successMsg = "Posters recargados correctamente."
                                        showTmdbDialog = false
                                        loadMovies(currentPage)
                                    } else {
                                        tmdbDialogError = "Error ${response.code()}: ${response.message()}"
                                    }
                                } catch (e: Exception) {
                                    tmdbDialogError = "Error: ${e.message}"
                                } finally {
                                    isTmdbLoading = false
                                }
                            }
                        },
                        enabled = !isTmdbLoading,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1A3A2A))
                    ) {
                        Icon(Icons.Filled.Image, null, tint = MovGreen, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("Recargar posters", color = MovGreen)
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { if (!isTmdbLoading) { showTmdbDialog = false; tmdbDialogError = "" } }) {
                    Text("Cerrar", color = MovTextSc)
                }
            }
        )
    }

    // ── Main scaffold ────────────────────────────────────────────────────────
    Scaffold(
        containerColor = MovBg,
        topBar = {
            TopAppBar(
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Volver", tint = MovTextPr)
                    }
                },
                title = {
                    Column {
                        Text("Películas", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = MovTextPr)
                        Text("Gestión del catálogo · ${movies.size} en página", fontSize = 12.sp, color = MovTextSc)
                    }
                },
                actions = {
                    IconButton(onClick = { loadMovies(currentPage) }) {
                        Icon(Icons.Filled.Refresh, "Recargar", tint = MovTextSc)
                    }
                    IconButton(onClick = { showTmdbDialog = true }) {
                        Icon(Icons.Filled.CloudDownload, "TMDB", tint = MovGreen)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MovSurf)
            )
        },
        bottomBar = {
            if (!isLoading && movies.isNotEmpty()) {
                Surface(color = MovSurf) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        FilledTonalButton(
                            onClick = { loadMovies(currentPage - 1) },
                            enabled = currentPage > 0,
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.filledTonalButtonColors(
                                containerColor = Color(0xFF252525), contentColor = MovTextPr,
                                disabledContainerColor = Color(0xFF1A1A1A), disabledContentColor = Color(0xFF404040)
                            )
                        ) {
                            Icon(Icons.Filled.ChevronLeft, null, modifier = Modifier.size(18.dp))
                            Text("Anterior", fontSize = 13.sp)
                        }

                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                "${currentPage + 1} / $totalPages",
                                fontSize = 14.sp, fontWeight = FontWeight.Bold, color = MovTextPr
                            )
                            Text("páginas", fontSize = 10.sp, color = MovTextMt)
                        }

                        FilledTonalButton(
                            onClick = { loadMovies(currentPage + 1) },
                            enabled = currentPage < totalPages - 1,
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.filledTonalButtonColors(
                                containerColor = Color(0xFF252525), contentColor = MovTextPr,
                                disabledContainerColor = Color(0xFF1A1A1A), disabledContentColor = Color(0xFF404040)
                            )
                        ) {
                            Text("Siguiente", fontSize = 13.sp)
                            Icon(Icons.Filled.ChevronRight, null, modifier = Modifier.size(18.dp))
                        }
                    }
                }
            }
        }
    ) { padding ->

        Box(modifier = Modifier.fillMaxSize()) {

            when {
                isLoading -> {
                    Box(
                        modifier = Modifier.fillMaxSize().padding(padding),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            CircularProgressIndicator(color = MovGreen, strokeWidth = 2.dp)
                            Spacer(Modifier.height(12.dp))
                            Text("Cargando películas…", color = MovTextSc, fontSize = 13.sp)
                        }
                    }
                }

                movies.isEmpty() -> {
                    Box(
                        modifier = Modifier.fillMaxSize().padding(padding),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Filled.Movie, null, tint = MovTextMt, modifier = Modifier.size(56.dp))
                            Spacer(Modifier.height(12.dp))
                            Text("No hay películas disponibles", color = MovTextSc)
                            Spacer(Modifier.height(8.dp))
                            Text("Usa el botón ☁ para cargar desde TMDB", color = MovTextMt, fontSize = 12.sp)
                            Spacer(Modifier.height(16.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                OutlinedButton(
                                    onClick = { loadMovies(0) },
                                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF2A2A2A))
                                ) {
                                    Icon(Icons.Filled.Refresh, null, tint = MovTextSc, modifier = Modifier.size(15.dp))
                                    Spacer(Modifier.width(4.dp))
                                    Text("Reintentar", color = MovTextSc)
                                }
                                Button(
                                    onClick = { showTmdbDialog = true },
                                    colors = ButtonDefaults.buttonColors(containerColor = MovGreen)
                                ) {
                                    Icon(Icons.Filled.CloudDownload, null, tint = Color.Black, modifier = Modifier.size(15.dp))
                                    Spacer(Modifier.width(4.dp))
                                    Text("Cargar TMDB", color = Color.Black, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }

                else -> {
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(3),
                        modifier = Modifier.fillMaxSize().padding(padding),
                        contentPadding = PaddingValues(12.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(movies, key = { it.id }) { movie ->
                            AdminMoviePoster(
                                movie = movie,
                                onClick = { onMovieClick(movie.id) }
                            )
                        }
                    }
                }
            }

            // Success banner
            AnimatedVisibility(
                visible = successMsg.isNotEmpty(),
                enter = fadeIn(), exit = fadeOut(),
                modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 80.dp, start = 16.dp, end = 16.dp)
            ) {
                Surface(shape = RoundedCornerShape(12.dp), color = MovGreen.copy(alpha = 0.97f)) {
                    Row(
                        modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Filled.CheckCircle, null, tint = Color.Black, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(8.dp))
                        Text(successMsg, color = Color.Black, fontWeight = FontWeight.Bold)
                    }
                }
            }

            // Error banner
            AnimatedVisibility(
                visible = errorMsg.isNotEmpty(),
                enter = fadeIn(), exit = fadeOut(),
                modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 80.dp, start = 16.dp, end = 16.dp)
            ) {
                Surface(shape = RoundedCornerShape(12.dp), color = MovRed.copy(alpha = 0.97f)) {
                    Row(
                        modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Filled.Warning, null, tint = Color.White, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(8.dp))
                        Text(errorMsg, color = Color.White, fontWeight = FontWeight.Medium)
                    }
                }
            }
        }
    }
}

@Composable
private fun AdminMoviePoster(movie: Movie, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(0.67f)
            .clip(RoundedCornerShape(8.dp))
            .clickable { onClick() }
    ) {
        // Poster image via GET /api/movies/{id}/poster (used as URL directly)
        if (!movie.posterUrl.isNullOrEmpty()) {
            AsyncImage(
                model = movie.posterUrl,
                contentDescription = movie.title,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
        } else {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.linearGradient(listOf(Color(0xFF1C1C1C), Color(0xFF0A0A0A)))
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    movie.title.take(1).uppercase(),
                    fontSize = 28.sp, fontWeight = FontWeight.Black, color = Color(0xFF2A2A2A)
                )
            }
        }

        // Gradient overlay
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(Color.Transparent, Color.Transparent, Color.Black.copy(alpha = 0.85f)),
                        startY = 80f
                    )
                )
        )

        // Title
        Text(
            text = movie.title,
            color = Color.White,
            fontSize = 9.sp,
            fontWeight = FontWeight.Bold,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(horizontal = 4.dp, vertical = 5.dp)
        )
    }
}