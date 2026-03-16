package io.ionic.libs.ioncameralib.manager

import android.app.Activity
import android.content.Intent
import android.graphics.drawable.Drawable
import android.net.Uri
import android.util.Base64
import android.util.Log
import androidx.activity.result.ActivityResultLauncher
import io.ionic.libs.ioncameralib.helper.IONCAMRFileHelperInterface
import io.ionic.libs.ioncameralib.helper.IONCAMRImageHelperInterface
import io.ionic.libs.ioncameralib.model.IONCAMRError
import io.ionic.libs.ioncameralib.model.IONCAMRMediaResult
import io.ionic.libs.ioncameralib.model.IONCAMREditParameters
import io.ionic.libs.ioncameralib.view.IONCAMRImageEditorActivity
import java.io.File
import androidx.core.net.toUri
import io.ionic.libs.ioncameralib.helper.IONCAMRExifHelperInterface
import io.ionic.libs.ioncameralib.helper.IONCAMRMediaHelperInterface
import io.ionic.libs.ioncameralib.processor.IONCAMRMediaProcessor

/**
 * Contains edit functions
 */
class IONCAMREditManager(
    private var applicationId: String,
    private var authority: String,
    private var exif: IONCAMRExifHelperInterface,
    private var fileHelper: IONCAMRFileHelperInterface,
    private var mediaHelper: IONCAMRMediaHelperInterface,
    private var imageHelper: IONCAMRImageHelperInterface,
) {

    private var croppedUri: Uri? = null
    private var croppedFilePath: String? = null

    private val mediaProcessor = IONCAMRMediaProcessor(
        exif = exif,
        fileHelper = fileHelper,
        mediaHelper = mediaHelper,
        imageHelper = imageHelper
    )

    companion object Companion {
        private const val JPEG = 0
        private const val PNG = 1
        private const val JPEG_TYPE = "jpg"
        private const val IMAGE_MAX_RESOLUTION = 1080
        private const val IMAGE_MAX_QUALITY = 100
        private const val LOG_TAG = "EditManager"
    }

    /**
     * Opens an activity with a UI to edit the provided image.
     * @param activity  Activity object that will be necessary to launch the edit activity.
     * @param image  String representing the image in Base64.
     * @param launcher ActivityResultLauncher to use when launching the edit activity
     */
    fun editImage(
        activity: Activity?,
        image: String,
        launcher: ActivityResultLauncher<Intent>
    ) {
        // put the following code inside the OSCAMRImageHelper which deals with Bitmap related stuff
        val imageByteArray: ByteArray = Base64.decode(image, Base64.NO_WRAP)
        val imageBitmap = imageHelper.base64toBitmap(imageByteArray)

        //Creates temp file
        val inputFilePath =
            createCaptureFile(
                activity,
                JPEG,
                System.currentTimeMillis().toString() + ""
            ).absolutePath
        val inputFileUri = inputFilePath.toUri()
        val inputFile = File(inputFilePath)

        try {
            //Writes bitmap in temp file
            imageHelper.writeBitmapToFile(imageBitmap, inputFile)
            openCropActivity(activity, inputFileUri, launcher)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * Opens an activity with a UI to edit the provided image.
     * @param activity  Activity object that will be necessary to launch the edit activity.
     * @param pictureFilePath  File path of the image to edit.
     * @param launcher ActivityResultLauncher to use when launching the edit activity
     */
    fun editURIPicture(
        activity: Activity?,
        pictureFilePath: String,
        launcher: ActivityResultLauncher<Intent>,
        onError: (IONCAMRError) -> Unit
    ) {
        val imageFile = File(pictureFilePath)
        if (!fileHelper.fileExists(imageFile)) {
            onError(IONCAMRError.FILE_DOES_NOT_EXIST_ERROR)
            return
        }
        val drawable: Drawable? = try {
            Drawable.createFromPath(pictureFilePath)
        } catch (ex: Exception) {
            ex.printStackTrace()
            null
        }
        if (drawable == null) {
            // provided file path does not seem to belong to an actual picture
            //  .e.g could be video, for which edit is not supported
            onError(IONCAMRError.FETCH_IMAGE_FROM_URI_ERROR)
            return
        }
        val pictureUri = fileHelper.getUriForFile(
            activity,
            "$applicationId$authority",
            imageFile
        )
        try {
            openCropActivity(activity, pictureUri, launcher)
        } catch (e: Exception) {
            e.printStackTrace()
            onError(IONCAMRError.EDIT_IMAGE_ERROR)
        }
    }

    /**
     * Opens the crop/edit activity with the provided image URI.
     * @param activity  Activity object that will be necessary to launch the edit activity.
     * @param picUri  URI of the picture to edit.
     * @param launcher ActivityResultLauncher to use when launching the edit activity
     */
    fun openCropActivity(
        activity: Activity?,
        picUri: Uri?,
        launcher: ActivityResultLauncher<Intent>
    ) {
        val cropIntent = Intent(activity, IONCAMRImageEditorActivity::class.java)

        // creates output file
        croppedFilePath = createCaptureFile(
            activity,
            JPEG,
            System.currentTimeMillis().toString() + ""
        ).absolutePath
        croppedUri = croppedFilePath?.toUri()

        cropIntent.putExtra(IONCAMRImageEditorActivity.IMAGE_OUTPUT_URI_EXTRAS, croppedFilePath)
        cropIntent.putExtra(IONCAMRImageEditorActivity.IMAGE_INPUT_URI_EXTRAS, picUri.toString())

        launcher.launch(cropIntent)
    }

    /**
     * Applies all needed transformation to the image received from the Edit screen.
     *
     * @param activity Activity object necessary to process the result.
     * @param intent An intent, containing the file path of the image that was just edited.
     * @param editParameters IONEditParameters object with parameters for edit
     * @param onImage callback that will be used when base64 image should be returned.
     * @param onMediaResult callback that will be used when MediaResult object should be returned.
     * @param onError callback that will be used when an error occurs.
     */
    fun processResultFromEdit(
        activity: Activity,
        intent: Intent?,
        editParameters: IONCAMREditParameters,
        onImage: (String) -> Unit,
        onMediaResult: (IONCAMRMediaResult) -> Unit,
        onError: (IONCAMRError) -> Unit
    ) {
        val resultImagePath = intent?.getStringExtra(IONCAMRImageEditorActivity.IMAGE_OUTPUT_URI_EXTRAS)
        if (resultImagePath.isNullOrEmpty()) {
            Log.d(LOG_TAG, "Image file path is null or empty")
            onError(IONCAMRError.EDIT_IMAGE_ERROR)
            return
        }
        if (editParameters.fromUri) {
            val imageFile = File(resultImagePath)
            val resultImageUri = fileHelper.getUriForFile(
                activity,
                "$applicationId$authority",
                imageFile
            )
            if (resultImageUri == null) {
                Log.d(LOG_TAG, "Image URI is null")
                onError(IONCAMRError.EDIT_IMAGE_ERROR)
                return
            }

            var savedSuccessfully = false

            if (editParameters.saveToGallery) {
                savedSuccessfully = mediaProcessor.savePictureInGallery(
                    activity,
                    if (fileHelper.getFileExtension(resultImagePath) == JPEG_TYPE) 0 else 1,
                    resultImageUri
                )
            }

            mediaProcessor.processEditedImage(
                activity = activity,
                imagePath = resultImagePath,
                uri = resultImageUri,
                includeMetadata = editParameters.includeMetadata,
                savedSuccessfully = savedSuccessfully,
                onMediaResult = onMediaResult,
                onError = onError
            )

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
}