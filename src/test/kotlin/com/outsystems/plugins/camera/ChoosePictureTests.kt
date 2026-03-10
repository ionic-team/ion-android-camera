package com.outsystems.plugins.camera

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Environment
import android.util.Base64
import android.util.Log
import com.outsystems.plugins.camera.controller.OSCAMRController
import com.outsystems.plugins.camera.mocks.OSCAMRMediaHelperMock
import com.outsystems.plugins.camera.mocks.OSCAMRExifHelperMock
import com.outsystems.plugins.camera.mocks.OSCAMRFileHelperMock
import com.outsystems.plugins.camera.mocks.OSCAMRImageHelperMock
import com.outsystems.plugins.camera.model.OSCAMREditParameters
import com.outsystems.plugins.camera.model.OSCAMRError
import com.outsystems.plugins.camera.model.OSCAMRParameters
import com.outsystems.plugins.camera.view.ImageEditorActivity
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito
import java.io.File
import java.lang.reflect.Field
import java.lang.reflect.Modifier

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
class ChoosePictureTests {

    private val mIntent = Mockito.mock(Intent::class.java)
    private val mEnvironment = Mockito.mockStatic(Environment::class.java)
    private val mLog = Mockito.mockStatic(Log::class.java)
    private val mUriStatic = Mockito.mockStatic(Uri::class.java)
    private val mUri = Mockito.mock(Uri::class.java)
    private val mFile = Mockito.mock(File::class.java)
    private val mBase64 = Mockito.mockStatic(Base64::class.java)
    private lateinit var mockActivity: Activity

    private var editOptions = OSCAMREditParameters(
        "",
        fromUri = false,
        saveToGallery = false,
        includeMetadata = false
    )

    companion object{
        private const val URI_STRING = "file://myUriString"
        private const val PROCESS_SUCCESS = "myImage"
        private const val FILE_LOCATION = "file://content/storage/emulated/sampleFileLocation"
        private const val PNG_MIME_TYPE = "image/png"
        private const val JPEG_MIME_TYPE = "image/jpeg"
        private const val FILE_LOCATION_TIME = "file://myPath/sampleFileLocation"

        @Throws(Exception::class)
        fun setFinalStatic(field: Field, newValue: Any?) {
            field.setAccessible(true)
            val modifiersField: Field = Field::class.java.getDeclaredField("modifiers")
            modifiersField.setAccessible(true)
            modifiersField.setInt(field, field.modifiers and Modifier.FINAL.inv())
            field.set(null, newValue)
        }
    }

    @Before
    fun before() {
        mockActivity = Mockito.mock(Activity::class.java)
    }

    @Test
    fun givenGetPictureWhenProcessResultNoMimeTypeSuccessThenSuccess() {

        val camParameters = OSCAMRParameters(
            60,
            1600,
            1600,
            0,
            0,
            allowEdit = false,
            correctOrientation = true,
            saveToPhotoAlbum = false,
            includeMetadata = false,
            latestVersion = false
        )

        val exifHelperMock = OSCAMRExifHelperMock()
        val fileHelperMock = OSCAMRFileHelperMock()
        val camHelperMock = OSCAMRMediaHelperMock()
        val imgHelperMock = OSCAMRImageHelperMock()

        val camController = OSCAMRController("someAppId", exifHelperMock, fileHelperMock, camHelperMock, imgHelperMock)

        Mockito.`when`(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)).thenReturn(mFile)

        val byteArray = ByteArray(1)
        Mockito.`when`(Base64.decode("imageInBinary", Base64.NO_WRAP)).thenReturn(byteArray)

        imgHelperMock.processPicSuccess = true
        fileHelperMock.fileLocationNotNull = true

        camController.getImage(null, 0, 0, camParameters)
        camController.processResultFromGallery(null, mIntent, camParameters,
            {
                assertEquals(it, PROCESS_SUCCESS)
            },
            {
                fail()
            })

        mEnvironment.close()
        mLog.close()
        mUriStatic.close()
        mBase64.close()
    }

    @Test
    fun givenGetPictureWhenProcessResultVideoSuccessThenSuccess() {

        val camParameters = OSCAMRParameters(
            60,
            1600,
            1600,
            0,
            1,
            allowEdit = false,
            correctOrientation = true,
            saveToPhotoAlbum = false,
            includeMetadata = false,
            latestVersion = false
        )

        val exifHelperMock = OSCAMRExifHelperMock()
        val fileHelperMock = OSCAMRFileHelperMock()
        val camHelperMock = OSCAMRMediaHelperMock()
        val imgHelperMock = OSCAMRImageHelperMock()

        val camController = OSCAMRController("someAppId", exifHelperMock, fileHelperMock, camHelperMock, imgHelperMock)

        Mockito.`when`(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)).thenReturn(mFile)

        val byteArray = ByteArray(1)
        Mockito.`when`(Base64.decode("imageInBinary", Base64.NO_WRAP)).thenReturn(byteArray)

        imgHelperMock.processPicSuccess = true
        fileHelperMock.fileLocationNotNull = true

        camController.getImage(null, 0, 0, camParameters)
        camController.processResultFromGallery(null, mIntent, camParameters,
            {
                assertEquals(it, PROCESS_SUCCESS)
            },
            {
                fail()
            })

        mEnvironment.close()
        mLog.close()
        mUriStatic.close()
        mBase64.close()
    }

    @Test
    fun givenGetPictureWhenProcessResultAllMediaSuccessThenSuccess() {

        val camParameters = OSCAMRParameters(
            60,
            1600,
            1600,
            0,
            2,
            allowEdit = false,
            correctOrientation = true,
            saveToPhotoAlbum = false,
            includeMetadata = false,
            latestVersion = false
        )

        val exifHelperMock = OSCAMRExifHelperMock()
        val fileHelperMock = OSCAMRFileHelperMock()
        val camHelperMock = OSCAMRMediaHelperMock()
        val imgHelperMock = OSCAMRImageHelperMock()

        val camController = OSCAMRController("someAppId", exifHelperMock, fileHelperMock, camHelperMock, imgHelperMock)

        Mockito.`when`(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)).thenReturn(mFile)

        val byteArray = ByteArray(1)
        Mockito.`when`(Base64.decode("imageInBinary", Base64.NO_WRAP)).thenReturn(byteArray)

        imgHelperMock.processPicSuccess = true
        fileHelperMock.fileLocationNotNull = true

        camController.getImage(null, 0, 0, camParameters)
        camController.processResultFromGallery(null, mIntent, camParameters,
            {
                assertEquals(it, PROCESS_SUCCESS)
            },
            {
                fail()
            })

        mEnvironment.close()
        mLog.close()
        mUriStatic.close()
        mBase64.close()
    }

    @Test
    fun givenNegativeDimensionsGetPictureWhenProcessResultSuccessThenSuccess() {

        val camParameters = OSCAMRParameters(
            60,
            -1,
            -1,
            0,
            0,
            allowEdit = false,
            correctOrientation = false,
            saveToPhotoAlbum = false,
            includeMetadata = false,
            latestVersion = false
        )

        val exifHelperMock = OSCAMRExifHelperMock()
        val fileHelperMock = OSCAMRFileHelperMock()
        val camHelperMock = OSCAMRMediaHelperMock()
        val imgHelperMock = OSCAMRImageHelperMock()

        val camController = OSCAMRController("someAppId", exifHelperMock, fileHelperMock, camHelperMock, imgHelperMock)

        Mockito.`when`(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)).thenReturn(mFile)

        val byteArray = ByteArray(1)
        Mockito.`when`(Base64.decode("imageInBinary", Base64.NO_WRAP)).thenReturn(byteArray)

        imgHelperMock.processPicSuccess = true
        fileHelperMock.fileLocationNotNull = true
        fileHelperMock.mimeType = JPEG_MIME_TYPE

        camController.getImage(null, 0, 0, camParameters)
        camController.processResultFromGallery(null, mIntent, camParameters,
            {
                assertEquals(it, PROCESS_SUCCESS)
            },
            {
                fail()
            })

        mEnvironment.close()
        mLog.close()
        mUriStatic.close()
        mBase64.close()
    }

    @Test
    fun givenGetPictureWhenProcessResultMimeTypeJPEGSuccessThenSuccess() {

        val camParameters = OSCAMRParameters(
            60,
            1600,
            1600,
            0,
            0,
            allowEdit = false,
            correctOrientation = true,
            saveToPhotoAlbum = false,
            includeMetadata = false,
            latestVersion = false
        )

        val exifHelperMock = OSCAMRExifHelperMock()
        val fileHelperMock = OSCAMRFileHelperMock()
        val camHelperMock = OSCAMRMediaHelperMock()
        val imgHelperMock = OSCAMRImageHelperMock()

        val camController = OSCAMRController("someAppId", exifHelperMock, fileHelperMock, camHelperMock, imgHelperMock)

        Mockito.`when`(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)).thenReturn(mFile)

        val byteArray = ByteArray(1)
        Mockito.`when`(Base64.decode("imageInBinary", Base64.NO_WRAP)).thenReturn(byteArray)

        imgHelperMock.bitmapToBase64Success = true
        imgHelperMock.processPicSuccess = true
        fileHelperMock.fileLocationNotNull = true
        fileHelperMock.mimeType = JPEG_MIME_TYPE

        camController.getImage(null, 0, 0, camParameters)
        camController.processResultFromGallery(null, mIntent, camParameters,
            {
                assertEquals(it, PROCESS_SUCCESS)
            },
            {
                fail()
            })

        mEnvironment.close()
        mLog.close()
        mUriStatic.close()
        mBase64.close()
    }

    @Test
    fun givenGetPictureWhenProcessResultMimeTypeJPEGErrorThenError() {

        val camParameters = OSCAMRParameters(
            60,
            1600,
            1600,
            0,
            0,
            allowEdit = false,
            correctOrientation = true,
            saveToPhotoAlbum = false,
            includeMetadata = false,
            latestVersion = false
        )

        val exifHelperMock = OSCAMRExifHelperMock()
        val fileHelperMock = OSCAMRFileHelperMock()
        val camHelperMock = OSCAMRMediaHelperMock()
        val imgHelperMock = OSCAMRImageHelperMock()

        val camController = OSCAMRController("someAppId", exifHelperMock, fileHelperMock, camHelperMock, imgHelperMock)

        Mockito.`when`(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)).thenReturn(mFile)

        val byteArray = ByteArray(1)
        Mockito.`when`(Base64.decode("imageInBinary", Base64.NO_WRAP)).thenReturn(byteArray)

        imgHelperMock.bitmapToBase64Success = true
        imgHelperMock.processPicSuccess = false
        fileHelperMock.fileLocationNotNull = true
        fileHelperMock.mimeType = JPEG_MIME_TYPE

        camController.getImage(null, 0, 0, camParameters)
        camController.processResultFromGallery(null, mIntent, camParameters,
            {
                fail()
            },
            {
                assertEquals(it.code, OSCAMRError.PROCESS_IMAGE_ERROR.code)
                assertEquals(it.description, OSCAMRError.PROCESS_IMAGE_ERROR.description)
            })

        mEnvironment.close()
        mLog.close()
        mUriStatic.close()
        mBase64.close()
    }

    @Test
    fun givenGetPictureWhenProcessResultMimeTypePNGSuccessThenSuccess() {

        val camParameters = OSCAMRParameters(
            60,
            1600,
            1600,
            1,
            0,
            allowEdit = false,
            correctOrientation = true,
            saveToPhotoAlbum = false,
            includeMetadata = false,
            latestVersion = false
        )

        val exifHelperMock = OSCAMRExifHelperMock()
        val fileHelperMock = OSCAMRFileHelperMock()
        val camHelperMock = OSCAMRMediaHelperMock()
        val imgHelperMock = OSCAMRImageHelperMock()

        val camController = OSCAMRController("someAppId", exifHelperMock, fileHelperMock, camHelperMock, imgHelperMock)

        Mockito.`when`(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)).thenReturn(mFile)

        val byteArray = ByteArray(1)
        Mockito.`when`(Base64.decode("imageInBinary", Base64.NO_WRAP)).thenReturn(byteArray)

        imgHelperMock.processPicSuccess = true
        imgHelperMock.bitmapToBase64Success = true
        fileHelperMock.fileLocationNotNull = true
        fileHelperMock.mimeType = PNG_MIME_TYPE

        camController.getImage(null, 0, 0, camParameters)
        camController.processResultFromGallery(null, mIntent, camParameters,
            {
                assertEquals(it, PROCESS_SUCCESS)
            },
            {
                fail()
            })

        mEnvironment.close()
        mLog.close()
        mUriStatic.close()
        mBase64.close()
    }

    @Test
    fun givenGetPictureWhenProcessResultMimeTypePNGErrorThenError() {

        val camParameters = OSCAMRParameters(
            60,
            1600,
            1600,
            1,
            0,
            allowEdit = false,
            correctOrientation = true,
            saveToPhotoAlbum = false,
            includeMetadata = false,
            latestVersion = false
        )

        val exifHelperMock = OSCAMRExifHelperMock()
        val fileHelperMock = OSCAMRFileHelperMock()
        val camHelperMock = OSCAMRMediaHelperMock()
        val imgHelperMock = OSCAMRImageHelperMock()

        val camController = OSCAMRController("someAppId", exifHelperMock, fileHelperMock, camHelperMock, imgHelperMock)

        Mockito.`when`(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)).thenReturn(mFile)

        val byteArray = ByteArray(1)
        Mockito.`when`(Base64.decode("imageInBinary", Base64.NO_WRAP)).thenReturn(byteArray)

        imgHelperMock.processPicSuccess = false
        imgHelperMock.bitmapToBase64Success = true
        fileHelperMock.fileLocationNotNull = true
        fileHelperMock.mimeType = PNG_MIME_TYPE

        camController.getImage(null, 0, 0, camParameters)
        camController.processResultFromGallery(null, mIntent, camParameters,
            {
                fail()
            },
            {
                assertEquals(it.code, OSCAMRError.PROCESS_IMAGE_ERROR.code)
                assertEquals(it.description, OSCAMRError.PROCESS_IMAGE_ERROR.description)
            })

        mEnvironment.close()
        mLog.close()
        mUriStatic.close()
        mBase64.close()
    }

    @Test
    fun givenGetPictureAllowEditWhenProcessResultSuccessThenSuccess() {
        val camParameters = OSCAMRParameters(
            60,
            1600,
            1600,
            1,
            0,
            allowEdit = true,
            correctOrientation = true,
            saveToPhotoAlbum = false,
            includeMetadata = false,
            latestVersion = false
        )

        val exifHelperMock = OSCAMRExifHelperMock()
        val fileHelperMock = OSCAMRFileHelperMock()
        val camHelperMock = OSCAMRMediaHelperMock()
        val imgHelperMock = OSCAMRImageHelperMock()

        val camController = OSCAMRController("someAppId", exifHelperMock, fileHelperMock, camHelperMock, imgHelperMock)

        Mockito.`when`(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)).thenReturn(mFile)

        val byteArray = ByteArray(1)
        Mockito.`when`(Base64.decode("imageInBinary", Base64.NO_WRAP)).thenReturn(byteArray)

        Mockito.doReturn(FILE_LOCATION).`when`(mIntent).getStringExtra(ImageEditorActivity.IMAGE_OUTPUT_URI_EXTRAS)

        imgHelperMock.processPicSuccess = true
        imgHelperMock.bitmapToBase64Success = true
        fileHelperMock.fileLocationNotNull = true
        fileHelperMock.mimeType = PNG_MIME_TYPE

        camController.getImage(null, 0, 0, camParameters)
        camController.openCropActivity(null, mUri, 1, 1)
        camController.processResultFromEdit(mockActivity, mIntent, editOptions,
            {
                assertEquals(it, OSCAMRImageHelperMock.SAMPLE_BASE64_THUMBNAIL)
            },
            {
                fail()
            },
            {
                fail()
            })

        mEnvironment.close()
        mLog.close()
        mUriStatic.close()
        mBase64.close()
    }

    @Test
    fun givenGetPictureAllowEditWhenProcessResultErrorThenError() {

        val camParameters = OSCAMRParameters(
            60,
            1600,
            1600,
            1,
            0,
            allowEdit = true,
            correctOrientation = true,
            saveToPhotoAlbum = false,
            includeMetadata = false,
            latestVersion = false
        )

        val exifHelperMock = OSCAMRExifHelperMock()
        val fileHelperMock = OSCAMRFileHelperMock()
        val camHelperMock = OSCAMRMediaHelperMock()
        val imgHelperMock = OSCAMRImageHelperMock()

        val camController = OSCAMRController("someAppId", exifHelperMock, fileHelperMock, camHelperMock, imgHelperMock)

        Mockito.`when`(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)).thenReturn(mFile)

        val byteArray = ByteArray(1)
        Mockito.`when`(Base64.decode("imageInBinary", Base64.NO_WRAP)).thenReturn(byteArray)

        imgHelperMock.processPicSuccess = true
        imgHelperMock.bitmapToBase64Success = false
        fileHelperMock.fileLocationNotNull = true
        fileHelperMock.mimeType = PNG_MIME_TYPE

        camController.getImage(null, 0, 0, camParameters)
        camController.openCropActivity(null, mUri, 1, 1)
        camController.processResultFromEdit(mockActivity, mIntent, editOptions,
            {
                fail()
            },
            {
                fail()
            },
            {
                assertEquals(it.code, OSCAMRError.EDIT_IMAGE_ERROR.code)
                assertEquals(it.description, OSCAMRError.EDIT_IMAGE_ERROR.description)
            })

        mEnvironment.close()
        mLog.close()
        mUriStatic.close()
        mBase64.close()
    }

}