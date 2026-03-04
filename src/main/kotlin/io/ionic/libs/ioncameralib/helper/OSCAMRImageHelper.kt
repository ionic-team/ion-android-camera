package io.ionic.libs.ioncameralib.helper

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Bitmap.CompressFormat
import android.graphics.BitmapFactory
import android.graphics.BitmapFactory.Options
import android.graphics.Matrix
import android.net.Uri
import android.util.Base64
import androidx.core.graphics.scale
import io.ionic.libs.ioncameralib.model.IONError
import java.io.*

class OSCAMRImageHelper: OSCAMRImageHelperInterface {

    companion object {
        private const val JPEG = 0
    }

    override fun getBitmapForInputStream(fileStream: InputStream?): Bitmap? {
        return BitmapFactory.decodeStream(fileStream)
    }

    /**
     * Down sizes a bitmap to a specific resolution.
     * @param bitmap The bitmap to resize
     * @param resolution The target resolution
     * @return The new resized bitmap
     * */
    override fun downsizeBitmapIfNeeded(bitmap: Bitmap, resolution: Int): Bitmap {
        val smallerSide = Integer.min(bitmap.width, bitmap.height)
        if (smallerSide > resolution) {
            // only if image is bigger than required resolution
            val aspectRatio = bitmap.width / bitmap.height.toFloat()
            return if (bitmap.width > bitmap.height) {
                bitmap.scale((resolution * aspectRatio).toInt(), resolution, false)
            } else {
                bitmap.scale(resolution, (resolution / aspectRatio).toInt(), false)
            }
        }
        return bitmap
    }

    override fun compressBitmap(bitmap: Bitmap, quality: Int): Bitmap {
        val stream = ByteArrayOutputStream()
        bitmap.compress(CompressFormat.JPEG, quality, stream)
        val byteArray = stream.toByteArray()
        return BitmapFactory.decodeByteArray(byteArray, 0, byteArray.size)
    }

    override fun compressImage(activity: Activity?, uri: Uri?, bitmap: Bitmap?, compressFormat: CompressFormat, mQuality: Int, onError : (IONError) -> Unit){
        if(bitmap == null || uri == null || activity == null){
            onError(IONError.PROCESS_IMAGE_ERROR)
            return
        }
        val os: OutputStream? =
            activity.contentResolver.openOutputStream(uri)
        if (os != null) {
            bitmap.compress(compressFormat, mQuality, os)
        }
        os?.close()
    }

    /**
     * Compress bitmap using jpeg, convert to Base64 encoded string.
     *
     * @param bitmap
     */
    override fun processPicture(bitmap: Bitmap?, encodingType: Int, mQuality: Int, onSuccess : (String) -> Unit, onError : (IONError) -> Unit) {
        val jpegData = ByteArrayOutputStream()
        val compressFormat: CompressFormat =
            if (encodingType == JPEG) CompressFormat.JPEG else CompressFormat.PNG
        try {
            if (bitmap!!.compress(compressFormat, mQuality, jpegData)) {
                val code = jpegData?.toByteArray()
                val output = Base64.encode(code, Base64.NO_WRAP)
                val jsOut = String(output)
                onSuccess(jsOut)
            }
        } catch (e: Exception) {
            onError(IONError.PROCESS_IMAGE_ERROR)
        }
    }

    override fun decodeStream(fileStream: InputStream?, options: Options): Bitmap? {
        return BitmapFactory.decodeStream(fileStream, null, options)
    }

    override fun decodeFile(resultImagePath: String?): Bitmap? {
        return BitmapFactory.decodeFile(resultImagePath)
    }

    override fun bitmapToBase64(result: Bitmap?, resolution: Int, quality: Int, onSuccess : (String) -> Unit, onError : (IONError) -> Unit) {
        val byteArrayOutputStream = ByteArrayOutputStream()
        result?.let {
            val resizedImage = this.downsizeBitmapIfNeeded(it, resolution)
            if (resizedImage.compress(CompressFormat.JPEG, quality, byteArrayOutputStream)) {
                val byteArray = byteArrayOutputStream.toByteArray()
                val base64Result = Base64.encodeToString(byteArray, Base64.NO_WRAP)
                onSuccess(base64Result)
                return
            }
        }

        onError(IONError.EDIT_IMAGE_ERROR)
        return
    }

    override fun base64toBitmap(imageByteArray: ByteArray): Bitmap? {
        return BitmapFactory.decodeByteArray(imageByteArray, 0, imageByteArray.size)
    }

    override fun writeBitmapToFile(imageBitmap: Bitmap?, inputFile: File?) {
        val outStream: OutputStream = FileOutputStream(inputFile)
        imageBitmap?.compress(CompressFormat.JPEG, 100, outStream)
        outStream.flush()
        outStream.close()
    }

    override fun getScaledBitmap(unscaledBitmap: Bitmap?, scaledWidth: Int, scaledHeight: Int): Bitmap? {
        return unscaledBitmap?.let {
            Bitmap.createScaledBitmap(it, scaledWidth, scaledHeight, true)
        }
    }

    override fun transformBitmapWithMatrix(bitmap: Bitmap?, transformMatrix: Matrix?): Bitmap? {
        if(transformMatrix == null) {
            return bitmap
        }
        return bitmap?.let {
            Bitmap.createBitmap(it,0,0, it.width, it.height, transformMatrix, true)
        }
    }

    override fun writeBitmapToFileByName(bitmap: Bitmap?, fileName: String?, encodingType: Int, quality: Int) {
        val outStream: OutputStream = FileOutputStream(fileName)
        val compressFormat: CompressFormat = if (encodingType == JPEG) CompressFormat.JPEG else CompressFormat.PNG
        bitmap?.compress(compressFormat, quality, outStream)
        outStream.close()
    }

    override fun recycleBitmap(bitmap: Bitmap?) {
        bitmap?.recycle()
    }

    override fun areOptionsZero(options: Options): Boolean {
        return (options.outWidth == 0 || options.outHeight == 0)
    }

    override fun getOutWidth(options: Options): Int {
        return options.outWidth
    }

    override fun getOutHeight(options: Options): Int {
        return options.outHeight
    }

    override fun getResultUriFromIntent(intent: Intent): List<Uri> {
        val uris: MutableSet<Uri> = mutableSetOf()
        // for single selections
        intent.data?.let {
            uris.add(it)
        }

        // for multiple selections
        intent.clipData?.let {
            for(i in 0 until it.itemCount) {
                uris.add(it.getItemAt(i).uri)
            }
        }
        return uris.toList()
    }

}