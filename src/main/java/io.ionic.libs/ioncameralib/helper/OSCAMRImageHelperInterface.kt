package io.ionic.libs.ioncameralib.helper

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.net.Uri
import io.ionic.libs.ioncameralib.model.IONError
import java.io.File
import java.io.InputStream

interface OSCAMRImageHelperInterface {
    fun getBitmapForInputStream(fileStream: InputStream?): Bitmap?
    fun downsizeBitmapIfNeeded(bitmap: Bitmap, resolution: Int): Bitmap
    fun compressBitmap(bitmap: Bitmap, quality: Int): Bitmap
    fun compressImage(activity: Activity?, uri: Uri?, bitmap: Bitmap?, compressFormat: Bitmap.CompressFormat, mQuality: Int, onError : (IONError) -> Unit)
    fun processPicture(bitmap: Bitmap?, encodingType: Int, mQuality: Int, onSuccess : (String) -> Unit, onError : (IONError) -> Unit)
    fun decodeStream(fileStream: InputStream?, options: BitmapFactory.Options): Bitmap?
    fun decodeFile(resultImagePath: String?): Bitmap?
    fun bitmapToBase64(result: Bitmap?, resolution: Int, quality: Int, onSuccess : (String) -> Unit, onError : (IONError) -> Unit)
    fun base64toBitmap(imageByteArray: ByteArray): Bitmap?
    fun writeBitmapToFile(imageBitmap: Bitmap?, inputFile: File?)
    fun getScaledBitmap(unscaledBitmap: Bitmap?, scaledWidth: Int, scaledHeight: Int): Bitmap?
    /**
     * Applies a transformation matrix to a bitmap.
     * @param bitmap  The original bitmap image.
     * @param transformMatrix  The transformation matrix to apply.
     * @return The new bitmap, changed accordingly to the transformation matrix.
     *
     *          The input bitmap if the transformation matrix is null.
     *          Null if bitmap is null.
     */
    fun transformBitmapWithMatrix(bitmap: Bitmap?, transformMatrix: Matrix?): Bitmap?
    fun writeBitmapToFileByName(bitmap: Bitmap?, fileName: String?, encodingType: Int, quality: Int)
    fun recycleBitmap(bitmap: Bitmap?)
    fun areOptionsZero(options: BitmapFactory.Options): Boolean
    fun getOutWidth(options: BitmapFactory.Options): Int
    fun getOutHeight(options: BitmapFactory.Options): Int
    fun getResultUriFromIntent(intent: Intent): List<Uri>
}