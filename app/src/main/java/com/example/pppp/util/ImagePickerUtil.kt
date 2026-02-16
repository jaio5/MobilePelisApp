package com.example.pppp.util

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.provider.MediaStore
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.MutableState
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import java.io.File

object ImagePickerUtil {
    fun getGalleryIntent(): Intent =
        Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)

    fun getCameraIntent(context: Context, photoFile: File): Intent =
        Intent(MediaStore.ACTION_IMAGE_CAPTURE).apply {
            val photoURI: Uri = FileProvider.getUriForFile(
                context,
                context.packageName + ".provider",
                photoFile
            )
            putExtra(MediaStore.EXTRA_OUTPUT, photoURI)
        }
}

