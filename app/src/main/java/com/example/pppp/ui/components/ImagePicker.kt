package com.example.pppp.ui.components

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import coil.compose.AsyncImage
import java.io.File

@Composable
fun ImagePicker(
    imageUri: MutableState<Uri?>,
    bitmap: MutableState<Bitmap?>,
    onImagePicked: (Uri?) -> Unit = {},
    onBitmapPicked: (Bitmap?) -> Unit = {}
) {
    val galleryLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        imageUri.value = uri
        onImagePicked(uri)
    }
    val cameraLauncher = rememberLauncherForActivityResult(ActivityResultContracts.TakePicturePreview()) { bmp ->
        bitmap.value = bmp
        onBitmapPicked(bmp)
    }
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
        Button(onClick = { galleryLauncher.launch("image/*") }) {
            Text("Galería")
        }
        Button(onClick = { cameraLauncher.launch(null) }) {
            Text("Cámara")
        }
    }
    Spacer(modifier = Modifier.height(8.dp))
    imageUri.value?.let { uri ->
        AsyncImage(
            model = uri,
            contentDescription = "Imagen seleccionada",
            modifier = Modifier.size(120.dp)
        )
    }
    bitmap.value?.let { bmp ->
        Image(
            bitmap = bmp.asImageBitmap(),
            contentDescription = "Foto tomada",
            modifier = Modifier.size(120.dp)
        )
    }
}
