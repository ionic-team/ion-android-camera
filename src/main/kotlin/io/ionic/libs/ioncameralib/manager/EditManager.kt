package io.ionic.libs.ioncameralib.manager

import android.app.Activity
import android.content.ContentResolver
import android.content.ContentValues
import android.content.Intent
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.util.Base64
import android.util.Log
import androidx.activity.result.ActivityResultLauncher
import io.ionic.libs.ioncameralib.helper.OSCAMRFileHelperInterface
import io.ionic.libs.ioncameralib.helper.OSCAMRImageHelperInterface
import io.ionic.libs.ioncameralib.model.IONError
import io.ionic.libs.ioncameralib.model.IONMediaMetadata
import io.ionic.libs.ioncameralib.model.IONMediaResult
import io.ionic.libs.ioncameralib.model.IONMediaType
import io.ionic.libs.ioncameralib.model.IONEditParameters
import io.ionic.libs.ioncameralib.view.ImageEditorActivity
import java.io.File
import androidx.core.net.toUri
import io.ionic.libs.ioncameralib.helper.OSCAMRGalleryHelper
import java.io.FileNotFoundException
import java.io.IOException
import java.io.InputStream
import java.text.SimpleDateFormat
import java.util.Date

/**
 * Contains edit functions
 */
class EditManager(
    private var applicationId: String,
    private var authority: String,
    private var fileHelper: OSCAMRFileHelperInterface,
    private var imageHelper: OSCAMRImageHelperInterface,
) {

    private var croppedUri: Uri? = null
    private var croppedFilePath: String? = null

    companion object {
        private const val JPEG = 0
        private const val PNG = 1
        private const val JPEG_TYPE = "jpg"
        private const val PNG_TYPE = "png"
        private const val JPEG_EXTENSION = ".$JPEG_TYPE"
        private const val PNG_EXTENSION = ".$PNG_TYPE"
        private const val IMAGE_MAX_RESOLUTION = 1080
        private const val IMAGE_MAX_QUALITY = 100
        private const val LOG_TAG = "EditManager"
        private const val TIME_FORMAT = "yyyyMMdd_HHmmss"

        private const val PNG_MIME_TYPE = "image/png"
        private const val JPEG_MIME_TYPE = "image/jpeg"
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
        onError: (IONError) -> Unit
    ) {
        val imageFile = File(pictureFilePath)
        if (!fileHelper.fileExists(imageFile)) {
            onError(IONError.FILE_DOES_NOT_EXIST_ERROR)
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
            onError(IONError.FETCH_IMAGE_FROM_URI_ERROR)
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
            onError(IONError.EDIT_IMAGE_ERROR)
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
        val cropIntent = Intent(activity, ImageEditorActivity::class.java)

        // creates output file
        croppedFilePath = createCaptureFile(
            activity,
            JPEG,
            System.currentTimeMillis().toString() + ""
        ).absolutePath
        croppedUri = croppedFilePath?.toUri()

        cropIntent.putExtra(ImageEditorActivity.IMAGE_OUTPUT_URI_EXTRAS, croppedFilePath)
        cropIntent.putExtra(ImageEditorActivity.IMAGE_INPUT_URI_EXTRAS, picUri.toString())

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
        editParameters: IONEditParameters,
        onImage: (String) -> Unit,
        onMediaResult: (IONMediaResult) -> Unit,
        onError: (IONError) -> Unit
    ) {
        val resultImagePath = intent?.getStringExtra(ImageEditorActivity.IMAGE_OUTPUT_URI_EXTRAS)
        if (resultImagePath.isNullOrEmpty()) {
            Log.d(LOG_TAG, "Image file path is null or empty")
            onError(IONError.EDIT_IMAGE_ERROR)
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
                onError(IONError.EDIT_IMAGE_ERROR)
                return
            }
            val mediaResult = createImageMediaResult(
                activity,
                resultImagePath,
                resultImageUri,
                editParameters.includeMetadata
            )
            if (mediaResult == null) {
                Log.d(LOG_TAG, "MediaResult is null")
                onError(IONError.EDIT_IMAGE_ERROR)
                return
            }
            if (editParameters.saveToGallery) {
                savePictureInGallery(
                    activity,
                    if (fileHelper.getFileExtension(resultImagePath) == JPEG_TYPE) 0 else 1,
                    resultImageUri)
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

    /**
     * Create a file in the applications temporary directory based upon the supplied encoding.
     *
     * @param encodingType of the image to be taken
     * @param fileName or resultant File object.
     * @return a File object pointing to the temporary picture
     */
    private fun createCaptureFile(
        activity: Activity?,
        encodingType: Int,
        fileName: String = ""
    ): File {
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
     * Transforms the image media item uri into a media result object.
     * @param imagePath  A string with the path for the image media item.
     * @return An object containing relevant information for the media item.
     *          Null if an error occurred.
     */
    private fun createImageMediaResult(
        activity: Activity,
        imagePath: String,
        mediaUri: Uri,
        includeMetadata: Boolean
    ): IONMediaResult? {
        var base64Image = ""
        var error: IONError? = null

        val file = File(imagePath)
        if (!fileHelper.fileExists(file)) return null

        val decodedImage = imageHelper.decodeFile(imagePath)

        if(decodedImage == null) return null

        val downsizedImage = imageHelper.downsizeBitmapIfNeeded(decodedImage, IMAGE_MAX_RESOLUTION)
        val compressedImage = imageHelper.compressBitmap(downsizedImage, 100)

        imageHelper.bitmapToBase64(compressedImage,
            resolution = IMAGE_MAX_RESOLUTION,
            quality = IMAGE_MAX_QUALITY,
            onSuccess = { base64Image = it },
            onError = { error = it }
        )

        if (error != null) {
            return null
        }

        var metadata: IONMediaMetadata? = null
        if (includeMetadata) {
            metadata = IONMediaMetadata(
                fileHelper.getFileSizeFromUri(activity, mediaUri),
                null,
                fileHelper.getFileExtension(imagePath),
                "${decodedImage.height}x${decodedImage.width}",
                fileHelper.getFileCreationDate(file),
            )
        }

        return IONMediaResult(IONMediaType.PICTURE.type, imagePath, base64Image, metadata, true)
    }

    private fun savePictureInGallery(activity: Activity, encodingType: Int, srcUri: Uri?) {
        val galleryPathVO: OSCAMRGalleryHelper = getPicturesPath(encodingType)
        val fileFromGalleryPath = File(galleryPathVO.galleryPath)
        val galleryUri = Uri.fromFile(fileFromGalleryPath)

        if (Build.VERSION.SDK_INT <= 28) {
            writeTakenPictureToGalleryLowerThanAndroidQ(activity, srcUri, galleryUri)
        } else {
            writeTakenPictureToGalleryStartingFromAndroidQ(
                activity,
                srcUri,
                galleryPathVO,
                encodingType
            )
        }
    }

    private fun getPicturesPath(encodingType: Int): OSCAMRGalleryHelper {
        val timeStamp =
            SimpleDateFormat(TIME_FORMAT).format(
                Date()
            )
        val imageFileName =
            "IMG_" + timeStamp + if (encodingType == JPEG) JPEG_EXTENSION else PNG_EXTENSION
        val storageDir = Environment.getExternalStoragePublicDirectory(
            Environment.DIRECTORY_PICTURES
        )
        storageDir.mkdirs()
        return OSCAMRGalleryHelper(storageDir.absolutePath, imageFileName)
    }

    @Throws(IOException::class)
    private fun writeTakenPictureToGalleryLowerThanAndroidQ(activity: Activity?, srcUri: Uri?, galleryUri: Uri?) {
        writeUncompressedImage(activity, srcUri, galleryUri)
        fileHelper.refreshGallery(activity, galleryUri)
    }

    @Throws(IOException::class)
    private fun writeTakenPictureToGalleryStartingFromAndroidQ(
        activity: Activity?,
        srcUri: Uri?,
        galleryPathVO: OSCAMRGalleryHelper,
        encodingType: Int
    ) {
        // Starting from Android Q, working with the ACTION_MEDIA_SCANNER_SCAN_FILE intent is deprecated
        // https://developer.android.com/reference/android/content/Intent#ACTION_MEDIA_SCANNER_SCAN_FILE
        // we must start working with the MediaStore from Android Q on.
        val resolver: ContentResolver? = activity?.contentResolver
        val contentValues = ContentValues()
        contentValues.put(MediaStore.MediaColumns.DISPLAY_NAME, galleryPathVO.galleryFileName)
        contentValues.put(MediaStore.MediaColumns.MIME_TYPE, getMimetypeForFormat(encodingType))
        val galleryOutputUri =
            resolver?.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
        val fileStream: InputStream? =
            fileHelper.getInputStreamFromUriString(
                srcUri.toString(),
                activity
            )
        fileHelper.writeUncompressedImage(activity, fileStream, galleryOutputUri)
    }

    /**
     * In the special case where the default width, height and quality are unchanged
     * we just write the file out to disk saving the expensive Bitmap.compress function.
     *
     * @param src
     * @throws FileNotFoundException
     * @throws IOException
     */
    @Throws(FileNotFoundException::class, IOException::class)
    private fun writeUncompressedImage(activity: Activity?, src: Uri?, dest: Uri?) {
        val fis: InputStream? = fileHelper.getInputStreamFromUriString(src.toString(), activity)
        fileHelper.writeUncompressedImage(activity, fis, dest)
    }

    /**
     * Converts output image format int value to string value of mime type.
     * @param outputFormat int Output format of camera API.
     * Must be value of either JPEG or PNG constant
     * @return String String value of mime type or empty string if mime type is not supported
     */
    private fun getMimetypeForFormat(outputFormat: Int): String? {
        if (outputFormat == PNG) return PNG_MIME_TYPE
        return if (outputFormat == JPEG) JPEG_MIME_TYPE else ""
    }

}