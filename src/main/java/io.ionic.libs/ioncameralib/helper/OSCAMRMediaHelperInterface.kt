package io.ionic.libs.ioncameralib.helper

import android.app.Activity
import android.content.Intent
import android.database.Cursor
import android.net.Uri

interface OSCAMRMediaHelperInterface {
    fun createCameraIntent(activity: Activity?, imageUri: Uri?): Intent?
    fun getCursor(activity: Activity?, contentStore: Uri): Cursor?
    fun getNumberOfImages(activity: Activity?, contentStore: Uri?): Int?
    fun getColumnIndex(cursor: Cursor?): Int?
    fun getStringNumber(cursor: Cursor?, int: Int?): String?
    fun equalsDifference(currentNumOfImages: Int, numPics: Int?, diff: Int): Boolean
    fun existsActivity(activity: Activity?, intent: Intent): Boolean
    fun openDeviceVideo(activity: Activity?, intent: Intent, videoFileUri: Uri?, saveToGallery: Boolean)
    fun getVideoPathFromUri(activity: Activity, uri: Uri): String?
    suspend fun getThumbnailBase64String(activity: Activity, videoUri: Uri, targetDimension: Int): String?
    fun getVideoDuration(activity: Activity, uri: Uri): Int
    fun getVideoResolution(activity: Activity?, uri: Uri): Pair<Int, Int>
    fun getImageResolution(imagePath: String): Pair<Int, Int>
}