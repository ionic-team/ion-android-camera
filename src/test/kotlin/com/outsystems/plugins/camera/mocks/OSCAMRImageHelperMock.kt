package com.outsystems.plugins.camera.mocks

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.net.Uri
import com.outsystems.plugins.camera.controller.helper.OSCAMRImageHelperInterface
import com.outsystems.plugins.camera.model.OSCAMRError
import org.mockito.Mockito
import java.io.File
import java.io.InputStream

class OSCAMRImageHelperMock : OSCAMRImageHelperInterface {

    var processPicSuccess = false
    var compressImageSuccess = false
    var areOptionsZero = false
    var outWidth = 10
    var outHeight = 10
    var bitmapToBase64Success = false
    var writeBitmapToFileByNameError = false
    var intentUris: List<Uri> = listOf()
    var bitmap: Bitmap? = Mockito.mock(Bitmap::class.java)
    var isBitmapNull = false

    override fun getBitmapForInputStream(fileStream: InputStream?): Bitmap? {
        return if (isBitmapNull) {
            null
        } else {
            bitmap
        }
    }

    override fun downsizeBitmapIfNeeded(bitmap: Bitmap, resolution: Int): Bitmap {
        // does nothing in mock
        return bitmap
    }

    override fun compressBitmap(bitmap: Bitmap, quality: Int): Bitmap {
        // does nothing in mock
        return bitmap
    }

    override fun compressImage(
        activity: Activity?,
        uri: Uri?,
        bitmap: Bitmap?,
        compressFormat: Bitmap.CompressFormat,
        mQuality: Int,
        onError: (OSCAMRError) -> Unit
    ) {
        if(!compressImageSuccess){
            onError(OSCAMRError.PROCESS_IMAGE_ERROR)
        }
    }

    override fun processPicture(
        bitmap: Bitmap?,
        encodingType: Int,
        mQuality: Int,
        onSuccess: (String) -> Unit,
        onError: (OSCAMRError) -> Unit
    ) {
        if(processPicSuccess){
            onSuccess("myImage")
        }
        else{
            onError(OSCAMRError.PROCESS_IMAGE_ERROR)
        }
    }

    override fun decodeStream(fileStream: InputStream?, options: BitmapFactory.Options): Bitmap? {
        return null
    }

    override fun decodeFile(resultImagePath: String?): Bitmap? {
        return bitmap
    }

    override fun bitmapToBase64(
        result: Bitmap?,
        resolution: Int,
        quality: Int,
        onSuccess: (String) -> Unit,
        onError: (OSCAMRError) -> Unit
    ) {
        if (bitmapToBase64Success) onSuccess(SAMPLE_BASE64_THUMBNAIL) else onError(OSCAMRError.EDIT_IMAGE_ERROR)
    }

    override fun base64toBitmap(imageByteArray: ByteArray): Bitmap? {
        return null
    }

    override fun writeBitmapToFile(imageBitmap: Bitmap?, inputFile: File?) {
        // do nothing
    }

    override fun getScaledBitmap(
        unscaledBitmap: Bitmap?,
        scaledWidth: Int,
        scaledHeight: Int
    ): Bitmap? {
        return null
    }

    override fun transformBitmapWithMatrix(bitmap: Bitmap?, transformMatrix: Matrix?): Bitmap? {
        if (isBitmapNull) {
            return null
        }
        return bitmap
    }

    override fun writeBitmapToFileByName(
        bitmap: Bitmap?,
        fileName: String?,
        encodingType: Int,
        quality: Int
    ) {
        if(writeBitmapToFileByNameError){
            throw Exception()
        }
    }

    override fun recycleBitmap(bitmap: Bitmap?) {
        // do nothing
    }

    override fun areOptionsZero(options: BitmapFactory.Options): Boolean {
        return areOptionsZero
    }

    override fun getOutWidth(options: BitmapFactory.Options): Int {
        return outWidth
    }

    override fun getOutHeight(options: BitmapFactory.Options): Int {
        return outHeight
    }

    override fun getResultUriFromIntent(intent: Intent): List<Uri> {
        return intentUris
    }

    companion object {
        const val SAMPLE_BASE64_THUMBNAIL = "base64"
    }

}