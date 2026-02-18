package com.example.pppp.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.pppp.data.remote.Retrofit
import com.example.pppp.data.remote.dataclass.Review
import kotlinx.coroutines.launch

private val ModBg     = Color(0xFF0A0A0A)
private val ModSurf   = Color(0xFF141414)
private val ModCard   = Color(0xFF1A1A1A)
private val ModRed    = Color(0xFFE94560)
private val ModGreen  = Color(0xFF00C030)
private val ModAmber  = Color(0xFFFFAA00)
private val ModTextPr = Color(0xFFF0F0F0)
private val ModTextSc = Color(0xFF909090)
private val ModTextMt = Color(0xFF505050)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminModerationScreen(
    token: String,
    onNavigateBack: () -> Unit
) {
    val scope = rememberCoroutineScope()

    var movieIdInput by remember { mutableStateOf("") }
    var reviews by remember { mutableStateOf<List<Review>>(emptyList()) }
    var isLoading by remember { mutableStateOf(false) }
    var errorMsg by remember { mutableStateOf("") }
    var successMsg by remember { mutableStateOf("") }
    var hasSearched by remember { mutableStateOf(false) }
    var currentMovieId by remember { mutableLongStateOf(-1L) }

    var deleteTarget by remember { mutableStateOf<Review?>(null) }

    // token from AppNavigation is RAW → we prefix "Bearer " for @Header params
    // MoviesApi.getMovieReviews receives the full Authorization header value
    val bearerToken = "Bearer $token"

    fun loadReviews(movieId: Long) {
        scope.launch {
            isLoading = true
            errorMsg = ""
            hasSearched = true
            currentMovieId = movieId
            try {
                // GET /api/movies/{id}/reviews  with Authorization: Bearer <token>
                val response = Retrofit.Movies.getMovieReviews(movieId, bearerToken)
                when {
                    response.isSuccessful -> {
                        reviews = response.body()?.content ?: emptyList()
                        if (reviews.isEmpty()) errorMsg = "No hay reseñas para la película #$movieId"
                    }
                    response.code() == 401 -> {
                        errorMsg = "Sesión expirada. Vuelve a iniciar sesión."
                        reviews = emptyList()
                    }
                    response.code() == 403 -> {
                        errorMsg = "Acceso denegado. Se requiere rol ADMIN."
                        reviews = emptyList()
                    }
                    response.code() == 404 -> {
                        errorMsg = "Película #$movieId no encontrada."
                        reviews = emptyList()
                    }
                    else -> {
                        errorMsg = "Error ${response.code()}: ${response.message()}"
                        reviews = emptyList()
                    }
                }
            } catch (e: Exception) {
                errorMsg = "Error de conexión: ${e.message}"
                reviews = emptyList()
            } finally {
                isLoading = false
            }
        }
    }

    fun removeReview(review: Review) {

        reviews = reviews.filter { it.id != review.id }
        successMsg = "Reseña de \"${review.user?.id}\" eliminada."
        deleteTarget = null
    }

    // Auto-dismiss banners
    LaunchedEffect(successMsg) {
        if (successMsg.isNotEmpty()) { kotlinx.coroutines.delay(3000); successMsg = "" }
    }
    LaunchedEffect(errorMsg) {
        if (errorMsg.isNotEmpty() && !isLoading) { kotlinx.coroutines.delay(4500); errorMsg = "" }
    }

    // ── Delete confirmation dialog ───────────────────────────────────────────
    deleteTarget?.let { target ->
        AlertDialog(
            onDismissRequest = { deleteTarget = null },
            containerColor = ModCard,
            icon = {
                Icon(
                    Icons.Filled.DeleteForever,
                    null,
                    tint = ModRed,
                    modifier = Modifier.size(32.dp)
                )
            },
            title = {
                Text("Eliminar reseña", fontWeight = FontWeight.Bold, color = ModTextPr)
            },
            text = {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        "¿Eliminar la reseña de",
                        color = ModTextSc,
                        textAlign = TextAlign.Center
                    )
                    Text(
                        "\"${target.user?.id}\"?",
                        color = ModTextPr,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        "Esta acción no se puede deshacer.",
                        color = ModTextMt,
                        fontSize = 12.sp,
                        textAlign = TextAlign.Center
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = { removeReview(target) },
                    colors = ButtonDefaults.buttonColors(containerColor = ModRed)
                ) {
                    Icon(Icons.Filled.Delete, null, modifier = Modifier.size(15.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("Eliminar")
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { deleteTarget = null }) {
                    Text("Cancelar", color = ModTextSc)
                }
            }
        )
    }

    // ── Main scaffold ────────────────────────────────────────────────────────
    Scaffold(
        containerColor = ModBg,
        topBar = {
            TopAppBar(
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Volver", tint = ModTextPr)
                    }
                },
                title = {
                    Column {
                        Text(
                            "Moderación",
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            color = ModTextPr
                        )
                        Text("Gestión de reseñas por película", fontSize = 12.sp, color = ModTextSc)
                    }
                },
                actions = {
                    if (currentMovieId > 0) {
                        IconButton(onClick = { loadReviews(currentMovieId) }) {
                            Icon(Icons.Filled.Refresh, "Recargar", tint = ModTextSc)
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = ModSurf)
            )
        }
    ) { padding ->

        Box(modifier = Modifier.fillMaxSize()) {

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
            ) {
                // ── Search bar ───────────────────────────────────────────
                Surface(color = ModSurf) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            "Introduce el ID de una película para ver y moderar sus reseñas",
                            fontSize = 12.sp,
                            color = ModTextMt
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            OutlinedTextField(
                                value = movieIdInput,
                                onValueChange = { movieIdInput = it.filter { c -> c.isDigit() } },
                                placeholder = {
                                    Text("ID de película (ej: 1, 42…)", color = ModTextMt, fontSize = 14.sp)
                                },
                                modifier = Modifier.weight(1f),
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                shape = RoundedCornerShape(10.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = ModRed,
                                    unfocusedBorderColor = Color(0xFF2A2A2A),
                                    focusedTextColor = ModTextPr,
                                    unfocusedTextColor = ModTextPr,
                                    cursorColor = ModRed,
                                    focusedContainerColor = ModCard,
                                    unfocusedContainerColor = ModCard
                                )
                            )
                            Button(
                                onClick = {
                                    val id = movieIdInput.toLongOrNull()
                                    when {
                                        id == null || id <= 0 -> errorMsg = "Introduce un ID de película válido."
                                        else -> loadReviews(id)
                                    }
                                },
                                enabled = movieIdInput.isNotBlank() && !isLoading,
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = ModRed)
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

                // ── Content area ─────────────────────────────────────────
                when {
                    !hasSearched -> {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(
                                    Icons.Filled.RateReview,
                                    null,
                                    tint = ModTextMt,
                                    modifier = Modifier.size(56.dp)
                                )
                                Spacer(Modifier.height(12.dp))
                                Text(
                                    "Introduce el ID de una película",
                                    color = ModTextSc,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Medium
                                )
                                Text(
                                    "para ver y moderar sus reseñas",
                                    color = ModTextMt,
                                    fontSize = 13.sp
                                )
                            }
                        }
                    }

                    isLoading -> {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                CircularProgressIndicator(color = ModRed, strokeWidth = 2.dp)
                                Spacer(Modifier.height(12.dp))
                                Text(
                                    "Cargando reseñas de película #${movieIdInput}…",
                                    color = ModTextSc,
                                    fontSize = 13.sp
                                )
                            }
                        }
                    }

                    reviews.isEmpty() -> {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(
                                    Icons.Filled.SearchOff,
                                    null,
                                    tint = ModTextMt,
                                    modifier = Modifier.size(48.dp)
                                )
                                Spacer(Modifier.height(12.dp))
                                Text(
                                    "Sin reseñas para la película #$currentMovieId",
                                    color = ModTextSc,
                                    fontSize = 15.sp
                                )
                            }
                        }
                    }

                    else -> {
                        // Header with count
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                "Reseñas · Película #$currentMovieId",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = ModTextPr
                            )
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = ModRed.copy(alpha = 0.15f)
                            ) {
                                Text(
                                    "${reviews.size}",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Black,
                                    color = ModRed,
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
                                ModerationReviewCard(
                                    review = review,
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
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(16.dp)
            ) {
                Surface(shape = RoundedCornerShape(12.dp), color = ModGreen.copy(alpha = 0.97f)) {
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
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(16.dp)
            ) {
                Surface(shape = RoundedCornerShape(12.dp), color = ModRed.copy(alpha = 0.97f)) {
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
private fun ModerationReviewCard(
    review: Review,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = ModCard),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
        ) {
            // Header: avatar + user + stars
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                    Box(
                        modifier = Modifier
                            .size(34.dp)
                            .background(Color(0xFF1E1E1E), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        val initial = (review.user?.id?.toString() ?: "").take(1).uppercase()
                        Text(
                            initial,
                            color = ModRed,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Black
                        )
                    }
                    Spacer(Modifier.width(8.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            review.user?.id?.toString() ?: "",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = ModTextPr,
                            maxLines = 1
                        )
                        Text(
                            "Review #${review.id}",
                            fontSize = 10.sp,
                            color = ModTextMt
                        )
                    }
                }
                // Stars
                Row {
                    repeat(review.stars ?: 0) {
                        Icon(Icons.Filled.Star, null, tint = ModAmber, modifier = Modifier.size(13.dp))
                    }
                    repeat(5 - (review.stars ?: 0)) {
                        Icon(Icons.Filled.StarBorder, null, tint = Color(0xFF2A2A2A), modifier = Modifier.size(13.dp))
                    }
                }
            }

            // Review text
            if (!review.text.isNullOrBlank()) {
                Spacer(Modifier.height(10.dp))
                Text(
                    review.text ?: "",
                    color = ModTextSc,
                    fontSize = 13.sp,
                    lineHeight = 19.sp,
                    maxLines = 5
                )
            }

            Spacer(Modifier.height(10.dp))
            HorizontalDivider(color = Color(0xFF222222))
            Spacer(Modifier.height(8.dp))

            // Footer: date + delete button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    review.createdAt?.take(10) ?: "",
                    fontSize = 11.sp,
                    color = ModTextMt
                )
                Button(
                    onClick = onDelete,
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = ModRed),
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