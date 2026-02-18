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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.pppp.data.datastore.TokenDataStore
import com.example.pppp.data.remote.Retrofit
import com.example.pppp.data.remote.dataclass.ReviewRequest
import com.example.pppp.data.repository.MoviesRepository
import com.example.pppp.viewmodel.MoviesViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

private val BgDark   = Color(0xFF0A0A0A)
private val Surface1 = Color(0xFF141414)
private val Surface2 = Color(0xFF1E1E1E)
private val Green    = Color(0xFF00C030)
private val StarAmber = Color(0xFFFFAA00)
private val TextPri  = Color(0xFFF0F0F0)
private val TextSec  = Color(0xFF909090)
private val TextMut  = Color(0xFF505050)
private val ErrorRed = Color(0xFFFF4444)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MovieDetailScreen(
    movieId: Long,
    onBack: () -> Unit = {},
    viewModel: MoviesViewModel = viewModel(factory = object : androidx.lifecycle.ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T =
            MoviesViewModel(MoviesRepository(Retrofit.Movies)) as T
    })
) {
    val movieDetails  by viewModel.movieDetails.collectAsState()
    val movieFiles    by viewModel.movieFiles.collectAsState()
    val movieReviews  by viewModel.movieReviews.collectAsState()
    val reviewPostResult by viewModel.reviewPostResult.collectAsState()

    val context = LocalContext.current
    val tokenDataStore = remember { TokenDataStore(context) }

    var token  by remember { mutableStateOf<String?>(null) }
    var userId by remember { mutableStateOf<Long?>(null) }
    var reviewText by remember { mutableStateOf("") }
    var rating  by remember { mutableIntStateOf(0) }
    var showSuccess by remember { mutableStateOf(false) }
    var showError   by remember { mutableStateOf(false) }
    var errorMsg    by remember { mutableStateOf("") }
    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        token  = tokenDataStore.getAccessToken().first()
        userId = tokenDataStore.getUserId().first()?.toLongOrNull()
    }
    LaunchedEffect(movieId) {
        if (movieId > 0) {
            viewModel.getMovieDetails(movieId)
            viewModel.getMovieFiles(movieId)
            viewModel.getMovieReviews(movieId)
        }
    }
    LaunchedEffect(movieId, token) {
        if (!token.isNullOrBlank() && movieId > 0) {
            viewModel.getMovieReviews(movieId)
        }
    }
    LaunchedEffect(reviewPostResult) {
        val result = reviewPostResult ?: return@LaunchedEffect
        if (result.isSuccessful) {
            showSuccess = true
            reviewText  = ""
            rating       = 0
            if (!token.isNullOrBlank()) viewModel.getMovieReviews(movieId)
            delay(2500)
            showSuccess = false
        } else {
            val body = result.errorBody()?.string()
            errorMsg = try {
                org.json.JSONObject(body ?: "").optString("message", body ?: result.message())
            } catch (_: Exception) { body ?: result.message() }
            showError = true
            delay(3000)
            showError = false
        }
    }

    Scaffold(
        containerColor = BgDark,
        topBar = {
            TopAppBar(
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Box(
                            modifier = Modifier.size(36.dp).background(Surface2, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, "Volver", tint = TextPri, modifier = Modifier.size(20.dp))
                        }
                    }
                },
                title = {},
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        }
    ) { padding ->

        when {
            movieId <= 0L -> {
                Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                    Text("ID de película inválido", color = ErrorRed)
                }
            }
            movieDetails == null -> {
                Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = Green, strokeWidth = 2.dp)
                }
            }
            movieDetails?.isSuccessful == true -> {
                val movie = movieDetails?.body() ?: return@Scaffold
                val files = if (movieFiles?.isSuccessful == true) movieFiles?.body() else null
                val reviews = if (movieReviews?.isSuccessful == true)
                    movieReviews?.body() ?: emptyList() else emptyList()

                // Feedback snackbars
                Box(Modifier.fillMaxSize()) {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize().padding(padding)
                    ) {

                        // ── Hero ──────────────────────────────────────────
                        item {
                            Box(
                                modifier = Modifier.fillMaxWidth().height(320.dp)
                            ) {
                                // Backdrop / background image
                                if (!movie.posterUrl.isNullOrEmpty()) {
                                    AsyncImage(
                                        model = movie.posterUrl,
                                        contentDescription = null,
                                        modifier = Modifier.fillMaxSize(),
                                        contentScale = ContentScale.Crop,
                                        alpha = 0.25f
                                    )
                                } else {
                                    Box(
                                        Modifier.fillMaxSize()
                                            .background(Brush.verticalGradient(listOf(Color(0xFF1A1A1A), BgDark)))
                                    )
                                }
                                // Gradient fade to background
                                Box(
                                    Modifier.fillMaxSize()
                                        .background(
                                            Brush.verticalGradient(listOf(Color.Transparent, BgDark))
                                        )
                                )

                                // Poster + info
                                Row(
                                    modifier = Modifier
                                        .align(Alignment.BottomStart)
                                        .fillMaxWidth()
                                        .padding(16.dp),
                                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                                    verticalAlignment = Alignment.Bottom
                                ) {
                                    // Poster
                                    Card(
                                        modifier = Modifier.width(110.dp).height(165.dp),
                                        shape = RoundedCornerShape(10.dp),
                                        elevation = CardDefaults.cardElevation(16.dp)
                                    ) {
                                        if (!movie.posterUrl.isNullOrEmpty()) {
                                            AsyncImage(
                                                model = movie.posterUrl,
                                                contentDescription = movie.title,
                                                modifier = Modifier.fillMaxSize(),
                                                contentScale = ContentScale.Crop
                                            )
                                        } else {
                                            Box(
                                                Modifier.fillMaxSize()
                                                    .background(Brush.linearGradient(listOf(Color(0xFF222222), Color(0xFF111111)))),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Icon(Icons.Filled.Movie, null, tint = Color(0xFF2A2A2A), modifier = Modifier.size(40.dp))
                                            }
                                        }
                                    }

                                    // Info
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            movie.title,
                                            color = TextPri,
                                            fontSize = 22.sp,
                                            fontWeight = FontWeight.Black,
                                            lineHeight = 26.sp,
                                            maxLines = 3,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                        Spacer(Modifier.height(8.dp))
                                        Row(
                                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            if (!movie.overview.isNullOrEmpty()) {
                                                Surface(
                                                    shape = RoundedCornerShape(4.dp),
                                                    color = Surface2
                                                ) {
                                                    Text(
                                                        "Ver sinopsis",
                                                        color = TextSec,
                                                        fontSize = 11.sp,
                                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        // ── Sinopsis ──────────────────────────────────────
                        if (!movie.overview.isNullOrEmpty()) {
                            item {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 16.dp)
                                        .padding(top = 4.dp, bottom = 20.dp)
                                ) {
                                    SectionLabel("Sinopsis")
                                    Spacer(Modifier.height(8.dp))
                                    Text(
                                        movie.overview,
                                        color = TextSec,
                                        fontSize = 14.sp,
                                        lineHeight = 22.sp
                                    )
                                }
                                DetailDivider()
                            }
                        }

                        // ── Archivos ──────────────────────────────────────
                        if (!files?.files.isNullOrEmpty()) {
                            item {
                                Column(
                                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 16.dp)
                                ) {
                                    SectionLabel("Archivos")
                                    Spacer(Modifier.height(10.dp))
                                }
                            }
                            items(files.files) { file ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 16.dp, vertical = 6.dp)
                                        .background(Surface1, RoundedCornerShape(8.dp))
                                        .padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(Icons.Filled.PlayArrow, null, tint = Green, modifier = Modifier.size(20.dp))
                                    Spacer(Modifier.width(10.dp))
                                    Column(Modifier.weight(1f)) {
                                        Text(file.name, color = TextPri, fontSize = 13.sp, fontWeight = FontWeight.Medium, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                        file.size?.let { Text("${it / 1024 / 1024} MB", color = TextMut, fontSize = 11.sp) }
                                    }
                                    Icon(Icons.Filled.Download, null, tint = TextSec, modifier = Modifier.size(18.dp))
                                }
                            }
                            item { DetailDivider(); Spacer(Modifier.height(4.dp)) }
                        }

                        // ── Reviews ───────────────────────────────────────
                        item {
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 16.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                SectionLabel("Reseñas")
                                if (reviews.isNotEmpty()) {
                                    Text(
                                        "${reviews.size} reseñas",
                                        color = TextMut,
                                        fontSize = 12.sp
                                    )
                                }
                            }
                        }

                        if (reviews.isEmpty()) {
                            item {
                                Box(
                                    modifier = Modifier.fillMaxWidth().padding(vertical = 20.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Icon(Icons.Filled.RateReview, null, tint = TextMut, modifier = Modifier.size(32.dp))
                                        Spacer(Modifier.height(8.dp))
                                        Text("Sin reseñas aún", color = TextMut, fontSize = 13.sp)
                                        Text("Sé el primero en comentar", color = TextMut, fontSize = 11.sp)
                                    }
                                }
                            }
                        } else {
                            items(reviews) { review ->
                                ReviewItem(
                                    username = review.user?.username ?: "Usuario",
                                    text = review.text ?: "",
                                    stars = review.stars ?: 0,
                                )
                            }
                        }

                        item { DetailDivider(); Spacer(Modifier.height(4.dp)) }

                        // ── Write review ──────────────────────────────────
                        item {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp)
                            ) {
                                SectionLabel("Tu reseña")
                                Spacer(Modifier.height(14.dp))

                                // Star selector
                                Row(
                                    horizontalArrangement = Arrangement.Center,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    repeat(5) { index ->
                                        val filled = index < rating
                                        IconButton(
                                            onClick = {
                                                rating = if (filled && index == rating - 1) 0 else index + 1
                                            },
                                            modifier = Modifier.size(40.dp)
                                        ) {
                                            Icon(
                                                imageVector = if (filled) Icons.Filled.Star else Icons.Filled.StarBorder,
                                                contentDescription = null,
                                                tint = if (filled) StarAmber else Color(0xFF3A3A3A),
                                                modifier = Modifier.size(28.dp)
                                            )
                                        }
                                    }
                                }
                                if (rating > 0) {
                                    Text(
                                        text = ratingText(rating),
                                        color = StarAmber,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Medium,
                                        modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                    )
                                }

                                Spacer(Modifier.height(10.dp))

                                // Text field
                                OutlinedTextField(
                                    value = reviewText,
                                    onValueChange = { reviewText = it },
                                    modifier = Modifier.fillMaxWidth(),
                                    placeholder = { Text("¿Qué te pareció?", color = TextMut, fontSize = 14.sp) },
                                    minLines = 3,
                                    maxLines = 6,
                                    shape = RoundedCornerShape(10.dp),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor   = Green,
                                        unfocusedBorderColor = Color(0xFF2A2A2A),
                                        focusedTextColor     = TextPri,
                                        unfocusedTextColor   = TextPri,
                                        cursorColor          = Green,
                                        focusedContainerColor   = Surface1,
                                        unfocusedContainerColor = Surface1
                                    )
                                )

                                Spacer(Modifier.height(12.dp))

                                Button(
                                    onClick = {
                                        scope.launch {
                                            if (reviewText.isBlank() || rating == 0) {
                                                errorMsg  = "Escribe tu reseña y selecciona una puntuación."
                                                showError = true
                                                delay(3000)
                                                showError = false
                                                return@launch
                                            }
                                            if (token.isNullOrBlank() || userId == null) {
                                                errorMsg  = "Debes iniciar sesión para publicar una reseña."
                                                showError = true
                                                delay(3000)
                                                showError = false
                                                return@launch
                                            }
                                            viewModel.postReview(
                                                ReviewRequest(userId!!, movieId, reviewText, rating),
                                                "Bearer $token"
                                            )
                                        }
                                    },
                                    modifier = Modifier.fillMaxWidth().height(48.dp),
                                    shape  = RoundedCornerShape(10.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = Green)
                                ) {
                                    Icon(Icons.AutoMirrored.Filled.Send, null, tint = Color.Black, modifier = Modifier.size(18.dp))
                                    Spacer(Modifier.width(8.dp))
                                    Text("Publicar reseña", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                }
                            }
                        }

                        item { Spacer(Modifier.height(40.dp)) }
                    }

                    // Feedback overlays
                    AnimatedVisibility(
                        visible = showSuccess,
                        enter   = fadeIn(),
                        exit    = fadeOut(),
                        modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 24.dp)
                    ) {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = Green.copy(alpha = 0.95f),
                            shadowElevation = 8.dp
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Filled.CheckCircle, null, tint = Color.Black, modifier = Modifier.size(18.dp))
                                Spacer(Modifier.width(8.dp))
                                Text("¡Reseña publicada!", color = Color.Black, fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    AnimatedVisibility(
                        visible = showError,
                        enter   = fadeIn(),
                        exit    = fadeOut(),
                        modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 24.dp)
                    ) {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = ErrorRed.copy(alpha = 0.95f),
                            shadowElevation = 8.dp
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Filled.Warning, null, tint = Color.White, modifier = Modifier.size(18.dp))
                                Spacer(Modifier.width(8.dp))
                                Text(errorMsg, color = Color.White, fontWeight = FontWeight.Medium, maxLines = 2)
                            }
                        }
                    }
                }
            }
            else -> {
                Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Filled.ErrorOutline, null, tint = ErrorRed, modifier = Modifier.size(48.dp))
                        Spacer(Modifier.height(12.dp))
                        Text("No se pudo cargar la película", color = TextSec)
                        Spacer(Modifier.height(16.dp))
                        OutlinedButton(
                            onClick = { viewModel.getMovieDetails(movieId) },
                            border  = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF2A2A2A))
                        ) {
                            Text("Reintentar", color = TextSec)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ReviewItem(username: String, text: String, stars: Int) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .background(Surface1, RoundedCornerShape(10.dp))
            .padding(14.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(30.dp)
                        .background(Color(0xFF1E1E1E), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(username.take(1).uppercase(), color = Green, fontSize = 12.sp, fontWeight = FontWeight.Black)
                }
                Spacer(Modifier.width(8.dp))
                Text(username, color = TextPri, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
            }
            Row {
                repeat(stars) {
                    Icon(Icons.Filled.Star, null, tint = StarAmber, modifier = Modifier.size(13.dp))
                }
                repeat(5 - stars) {
                    Icon(Icons.Filled.StarBorder, null, tint = Color(0xFF2A2A2A), modifier = Modifier.size(13.dp))
                }
            }
        }
        if (text.isNotBlank()) {
            Spacer(Modifier.height(8.dp))
            Text(text, color = TextSec, fontSize = 13.sp, lineHeight = 20.sp)
        }
    }
}

@Composable
private fun SectionLabel(text: String) {
    Text(text, color = TextPri, fontSize = 15.sp, fontWeight = FontWeight.Bold, letterSpacing = 0.3.sp)
}

@Composable
private fun DetailDivider() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(1.dp)
            .padding(horizontal = 16.dp)
            .background(Color(0xFF1E1E1E))
    )
}

private fun ratingText(stars: Int) = when (stars) {
    1 -> "★  Mala"
    2 -> "★★  Regular"
    3 -> "★★★  Buena"
    4 -> "★★★★  Muy buena"
    5 -> "★★★★★  Obra maestra"
    else -> ""
}