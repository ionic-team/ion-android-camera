package com.outsystems.plugins.camera.controller.imageeditor

import android.graphics.Bitmap
import android.graphics.Rect

interface OSCAMRImageEditorControllerInterface {
    suspend fun rotateLeft(image: Bitmap): Bitmap
    suspend fun crop(image: Bitmap, rect: Rect) : Bitmap
    suspend fun flip(image: Bitmap): Bitmap
}