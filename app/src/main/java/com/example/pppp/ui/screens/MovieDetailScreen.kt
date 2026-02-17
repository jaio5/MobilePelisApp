package com.example.pppp.ui.screens

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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
import com.example.pppp.data.repository.MoviesRepository
import com.example.pppp.viewmodel.MoviesViewModel
import com.example.pppp.data.remote.dataclass.ReviewRequest
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

// Mejoras: Eliminar warnings, asegurar compatibilidad API, robustez en manejo de errores y validación de datos

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MovieDetailScreen(
    movieId: Long,
    onBack: () -> Unit = {},
    viewModel: MoviesViewModel = viewModel(factory = object : androidx.lifecycle.ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
            val moviesApi = Retrofit.Movies
            val repository = MoviesRepository(moviesApi)
            return MoviesViewModel(repository) as T
        }
    })
) {
    val movieDetails by viewModel.movieDetails.collectAsState()
    val movieFiles by viewModel.movieFiles.collectAsState()
    val movieReviews by viewModel.movieReviews.collectAsState()
    val reviewPostResult by viewModel.reviewPostResult.collectAsState()
    // Eliminar variable deserializationError no utilizada

    val context = LocalContext.current
    val tokenDataStore = remember { TokenDataStore(context) }

    var token by remember { mutableStateOf<String?>(null) }
    var userId by remember { mutableStateOf<Long?>(null) }
    var showSuccessMessage by remember { mutableStateOf(false) }
    var showErrorMessage by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }
    var text by remember { mutableStateOf("") }
    var stars by remember { mutableIntStateOf(0) }
    val scope = rememberCoroutineScope()

    // Cargar datos del usuario
    LaunchedEffect(Unit) {
        token = tokenDataStore.getAccessToken().first()
        val userIdStr = tokenDataStore.getUserId().first()
        userId = userIdStr?.toLongOrNull()
    }

    // Cargar detalles de la película
    LaunchedEffect(movieId) {
        if (movieId > 0) {
            viewModel.getMovieDetails(movieId)
            viewModel.getMovieFiles(movieId)
        }
    }

    // Cargar reseñas cuando tengamos el token
    LaunchedEffect(movieId, token) {
        if (!token.isNullOrBlank() && movieId > 0) {
            viewModel.getMovieReviews(movieId, "Bearer $token")
        }
    }

    // Manejar resultado de envío de reseña
    LaunchedEffect(reviewPostResult) {
        val result = reviewPostResult
        if (result != null) {
            try {
                if (result.isSuccessful) {
                    showSuccessMessage = true
                    if (!token.isNullOrBlank()) {
                        viewModel.getMovieReviews(movieId, "Bearer $token")
                    }
                    kotlinx.coroutines.delay(3000)
                    showSuccessMessage = false
                } else {
                    val errorRaw = result.errorBody()?.string()
                    val errorMsg = try {
                        val json = org.json.JSONObject(errorRaw ?: "")
                        json.optString("message", errorRaw ?: result.message())
                    } catch (e: Exception) {
                        Log.e("MOVIE_REVIEWS", "Error al extraer mensaje de error", e)
                        errorRaw ?: result.message()
                    }
                    showErrorMessage = true
                    errorMessage = errorMsg
                    kotlinx.coroutines.delay(3000)
                    showErrorMessage = false
                }
            } catch (e: Exception) {
                Log.e("MOVIE_REVIEWS", "Error de excepción", e)
                errorMessage = "Error inesperado: ${e.localizedMessage}"
                showErrorMessage = true
                kotlinx.coroutines.delay(3000)
                showErrorMessage = false
            }
        }
    }

    // Mostrar mensaje de error en la UI
    if (showErrorMessage) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.TopCenter) {
            Surface(color = MaterialTheme.colorScheme.error, shape = RoundedCornerShape(8.dp), shadowElevation = 8.dp) {
                Text(
                    text = errorMessage,
                    color = MaterialTheme.colorScheme.onError,
                    modifier = Modifier.padding(16.dp)
                )
            }
        }
    }
    if (showSuccessMessage) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.TopCenter) {
            Surface(color = Color(0xFF4CAF50), shape = RoundedCornerShape(8.dp), shadowElevation = 8.dp) {
                Text(
                    text = "¡Reseña enviada correctamente!",
                    color = Color.White,
                    modifier = Modifier.padding(16.dp)
                )
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Detalles de Película") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Volver")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { padding ->
        when {
            movieId <= 0L -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                    contentAlignment = Alignment.Center
                ) {
                    Text("ID de película inválido")
                }
            }
            movieDetails == null -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
            movieDetails?.isSuccessful == true -> {
                val movie = movieDetails?.body()
                if (movie != null) {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(padding)
                    ) {
                        // Hero section
                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(500.dp)
                            ) {
                                // Poster de fondo
                                if (movie.posterUrl != null) {
                                    AsyncImage(
                                        model = movie.posterUrl,
                                        contentDescription = movie.title,
                                        modifier = Modifier.fillMaxSize(),
                                        contentScale = ContentScale.Crop,
                                        alpha = 0.3f
                                    )
                                }

                                // Gradient overlay
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .background(
                                            Brush.verticalGradient(
                                                colors = listOf(
                                                    Color.Black.copy(alpha = 0.7f),
                                                    Color.Black.copy(alpha = 0.9f)
                                                )
                                            )
                                        )
                                )

                                // Contenido
                                Row(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .padding(24.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    // Poster principal
                                    Card(
                                        modifier = Modifier
                                            .width(180.dp)
                                            .height(270.dp),
                                        shape = RoundedCornerShape(16.dp),
                                        elevation = CardDefaults.cardElevation(12.dp)
                                    ) {
                                        if (movie.posterUrl != null) {
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
                                                        Brush.linearGradient(
                                                            colors = listOf(
                                                                Color(0xFF6366F1),
                                                                Color(0xFFEC4899)
                                                            )
                                                        )
                                                    ),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Text(
                                                    movie.title.take(1),
                                                    fontSize = 64.sp,
                                                    fontWeight = FontWeight.Black,
                                                    color = Color.White.copy(alpha = 0.3f)
                                                )
                                            }
                                        }
                                    }

                                    Spacer(modifier = Modifier.width(24.dp))

                                    // Información de la película
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = movie.title,
                                            fontSize = 28.sp,
                                            fontWeight = FontWeight.Black,
                                            color = Color.White,
                                            maxLines = 3,
                                            overflow = TextOverflow.Ellipsis
                                        )

                                        Spacer(modifier = Modifier.height(16.dp))

                                        // Acciones rápidas
                                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                            IconButton(
                                                onClick = { /* Like */ },
                                                modifier = Modifier
                                                    .size(48.dp)
                                                    .background(
                                                        Color.White.copy(alpha = 0.1f),
                                                        CircleShape
                                                    )
                                            ) {
                                                Icon(
                                                    Icons.Filled.Favorite,
                                                    "Me gusta",
                                                    tint = Color(0xFFE94560)
                                                )
                                            }

                                            IconButton(
                                                onClick = { /* Watchlist */ },
                                                modifier = Modifier
                                                    .size(48.dp)
                                                    .background(
                                                        Color.White.copy(alpha = 0.1f),
                                                        CircleShape
                                                    )
                                            ) {
                                                Icon(
                                                    Icons.Filled.Add,
                                                    "Watchlist",
                                                    tint = Color.White
                                                )
                                            }

                                            IconButton(
                                                onClick = { /* Compartir */ },
                                                modifier = Modifier
                                                    .size(48.dp)
                                                    .background(
                                                        Color.White.copy(alpha = 0.1f),
                                                        CircleShape
                                                    )
                                            ) {
                                                Icon(
                                                    Icons.Filled.Share,
                                                    "Compartir",
                                                    tint = Color.White
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        // Sinopsis
                        item {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(24.dp)
                            ) {
                                Text(
                                    "Sinopsis",
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Spacer(modifier = Modifier.height(12.dp))
                                Text(
                                    text = movie.overview ?: "Sin descripción disponible",
                                    fontSize = 16.sp,
                                    lineHeight = 24.sp,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                                )
                            }
                        }

                        // Archivos disponibles
                        val files = if (movieFiles?.isSuccessful == true) {
                            movieFiles?.body()
                        } else null

                        if (files != null && files.files.isNotEmpty()) {
                            item {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 24.dp)
                                ) {
                                    Text(
                                        "Archivos disponibles",
                                        fontSize = 20.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Spacer(modifier = Modifier.height(12.dp))
                                }
                            }

                            items(files.files) { file ->
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 24.dp, vertical = 6.dp),
                                    shape = RoundedCornerShape(12.dp),
                                    colors = CardDefaults.cardColors(
                                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                                    )
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(16.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            Icons.Filled.PlayArrow,
                                            "Archivo",
                                            tint = MaterialTheme.colorScheme.primary
                                        )
                                        Spacer(modifier = Modifier.width(12.dp))
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                file.name,
                                                fontWeight = FontWeight.Medium
                                            )
                                            Text(
                                                "${file.size / 1024 / 1024} MB",
                                                fontSize = 12.sp,
                                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                            )
                                        }
                                        IconButton(onClick = { /* Descargar */ }) {
                                            Icon(Icons.Filled.Download, "Descargar")
                                        }
                                    }
                                }
                            }
                        }

                        // Sección de reseñas
                        item {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(24.dp)
                            ) {
                                Text(
                                    "Reseñas",
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.height(12.dp))

                                // Mensajes de éxito/error
                                if (showSuccessMessage) {
                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = Color(0xFF4CAF50).copy(alpha = 0.2f),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(12.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Icon(
                                                Icons.Filled.CheckCircle,
                                                contentDescription = null,
                                                tint = Color(0xFF4CAF50)
                                            )
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text(
                                                "¡Reseña enviada correctamente!",
                                                color = Color(0xFF4CAF50)
                                            )
                                        }
                                    }
                                    Spacer(modifier = Modifier.height(12.dp))
                                }

                                if (showErrorMessage) {
                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = Color(0xFFE94560).copy(alpha = 0.2f),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(12.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Icon(
                                                Icons.Filled.Warning,
                                                contentDescription = null,
                                                tint = Color(0xFFE94560)
                                            )
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text(
                                                errorMessage,
                                                color = Color(0xFFE94560)
                                            )
                                        }
                                    }
                                    Spacer(modifier = Modifier.height(12.dp))
                                }

                                when {
                                    movieReviews == null -> {
                                        CircularProgressIndicator()
                                    }
                                    movieReviews?.isSuccessful == true -> {
                                        val body = movieReviews?.body()
                                        val reviews = body?.content ?: emptyList()
                                        if (reviews.isEmpty()) {
                                            Text(
                                                "No hay reseñas todavía. ¡Sé el primero en comentar!",
                                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                            )
                                        } else {
                                            reviews.forEach { review ->
                                                Card(
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .padding(vertical = 6.dp),
                                                    shape = RoundedCornerShape(12.dp),
                                                    colors = CardDefaults.cardColors(
                                                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                                                    )
                                                ) {
                                                    Column(modifier = Modifier.padding(16.dp)) {
                                                        Row(
                                                            modifier = Modifier.fillMaxWidth(),
                                                            horizontalArrangement = Arrangement.SpaceBetween,
                                                            verticalAlignment = Alignment.CenterVertically
                                                        ) {
                                                            Text(
                                                                review.username ?: "Usuario ${review.userId}",
                                                                fontWeight = FontWeight.Bold,
                                                                fontSize = 14.sp
                                                            )
                                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                                repeat(review.stars) {
                                                                    Icon(
                                                                        Icons.Filled.Star,
                                                                        contentDescription = null,
                                                                        tint = Color(0xFFFFC107),
                                                                        modifier = Modifier.size(16.dp)
                                                                    )
                                                                }
                                                                repeat(5 - review.stars) {
                                                                    Icon(
                                                                        Icons.Filled.StarBorder,
                                                                        contentDescription = null,
                                                                        tint = Color.Gray,
                                                                        modifier = Modifier.size(16.dp)
                                                                    )
                                                                }
                                                            }
                                                        }
                                                        Spacer(modifier = Modifier.height(8.dp))
                                                        Text(
                                                            review.text,
                                                            fontSize = 14.sp,
                                                            lineHeight = 20.sp
                                                        )
                                                    }
                                                }
                                                Spacer(modifier = Modifier.height(8.dp))
                                            }
                                        }
                                    }
                                    else -> {
                                        val errorBody = try { movieReviews?.errorBody()?.string() } catch (_: Exception) { null }
                                        val errorMsg = try {
                                            val json = org.json.JSONObject(errorBody ?: "")
                                            json.optString("message", errorBody ?: "Error desconocido")
                                        } catch (e: Exception) {
                                            errorBody ?: "Error desconocido"
                                        }
                                        Text(
                                            errorMsg,
                                            color = Color.Red,
                                            modifier = Modifier.padding(8.dp)
                                        )
                                    }
                                }
                            }
                        }

                        item {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(24.dp)
                            ) {
                                Text(
                                    "Escribe una reseña",
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.height(12.dp))

                                // Campos de texto para la reseña
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(12.dp),
                                    colors = CardDefaults.cardColors(
                                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                                    )
                                ) {
                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(16.dp)
                                    ) {
                                        TextField(
                                            value = text,
                                            onValueChange = { text = it },
                                            placeholder = { Text("Escribe tu reseña aquí...") },
                                            modifier = Modifier.fillMaxWidth(),
                                            // Usar correctamente TextFieldDefaults.textFieldColors
                                            colors = TextFieldDefaults.colors(
                                                focusedContainerColor = Color.Transparent,
                                                unfocusedContainerColor = Color.Transparent,
                                                disabledContainerColor = Color.Transparent,
                                                errorContainerColor = Color.Transparent,
                                                focusedIndicatorColor = Color.Transparent,
                                                unfocusedIndicatorColor = Color.Transparent,
                                                disabledIndicatorColor = Color.Transparent,
                                                errorIndicatorColor = Color.Transparent
                                            ),
                                            maxLines = 4
                                        )

                                        Spacer(modifier = Modifier.height(8.dp))

                                        // Selector de estrellas
                                        Row {
                                            repeat(5) { index ->
                                                val starFilled = index < stars
                                                IconButton(
                                                    onClick = {
                                                        stars = if (starFilled) index else index + 1
                                                    },
                                                    modifier = Modifier.size(48.dp)
                                                ) {
                                                    Icon(
                                                        imageVector = if (starFilled) Icons.Filled.Star else Icons.Filled.StarBorder,
                                                        contentDescription = if (starFilled) "Quitar estrella" else "Agregar estrella",
                                                        tint = if (starFilled) Color(0xFFFFC107) else Color.Gray
                                                    )
                                                }
                                            }
                                        }

                                        Spacer(modifier = Modifier.height(16.dp))

                                        // Botón de enviar reseña
                                        Button(
                                            onClick = {
                                                scope.launch {
                                                    if (text.isNotBlank() && stars > 0 && !token.isNullOrBlank() && userId != null) {
                                                        val review = ReviewRequest(
                                                            userId = userId!!,
                                                            movieId = movieId,
                                                            text = text,
                                                            stars = stars
                                                        )
                                                        viewModel.postReview(review, "Bearer $token")
                                                    } else {
                                                        errorMessage = "Por favor completa todos los campos y asegúrate de estar autenticado."
                                                        showErrorMessage = true
                                                        kotlinx.coroutines.delay(3000)
                                                        showErrorMessage = false
                                                    }
                                                }
                                            },
                                            modifier = Modifier.fillMaxWidth(),
                                            shape = RoundedCornerShape(12.dp)
                                        ) {
                                            Text("Enviar reseña")
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
            else -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Estado desconocido")
                }
            }
        }
    }
}
