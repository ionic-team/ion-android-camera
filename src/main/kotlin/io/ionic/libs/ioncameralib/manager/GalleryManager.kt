package io.ionic.libs.ioncameralib.manager

import android.app.Activity
import android.app.Activity.RESULT_CANCELED
import android.app.Activity.RESULT_OK
import android.content.Intent
import android.net.Uri
import android.util.Log
import androidx.activity.result.ActivityResultLauncher
import io.ionic.libs.ioncameralib.helper.OSCAMRExifHelperInterface
import io.ionic.libs.ioncameralib.helper.OSCAMRFileHelperInterface
import io.ionic.libs.ioncameralib.helper.OSCAMRImageHelperInterface
import io.ionic.libs.ioncameralib.helper.OSCAMRMediaHelperInterface
import io.ionic.libs.ioncameralib.model.IONError
import io.ionic.libs.ioncameralib.model.IONMediaResult
import io.ionic.libs.ioncameralib.model.IONMediaType
import io.ionic.libs.ioncameralib.processor.IONMediaProcessor
import io.ionic.libs.ioncameralib.view.IONOpenPhotoPickerActivity
import io.ionic.libs.ioncameralib.view.IONLoadingActivity
import io.ionic.libs.ioncameralib.view.ImageEditorActivity
import java.io.File

class GalleryManager(
    private var exif: OSCAMRExifHelperInterface,
    private var fileHelper: OSCAMRFileHelperInterface,
    private var mediaHelper: OSCAMRMediaHelperInterface,
    private var imageHelper: OSCAMRImageHelperInterface
) {
    private var croppedUri: Uri? = null
    private var croppedFilePath: String? = null

    private val mediaProcessor = IONMediaProcessor(
        exif = exif,
        fileHelper = fileHelper,
        mediaHelper = mediaHelper,
        imageHelper = imageHelper
    )

    companion object {
        private const val JPEG = 0
        private const val PNG = 1
        private const val JPEG_TYPE = "jpg"
        private const val PNG_TYPE = "png"
        private const val JPEG_EXTENSION = ".$JPEG_TYPE"
        private const val PNG_EXTENSION = ".${PNG_TYPE}"
        private const val LOG_TAG = "GalleryManager"
        private const val ALLOW_MULTIPLE = "allowMultiple"
        private const val MEDIA_TYPE = "mediaType"
    }

    /**
     * Opens a screen that allows users to select media from device gallery
     * @param activity  Activity object that will be necessary to launch the edit activity.
     * @param mediaType  The type of content the user is allowed to select.
     * @param allowMultiSelect  Whether or not the user should be allowed to select multiple items
     *                          from gallery.
     */
    fun chooseFromGallery(
        activity: Activity,
        mediaType: IONMediaType,
        allowMultiSelect: Boolean,
        launcher: ActivityResultLauncher<Intent>
    ) {
        try {
            val intent = Intent(activity, IONOpenPhotoPickerActivity::class.java).apply {
                putExtra(ALLOW_MULTIPLE, allowMultiSelect)
                putExtra(MEDIA_TYPE, mediaType.mimeType)
            }

            launcher.launch(intent)
        } catch (e: Exception) {
            Log.e(LOG_TAG, e.message.toString())
        }
    }

    /**
     * Handles the result after users have selected media from device gallery.
     * @param activity  Activity object that will be necessary to launch the edit activity.
     * @param resultCode  The code resulting from the operation.
     * @param intent  The intent resulting from the operation
     * @param onSuccess The code the be executed if the operation was successfully.
     *                  Returns a list of media item results.
     * @param onError  he code the be executed if the operation was not successfully.
     */
    suspend fun onChooseFromGalleryResult(
        activity: Activity,
        resultCode: Int,
        intent: Intent?,
        includeMetadata: Boolean = false,
        onSuccess: (List<IONMediaResult>) -> Unit,
        onError: (IONError) -> Unit
    ) {

        if (intent == null) {
            onError(IONError.CHOOSE_MULTIMEDIA_CANCELLED_ERROR)
            return
        }

        when (resultCode) {

            RESULT_OK -> {

                val uris = imageHelper.getResultUriFromIntent(intent)

                showLoadingScreen(activity)

                val results: MutableList<IONMediaResult> = mutableListOf()
                for (uri in uris) {

                    var fileLocation = fileHelper.getRealPath(uri, activity)

                    // when fileLocation = null means the file isn't available on the local filesystem.

                    if (fileLocation == null) {
                        fileLocation =
                            fileHelper.getImagePathFromInputStreamUri(activity, uri) ?: continue
                    }

                    val mediaResult =
                        createMediaResult(activity, fileLocation, uri, includeMetadata)

                    if (mediaResult == null) {
                        onError(IONError.GENERIC_CHOOSE_MULTIMEDIA_ERROR)
                        dismissLoadingScreen(activity)
                        return
                    }
                    results.add(mediaResult)
                }

                onSuccess(results)
                dismissLoadingScreen(activity)
            }

            RESULT_CANCELED -> {
                onError(IONError.CHOOSE_MULTIMEDIA_CANCELLED_ERROR)
            }

            else -> {
                onError(IONError.GENERIC_CHOOSE_MULTIMEDIA_ERROR)
            }
        }
    }


    /**
     * Handles the result after users have edited media from device gallery.
     * @param activity  Activity object that will be necessary to launch the edit activity.
     * @param resultCode  The code resulting from the operation.
     * @param intent  The intent resulting from the operation
     * @param onSuccess The code the be executed if the operation was successfully.
     *                  Returns a list of media item results.
     * @param onError  he code the be executed if the operation was not successfully.
     */
    suspend fun onChooseFromGalleryEditResult(
        activity: Activity,
        resultCode: Int,
        intent: Intent?,
        includeMetadata: Boolean = false,
        onSuccess: (List<IONMediaResult>) -> Unit,
        onError: (IONError) -> Unit
    ) {
        when (resultCode) {

            RESULT_OK -> {
                if (intent == null) {
                    onError(IONError.EDIT_IMAGE_ERROR)
                    return
                }

                // An empty string here will trigger EDIT_IMAGE_ERROR later
                val fileLocation =
                    intent.getStringExtra(ImageEditorActivity.IMAGE_OUTPUT_URI_EXTRAS) ?: ""

                val mediaResult = createMediaResult(
                    activity,
                    fileLocation,
                    fileHelper.getUriFromString(fileLocation),
                    includeMetadata
                )
                if (mediaResult == null) {
                    onError(IONError.EDIT_IMAGE_ERROR)
                    return
                }
                onSuccess(listOf(mediaResult))
            }

            RESULT_CANCELED -> {
                onError(IONError.EDIT_CANCELLED_ERROR)
            }

            else -> {
                onError(IONError.EDIT_IMAGE_ERROR)
            }
        }
    }

    fun openCropActivity(
        activity: Activity?,
        picUri: Uri?,
        launcher: ActivityResultLauncher<Intent>
    ) {
        val cropIntent = Intent(activity, ImageEditorActivity::class.java)

        // creates output file
        croppedFilePath = createCaptureFile(
            activity,
            JPEG,
            System.currentTimeMillis().toString() + ""
        ).absolutePath
        croppedUri = Uri.parse(croppedFilePath)

        cropIntent.putExtra(ImageEditorActivity.IMAGE_OUTPUT_URI_EXTRAS, croppedFilePath)
        cropIntent.putExtra(ImageEditorActivity.IMAGE_INPUT_URI_EXTRAS, picUri.toString())

        launcher.launch(cropIntent)
    }

    /**
     * Create a file in the applications temporary directory based upon the supplied encoding.
     *
     * @param encodingType of the image to be taken
     * @param fileName or resultant File object.
     * @return a File object pointing to the temporary picture
     */
    fun createCaptureFile(activity: Activity?, encodingType: Int, fileName: String = ""): File {
        var fileName = fileName
        if (fileName.isEmpty()) {
            fileName = ".Pic"
        }
        fileName = if (encodingType == JPEG) {
            fileName + JPEG_EXTENSION
        } else if (encodingType == PNG) {
            fileName + PNG_EXTENSION
        } else {
            throw IllegalArgumentException("Invalid Encoding Type: $encodingType")
        }
        return fileHelper.createCaptureFile(activity, fileName)
    }

    /**
     * Transforms the media item uri into a media result object.
     * @param activity  Activity object that will be necessary to launch the edit activity.
     * @param filePath  The uri for the media item.
     * @return An object containing relevant information for the media item.
     *          Null if an error occurred.
     */
    private suspend fun createMediaResult(
        activity: Activity,
        filePath: String,
        uri: Uri,
        includeMetadata: Boolean,
    ): IONMediaResult? {

        val mimeType = fileHelper.getMimeType(filePath, activity)
        val isImage = mimeType != null && mimeType.startsWith("image")

        return if (isImage) {
            mediaProcessor.createImageMediaResult(activity, filePath, uri, includeMetadata, null)
        } else {
            mediaProcessor.createVideoMediaResult(activity, filePath, uri, includeMetadata)
        }
    }

    fun extractUris(intent: Intent?): List<Uri> {
        if (intent == null) return emptyList()
        return imageHelper.getResultUriFromIntent(intent)
    }

    private fun showLoadingScreen(activity: Activity) {
        activity.startActivity(Intent(activity, IONLoadingActivity::class.java))
    }

    private fun dismissLoadingScreen(activity: Activity) {
        activity.sendBroadcast(Intent(IONLoadingActivity.DISMISS_INTENT_FILTER))
    }
}