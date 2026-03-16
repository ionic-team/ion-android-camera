package io.ionic.libs.ioncameralib.manager

import android.app.Activity
import android.content.Intent
import io.ionic.libs.ioncameralib.helper.IONCAMRFileHelperInterface
import io.ionic.libs.ioncameralib.model.IONCAMRError
import java.io.File

class IONCAMRVideoManager(
    private var authority: String,
    private var fileHelper: IONCAMRFileHelperInterface,
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
        onError: (IONCAMRError) -> Unit
    ) {
        val mimeType = fileHelper.getMimeType(videoUri)
        val file = File(videoUri)

        if (!fileHelper.fileExists(file)) {
            onError(IONCAMRError.FILE_DOES_NOT_EXIST_ERROR)
            return
        }

        if (mimeType.isNullOrEmpty()) {
            onError(IONCAMRError.MEDIA_PATH_ERROR)
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