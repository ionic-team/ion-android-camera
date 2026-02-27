package io.ionic.libs.ioncameralib.manager

import android.app.Activity
import android.content.Intent
import android.util.Log
import io.ionic.libs.ioncameralib.helper.OSCAMRFileHelperInterface
import io.ionic.libs.ioncameralib.model.IONError
import java.io.File

class VideoManager(
    private var authority: String,
    private var fileHelper: OSCAMRFileHelperInterface,
) {

    /**
     * Calls the intent to open the device's camera to record a video.
     * @param activity  Activity object that will be necessary to launch the video player activity.
     * @param videoUri Location of the video file to be played, as String.
     */
    fun playVideo(
        activity: Activity,
        videoUri: String,
        onSuccess: () -> Unit,
        onError: (IONError) -> Unit
    ) {
        val mimeType = fileHelper.getMimeType(videoUri)
        val file = File(videoUri)

        if (!fileHelper.fileExists(file)) {
            onError(IONError.FILE_DOES_NOT_EXIST_ERROR)
            return
        }

        if (mimeType.isNullOrEmpty()) {
            onError(IONError.MEDIA_PATH_ERROR)
            return
        }

        val contentUri = fileHelper.getUriForFile(activity, activity.packageName + authority, file)
        val intent = Intent(Intent.ACTION_VIEW)
        intent.setDataAndType(contentUri, mimeType)
        intent.flags = Intent.FLAG_ACTIVITY_NO_HISTORY
        intent.flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
        activity.startActivity(intent)
        onSuccess()
    }
}