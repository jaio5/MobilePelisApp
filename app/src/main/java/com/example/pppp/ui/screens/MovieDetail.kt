package com.example.pppp.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.Button
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.pppp.ui.components.ImagePicker

@Composable
fun MovieDetail() {
    val imageUri = remember { mutableStateOf<android.net.Uri?>(null) }
    val bitmap = remember { mutableStateOf<android.graphics.Bitmap?>(null) }
    val title = remember { mutableStateOf("") }
    val description = remember { mutableStateOf("") }
    val date = remember { mutableStateOf("") }
    val titleError = remember { mutableStateOf("") }
    val descriptionError = remember { mutableStateOf("") }
    val dateError = remember { mutableStateOf("") }
    val formError = remember { mutableStateOf("") }

    fun validate(): Boolean {
        titleError.value = if (title.value.isBlank()) "El título es obligatorio" else ""
        descriptionError.value = if (description.value.length < 10) "La descripción debe tener al menos 10 caracteres" else ""
        dateError.value = if (date.value.isBlank()) "La fecha es obligatoria" else ""
        formError.value = if (titleError.value.isNotEmpty() || descriptionError.value.isNotEmpty() || dateError.value.isNotEmpty()) "Corrige los errores antes de continuar" else ""
        return formError.value.isEmpty()
    }

    Column {
        Text("Selecciona una imagen para la película")
        ImagePicker(imageUri = imageUri, bitmap = bitmap)
        Spacer(modifier = Modifier.height(16.dp))
        TextField(
            value = title.value,
            onValueChange = { title.value = it },
            label = { Text("Título") },
            isError = titleError.value.isNotEmpty()
        )
        if (titleError.value.isNotEmpty()) {
            Text(titleError.value, color = Color.Red)
        }
        Spacer(modifier = Modifier.height(8.dp))
        TextField(
            value = description.value,
            onValueChange = { description.value = it },
            label = { Text("Descripción") },
            isError = descriptionError.value.isNotEmpty()
        )
        if (descriptionError.value.isNotEmpty()) {
            Text(descriptionError.value, color = Color.Red)
        }
        Spacer(modifier = Modifier.height(8.dp))
        TextField(
            value = date.value,
            onValueChange = { date.value = it },
            label = { Text("Fecha de creación") },
            isError = dateError.value.isNotEmpty()
        )
        if (dateError.value.isNotEmpty()) {
            Text(dateError.value, color = Color.Red)
        }
        Spacer(modifier = Modifier.height(16.dp))
        if (formError.value.isNotEmpty()) {
            Text(formError.value, color = Color.Red)
        }
        // Botón para crear/editar película
        Button(onClick = { if (validate()) {/* lógica de guardar */} }) {
            Text("Guardar película")
        }
        Spacer(modifier = Modifier.height(16.dp))
        imageUri.value?.let { uri ->
            Text("Imagen seleccionada: $uri")
        }
        bitmap.value?.let { bmp ->
            Text("Foto tomada")
        }
    }
}