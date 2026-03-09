package io.ionic.libs.ioncameralib.mocks

import android.app.Activity
import android.content.Intent
import android.database.Cursor
import android.net.Uri
import io.ionic.libs.ioncameralib.helper.IONMediaHelperInterface

class IONMediaHelperMock: IONMediaHelperInterface {

    var equalsDiff = true
    var existsActivity = true
    var duration = 2222
    var resolution = Pair(1080, 1080)

    override fun openDeviceCamera(activity: Activity?, imageUri: Uri?, returnType: Int) {
        // do nothing
    }

    override fun createCameraIntent(activity: Activity?, imageUri: Uri?): Intent? {
        return Intent()
    }

    override fun getCursor(activity: Activity?, contentStore: Uri): Cursor? {
        return null
    }

    override fun getNumberOfImages(activity: Activity?, contentStore: Uri?): Int? {
        return 3
    }

    override fun getColumnIndex(cursor: Cursor?): Int? {
        return 1
    }

    override fun getStringNumber(cursor: Cursor?, int: Int?): String? {
        return "3"
    }

    override fun equalsDifference(currentNumOfImages: Int, numPics: Int?, diff: Int): Boolean {
        return equalsDiff
    }

    override fun existsActivity(activity: Activity?, intent: Intent): Boolean {
        return existsActivity
    }

    override fun getVideoPathFromUri(activity: Activity, uri: Uri): String? {
        return "videoUriFromGallery"
    }

    override suspend fun getThumbnailBase64String(
        activity: Activity,
        videoUri: Uri,
        targetDimension: Int
    ): String {
        return "base64"
    }

    override fun getVideoDuration(activity: Activity, uri: Uri): Int {
        return duration
    }

    override fun getVideoResolution(activity: Activity?, uri: Uri): Pair<Int, Int> {
        return resolution
    }

    override fun getImageResolution(imagePath: String): Pair<Int, Int> {
        return resolution
    }

    override fun openDeviceVideo(
        activity: Activity?,
        intent: Intent,
        videoFileUri: Uri?,
        saveToGallery: Boolean
    ) {
        // do nothing
    }

    override fun createDeviceVideoIntent(
        activity: Activity?,
        intent: Intent,
        videoFileUri: Uri?,
        saveToGallery: Boolean
    ): Intent? {
        return Intent()
    }

}