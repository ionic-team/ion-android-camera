package io.ionic.libs.ioncameralib.manager

import android.app.Activity
import androidx.activity.result.ActivityResultLauncher
import android.content.ContentResolver
import android.content.ContentValues
import android.content.Intent
import android.media.ExifInterface
import android.media.MediaScannerConnection
import android.media.MediaScannerConnection.MediaScannerConnectionClient
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import io.ionic.libs.ioncameralib.helper.OSCAMRExifHelperInterface
import io.ionic.libs.ioncameralib.helper.OSCAMRFileHelperInterface
import io.ionic.libs.ioncameralib.helper.OSCAMRImageHelperInterface
import io.ionic.libs.ioncameralib.helper.OSCAMRMediaHelperInterface
import io.ionic.libs.ioncameralib.helper.OSCAMRGalleryHelper
import io.ionic.libs.ioncameralib.model.IONError
import io.ionic.libs.ioncameralib.model.IONMediaResult
import io.ionic.libs.ioncameralib.model.IONMediaType
import io.ionic.libs.ioncameralib.model.IONCameraParameters
import io.ionic.libs.ioncameralib.view.ImageEditorActivity
import io.ionic.libs.ioncameralib.processor.IONMediaProcessor
import kotlinx.coroutines.Job
import java.io.File
import java.io.FileNotFoundException
import java.io.IOException
import java.io.InputStream
import java.text.SimpleDateFormat
import java.util.Date


class CameraManager(
    private var applicationId: String,
    private var authority: String,
    private var exif: OSCAMRExifHelperInterface,
    private var fileHelper: OSCAMRFileHelperInterface,
    private var mediaHelper: OSCAMRMediaHelperInterface,
    private var imageHelper: OSCAMRImageHelperInterface
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
        private const val PNG = 1
        private const val JPEG_TYPE = "jpg"
        private const val PNG_TYPE = "png"
        private const val JPEG_EXTENSION = ".$JPEG_TYPE"
        private const val PNG_EXTENSION = ".$PNG_TYPE"
        private const val PNG_MIME_TYPE = "image/png"
        private const val JPEG_MIME_TYPE = "image/jpeg"
        private const val TIME_FORMAT = "yyyyMMdd_HHmmss"
        private const val LOG_TAG = "CameraManager"
        private const val PICTURE_NAMES_PREFIX = "PIC_"
        private const val VIDEO_NAMES_PREFIX = "VID_"
        private const val VIDEO_FORMAT = ".mp4"
        private const val STORE = "CameraStore"
        private const val EDIT_FILE_NAME_KEY = "EditFileName"
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
        camParameters: IONCameraParameters,
        onImage: (String) -> Unit,
        onMediaResult: (IONMediaResult) -> Unit,
        onError: (IONError) -> Unit
    ) {
        // Create an ExifHelper to save the exif data that is lost during compression
        //no longer necessary, this will be passed by dependency injection through the constructor
        //val exif = OSCAMRExifHelper()
        val sourcePath =
            if (camParameters.allowEdit && this.croppedUri != null) this.croppedFilePath else imageFilePath

        if (sourcePath == null) {
            onError(IONError.TAKE_PHOTO_ERROR)
            return
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
            val srcUri: Uri? = if (camParameters.allowEdit && this.croppedUri != null) {
                croppedUri
            } else {
                imageUri
            }

            savedSuccessfully = savePictureInGallery(activity, camParameters.encodingType, srcUri)
        }

        mediaProcessor.processCameraImage(
            activity = activity,
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

    private fun savePictureInGallery(activity: Activity, encodingType: Int, srcUri: Uri?): Boolean {
        return try {
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
            true
        } catch (e: Exception) {
            false
        }
    }

    @Throws(IOException::class)
    private fun writeTakenPictureToGalleryLowerThanAndroidQ(
        activity: Activity?,
        srcUri: Uri?,
        galleryUri: Uri?
    ) {
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
     * Converts output image format int value to string value of mime type.
     * @param outputFormat int Output format of camera API.
     * Must be value of either JPEG or PNG constant
     * @return String String value of mime type or empty string if mime type is not supported
     */
    private fun getMimetypeForFormat(outputFormat: Int): String? {
        if (outputFormat == PNG) return PNG_MIME_TYPE
        return if (outputFormat == JPEG) JPEG_MIME_TYPE else ""
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
}