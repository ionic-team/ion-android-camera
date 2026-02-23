package io.ionic.libs.ioncameralib.helper

import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.database.Cursor
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.MediaMetadataRetriever
import android.media.MediaPlayer
import android.net.Uri
import android.provider.MediaStore
import android.util.Base64
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream

class OSCAMRMediaHelper : OSCAMRMediaHelperInterface {

    companion object {
        private const val CAMERA = 1
        private const val LOG_TAG = "OSCAMRMediaHelper"
        const val REQUEST_VIDEO_CAPTURE = 1
        const val REQUEST_VIDEO_CAPTURE_SAVE_TO_GALLERY = 2
    }

    override fun openDeviceCamera(activity: Activity?, imageUri: Uri?, returnType: Int) {
        if (activity != null && imageUri != null) {
            val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri)
            intent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
            val mPm: PackageManager = activity.packageManager
            if (intent.resolveActivity(mPm) != null) {
                activity.startActivityForResult(
                    intent,
                    (CAMERA + 1) * 16 + returnType + 1
                )
            } else {
                Log.d(
                    LOG_TAG,
                    "Error: You don't have a default camera.  Your device may not be CTS complaint."
                )
            }
        }
    }

    /**
     * Creates a cursor that can be used to determine how many images we have.
     *
     * @return a cursor
     */
    override fun getCursor(activity: Activity?, contentStore: Uri): Cursor? {
        return activity?.contentResolver?.query(
            contentStore, arrayOf(MediaStore.Images.Media._ID),
            null,
            null,
            null
        )
    }

    /**
     * Determines how many images we have.
     *
     * @return a number of images
     */
    override fun getNumberOfImages(activity: Activity?, contentStore: Uri?): Int? {
        return contentStore?.let {
            activity?.contentResolver?.query(
                it, arrayOf(MediaStore.Images.Media._ID),
                null,
                null,
                null
            )?.count
        }
    }

    override fun getColumnIndex(cursor: Cursor?): Int? {
        return cursor?.getColumnIndex(MediaStore.Images.Media._ID)
    }

    override fun getStringNumber(cursor: Cursor?, int: Int?): String? {
        return int?.let { cursor?.getString(it) }
    }

    override fun equalsDifference(currentNumOfImages: Int, numPics: Int?, diff: Int): Boolean {
        return if (numPics == null) {
            false
        } else {
            (currentNumOfImages - numPics) == diff
        }
    }

    override fun existsActivity(activity: Activity?, intent: Intent): Boolean {
        val packageManager: PackageManager = activity?.packageManager ?: return false
        return intent.resolveActivity(packageManager) != null
    }

    override fun openDeviceVideo(
        activity: Activity?,
        intent: Intent,
        videoFileUri: Uri?,
        saveToGallery: Boolean
    ) {
        if (!saveToGallery) intent.putExtra(MediaStore.EXTRA_OUTPUT, videoFileUri)
        activity?.startActivityForResult(
            intent,
            if (!saveToGallery) REQUEST_VIDEO_CAPTURE else REQUEST_VIDEO_CAPTURE_SAVE_TO_GALLERY
        )
    }

    override fun getVideoPathFromUri(activity: Activity, uri: Uri): String? {
        val projection = arrayOf(MediaStore.Video.Media.DATA)
        activity.applicationContext.contentResolver.query(uri, projection, null, null, null)
            ?.use { cursor ->
                val columnIndex = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DATA)
                if (cursor.moveToFirst()) {
                    return cursor.getString(columnIndex)
                }
            }
        return null
    }

    override fun getVideoDuration(activity: Activity, uri: Uri): Int {
        return MediaPlayer.create(activity, uri).duration
    }

    override fun getVideoResolution(activity: Activity?, uri: Uri): Pair<Int, Int> {
        val metaRetriever = MediaMetadataRetriever()
        metaRetriever.setDataSource(activity, uri)
        val height = metaRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_HEIGHT)?.toInt() ?: 0
        val width = metaRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_WIDTH)?.toInt() ?: 0
        return Pair(height, width)
    }

    override fun getImageResolution(imagePath: String): Pair<Int, Int> {
        val bitmap = BitmapFactory.decodeFile(imagePath)
        return Pair(bitmap.height,bitmap.width)
    }

    override suspend fun getThumbnailBase64String(activity: Activity, videoUri: Uri, targetDimension: Int): String? =
        withContext(Dispatchers.IO) {
            val retriever = MediaMetadataRetriever()

            return@withContext try {
                retriever.setDataSource(activity.applicationContext, videoUri)
                retriever.getFrameAtTime(0, MediaMetadataRetriever.OPTION_CLOSEST_SYNC)
                    ?.let { thumbnailBitmap ->
                        val sizes = defineNewDimensionSize(thumbnailBitmap.width,thumbnailBitmap.height,targetDimension)
                        val newWidth = sizes.first
                        val newHeight = sizes.second
                        val resizedThumbnailBitmap =
                            Bitmap.createScaledBitmap(thumbnailBitmap, newWidth, newHeight, false)
                        ByteArrayOutputStream().use { byteArrayOutputStream ->
                            resizedThumbnailBitmap.compress(
                                Bitmap.CompressFormat.JPEG,
                                100,
                                byteArrayOutputStream
                            )
                            val byteArray = byteArrayOutputStream.toByteArray()
                            Base64.encodeToString(byteArray, Base64.DEFAULT)
                        }
                    }
            } catch (e: Exception) {
                e.printStackTrace()
                null
            } finally {
                retriever.release()
            }
        }

    fun defineNewDimensionSize(width: Int, height: Int, target: Int = 480): Pair<Int, Int> {
        val ratio = maxOf(width,height).toFloat() / minOf(width,height)
        val newHeight: Float
        val newWidth: Float
        if(height>width){
            newWidth = target.toFloat()
            newHeight = target * ratio
        } else {
            newHeight = target.toFloat()
            newWidth = target * ratio
        }
        return Pair(newWidth.toInt(),newHeight.toInt())
    }

}