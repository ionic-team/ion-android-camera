package io.ionic.libs.ioncameralib

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Environment
import android.util.Log
import io.ionic.libs.ioncameralib.manager.IONCAMRCameraManager
import io.ionic.libs.ioncameralib.mocks.IONCAMRMediaHelperMock
import io.ionic.libs.ioncameralib.mocks.IONExifHelperMock
import io.ionic.libs.ioncameralib.mocks.IONCAMRFileHelperMock
import io.ionic.libs.ioncameralib.mocks.IONCAMRImageHelperMock
import io.ionic.libs.ioncameralib.model.IONCAMRError
import io.ionic.libs.ioncameralib.model.IONCAMRCameraParameters
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.mockito.ArgumentMatchers.anyString
import org.mockito.MockedStatic
import org.mockito.Mockito
import java.io.File

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
class TakePictureTests {

    private val mIntent = Mockito.mock(Intent::class.java)
    private val mockUri = Mockito.mock(Uri::class.java)
    private val mFile = Mockito.mock(File::class.java)

    private lateinit var mEnvironment: MockedStatic<Environment>
    private lateinit var mLog: MockedStatic<Log>
    private lateinit var mUri: MockedStatic<Uri>
    private lateinit var mockActivity: Activity

    companion object{
        private const val BASE_64 = "base64"
        private const val METADATA_SIZE = 23456L
        private const val METADATA_FORMAT = "jpg"
        private const val METADATA_RESOLUTION = "1080x1080"
        private const val METADATA_CREATION_DATE = "2023-03-30T09:01:26Z"
    }

    @Before
    fun before() {
        mEnvironment = Mockito.mockStatic(Environment::class.java)
        mLog = Mockito.mockStatic(Log::class.java)
        mUri = Mockito.mockStatic(Uri::class.java)

        mockActivity = Mockito.mock(Activity::class.java)
        Mockito.`when`(Uri.parse(anyString())).thenReturn(mockUri)
        Mockito.`when`(mockUri.scheme).thenReturn("http")
        Mockito.`when`(mockUri.host).thenReturn("example.com")
        Mockito.`when`(mockUri.path).thenReturn("/path/to/resource")
    }

    @After
    fun after() {
        mEnvironment.close()
        mLog.close()
        mUri.close()
    }

    @Test
    fun givenAPI30TakePictureCalledJPEGAndDataUriAndUriNullWhenProcessResultFromCameraThenError() {
        val camParameters = IONCAMRCameraParameters(
            20,
            -1,
            -1,
            0,
            0,
            allowEdit = false,
            correctOrientation = false,
            saveToPhotoAlbum = true,
            includeMetadata = true
        )

        val exifHelperMock = IONExifHelperMock()
        val fileHelperMock = IONCAMRFileHelperMock()
        val camHelperMock = IONCAMRMediaHelperMock()
        val imgHelperMock = IONCAMRImageHelperMock()

        val IONCAMRCameraManager = IONCAMRCameraManager(
            applicationId = "someAppId",
            exif = exifHelperMock,
            fileHelper = fileHelperMock,
            mediaHelper = camHelperMock,
            imageHelper = imgHelperMock
        )

        Mockito.`when`(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)).thenReturn(mFile)

        imgHelperMock.processPicSuccess = true
        fileHelperMock.isUriNull = true

        IONCAMRCameraManager.takePicture(mockActivity, 0, 0)
        IONCAMRCameraManager.processResultFromCamera(mockActivity, mIntent, camParameters,
            onMediaResult = {
                fail()
            },
            onError = {
                assertEquals(it.code, IONCAMRError.TAKE_PHOTO_ERROR.code)
                assertEquals(it.description, IONCAMRError.TAKE_PHOTO_ERROR.description)
            })
    }

    @Test
    fun givenAPI30TakePictureCalledJPEGAndDataUriAndUriNotNullFileStreamNullWhenProcessResultFromCameraThenError() {
        val camParameters = IONCAMRCameraParameters(
            20,
            -1,
            -1,
            0,
            0,
            allowEdit = false,
            correctOrientation = false,
            saveToPhotoAlbum = true,
            includeMetadata = true
        )

        val exifHelperMock = IONExifHelperMock()
        val fileHelperMock = IONCAMRFileHelperMock()
        val camHelperMock = IONCAMRMediaHelperMock()
        val imgHelperMock = IONCAMRImageHelperMock()

        val IONCAMRCameraManager = IONCAMRCameraManager(
            applicationId = "someAppId",
            exif = exifHelperMock,
            fileHelper = fileHelperMock,
            mediaHelper = camHelperMock,
            imageHelper = imgHelperMock
        )

        Mockito.`when`(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)).thenReturn(mFile)

        imgHelperMock.processPicSuccess = true
        fileHelperMock.fileExists = true
        fileHelperMock.isUriNull = false
        fileHelperMock.isFileStreamNull = true

        IONCAMRCameraManager.takePicture(mockActivity, 0, 0)
        IONCAMRCameraManager.processResultFromCamera(mockActivity, mIntent, camParameters,
            onMediaResult = {
                fail()
            },
            onError = {
                assertEquals(it.code, IONCAMRError.TAKE_PHOTO_ERROR.code)
                assertEquals(it.description, IONCAMRError.TAKE_PHOTO_ERROR.description)
            })
    }

    @Test
    fun givenAPI30TakePictureCalledJPEGAndDataUriAndUriNotNullBitmapNullWhenProcessResultFromCameraThenError() {
        val camParameters = IONCAMRCameraParameters(
            20,
            -1,
            -1,
            0,
            0,
            allowEdit = false,
            correctOrientation = false,
            saveToPhotoAlbum = true,
            includeMetadata = true
        )

        val exifHelperMock = IONExifHelperMock()
        val fileHelperMock = IONCAMRFileHelperMock()
        val camHelperMock = IONCAMRMediaHelperMock()
        val imgHelperMock = IONCAMRImageHelperMock()

        val IONCAMRCameraManager = IONCAMRCameraManager(
            applicationId = "someAppId",
            exif = exifHelperMock,
            fileHelper = fileHelperMock,
            mediaHelper = camHelperMock,
            imageHelper = imgHelperMock
        )

        Mockito.`when`(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)).thenReturn(mFile)

        imgHelperMock.processPicSuccess = true
        fileHelperMock.fileExists = true
        fileHelperMock.isUriNull = false
        fileHelperMock.isFileStreamNull = false
        imgHelperMock.isBitmapNull = true

        IONCAMRCameraManager.takePicture(mockActivity, 0, 0)
        IONCAMRCameraManager.processResultFromCamera(mockActivity, mIntent, camParameters,
            onMediaResult = {
                fail()
            },
            onError = {
                assertEquals(it.code, IONCAMRError.TAKE_PHOTO_ERROR.code)
                assertEquals(it.description, IONCAMRError.TAKE_PHOTO_ERROR.description)
            })
    }

    @Test
    fun givenAPI30TakePictureCalledJPEGAndDataUriAndUriNotNullBitmapNotNullWhenProcessResultFromCameraThenSuccess() {
        val camParameters = IONCAMRCameraParameters(
            20,
            -1,
            -1,
            0,
            0,
            allowEdit = false,
            correctOrientation = false,
            saveToPhotoAlbum = true,
            includeMetadata = true
        )

        val exifHelperMock = IONExifHelperMock()
        val fileHelperMock = IONCAMRFileHelperMock()
        val camHelperMock = IONCAMRMediaHelperMock()
        val imgHelperMock = IONCAMRImageHelperMock()

        val IONCAMRCameraManager = IONCAMRCameraManager(
            applicationId = "someAppId",
            exif = exifHelperMock,
            fileHelper = fileHelperMock,
            mediaHelper = camHelperMock,
            imageHelper = imgHelperMock
        )

        Mockito.`when`(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)).thenReturn(mFile)

        imgHelperMock.processPicSuccess = true
        fileHelperMock.fileExists = true
        fileHelperMock.isUriNull = false
        fileHelperMock.isFileStreamNull = false
        imgHelperMock.isBitmapNull = false
        imgHelperMock.bitmapToBase64Success = true
        fileHelperMock.fileExtension = "jpg"

        IONCAMRCameraManager.takePicture(mockActivity, 0, 0)
        IONCAMRCameraManager.processResultFromCamera(mockActivity, mIntent, camParameters,
            onMediaResult = {
                assertEquals(it.type, 0)
                assertTrue(it.uri.contains("myFile"))
                assertEquals(it.thumbnail, BASE_64)
                assertNotEquals(it.metadata, null)
                assertEquals(it.metadata?.size, METADATA_SIZE)
                assertEquals(it.metadata?.duration, null)
                assertEquals(it.metadata?.resolution, METADATA_RESOLUTION)
                assertEquals(it.metadata?.format, METADATA_FORMAT)
                assertEquals(it.metadata?.creationDate, METADATA_CREATION_DATE)
            },
            onError = {
                fail()
            })
    }

}
