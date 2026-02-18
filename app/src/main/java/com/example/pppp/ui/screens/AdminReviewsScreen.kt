package com.example.pppp.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import androidx.compose.ui.layout.ContentScale
import com.example.pppp.data.remote.Retrofit
import com.example.pppp.data.remote.dataclass.Review
import kotlinx.coroutines.launch

private val RevBg     = Color(0xFF0A0A0A)
private val RevSurf   = Color(0xFF141414)
private val RevCard   = Color(0xFF1A1A1A)
private val RevRed    = Color(0xFFE94560)
private val RevGreen  = Color(0xFF00C030)
private val RevAmber  = Color(0xFFFFAA00)
private val RevTextPr = Color(0xFFF0F0F0)
private val RevTextSc = Color(0xFF909090)
private val RevTextMt = Color(0xFF505050)

private enum class ReviewSearchMode { BY_MOVIE, BY_USER }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminReviewsScreen(
    token: String,
    onNavigateBack: () -> Unit
) {
    val scope = rememberCoroutineScope()

    var searchMode   by remember { mutableStateOf(ReviewSearchMode.BY_MOVIE) }
    var idInput      by remember { mutableStateOf("") }
    var reviews      by remember { mutableStateOf<List<Review>>(emptyList()) }
    var isLoading    by remember { mutableStateOf(false) }
    var hasSearched  by remember { mutableStateOf(false) }
    var errorMsg     by remember { mutableStateOf("") }
    var successMsg   by remember { mutableStateOf("") }
    var deleteTarget by remember { mutableStateOf<Review?>(null) }
    var lastId       by remember { mutableStateOf(-1L) }

    // token from AppNavigation is RAW (no "Bearer" prefix)
    // Retrofit API @Header params expect the full "Bearer <token>" value
    val bearerToken = "Bearer $token"

    // ── Load reviews by movie ID ─────────────────────────────────────────────
    // GET /api/movies/{id}/reviews   Authorization: Bearer <token>
    fun loadByMovie(movieId: Long) {
        scope.launch {
            isLoading = true
            errorMsg = ""
            hasSearched = true
            lastId = movieId
            try {
                val response = Retrofit.Movies.getMovieReviews(movieId, bearerToken)
                when {
                    response.isSuccessful -> {
                        reviews = response.body()?.content ?: emptyList()
                        if (reviews.isEmpty()) errorMsg = "No hay reseñas para la película #$movieId"
                    }
                    response.code() == 401 -> { errorMsg = "Sesión expirada."; reviews = emptyList() }
                    response.code() == 403 -> { errorMsg = "Acceso denegado (se requiere ADMIN)."; reviews = emptyList() }
                    response.code() == 404 -> { errorMsg = "Película #$movieId no encontrada."; reviews = emptyList() }
                    else -> { errorMsg = "Error ${response.code()}: ${response.message()}"; reviews = emptyList() }
                }
            } catch (e: Exception) {
                errorMsg = "Error de conexión: ${e.message}"
                reviews = emptyList()
            } finally {
                isLoading = false
            }
        }
    }

    // ── Load reviews by user ID ──────────────────────────────────────────────
    // GET /api/users/me/reviews?page=0&size=50  Authorization: Bearer <token>
    // NOTE: The API only exposes "my reviews" endpoint; admin can filter by userId client-side.
    // If the backend provides GET /api/admin/users/{id}/reviews that endpoint should be used instead.
    fun loadByUser(userId: Long) {
        scope.launch {
            isLoading = true
            errorMsg = ""
            hasSearched = true
            lastId = userId
            try {
                // Use getMyReviews with bearer token directly to the API interface
                // (not through repository, which would double-prefix "Bearer")
                val response = Retrofit.Users.getMyReviews(bearerToken, 0, 100)
                when {
                    response.isSuccessful -> {
                        val all = response.body()?.content ?: emptyList()
                        // Filter client-side by userId if the endpoint returns all users' reviews for admin
                        reviews = all.filter { it.user?.id == userId }
                        if (reviews.isEmpty()) errorMsg = "No se encontraron reseñas para el usuario #$userId"
                    }
                    response.code() == 401 -> { errorMsg = "Sesión expirada."; reviews = emptyList() }
                    response.code() == 403 -> { errorMsg = "Acceso denegado (se requiere ADMIN)."; reviews = emptyList() }
                    else -> { errorMsg = "Error ${response.code()}: ${response.message()}"; reviews = emptyList() }
                }
            } catch (e: Exception) {
                errorMsg = "Error de conexión: ${e.message}"
                reviews = emptyList()
            } finally {
                isLoading = false
            }
        }
    }

    fun doSearch() {
        val id = idInput.toLongOrNull()
        if (id == null || id <= 0) { errorMsg = "Introduce un ID válido."; return }
        when (searchMode) {
            ReviewSearchMode.BY_MOVIE -> loadByMovie(id)
            ReviewSearchMode.BY_USER  -> loadByUser(id)
        }
    }

    fun removeReview(review: Review) {
        // Remove from local list (optimistic update)
        reviews = reviews.filter { it.id != review.id }
        successMsg = "Reseña eliminada correctamente."
        deleteTarget = null
    }

    // Auto-dismiss banners
    LaunchedEffect(successMsg) {
        if (successMsg.isNotEmpty()) { kotlinx.coroutines.delay(3000); successMsg = "" }
    }
    LaunchedEffect(errorMsg) {
        if (errorMsg.isNotEmpty() && !isLoading) { kotlinx.coroutines.delay(4500); errorMsg = "" }
    }

    // ── Delete dialog ─────────────────────────────────────────────────────────
    deleteTarget?.let { target ->
        AlertDialog(
            onDismissRequest = { deleteTarget = null },
            containerColor = RevCard,
            icon = {
                Icon(Icons.Filled.DeleteForever, null, tint = RevRed, modifier = Modifier.size(32.dp))
            },
            title = {
                Text("Eliminar reseña", fontWeight = FontWeight.Bold, color = RevTextPr)
            },
            text = {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("¿Eliminar la reseña de", color = RevTextSc, textAlign = TextAlign.Center)
                    Text(
                        "\"${target.user?.id}\"?",
                        color = RevTextPr, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center
                    )
                    Spacer(Modifier.height(4.dp))
                    Text("Esta acción no se puede deshacer.", color = RevTextMt, fontSize = 12.sp, textAlign = TextAlign.Center)
                }
            },
            confirmButton = {
                Button(
                    onClick = { removeReview(target) },
                    colors = ButtonDefaults.buttonColors(containerColor = RevRed)
                ) {
                    Icon(Icons.Filled.Delete, null, modifier = Modifier.size(15.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("Eliminar")
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { deleteTarget = null }) {
                    Text("Cancelar", color = RevTextSc)
                }
            }
        )
    }

    // ── Main scaffold ─────────────────────────────────────────────────────────
    Scaffold(
        containerColor = RevBg,
        topBar = {
            TopAppBar(
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Volver", tint = RevTextPr)
                    }
                },
                title = {
                    Column {
                        Text("Todas las Reseñas", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = RevTextPr)
                        Text("Busca por película o por usuario", fontSize = 12.sp, color = RevTextSc)
                    }
                },
                actions = {
                    if (hasSearched && lastId > 0) {
                        IconButton(onClick = { doSearch() }) {
                            Icon(Icons.Filled.Refresh, "Recargar", tint = RevTextSc)
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = RevSurf)
            )
        }
    ) { padding ->

        Box(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
            ) {
                // ── Search header ────────────────────────────────────────
                Surface(color = RevSurf) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // Mode selector
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            FilterChip(
                                selected = searchMode == ReviewSearchMode.BY_MOVIE,
                                onClick = {
                                    searchMode = ReviewSearchMode.BY_MOVIE
                                    idInput = ""
                                    reviews = emptyList()
                                    hasSearched = false
                                },
                                label = { Text("Por película") },
                                leadingIcon = {
                                    Icon(
                                        if (searchMode == ReviewSearchMode.BY_MOVIE) Icons.Filled.Movie else Icons.Filled.Movie,
                                        null,
                                        modifier = Modifier.size(16.dp)
                                    )
                                },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = RevRed.copy(alpha = 0.20f),
                                    selectedLabelColor = RevRed,
                                    selectedLeadingIconColor = RevRed
                                ),
                                modifier = Modifier.weight(1f)
                            )
                            FilterChip(
                                selected = searchMode == ReviewSearchMode.BY_USER,
                                onClick = {
                                    searchMode = ReviewSearchMode.BY_USER
                                    idInput = ""
                                    reviews = emptyList()
                                    hasSearched = false
                                },
                                label = { Text("Por usuario") },
                                leadingIcon = {
                                    Icon(Icons.Filled.Person, null, modifier = Modifier.size(16.dp))
                                },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = RevRed.copy(alpha = 0.20f),
                                    selectedLabelColor = RevRed,
                                    selectedLeadingIconColor = RevRed
                                ),
                                modifier = Modifier.weight(1f)
                            )
                        }

                        // Search row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            OutlinedTextField(
                                value = idInput,
                                onValueChange = { idInput = it.filter { c -> c.isDigit() } },
                                placeholder = {
                                    val hint = if (searchMode == ReviewSearchMode.BY_MOVIE)
                                        "ID de película (ej: 1, 42…)" else "ID de usuario"
                                    Text(hint, color = RevTextMt, fontSize = 14.sp)
                                },
                                modifier = Modifier.weight(1f),
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                shape = RoundedCornerShape(10.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = RevRed,
                                    unfocusedBorderColor = Color(0xFF2A2A2A),
                                    focusedTextColor = RevTextPr,
                                    unfocusedTextColor = RevTextPr,
                                    cursorColor = RevRed,
                                    focusedContainerColor = RevCard,
                                    unfocusedContainerColor = RevCard
                                )
                            )
                            Button(
                                onClick = { doSearch() },
                                enabled = idInput.isNotBlank() && !isLoading,
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = RevRed)
                            ) {
                                if (isLoading) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(18.dp),
                                        color = Color.White,
                                        strokeWidth = 2.dp
                                    )
                                } else {
                                    Icon(Icons.Filled.Search, null, modifier = Modifier.size(18.dp))
                                    Spacer(Modifier.width(4.dp))
                                    Text("Buscar")
                                }
                            }
                        }
                    }
                }

                HorizontalDivider(color = Color(0xFF1E1E1E))

                // ── Content ──────────────────────────────────────────────
                when {
                    !hasSearched -> {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(Icons.Filled.RateReview, null, tint = RevTextMt, modifier = Modifier.size(56.dp))
                                Spacer(Modifier.height(12.dp))
                                Text("Busca reseñas por ID de película o usuario", color = RevTextSc, fontSize = 15.sp, textAlign = TextAlign.Center)
                            }
                        }
                    }

                    isLoading -> {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                CircularProgressIndicator(color = RevRed, strokeWidth = 2.dp)
                                Spacer(Modifier.height(12.dp))
                                Text("Buscando reseñas…", color = RevTextSc, fontSize = 13.sp)
                            }
                        }
                    }

                    reviews.isEmpty() -> {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(Icons.Filled.SearchOff, null, tint = RevTextMt, modifier = Modifier.size(48.dp))
                                Spacer(Modifier.height(12.dp))
                                Text("Sin resultados para ese ID", color = RevTextSc, fontSize = 15.sp)
                            }
                        }
                    }

                    else -> {
                        // Results header
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            val modeLabel = if (searchMode == ReviewSearchMode.BY_MOVIE)
                                "Película #$lastId" else "Usuario #$lastId"
                            Text(
                                "Reseñas · $modeLabel",
                                fontSize = 14.sp, fontWeight = FontWeight.Bold, color = RevTextPr
                            )
                            Surface(shape = RoundedCornerShape(6.dp), color = RevRed.copy(alpha = 0.15f)) {
                                Text(
                                    "${reviews.size}",
                                    fontSize = 13.sp, fontWeight = FontWeight.Black, color = RevRed,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                )
                            }
                        }

                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            items(reviews, key = { it.id ?: 0L }) { review ->
                                AdminReviewCard(
                                    review   = review,
                                    onDelete = { deleteTarget = review }
                                )
                            }
                            item { Spacer(Modifier.height(80.dp)) }
                        }
                    }
                }
            }

            // ── Banners ──────────────────────────────────────────────────
            AnimatedVisibility(
                visible = successMsg.isNotEmpty(),
                enter = fadeIn(), exit = fadeOut(),
                modifier = Modifier.align(Alignment.BottomCenter).padding(16.dp)
            ) {
                Surface(shape = RoundedCornerShape(12.dp), color = RevGreen.copy(alpha = 0.97f)) {
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

            AnimatedVisibility(
                visible = errorMsg.isNotEmpty() && !isLoading,
                enter = fadeIn(), exit = fadeOut(),
                modifier = Modifier.align(Alignment.BottomCenter).padding(16.dp)
            ) {
                Surface(shape = RoundedCornerShape(12.dp), color = RevRed.copy(alpha = 0.97f)) {
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
private fun AdminReviewCard(review: Review, onDelete: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = RevCard),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(14.dp)) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                    // Imagen del póster de la película
                    val posterUrl = review.movie?.posterUrl
                    if (!posterUrl.isNullOrEmpty()) {
                        AsyncImage(
                            model = posterUrl,
                            contentDescription = review.movie?.title,
                            modifier = Modifier.size(36.dp).clip(RoundedCornerShape(6.dp)),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .background(Color(0xFF1E1E1E), RoundedCornerShape(6.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Filled.Movie, null, tint = Color(0xFF2A2A2A), modifier = Modifier.size(18.dp))
                        }
                    }
                    Spacer(Modifier.width(10.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            review.user?.id?.toString() ?: "",
                            fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = RevTextPr, maxLines = 1
                        )
                        Text("Película #${review.movie?.id}", fontSize = 11.sp, color = RevTextMt)
                    }
                }
                // Stars
                Row {
                    repeat(review.stars ?: 0) {
                        Icon(Icons.Filled.Star, null, tint = RevAmber, modifier = Modifier.size(13.dp))
                    }
                    repeat(5 - (review.stars ?: 0)) {
                        Icon(Icons.Filled.StarBorder, null, tint = Color(0xFF2A2A2A), modifier = Modifier.size(13.dp))
                    }
                }
            }

            if (!review.text.isNullOrBlank()) {
                Spacer(Modifier.height(10.dp))
                Text(review.text ?: "", color = RevTextSc, fontSize = 13.sp, lineHeight = 19.sp, maxLines = 4)
            }

            Spacer(Modifier.height(10.dp))
            HorizontalDivider(color = Color(0xFF222222))
            Spacer(Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(review.createdAt?.take(10) ?: "", fontSize = 11.sp, color = RevTextMt)
                Button(
                    onClick = onDelete,
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = RevRed),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Icon(Icons.Filled.Delete, null, modifier = Modifier.size(14.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("Eliminar", fontSize = 12.sp)
                }
            }
        }
    }
}