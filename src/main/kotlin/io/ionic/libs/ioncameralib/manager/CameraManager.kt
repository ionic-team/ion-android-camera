package io.ionic.libs.ioncameralib.manager

import android.app.Activity
import androidx.activity.result.ActivityResultLauncher
import android.content.Intent
import android.media.MediaScannerConnection
import android.media.MediaScannerConnection.MediaScannerConnectionClient
import android.net.Uri
import android.provider.MediaStore
import android.util.Log
import io.ionic.libs.ioncameralib.helper.IONExifHelperInterface
import io.ionic.libs.ioncameralib.helper.IONFileHelperInterface
import io.ionic.libs.ioncameralib.helper.IONImageHelperInterface
import io.ionic.libs.ioncameralib.helper.IONMediaHelperInterface
import io.ionic.libs.ioncameralib.model.IONError
import io.ionic.libs.ioncameralib.model.IONMediaResult
import io.ionic.libs.ioncameralib.model.IONMediaType
import io.ionic.libs.ioncameralib.model.IONCameraParameters
import io.ionic.libs.ioncameralib.view.IONImageEditorActivity
import io.ionic.libs.ioncameralib.processor.IONMediaProcessor
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date


class CameraManager(
    private var applicationId: String,
    private var authority: String,
    private var exif: IONExifHelperInterface,
    private var fileHelper: IONFileHelperInterface,
    private var mediaHelper: IONMediaHelperInterface,
    private var imageHelper: IONImageHelperInterface
) : MediaScannerConnectionClient {
    private var imageFilePath: String? = null
    private var imageUri: Uri? = null
    private var croppedUri: Uri? = null
    private var croppedFilePath: String? = null
    private var conn: MediaScannerConnection? = null
    private var scanMe: Uri? = null
    private val mediaProcessor = IONMediaProcessor(
        exif = exif,
        fileHelper = fileHelper,
        mediaHelper = mediaHelper,
        imageHelper = imageHelper
    )

    companion object {
        private const val JPEG = 0
        private const val TIME_FORMAT = "yyyyMMdd_HHmmss"
        private const val LOG_TAG = "CameraManager"
        private const val PICTURE_NAMES_PREFIX = "PIC_"
        private const val VIDEO_NAMES_PREFIX = "VID_"
        private const val VIDEO_FORMAT = ".mp4"
        private const val STORE = "CameraStore"
        private const val EDIT_FILE_NAME_KEY = "EditFileName"
        const val EDIT_REQUEST_CODE = 7
    }

    /**
     * Deletes all the videos that were captured and saved on the cache while the app was running.
     * @param activity  Activity to provide the context to delete the file.
     */
    fun deleteVideoFilesFromCache(activity: Activity) {
        fileHelper.getCachedFileNames(activity)?.keys?.let { fileNames ->
            for (fileName in fileNames) {
                fileHelper.deleteFileFromCache(activity, fileName)
                fileHelper.removeFileNameFromPrefs(fileName, activity)
            }
        }
    }

    /**
     * Take a picture with the camera.
     * @param activity  Activity object that will be necessary to take the picture
     * @param encodingType  JPEG or PNG.
     * @param launcher ActivityResultLauncher to use when launching the camera activity
     */
    fun takePhoto(activity: Activity, encodingType: Int, launcher: ActivityResultLauncher<Intent>) {
        // Save filename to fetch later (needed when allowEdit is true)
        val fileName = PICTURE_NAMES_PREFIX + SimpleDateFormat(TIME_FORMAT).format(Date())
        fileHelper.saveStringSharedPreferences(activity, EDIT_FILE_NAME_KEY, fileName)

        // Specify file so that large image is captured and returned
        val photo: File = createCaptureFile(
            activity,
            encodingType,
            fileName
        )
        this.imageFilePath = photo.absolutePath
        this.imageUri = fileHelper.getUriForFile(activity, "$applicationId$authority", photo)

        val intent = mediaHelper.createCameraIntent(activity, imageUri)

        if (intent != null) {
            launcher.launch(intent)
        } else {
            Log.d(LOG_TAG, "Failed to launch camera intent")
        }
    }


    /**
     * Calls the intent to open the device's camera to record a video.
     * @param activity  Activity object that will be necessary to launch the edit activity.
     * @param saveVideoToGallery Indicates if the recorded video should be saved to the device gallery
     * @param launcher ActivityResultLauncher to use when launching the camera activity
     * @param onError callback that will be used when an error occurs.
     */
    fun recordVideo(
        activity: Activity,
        saveVideoToGallery: Boolean = false,
        launcher: ActivityResultLauncher<Intent>,
        onError: (IONError) -> Unit
    ) {
        val videoFileUri = fileHelper.getUriForFile(
            activity,
            "$applicationId$authority",
            createVideoFile(activity)
        )
        fileHelper.saveStringSharedPreferences(
            activity,
            STORE, videoFileUri.toString()
        )
        val captureVideoIntent = Intent(MediaStore.ACTION_VIDEO_CAPTURE)

        if (mediaHelper.existsActivity(activity, captureVideoIntent)) {

            val intent = mediaHelper.createDeviceVideoIntent(
                activity,
                captureVideoIntent,
                videoFileUri,
                saveVideoToGallery
            )
            if (intent != null) {
                launcher.launch(intent)
            } else {
                Log.d(LOG_TAG, "Failed to launch device video intent")
            }
        } else {
            Log.d(LOG_TAG, "Error: You don't have a default camera for recording video.")
            onError(IONError.NO_CAMERA_AVAILABLE_ERROR)
        }
    }

    /**
     * Create a file in the applications temporary directory based upon the supplied encoding.
     *
     * @param encodingType of the image to be taken
     * @param fileName or resultant File object.
     * @return a File object pointing to the temporary picture
     */
    fun createCaptureFile(
        activity: Activity?,
        encodingType: Int,
        fileName: String = ""
    ): File {
        return mediaProcessor.createCaptureFile(
            activity = activity,
            encodingType = encodingType,
            fileName = fileName
        )
    }

    /**
     * Create a video file in the applications temporary directory based upon the supplied encoding.
     *
     * @return a File object pointing to the temporary picture
     */
    fun createVideoFile(activity: Activity?): File {
        val fileName =
            VIDEO_NAMES_PREFIX + SimpleDateFormat(TIME_FORMAT).format(Date()) + VIDEO_FORMAT
        return fileHelper.createCaptureFile(activity, fileName)
    }


    /**
     * Applies all needed transformation to the image received from the camera.
     *
     * @param intent  An Intent, which can return result data to the caller (various data can be attached to Intent "extras").
     */
    @Throws(IOException::class)
    fun processResultFromCamera(
        activity: Activity,
        intent: Intent?,
        camParameters: IONCameraParameters,
        onImage: (String) -> Unit,
        onMediaResult: (IONMediaResult) -> Unit,
        onError: (IONError) -> Unit
    ) {
        val intentEditedPath =
            intent?.getStringExtra(IONImageEditorActivity.IMAGE_OUTPUT_URI_EXTRAS)

        // NOTE: croppedUri/croppedFilePath are kept only for the legacy flow
        // The new API returns the edited image
        // via IMAGE_OUTPUT_URI_EXTRAS. This can be removed once
        // the legacy flow is removed.
        val sourcePath = when {
            camParameters.allowEdit && this.croppedUri != null && !this.croppedFilePath.isNullOrEmpty() -> this.croppedFilePath
            camParameters.allowEdit && !intentEditedPath.isNullOrEmpty() -> intentEditedPath
            else -> imageFilePath
        }

        if (camParameters.encodingType == JPEG) {
            try {
                exif.createInFile(sourcePath)
                exif.readExifData()
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }

        var savedSuccessfully = false

        // CB-5479 When this option is given the unchanged image should be saved
        // in the gallery and the modified image is saved in the temporary directory
        if (camParameters.saveToPhotoAlbum) {
            val srcUri: Uri? = sourcePath?.let {
                fileHelper.getUriForFile(
                    activity,
                    "$applicationId$authority",
                    File(it)
                )
            }

            savedSuccessfully = mediaProcessor.savePictureInGallery(activity, camParameters.encodingType, srcUri)
        }

        mediaProcessor.processCameraImage(
            activity = activity,
            intent = intent,
            sourcePath = sourcePath,
            "$applicationId$authority",
            camParameters = camParameters,
            savedSuccessfully = savedSuccessfully,
            onImage = onImage,
            onMediaResult = onMediaResult,
            onError = onError
        )
    }

    /**
     * Obtains the URI of the file containing the video that was just recorded and returns it.
     *
     * @param intent An Intent containing the video URI in the its data property.
     */
    suspend fun processResultFromVideo(
        activity: Activity,
        uri: Uri?,
        fromGallery: Boolean = false,
        includeMetadata: Boolean = false,
        onSuccess: (IONMediaResult) -> Unit,
        onError: (IONError) -> Unit
    ) {
        if (uri == null || uri.path == null) {
            onError(IONError.CAPTURE_VIDEO_ERROR)
            return
        }

        var videoFilePath: String?
        var recordedInGallery = false
        if (fromGallery) {
            videoFilePath = mediaHelper.getVideoPathFromUri(activity, uri)
            recordedInGallery = true
        } else {
            val fileName = uri.path?.split("/")?.last() ?: ""
            videoFilePath = fileHelper.getAbsoluteCachedFilePath(activity, fileName)
            recordedInGallery = false
            if (videoFilePath.isNotEmpty()) {
                val fileName = videoFilePath.split("/").last()
                fileHelper.storeFileNameInPrefs(fileName, activity)
            } else {
                onError(IONError.CAPTURE_VIDEO_ERROR)
            }
        }

        if (videoFilePath.isNullOrEmpty()) {
            onError(IONError.MEDIA_PATH_ERROR)
            return
        }

        mediaProcessor.processVideo(
            activity = activity,
            videoPath = videoFilePath,
            uri = uri,
            includeMetadata = includeMetadata,
            recordedInGallery = recordedInGallery,
            onSuccess = onSuccess,
            onError = onError
        )
    }

    fun onDestroy(activity: Activity) {
        deleteVideoFilesFromCache(activity)
    }

    override fun onScanCompleted(p0: String?, p1: Uri?) {
        conn?.disconnect()
    }

    override fun onMediaScannerConnected() {
        try {
            conn?.scanFile(scanMe.toString(), IONMediaType.PICTURE.mimeType)
        } catch (e: IllegalStateException) {
            Log.d(
                LOG_TAG,
                "Can't scan file in MediaScanner after taking picture"
            )
        }
    }

// ---------------------------------------------------------------------
// Legacy API (startActivityForResult) – kept for backward compatibility
// ---------------------------------------------------------------------

    /**
     * Take a picture with the camera.
     * @param activity  Activity object that will be necessary to take the picture
     * @param encodingType  JPEG or PNG.
     */
    fun takePicture(activity: Activity, returnType: Int, encodingType: Int) {
        // Save filename to fetch later (needed when allowEdit is true)
        val fileName = PICTURE_NAMES_PREFIX + SimpleDateFormat(
            TIME_FORMAT
        ).format(Date())
        fileHelper.saveStringSharedPreferences(
            activity,
            EDIT_FILE_NAME_KEY, fileName
        )

        // Specify file so that large image is captured and returned
        val photo: File = createCaptureFile(
            activity,
            encodingType,
            fileName
        )
        this.imageFilePath = photo.absolutePath
        this.imageUri = fileHelper.getUriForFile(activity, "$applicationId$authority", photo)

        mediaHelper.openDeviceCamera(activity, imageUri, returnType)
    }

    fun openCropActivity(activity: Activity?, picUri: Uri?, requestCode: Int?, destType: Int?) {
        val cropIntent = createCropIntent(activity, picUri)
        var code = EDIT_REQUEST_CODE
        if (requestCode != null && destType != null) {
            code = requestCode + destType
        }
        activity?.startActivityForResult(
            cropIntent,
            code
        )
    }

    private fun createCropIntent(activity: Activity?, picUri: Uri?): Intent {
        val cropIntent = Intent(activity, IONImageEditorActivity::class.java)
        croppedFilePath = createCaptureFile(
            activity,
            JPEG,
            System.currentTimeMillis().toString() + ""
        ).absolutePath
        croppedUri = Uri.parse(croppedFilePath)

        cropIntent.putExtra(IONImageEditorActivity.IMAGE_OUTPUT_URI_EXTRAS, croppedFilePath)
        cropIntent.putExtra(IONImageEditorActivity.IMAGE_INPUT_URI_EXTRAS, picUri.toString())
        return cropIntent
    }

}