package io.ionic.libs.ioncameralib.processor

import android.app.Activity
import android.content.ContentResolver
import android.content.ContentValues
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory.Options
import android.graphics.Matrix
import android.media.ExifInterface
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import io.ionic.libs.ioncameralib.helper.IONCAMRExifHelperInterface
import io.ionic.libs.ioncameralib.helper.IONCAMRFileHelperInterface
import io.ionic.libs.ioncameralib.helper.IONCAMRGalleryHelper
import io.ionic.libs.ioncameralib.helper.IONCAMRImageHelperInterface
import io.ionic.libs.ioncameralib.helper.IONCAMRMediaHelperInterface
import io.ionic.libs.ioncameralib.model.IONCAMRCameraParameters
import io.ionic.libs.ioncameralib.model.IONCAMRError
import io.ionic.libs.ioncameralib.model.IONCAMRMediaMetadata
import io.ionic.libs.ioncameralib.model.IONCAMRMediaResult
import io.ionic.libs.ioncameralib.model.IONCAMRMediaType
import java.io.File
import java.io.FileNotFoundException
import java.io.IOException
import java.io.InputStream
import java.text.SimpleDateFormat
import java.util.Date
import kotlin.math.roundToInt

class IONCAMRMediaProcessor(
    private val exif: IONCAMRExifHelperInterface,
    private val fileHelper: IONCAMRFileHelperInterface,
    private val mediaHelper: IONCAMRMediaHelperInterface,
    private val imageHelper: IONCAMRImageHelperInterface
) {
    private var orientationCorrected = false
    private val TARGET_THUMBNAIL_DIMENSION: Int = 480

    companion object {
        private const val JPEG = 0
        private const val PNG = 1
        private const val JPEG_TYPE = "jpg"
        private const val PNG_TYPE = "png"
        private const val PNG_MIME_TYPE = "image/png"
        private const val JPEG_MIME_TYPE = "image/jpeg"
        private const val JPEG_EXTENSION = ".$JPEG_TYPE"
        private const val PNG_EXTENSION = ".$PNG_TYPE"
        private const val TIME_FORMAT = "yyyyMMdd_HHmmss"
        private const val LOG_TAG = "IONCAMRMediaProcessor"
        private const val CLOSING_INPUT_STREAM_ERROR = "Exception while closing file input stream."
        private const val IMAGE_MAX_RESOLUTION = 1080
        private const val IMAGE_MAX_QUALITY = 100
    }

    /**
     * Transforms the image media item uri into a media result object.
     * @param imagePath  A string with the path for the image media item.
     * @return An object containing relevant information for the media item.
     *          Null if an error occurred.
     */
    fun createImageMediaResult(
        activity: Activity,
        imagePath: String,
        mediaUri: Uri,
        includeMetadata: Boolean,
        camParameters: IONCAMRCameraParameters?,
        saved: Boolean = false
    ): IONCAMRMediaResult? {
        var base64Image = ""
        var error: IONCAMRError? = null

        val file = File(imagePath)
        if (!fileHelper.fileExists(file)) return null

        //camParameters are only set when createImageMediaResult is called from processResultFromCamera
        val image: Bitmap? = if (camParameters == null) {
            val exifOrientation = this.exif.getOrientationFromExif(ExifInterface(imagePath))
            val rotationDegrees = exifToDegrees(exifOrientation)
            val decodedImage = imageHelper.decodeFile(imagePath)
            var rotationMatrix: Matrix? = null

            if (rotationDegrees != 0) {
                rotationMatrix = Matrix().apply { setRotate(rotationDegrees.toFloat()) }
            }

            imageHelper.transformBitmapWithMatrix(decodedImage, rotationMatrix)
        } else {
            getScaledAndRotatedBitmap(activity, imagePath, camParameters)
        }

        if (image == null) return null

        val downsizedImage = imageHelper.downsizeBitmapIfNeeded(
            image,
            IMAGE_MAX_RESOLUTION
        )
        val compressedImage = imageHelper.compressBitmap(downsizedImage, 100)

        imageHelper.bitmapToBase64(
            compressedImage,
            resolution = camParameters?.let {
                minOf(
                    it.targetWidth, it.targetHeight,
                    IMAGE_MAX_RESOLUTION
                )
            } ?: IMAGE_MAX_RESOLUTION,
            quality = camParameters?.mQuality ?: IMAGE_MAX_QUALITY,
            onSuccess = { base64Image = it },
            onError = { error = it }
        )

        if (error != null) {
            return null
        }

        var metadata: IONCAMRMediaMetadata? = null
        if (includeMetadata) {
            val resolution = getMediaResolution(activity, true, imagePath, mediaUri)
            metadata = IONCAMRMediaMetadata(
                fileHelper.getFileSizeFromUri(activity, mediaUri),
                null,
                fileHelper.getFileExtension(imagePath),
                resolution,
                fileHelper.getFileCreationDate(file),
            )
        }

        return IONCAMRMediaResult(IONCAMRMediaType.PICTURE.type, imagePath, base64Image, metadata, saved)
    }

    /**
     * Transforms the image media item uri into a media result object.
     * @param imagePath  A string with the path for the image media item.
     * @return An object containing relevant information for the media item.
     *          Null if an error occurred.
     */
    private fun createEditedImageMediaResult(
        activity: Activity,
        imagePath: String,
        mediaUri: Uri,
        includeMetadata: Boolean,
        saved: Boolean = false
    ): IONCAMRMediaResult? {
        var base64Image = ""
        var error: IONCAMRError? = null

        val file = File(imagePath)
        if (!fileHelper.fileExists(file)) return null

        val decodedImage = imageHelper.decodeFile(imagePath)

        if (decodedImage == null) return null

        val downsizedImage = imageHelper.downsizeBitmapIfNeeded(
            decodedImage,
            IMAGE_MAX_RESOLUTION
        )
        val compressedImage = imageHelper.compressBitmap(downsizedImage, 100)

        imageHelper.bitmapToBase64(
            compressedImage,
            resolution = IMAGE_MAX_RESOLUTION,
            quality = IMAGE_MAX_QUALITY,
            onSuccess = { base64Image = it },
            onError = { error = it }
        )

        if (error != null) {
            return null
        }

        var metadata: IONCAMRMediaMetadata? = null
        if (includeMetadata) {
            metadata = IONCAMRMediaMetadata(
                fileHelper.getFileSizeFromUri(activity, mediaUri),
                null,
                fileHelper.getFileExtension(imagePath),
                "${decodedImage.height}x${decodedImage.width}",
                fileHelper.getFileCreationDate(file),
            )
        }

        return IONCAMRMediaResult(IONCAMRMediaType.PICTURE.type, imagePath, base64Image, metadata, saved)
    }

    fun processCameraImage(
        activity: Activity,
        intent: Intent?,
        sourcePath: String?,
        authority: String,
        camParameters: IONCAMRCameraParameters,
        savedSuccessfully: Boolean,
        onImage: (String) -> Unit,
        onMediaResult: (IONCAMRMediaResult) -> Unit,
        onError: (IONCAMRError) -> Unit
    ) {
        val mediaResult =
            sourcePath?.let {
                val imageUri = fileHelper.getUriForFile(
                    activity,
                    authority,
                    File(sourcePath)
                )
                if (imageUri == null) {
                    onError(IONCAMRError.TAKE_PHOTO_ERROR)
                    return
                }
                createImageMediaResult(
                    activity,
                    it,
                    imageUri,
                    camParameters.includeMetadata,
                    camParameters,
                    savedSuccessfully
                )
            }

        if (mediaResult == null) {
            onError(IONCAMRError.TAKE_PHOTO_ERROR)
            return
        }
        onMediaResult(mediaResult)
    }

    fun processEditedImage(
        activity: Activity,
        imagePath: String,
        uri: Uri,
        includeMetadata: Boolean,
        savedSuccessfully: Boolean,
        onMediaResult: (IONCAMRMediaResult) -> Unit,
        onError: (IONCAMRError) -> Unit
    ) {
        val mediaResult = createEditedImageMediaResult(
            activity,
            imagePath,
            uri,
            includeMetadata,
            savedSuccessfully
        )

        if (mediaResult == null) {
            onError(IONCAMRError.EDIT_IMAGE_ERROR)
            return
        }

        onMediaResult(mediaResult)
    }

    /**
     * Transforms the media item uri into a media result object.
     * @param activity  Activity object that will be necessary to launch the edit activity.
     * @param videoPath  A string with the path for the video media item.
     * @return An object containing relevant information for the media item.
     *          Null if an error occurred.
     */
    suspend fun createVideoMediaResult(
        activity: Activity,
        videoPath: String,
        mediaUri: Uri,
        includeMetadata: Boolean,
    ): IONCAMRMediaResult? {

        val file = File(videoPath)
        if (!fileHelper.fileExists(file)) return null

        val uri = fileHelper.getUriFromString(videoPath)
        val base64Thumbnail = getVideoThumbnailBase64(activity, uri) ?: return null

        var metadata: IONCAMRMediaMetadata? = null
        if (includeMetadata) {
            val resolution = getMediaResolution(activity, false, videoPath, uri)
            metadata = IONCAMRMediaMetadata(
                fileHelper.getFileSizeFromUri(activity, mediaUri),
                (mediaHelper.getVideoDuration(activity, uri).toDouble() / 1000).roundToInt(),
                fileHelper.getFileExtension(videoPath),
                resolution,
                fileHelper.getFileCreationDate(file),
            )
        }
        return IONCAMRMediaResult(
            IONCAMRMediaType.VIDEO.type,
            videoPath,
            base64Thumbnail,
            metadata,
            false //For Gallery keep this false
        )
    }

    suspend fun processVideo(
        activity: Activity,
        videoPath: String,
        uri: Uri,
        includeMetadata: Boolean,
        recordedInGallery: Boolean,
        onSuccess: (IONCAMRMediaResult) -> Unit,
        onError: (IONCAMRError) -> Unit
    ) {

        val file = File(videoPath)

        if (!fileHelper.fileExists(file)) {
            onError(IONCAMRError.MEDIA_PATH_ERROR)
            return
        }

        val thumbnail = getVideoThumbnailBase64(activity, uri)

        if (thumbnail == null) {
            onError(IONCAMRError.CAPTURE_VIDEO_ERROR)
            return
        }

        var metadata: IONCAMRMediaMetadata? = null
        if (includeMetadata) {
            val resolution = getMediaResolution(activity, false, videoPath, uri)
            metadata = IONCAMRMediaMetadata(
                fileHelper.getFileSizeFromUri(activity, uri),
                (mediaHelper.getVideoDuration(activity, uri).toDouble() / 1000).roundToInt(),
                fileHelper.getFileExtension(videoPath),
                resolution,
                fileHelper.getFileCreationDate(file),
            )
        }
        val mediaResult = IONCAMRMediaResult(
            IONCAMRMediaType.VIDEO.ordinal,
            videoPath,
            thumbnail,
            metadata,
            recordedInGallery
        )
        onSuccess(mediaResult)
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


    suspend fun getVideoThumbnailBase64(
        activity: Activity,
        videoUri: Uri
    ): String? {
        return mediaHelper.getThumbnailBase64String(activity, videoUri, TARGET_THUMBNAIL_DIMENSION)
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
        camParameters: IONCAMRCameraParameters
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
                this.exif.createInFile(filePath)
                this.exif.readExifData()
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

    internal fun savePictureInGallery(activity: Activity, encodingType: Int, srcUri: Uri?): Boolean {
        return try {
            val galleryPathVO: IONCAMRGalleryHelper = getPicturesPath(encodingType)
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

    private fun getPicturesPath(encodingType: Int): IONCAMRGalleryHelper {
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
        return IONCAMRGalleryHelper(storageDir.absolutePath, imageFileName)
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
        galleryPathVO: IONCAMRGalleryHelper,
        encodingType: Int
    ) {
        // Starting from Android Q, working with the ACTION_MEDIA_SCANNER_SCAN_FILE intent is deprecated
        // https://developer.android.com/reference/android/content/Intent#ACTION_MEDIA_SCANNER_SCAN_FILE
        // we must start working with the MediaStore from Android Q on.
        val resolver: ContentResolver? = activity?.contentResolver
        val contentValues = ContentValues()
        contentValues.put(MediaStore.MediaColumns.DISPLAY_NAME, galleryPathVO.galleryFileName)
        contentValues.put(MediaStore.MediaColumns.MIME_TYPE, getMimetypeForFormat(encodingType))
        contentValues.put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_PICTURES)
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


// ---------------------------------------------------------------------
// Legacy API (startActivityForResult) – kept for backward compatibility
// ---------------------------------------------------------------------


    /**
     * Applies all needed transformation to the image received from the gallery.
     *
     * @param intent   An Intent, which can return result data to the caller (various data can be attached to Intent "extras").
     */
    fun processResultFromGallery(
        activity: Activity?,
        intent: Intent,
        camParameters: IONCAMRCameraParameters,
        onSuccess: (String) -> Unit,
        onError: (IONCAMRError) -> Unit
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
        imageHelper.processPicture(
            bitmap, camParameters.encodingType, camParameters.mQuality,
            {
                onSuccess(it)
            },
            {
                onError(it)
            }
        )
        bitmap?.recycle()
        System.gc()
    }
}