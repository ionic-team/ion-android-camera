package io.ionic.libs.ioncameralib.manager

import android.app.Activity
import android.content.ContentResolver
import android.content.ContentValues
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory.Options
import android.graphics.Matrix
import android.media.ExifInterface
import android.media.MediaScannerConnection
import android.media.MediaScannerConnection.MediaScannerConnectionClient
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import io.ionic.libs.ioncameralib.helper.OSCAMRExifHelper
import io.ionic.libs.ioncameralib.helper.OSCAMRExifHelperInterface
import io.ionic.libs.ioncameralib.helper.OSCAMRFileHelperInterface
import io.ionic.libs.ioncameralib.helper.OSCAMRImageHelperInterface
import io.ionic.libs.ioncameralib.helper.OSCAMRMediaHelperInterface
import io.ionic.libs.ioncameralib.helper.OSCAMRGalleryHelper
import io.ionic.libs.ioncameralib.model.IONError
import io.ionic.libs.ioncameralib.model.IONMediaMetadata
import io.ionic.libs.ioncameralib.model.IONMediaResult
import io.ionic.libs.ioncameralib.model.IONMediaType
import io.ionic.libs.ioncameralib.model.IONParameters
//import com.outsystems.plugins.camera.view.ImageEditorActivity
//import com.outsystems.plugins.camera.view.LoadingActivity
//import com.outsystems.plugins.camera.view.OSCAMROpenPhotoPickerActivity
import kotlinx.coroutines.Job
import java.io.File
import java.io.FileNotFoundException
import java.io.IOException
import java.io.InputStream
import java.text.SimpleDateFormat
import java.util.Date
import kotlin.math.roundToInt

class OSCAMRController(
    private var applicationId: String,
    private var exif: OSCAMRExifHelperInterface,
    private var fileHelper: OSCAMRFileHelperInterface,
    private var mediaHelper: OSCAMRMediaHelperInterface,
    private var imageHelper: OSCAMRImageHelperInterface
) : MediaScannerConnectionClient {

    private var imageFilePath: String? = null
    private var imageUri: Uri? = null
    private var croppedUri: Uri? = null
    private var croppedFilePath: String? = null
    private var exifData: OSCAMRExifHelperInterface? = OSCAMRExifHelper()
    private var orientationCorrected = false

    private var conn: MediaScannerConnection? = null
    private var scanMe: Uri? = null

    private val job = Job()
    private val TARGET_THUMBNAIL_DIMENSION: Int = 480

    companion object {
        private const val JPEG = 0
        private const val PNG = 1
        private const val JPEG_TYPE = "jpg"
        private const val PNG_TYPE = "png"
        private const val JPEG_EXTENSION = ".$JPEG_TYPE"
        private const val PNG_EXTENSION = ".$PNG_TYPE"
        private const val PNG_MIME_TYPE = "image/png"
        private const val JPEG_MIME_TYPE = "image/jpeg"

        private const val GET_PICTURE = "Get Picture"

        private const val TIME_FORMAT = "yyyyMMdd_HHmmss"
        private const val LOG_TAG = "OSCAMRController"

        private const val CLOSING_INPUT_STREAM_ERROR = "Exception while closing file input stream."

        const val EDIT_REQUEST_CODE = 7
        const val EDIT_FROM_GALLERY_REQUEST_CODE = 11

        private const val AUTHORITY = ".camera.provider"

        private const val PICTURE_NAMES_PREFIX = "PIC_"
        private const val VIDEO_NAMES_PREFIX = "VID_"
        private const val VIDEO_FORMAT = ".mp4"
        private const val IMAGE_MAX_RESOLUTION = 1080
        private const val IMAGE_MAX_QUALITY = 100
        private const val STORE = "CameraStore"
        private const val EDIT_FILE_NAME_KEY = "EditFileName"
        private const val ALLOW_MULTIPLE = "allowMultiple"
        private const val MEDIA_TYPE = "mediaType"
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
     * @param returnType  Set the type of image to return.
     * @param encodingType  JPEG or PNG.
     */
    fun takePhoto(activity: Activity, returnType: Int, encodingType: Int) {

        Log.d("CAMERA_DEBUG", "=================> takePicture zzz")
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
        this.imageUri = fileHelper.getUriForFile(activity, "$applicationId.camera.provider", photo)

        mediaHelper.openDeviceCamera(activity, imageUri, returnType)
    }

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
        camParameters: IONParameters
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

    /**
     * Opens an activity with a UI to edit the provided image.
     * @param activity  Activity object that will be necessary to launch the edit activity.
     * @param image  String representing the image in Base64.
     */
   /* fun editImage(activity: Activity?, image: String, requestCode: Int?, destType: Int?) {
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
        val inputFileUri = Uri.parse(inputFilePath)
        val inputFile = File(inputFilePath)

        try {
            //Writes bitmap in temp file
            imageHelper.writeBitmapToFile(imageBitmap, inputFile)
            openCropActivity(activity, inputFileUri, requestCode, destType)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }*/

    /**
     * Opens an activity with a UI to edit the provided image.
     * @param activity  Activity object that will be necessary to launch the edit activity.
     * @param pictureFilePath  File path of the image to edit.
     */
  /*  fun editURIPicture(activity: Activity?, pictureFilePath: String, requestCode: Int?, destType: Int?, onError: (OSCAMRError) -> Unit) {
        val imageFile = File(pictureFilePath)
        if (!fileHelper.fileExists(imageFile)) {
            onError(OSCAMRError.FILE_DOES_NOT_EXIST_ERROR)
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
            onError(OSCAMRError.FETCH_IMAGE_FROM_URI_ERROR)
            return
        }
        val pictureUri = fileHelper.getUriForFile(
            activity,
            "$applicationId$AUTHORITY",
            imageFile
        )
        try {
            openCropActivity(activity, pictureUri, requestCode, destType)
        } catch (e: Exception) {
            e.printStackTrace()
            onError(OSCAMRError.EDIT_IMAGE_ERROR)
        }
    }*/



    /**
     * Calls the intent to open the device's camera to record a video.
     * @param activity  Activity object that will be necessary to launch the edit activity.
     * @param saveVideoToGallery Indicates if the recorded video should be saved to the device gallery
     */
    fun captureVideo(
        activity: Activity,
        saveVideoToGallery: Boolean = false,
        onError: (IONError) -> Unit
    ) {
        val videoFileUri = fileHelper.getUriForFile(
            activity,
            "$applicationId$AUTHORITY",
            createVideoFile(activity)
        )
        fileHelper.saveStringSharedPreferences(activity, STORE, videoFileUri.toString())
        val captureVideoIntent = Intent(MediaStore.ACTION_VIDEO_CAPTURE)

        if (mediaHelper.existsActivity(activity, captureVideoIntent)) {
            mediaHelper.openDeviceVideo(
                activity,
                captureVideoIntent,
                videoFileUri,
                saveVideoToGallery
            )
        } else {
            Log.d(LOG_TAG, "Error: You don't have a default camera for recording video.")
            onError(IONError.NO_CAMERA_AVAILABLE_ERROR)
        }
    }

    /**
     * Opens a screen that allows users to select media from device gallery
     * @param activity  Activity object that will be necessary to launch the edit activity.
     * @param mediaType  The type of content the user is allowed to select.
     * @param allowMultiSelect  Whether or not the user should be allowed to select multiple items
     *                          from gallery.
     * @param requestCode  Request code for receiving activity results.
     */
   /* fun chooseFromGallery(
        activity: Activity,
        mediaType: OSCAMRMediaType,
        allowMultiSelect: Boolean,
        requestCode: Int
    ) {
        try {
            activity.startActivityForResult(
                Intent(activity, OSCAMROpenPhotoPickerActivity::class.java).apply {
                    putExtra(ALLOW_MULTIPLE, allowMultiSelect)
                    putExtra(MEDIA_TYPE, mediaType.mimeType)
                },
                requestCode
            )
        } catch (e: Exception) {
            Log.e(LOG_TAG, e.message.toString())
        }
    }*/

    /**
     * Handles the result after users have selected media from device gallery.
     * @param activity  Activity object that will be necessary to launch the edit activity.
     * @param resultCode  The code resulting from the operation.
     * @param intent  The intent resulting from the operation
     * @param onSuccess The code the be executed if the operation was successfully.
     *                  Returns a list of media item results.
     * @param onError  he code the be executed if the operation was not successfully.
     */
   /* suspend fun onChooseFromGalleryResult(
        activity: Activity,
        resultCode: Int,
        intent: Intent?,
        includeMetadata: Boolean = false,
        allowEdit: Boolean = false,
        galleryMediaType: OSCAMRMediaType,
        onSuccess: (List<OSCAMRMediaResult>) -> Unit,
        onError: (OSCAMRError) -> Unit) {

        if (intent == null) {
            onError(OSCAMRError.CHOOSE_MULTIMEDIA_CANCELLED_ERROR)
            return
        }

        when (resultCode) {

            RESULT_OK -> {

                val uris = imageHelper.getResultUriFromIntent(intent)

                if (allowEdit && uris.size == 1 && galleryMediaType == OSCAMRMediaType.IMAGE) {
                    /*
                    This "destType" needs to be zero so the actual request code is passed to
                    the activity.
                    This looks strange, but I'm not brave enough to change the "openCropActivity"
                    logic. So I'll just play along for now.
                    */
                    openCropActivity(activity, uris.first(), EDIT_FROM_GALLERY_REQUEST_CODE, 0)
                    return
                }

                showLoadingScreen(activity)

                val results: MutableList<OSCAMRMediaResult> = mutableListOf()
                for (uri in uris) {

                    var fileLocation = fileHelper.getRealPath(uri, activity)

                    // when fileLocation = null means the file isn't available on the local filesystem.

                    if(fileLocation == null) {
                        fileLocation = fileHelper.getImagePathFromInputStreamUri(activity, uri) ?: continue
                    }

                    val mediaResult = createMediaResult(activity, fileLocation, uri, includeMetadata)
                    if (mediaResult == null) {
                        onError(OSCAMRError.GENERIC_CHOOSE_MULTIMEDIA_ERROR)
                        return
                    }
                    results.add(mediaResult)
                }

                onSuccess(results)
                dismissLoadingScreen(activity)
            }
            RESULT_CANCELED -> {
                onError(OSCAMRError.CHOOSE_MULTIMEDIA_CANCELLED_ERROR)
            }
            else -> {
                onError(OSCAMRError.GENERIC_CHOOSE_MULTIMEDIA_ERROR)
            }
        }
    }*/

    /**
     * Handles the result after users have edited media from device gallery.
     * @param activity  Activity object that will be necessary to launch the edit activity.
     * @param resultCode  The code resulting from the operation.
     * @param intent  The intent resulting from the operation
     * @param onSuccess The code the be executed if the operation was successfully.
     *                  Returns a list of media item results.
     * @param onError  he code the be executed if the operation was not successfully.
     */
   /* suspend fun onChooseFromGalleryEditResult(
        activity: Activity,
        resultCode: Int,
        intent: Intent?,
        includeMetadata: Boolean = false,
        onSuccess: (List<OSCAMRMediaResult>) -> Unit,
        onError: (OSCAMRError) -> Unit
    ) {
        when (resultCode) {

            RESULT_OK -> {
                if(intent == null) {
                    onError(OSCAMRError.EDIT_IMAGE_ERROR)
                    return
                }

                // An empty string here will trigger EDIT_IMAGE_ERROR later
                val fileLocation = intent.getStringExtra(ImageEditorActivity.IMAGE_OUTPUT_URI_EXTRAS) ?: ""

                val mediaResult = createMediaResult(
                    activity,
                    fileLocation,
                    fileHelper.getUriFromString(fileLocation),
                    includeMetadata
                )
                if (mediaResult == null) {
                    onError(OSCAMRError.EDIT_IMAGE_ERROR)
                    return
                }
                onSuccess(listOf(mediaResult))
            }
            RESULT_CANCELED -> {
                onError(OSCAMRError.EDIT_CANCELLED_ERROR)
            }
            else -> {
                onError(OSCAMRError.EDIT_IMAGE_ERROR)
            }
        }
    }*/

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

        val contentUri = fileHelper.getUriForFile(activity, activity.packageName + AUTHORITY, file)
        val intent = Intent(Intent.ACTION_VIEW)
        intent.setDataAndType(contentUri, mimeType)
        intent.flags = Intent.FLAG_ACTIVITY_NO_HISTORY
        intent.flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
        activity.startActivity(intent)
        onSuccess
    }

    /**
     * Transforms the media item uri into a media result object.
     * @param activity  Activity object that will be necessary to launch the edit activity.
     * @param filePath  The uri for the media item.
     * @return An object containing relevant information for the media item.
     *          Null if an error occurred.
     */
   /* private suspend fun createMediaResult(
        activity: Activity,
        filePath: String,
        uri: Uri,
        includeMetadata: Boolean,
    ): OSCAMRMediaResult? {

        val mimeType = fileHelper.getMimeType(filePath, activity)
        val isImage = mimeType != null && mimeType.startsWith("image")

        return if (isImage) {
            createImageMediaResult(activity, filePath, uri, includeMetadata, null)
        } else {
            createVideoMediaResult(activity, filePath, uri, includeMetadata)
        }
    }*/

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
        includeMetadata: Boolean,
        camParameters: IONParameters?
    ): IONMediaResult? {
        var base64Image = ""
        var error: IONError? = null

        val file = File(imagePath)
        if (!fileHelper.fileExists(file)) return null

        //camParameters are only set when createImageMediaResult is called from processResultFromCamera
        val image: Bitmap? = if (camParameters == null) {
            val exifOrientation = this.exif.getOrientationFromExif(ExifInterface(imagePath))
            val rotationDegrees = exifToDegrees(exifOrientation)
            val decodedImage = imageHelper.decodeFile(imagePath)
            var rotationMatrix: Matrix? = null

            if(rotationDegrees != 0) {
                rotationMatrix = Matrix().apply { setRotate(rotationDegrees.toFloat())  }
            }

            imageHelper.transformBitmapWithMatrix(decodedImage, rotationMatrix)
        }
        else {
            getScaledAndRotatedBitmap(activity, imagePath, camParameters)
        }

        if(image == null) return null

        val downsizedImage = imageHelper.downsizeBitmapIfNeeded(image, IMAGE_MAX_RESOLUTION)
        val compressedImage = imageHelper.compressBitmap(downsizedImage, 100)

        imageHelper.bitmapToBase64(compressedImage,
            resolution = camParameters?.let { minOf(it.targetWidth, it.targetHeight, IMAGE_MAX_RESOLUTION) } ?: IMAGE_MAX_RESOLUTION,
            quality = camParameters?.mQuality ?: IMAGE_MAX_QUALITY,
            onSuccess = { base64Image = it },
            onError = { error = it }
        )

        if (error != null) {
            return null
        }

        var metadata: IONMediaMetadata? = null
        if (includeMetadata) {
            val resolution = getMediaResolution(activity, true, imagePath, mediaUri)
            metadata = IONMediaMetadata(
                fileHelper.getFileSizeFromUri(activity, mediaUri),
                null,
                fileHelper.getFileExtension(imagePath),
                resolution,
                fileHelper.getFileCreationDate(file),
            )
        }

        return IONMediaResult(IONMediaType.PICTURE.type, imagePath, base64Image, metadata)
    }

    /**
     * Transforms the media item uri into a media result object.
     * @param activity  Activity object that will be necessary to launch the edit activity.
     * @param videoPath  A string with the path for the video media item.
     * @return An object containing relevant information for the media item.
     *          Null if an error occurred.
     */
   /* private suspend fun createVideoMediaResult(
        activity: Activity,
        videoPath: String,
        mediaUri: Uri,
        includeMetadata: Boolean,
    ): OSCAMRMediaResult? {

        val file = File(videoPath)
        if (!fileHelper.fileExists(file)) return null

        val uri = fileHelper.getUriFromString(videoPath)
        val base64Thumbnail = getVideoThumbnailBase64(activity, uri) ?: return null

        var metadata: IONMediaMetadata? = null
        if (includeMetadata) {
            val resolution = getMediaResolution(activity, false, videoPath, uri)
            metadata = IONMediaMetadata(
                fileHelper.getFileSizeFromUri(activity, mediaUri),
                (mediaHelper.getVideoDuration(activity, uri).toDouble() / 1000).roundToInt(),
                fileHelper.getFileExtension(videoPath),
                resolution,
                fileHelper.getFileCreationDate(file),
            )
        }
        return OSCAMRMediaResult(IONMediaType.VIDEO.type, videoPath, base64Thumbnail, metadata)
    }*/

   /* fun openCropActivity(activity: Activity?, picUri: Uri?, requestCode: Int?, destType: Int?) {
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
        var code = EDIT_REQUEST_CODE
        if (requestCode != null && destType != null) {
            code = requestCode + destType
        }
        activity?.startActivityForResult(
            cropIntent,
            code
        )
    }*/

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
     * @param fileName or resultant File object.
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
        camParameters: IONParameters,
        onImage: (String) -> Unit,
        onMediaResult: (IONMediaResult) -> Unit,
        onError: (IONError) -> Unit
    ) {
        // Create an ExifHelper to save the exif data that is lost during compression
        //no longer necessary, this will be passed by dependency injection through the constructor
        //val exif = OSCAMRExifHelper()
        val sourcePath =
            if (camParameters.allowEdit && this.croppedUri != null) this.croppedFilePath else imageFilePath
        if (camParameters.encodingType == JPEG) {
            try {
                exif.createInFile(sourcePath)
                exif.readExifData()
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
        var bitmap: Bitmap?

        // CB-5479 When this option is given the unchanged image should be saved
        // in the gallery and the modified image is saved in the temporary directory
        if (camParameters.saveToPhotoAlbum) {
            val srcUri: Uri? = if (camParameters.allowEdit && this.croppedUri != null) {
                croppedUri
            } else {
                imageUri
            }
            savePictureInGallery(activity, camParameters.encodingType, srcUri)
        }
        if (camParameters.latestVersion) {
            val mediaResult =
                sourcePath?.let {
                    val imageUri = fileHelper.getUriForFile(activity, "$applicationId$AUTHORITY", File(sourcePath))
                    if (imageUri == null) {
                        onError(IONError.TAKE_PHOTO_ERROR)
                        return
                    }
                    createImageMediaResult(
                        activity,
                        it,
                        imageUri,
                        camParameters.includeMetadata,
                        camParameters
                    )
                }
            if (mediaResult == null) {
                onError(IONError.TAKE_PHOTO_ERROR)
                return
            }
            onMediaResult(mediaResult)
        }
        else {
            //get bitmap
            bitmap = sourcePath?.let { getScaledAndRotatedBitmap(activity, it, camParameters) }
            if (bitmap == null) {
                // Try to get the bitmap from intent.
                if (intent != null) {
                    try {
                        // getExtras can throw different exceptions
                        val extras = intent.extras
                        if (extras != null) {
                            bitmap = extras["data"] as Bitmap?
                        }
                    } catch (e: Exception) {
                        // Don't let the exception bubble up, bitmap will be null (check below)
                    }
                }
            }
            //get base64 representation of bitmap
            var processPictureError = false
            imageHelper.processPicture(bitmap, camParameters.encodingType, camParameters.mQuality,
                {
                    onImage(it)
                },
                {
                    processPictureError = true
                    onError(it)
                }
            )
            if (processPictureError) {
                return
            }
        }
    }


    /**
     * Applies all needed transformation to the image received from the gallery.
     *
     * @param intent   An Intent, which can return result data to the caller (various data can be attached to Intent "extras").
     */
    /*fun processResultFromGallery(
        activity: Activity?,
        intent: Intent,
        camParameters: OSCAMRParameters,
        onSuccess: (String) -> Unit,
        onError: (OSCAMRError) -> Unit
    ) {
        var uri = intent.data
        val fileLocation = fileHelper.getRealPath(uri, activity)
        Log.d(LOG_TAG, "File location is: $fileLocation")
        val uriString = fileHelper.getUriString(uri)

        var bitmap: Bitmap? = null
        try {
            bitmap = getScaledAndRotatedBitmap(activity, uriString, camParameters)
        } catch (e: IOException) {
            e.printStackTrace()
        }
        imageHelper.processPicture(bitmap, camParameters.encodingType, camParameters.mQuality,
            {
                onSuccess(it)
            },
            {
                onError(it)
            }
        )
        bitmap?.recycle()
        System.gc()
    }*/

    /**
     * Applies all needed transformation to the image received from the Edit screen.
     *
     * @param intent An intent, containing the file path of the image that was just edited.
     * @param fromUri Indicates if image editing was made from an input uri or base64.
     * @param onImage callback that will be used when base64 image should be returned.
     * @param onMediaResult callback that will be used when MediaResult object should be returned.
     * @param onError callback that will be used when an error occurs.
     */
   /* fun processResultFromEdit(
        activity: Activity,
        intent: Intent?,
        editParameters: OSCAMREditParameters,
        onImage: (String) -> Unit,
        onMediaResult: (OSCAMRMediaResult) -> Unit,
        onError: (OSCAMRError) -> Unit
    ) {
        val resultImagePath = intent?.getStringExtra(ImageEditorActivity.IMAGE_OUTPUT_URI_EXTRAS)
        if (resultImagePath.isNullOrEmpty()) {
            Log.d(LOG_TAG, "Image file path is null or empty")
            onError(OSCAMRError.EDIT_IMAGE_ERROR)
            return
        }
        if (editParameters.fromUri) {
            val imageFile = File(resultImagePath)
            val resultImageUri = fileHelper.getUriForFile(
                activity,
                "$applicationId$AUTHORITY",
                imageFile
            )
            if (resultImageUri == null) {
                Log.d(LOG_TAG, "Image URI is null")
                onError(OSCAMRError.EDIT_IMAGE_ERROR)
                return
            }
            val mediaResult = createImageMediaResult(
                activity,
                resultImagePath,
                resultImageUri,
                editParameters.includeMetadata,
                null
            )
            if (mediaResult == null) {
                Log.d(LOG_TAG, "MediaResult is null")
                onError(OSCAMRError.EDIT_IMAGE_ERROR)
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
    }*/

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

    /**
     * Obtains the URI of the file containing the video that was just recorded and returns it.
     *
     * @param intent An Intent containing the video URI in the its data property.
     */
    /*suspend fun processResultFromVideo(
        activity: Activity,
        uri: Uri?,
        fromGallery: Boolean = false,
        includeMetadata: Boolean = false,
        onSuccess: (OSCAMRMediaResult) -> Unit,
        onError: (IONError) -> Unit
    ) {
        if (uri == null || uri.path == null) {
            onError(IONError.CAPTURE_VIDEO_ERROR)
            return
        }

        val thumbnail = getVideoThumbnailBase64(activity, uri) ?: ""
        var videoFilePath: String?
        if (fromGallery) {
            videoFilePath = mediaHelper.getVideoPathFromUri(activity, uri)
        } else {
            val fileName = uri.path?.split("/")?.last() ?: ""
            videoFilePath = fileHelper.getAbsoluteCachedFilePath(activity, fileName)
            if (videoFilePath.isNotEmpty()) {
                val fileName = videoFilePath.split("/").last()
                fileHelper.storeFileNameInPrefs(fileName, activity)
            } else {
                onError(IONError.CAPTURE_VIDEO_ERROR)
            }
        }
        val file = File(videoFilePath)
        if (videoFilePath != null && fileHelper.fileExists(file)) {
            var metadata: IONMediaMetadata? = null
            if (includeMetadata) {
                val resolution = getMediaResolution(activity, false, videoFilePath, uri)
                metadata = IONMediaMetadata(
                    fileHelper.getFileSizeFromUri(activity, uri),
                    (mediaHelper.getVideoDuration(activity, uri)
                        .toDouble() / 1000).roundToInt(),
                    fileHelper.getFileExtension(videoFilePath),
                    resolution,
                    fileHelper.getFileCreationDate(file),
                )
            }
            val mediaResult = OSCAMRMediaResult(
                IONMediaType.VIDEO.ordinal,
                videoFilePath,
                thumbnail,
                metadata
            )
            onSuccess(mediaResult)
        } else {
            onError(IONError.MEDIA_PATH_ERROR)
        }
    }*/

    private fun getMediaResolution(
        activity: Activity,
        isImage: Boolean,
        mediaPath: String,
        uri: Uri
    ): String {
        var resolutionPair: Pair<Int, Int> =
            if (isImage) mediaHelper.getImageResolution(mediaPath) else mediaHelper.getVideoResolution(
                activity,
                uri
            )
        val height = resolutionPair.first
        val width = resolutionPair.second
        return if (height >= width) "${height}x${width}" else "${width}x${height}"
    }

    private suspend fun getVideoThumbnailBase64(
        activity: Activity,
        videoUri: Uri
    ): String? {
        return mediaHelper.getThumbnailBase64String(activity, videoUri, TARGET_THUMBNAIL_DIMENSION)
    }

    fun onDestroy(activity: Activity) {
        deleteVideoFilesFromCache(activity)
        job.cancel()
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
     * Return a scaled and rotated bitmap based on the target width and height
     *
     * @param imageUrl
     * @return
     * @throws IOException
     */
    @Throws(IOException::class)
    private fun getScaledAndRotatedBitmap(
        activity: Activity?,
        imageUrl: String,
        camParameters: IONParameters
    ): Bitmap? {
        // If no new width or height were specified, and orientation is not needed return the original bitmap
        if (camParameters.targetWidth <= 0 && camParameters.targetHeight <= 0 && !camParameters.correctOrientation) {
            var fileStream: InputStream? = null
            var image: Bitmap? = null
            try {
                fileStream = fileHelper.getInputStreamFromUriString(imageUrl, activity)
                image = imageHelper.getBitmapForInputStream(fileStream)
            } catch (e: Exception) {
                Log.d(LOG_TAG, e.localizedMessage)
            } finally {
                if (fileStream != null) {
                    try {
                        fileStream.close()
                    } catch (e: IOException) {
                        Log.d(
                            LOG_TAG,
                            CLOSING_INPUT_STREAM_ERROR
                        )
                    }
                }
            }
            return image
        }


        /*  Copy the input stream to a temporary file on the device.
            We then use this temporary file to determine the width/height/orientation.
            This is the only way to determine the orientation of the photo coming from 3rd party providers (Google Drive, Dropbox,etc)
            This also ensures we create a scaled bitmap with the correct orientation

             We delete the temporary file once we are done
         */
        val localFile: File?
        val galleryUri: Uri?
        var rotate = 0
        try {
            val fileStream: InputStream? =
                fileHelper.getInputStreamFromUriString(imageUrl, activity)
            // Generate a temporary file
            val timeStamp =
                SimpleDateFormat(TIME_FORMAT).format(
                    Date()
                )
            val fileName =
                "IMG_" + timeStamp + if (camParameters.encodingType == JPEG) JPEG_EXTENSION else PNG_EXTENSION
            localFile = File(activity?.let { fileHelper.getTempDirectoryPath(it) } + fileName)
            galleryUri = Uri.fromFile(localFile)
            fileHelper.writeUncompressedImage(activity, fileStream, galleryUri)
            try {
                //  ExifInterface doesn't like the file:// prefix
                val filePath = fileHelper.getUriString(galleryUri).replace("file://", "")
                // read exifData of source
                exifData?.createInFile(filePath)
                exifData?.readExifData()
                // Use ExifInterface to pull rotation information
                if (camParameters.correctOrientation) {
                    rotate =
                        exifToDegrees(this.exif.getOrientationFromExif(ExifInterface(filePath)))
                }
            } catch (oe: Exception) {
                Log.d(
                    LOG_TAG,
                    "Unable to read Exif data: $oe"
                )
                rotate = 0
            }
        } catch (e: Exception) {
            Log.d(
                LOG_TAG,
                "Exception while getting input stream: $e"
            )
            return null
        }
        return try {
            // figure out the original width and height of the image
            val options = Options()
            options.inJustDecodeBounds = true
            var fileStream: InputStream? = null
            try {
                fileStream = fileHelper.getInputStreamFromUriString(
                    fileHelper.getUriString(galleryUri),
                    activity
                )
                imageHelper.decodeStream(fileStream, options)
            } finally {
                if (fileStream != null) {
                    try {
                        fileStream.close()
                    } catch (e: IOException) {
                        Log.d(
                            LOG_TAG,
                            CLOSING_INPUT_STREAM_ERROR
                        )
                    }
                }
            }

            if (imageHelper.areOptionsZero(options)) {
                return null
            }

            // User didn't specify output dimensions, but they need orientation
            if (camParameters.targetWidth <= 0 && camParameters.targetHeight <= 0) {
                camParameters.targetWidth = imageHelper.getOutWidth(options)
                camParameters.targetHeight = imageHelper.getOutHeight(options)
            }

            // Setup target width/height based on orientation
            val rotatedWidth: Int
            val rotatedHeight: Int
            var rotated = false
            if (rotate == 90 || rotate == 270) {
                rotatedWidth = imageHelper.getOutHeight(options)
                rotatedHeight = imageHelper.getOutWidth(options)
                rotated = true
            } else {
                rotatedWidth = imageHelper.getOutWidth(options)
                rotatedHeight = imageHelper.getOutHeight(options)
            }

            // determine the correct aspect ratio
            val widthHeight: IntArray = calculateAspectRatio(
                rotatedWidth,
                rotatedHeight,
                camParameters.targetWidth,
                camParameters.targetHeight
            )

            // Load in the smallest bitmap possible that is closest to the size we want
            options.inJustDecodeBounds = false
            options.inSampleSize = calculateSampleSize(
                rotatedWidth, rotatedHeight,
                widthHeight[0],
                widthHeight[1]
            )
            var unscaledBitmap: Bitmap?
            try {
                fileStream = fileHelper.getInputStreamFromUriString(
                    fileHelper.getUriString(galleryUri),
                    activity
                )
                unscaledBitmap = imageHelper.decodeStream(fileStream, options)
            } finally {
                if (fileStream != null) {
                    try {
                        fileStream.close()
                    } catch (e: IOException) {
                        Log.d(
                            LOG_TAG,
                            CLOSING_INPUT_STREAM_ERROR
                        )
                    }
                }
            }
            val scaledWidth = if (!rotated) widthHeight[0] else widthHeight[1]
            val scaledHeight = if (!rotated) widthHeight[1] else widthHeight[0]
            var scaledBitmap =
                imageHelper.getScaledBitmap(unscaledBitmap, scaledWidth, scaledHeight)
            if (scaledBitmap != unscaledBitmap) {
                unscaledBitmap?.recycle()
            }
            if (camParameters.correctOrientation && rotate != 0) {
                val matrix = Matrix()
                matrix.setRotate(rotate.toFloat())
                try {
                    scaledBitmap = imageHelper.transformBitmapWithMatrix(scaledBitmap, matrix)
                    this.orientationCorrected = true
                } catch (oom: OutOfMemoryError) {
                    this.orientationCorrected = false
                }
            }
            scaledBitmap
        } finally {
            // delete the temporary copy
            localFile?.delete()
        }
    }

    /**
     * Maintain the aspect ratio so the resulting image does not look smooshed
     *
     * @param origWidth
     * @param origHeight
     * @return
     */
    private fun calculateAspectRatio(
        origWidth: Int,
        origHeight: Int,
        targetWidth: Int,
        targetHeight: Int
    ): IntArray {
        var newWidth: Int = targetWidth
        var newHeight: Int = targetHeight

        // If no new width or height were specified return the original bitmap
        if (newWidth <= 0 && newHeight <= 0) {
            newWidth = origWidth
            newHeight = origHeight
        } else if (newWidth > 0 && newHeight <= 0) {
            newHeight = ((newWidth / origWidth.toDouble()) * origHeight).toInt()
        } else if (newWidth <= 0 && newHeight > 0) {
            newWidth = ((newHeight / origHeight.toDouble()) * origWidth).toInt()
        } else {
            val newRatio = newWidth / newHeight.toDouble()
            val origRatio = origWidth / origHeight.toDouble()
            if (origRatio > newRatio) {
                newHeight = newWidth * origHeight / origWidth
            } else if (origRatio < newRatio) {
                newWidth = newHeight * origWidth / origHeight
            }
        }
        val retVal = IntArray(2)
        retVal[0] = if (newWidth > 0) newWidth else 1
        retVal[1] = if (newHeight > 0) newHeight else 1
        return retVal
    }

    /**
     * Figure out what ratio we can load our image into memory at while still being bigger than
     * our desired width and height
     *
     * @param srcWidth
     * @param srcHeight
     * @param dstWidth
     * @param dstHeight
     * @return
     */
    private fun calculateSampleSize(
        srcWidth: Int,
        srcHeight: Int,
        dstWidth: Int,
        dstHeight: Int
    ): Int {
        val srcAspect = srcWidth.toFloat() / srcHeight.toFloat()
        val dstAspect = dstWidth.toFloat() / dstHeight.toFloat()
        return if (srcAspect > dstAspect) {
            srcWidth / dstWidth
        } else {
            srcHeight / dstHeight
        }
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

    private fun exifToDegrees(exifOrientation: Int): Int {
        return if (exifOrientation == ExifInterface.ORIENTATION_ROTATE_90) {
            90
        } else if (exifOrientation == ExifInterface.ORIENTATION_ROTATE_180) {
            180
        } else if (exifOrientation == ExifInterface.ORIENTATION_ROTATE_270) {
            270
        } else {
            0
        }
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

   /* private fun showLoadingScreen(activity: Activity) {
        activity.startActivity(Intent(activity, LoadingActivity::class.java))
    }

    private fun dismissLoadingScreen(activity: Activity) {
        activity.sendBroadcast(Intent(LoadingActivity.DISMISS_INTENT_FILTER))
    }*/

}