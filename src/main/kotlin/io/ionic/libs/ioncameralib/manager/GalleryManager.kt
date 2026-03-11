package io.ionic.libs.ioncameralib.manager

import android.app.Activity
import android.app.Activity.RESULT_CANCELED
import android.app.Activity.RESULT_OK
import android.content.Intent
import android.net.Uri
import android.provider.MediaStore
import android.util.Log
import androidx.activity.result.ActivityResultLauncher
import io.ionic.libs.ioncameralib.helper.IONExifHelperInterface
import io.ionic.libs.ioncameralib.helper.IONFileHelperInterface
import io.ionic.libs.ioncameralib.helper.IONImageHelperInterface
import io.ionic.libs.ioncameralib.helper.IONMediaHelperInterface
import io.ionic.libs.ioncameralib.model.IONCameraParameters
import io.ionic.libs.ioncameralib.model.IONEditParameters
import io.ionic.libs.ioncameralib.model.IONError
import io.ionic.libs.ioncameralib.model.IONMediaResult
import io.ionic.libs.ioncameralib.model.IONMediaType
import io.ionic.libs.ioncameralib.processor.IONMediaProcessor
import io.ionic.libs.ioncameralib.view.IONOpenPhotoPickerActivity
import io.ionic.libs.ioncameralib.view.IONLoadingActivity
import io.ionic.libs.ioncameralib.view.IONImageEditorActivity
import java.io.File

class GalleryManager(
    private var exif: IONExifHelperInterface,
    private var fileHelper: IONFileHelperInterface,
    private var mediaHelper: IONMediaHelperInterface,
    private var imageHelper: IONImageHelperInterface
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
        private const val JPEG_TYPE = "jpg"
        private const val LOG_TAG = "GalleryManager"
        private const val ALLOW_MULTIPLE = "allowMultiple"
        private const val MEDIA_TYPE = "mediaType"
        private const val EDIT_REQUEST_CODE = 7
        private const val IMAGE_MAX_RESOLUTION = 1080
        private const val IMAGE_MAX_QUALITY = 100
    }

    /**
     * Opens a screen that allows users to select media from device gallery
     * @param activity  Activity object that will be necessary to launch the edit activity.
     * @param mediaType  The type of content the user is allowed to select.
     * @param allowMultiSelect  Whether or not the user should be allowed to select multiple items
     *                          from gallery.
     * @param launcher ActivityResultLauncher to use when launching the gallery activity
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
                    intent.getStringExtra(IONImageEditorActivity.IMAGE_OUTPUT_URI_EXTRAS) ?: ""

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

// ---------------------------------------------------------------------
// Legacy API (startActivityForResult) – kept for backward compatibility
// ---------------------------------------------------------------------

    /**
     * Get image from photo library.
     *
     * @param srcType           The album to get image from.
     * @param returnType        Set the type of image to return.
     */
    fun getImage(
        activity: Activity?,
        srcType: Int,
        returnType: Int,
        camParameters: IONCameraParameters
    ) {
        val intent = Intent()
        croppedUri = null
        croppedFilePath = null
        intent.type = IONMediaType.PICTURE.mimeType
        intent.action = Intent.ACTION_PICK
        if (camParameters.allowEdit) {
            intent.putExtra("crop", "true")
            if (camParameters.targetWidth > 0) {
                intent.putExtra("outputX", camParameters.targetWidth)
            }
            if (camParameters.targetHeight > 0) {
                intent.putExtra("outputY", camParameters.targetHeight)
            }
            if (camParameters.targetHeight > 0 && camParameters.targetWidth > 0 && camParameters.targetWidth == camParameters.targetHeight) {
                intent.putExtra("aspectX", 1)
                intent.putExtra("aspectY", 1)
            }
            val croppedFile = createCaptureFile(activity, JPEG)
            croppedFilePath = croppedFile.absolutePath
            croppedUri = Uri.fromFile(croppedFile)
            intent.putExtra(MediaStore.EXTRA_OUTPUT, croppedUri)
        }
        activity?.startActivityForResult(intent, (srcType + 1) * 16 + returnType + 1)
    }

    fun openCropActivity(activity: Activity?, picUri: Uri?, requestCode: Int?, destType: Int?) {
        val cropIntent = createCropIntent(activity, picUri)
        var code = EDIT_REQUEST_CODE
        if (requestCode != null && destType != null) {
            code = requestCode + destType
        }
        activity?.startActivityForResult(cropIntent, code)
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

    /**
     * Applies all needed transformation to the image received from the gallery.
     *
     * @param intent   An Intent, which can return result data to the caller (various data can be attached to Intent "extras").
     */
    fun processResultFromGallery(
        activity: Activity?,
        intent: Intent,
        camParameters: IONCameraParameters,
        onSuccess: (String) -> Unit,
        onError: (IONError) -> Unit
    ) {
        mediaProcessor.processResultFromGallery(
            activity = activity,
            intent = intent,
            camParameters = camParameters,
            onSuccess = onSuccess,
            onError = onError
        )
    }

    /**
     * Applies all needed transformation to the image received from the Edit screen.
     *
     * @param intent An intent, containing the file path of the image that was just edited.
     * @param fromUri Indicates if image editing was made from an input uri or base64.
     * @param onImage callback that will be used when base64 image should be returned.
     * @param onMediaResult callback that will be used when MediaResult object should be returned.
     * @param onError callback that will be used when an error occurs.
     */
    fun processResultFromEdit(
        activity: Activity,
        intent: Intent?,
        editParameters: IONEditParameters,
        onImage: (String) -> Unit,
        authority: String,
        onMediaResult: (IONMediaResult) -> Unit,
        onError: (IONError) -> Unit
    ) {
        val resultImagePath = intent?.getStringExtra(IONImageEditorActivity.IMAGE_OUTPUT_URI_EXTRAS)
        if (resultImagePath.isNullOrEmpty()) {
            Log.d(LOG_TAG, "Image file path is null or empty")
            onError(IONError.EDIT_IMAGE_ERROR)
            return
        }
        if (editParameters.fromUri) {
            val imageFile = File(resultImagePath)
            val resultImageUri = fileHelper.getUriForFile(
                activity,
                authority,
                imageFile
            )
            if (resultImageUri == null) {
                Log.d(LOG_TAG, "Image URI is null")
                onError(IONError.EDIT_IMAGE_ERROR)
                return
            }

            val mediaResult = mediaProcessor.createImageMediaResult(
                activity,
                resultImagePath,
                resultImageUri,
                editParameters.includeMetadata,
                null
            )

            if (mediaResult == null) {
                Log.d(LOG_TAG, "MediaResult is null")
                onError(IONError.EDIT_IMAGE_ERROR)
                return
            }
            if (editParameters.saveToGallery) {
                mediaProcessor.savePictureInGallery(
                    activity,
                    if (fileHelper.getFileExtension(resultImagePath) == JPEG_TYPE) 0 else 1,
                    resultImageUri
                )
            }
            onMediaResult(mediaResult)
        } else {
            val result = imageHelper.decodeFile(resultImagePath)
            imageHelper.bitmapToBase64(
                result = result,
                resolution = IMAGE_MAX_RESOLUTION,
                quality = IMAGE_MAX_QUALITY,
                onSuccess = { onImage(it) },
                onError = { onError(it) }
            )
        }
    }

}