package io.ionic.libs.ioncameralib.imageeditor

import android.graphics.Bitmap
import android.graphics.Matrix
import android.graphics.Rect

class OSCAMRImageEditorController : OSCAMRImageEditorControllerInterface {

    override suspend fun rotateLeft(image: Bitmap): Bitmap {
        val rotationMatrix = Matrix().apply {
            postRotate(-90F)
        }
        val rotated = Bitmap.createBitmap(image, 0, 0, image.width, image.height, rotationMatrix, true)
        return Bitmap.createScaledBitmap(rotated, image.height, image.width, true)
    }

    override suspend fun crop(image: Bitmap, rect: Rect): Bitmap {
        // Cropper view should already have valid crop values.
        // However, let's double check everything is within the original image size.
        val leftOffset = rect.left.coerceAtLeast(0)
        val topOffset = rect.top.coerceAtLeast(0)
        val width = rect.width().coerceAtMost(image.width - leftOffset)
        val height = rect.height().coerceAtMost(image.height - topOffset)
        return Bitmap.createBitmap(image, leftOffset, topOffset, width, height)
    }

    override suspend fun flip(image: Bitmap): Bitmap {
        val scalingMatrix = Matrix()
        scalingMatrix.postScale(-1F, 1F)
        return Bitmap.createBitmap(image, 0, 0, image.width, image.height, scalingMatrix, false)
    }

}