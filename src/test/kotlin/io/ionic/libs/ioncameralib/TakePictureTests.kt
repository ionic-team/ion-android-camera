package io.ionic.libs.ioncameralib

import android.app.Activity
import android.content.Intent
import android.media.ExifInterface
import android.net.Uri
import android.os.Environment
import android.util.Log
import io.ionic.libs.ioncameralib.manager.CameraManager
import io.ionic.libs.ioncameralib.mocks.IONMediaHelperMock
import io.ionic.libs.ioncameralib.mocks.IONExifHelperMock
import io.ionic.libs.ioncameralib.mocks.IONFileHelperMock
import io.ionic.libs.ioncameralib.mocks.IONImageHelperMock
import io.ionic.libs.ioncameralib.model.IONError
import io.ionic.libs.ioncameralib.model.IONCameraParameters
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
        private const val PROCESS_SUCCESS = "myImage"
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
    fun givenTakePictureNotCalledJPEGWhenProcessResultFromCameraThenTakePhotoError() {

        val camParameters = IONCameraParameters(
            20,
            -1,
            -1,
            0,
            0,
            allowEdit = false,
            correctOrientation = true,
            saveToPhotoAlbum = true,
            includeMetadata = false,
            latestVersion = false
        )

        val exifHelperMock = IONExifHelperMock()
        val fileHelperMock = IONFileHelperMock()
        val camHelperMock = IONMediaHelperMock()
        val imgHelperMock = IONImageHelperMock()

        val cameraManager = CameraManager(
            applicationId = "someAppId",
            authority = ".camera.provider",
            exif = exifHelperMock,
            fileHelper = fileHelperMock,
            mediaHelper = camHelperMock,
            imageHelper = imgHelperMock
        )

        Mockito.`when`(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)).thenReturn(mFile)

        cameraManager.processResultFromCamera(mockActivity, mIntent, camParameters,
            onImage = {
                fail()
            },
            onMediaResult = {
                fail()
            },
            onError = {
                assertEquals(it.code, IONError.PROCESS_IMAGE_ERROR.code)
                assertEquals(it.description, IONError.PROCESS_IMAGE_ERROR.description)
            })
    }

    @Test
    fun givenTakePictureNotCalledPNGWhenProcessResultFromCameraThenTakePhotoError() {

        val camParameters = IONCameraParameters(
            20,
            -1,
            -1,
            1,
            0,
            allowEdit = false,
            correctOrientation = true,
            saveToPhotoAlbum = true,
            includeMetadata = false,
            latestVersion = false
        )

        val exifHelperMock = IONExifHelperMock()
        val fileHelperMock = IONFileHelperMock()
        val camHelperMock = IONMediaHelperMock()
        val imgHelperMock = IONImageHelperMock()

        val cameraManager = CameraManager(
            applicationId = "someAppId",
            authority = ".camera.provider",
            exif = exifHelperMock,
            fileHelper = fileHelperMock,
            mediaHelper = camHelperMock,
            imageHelper = imgHelperMock
        )

        Mockito.`when`(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)).thenReturn(mFile)

        cameraManager.processResultFromCamera(mockActivity, mIntent, camParameters,
            onImage = {
                fail()
            },
            onMediaResult = {
                fail()
            },
            onError = {
                assertEquals(it.code, IONError.PROCESS_IMAGE_ERROR.code)
                assertEquals(it.description, IONError.PROCESS_IMAGE_ERROR.description)
            })
    }

    @Test
    fun givenAPI28TakePictureCalledBitmapNullWhenProcessResultFromCameraThenError() {

        val camParameters = IONCameraParameters(
            20,
            -1,
            -1,
            0,
            0,
            allowEdit = false,
            correctOrientation = false,
            saveToPhotoAlbum = true,
            includeMetadata = false,
            latestVersion = false
        )

        val exifHelperMock = IONExifHelperMock()
        val fileHelperMock = IONFileHelperMock()
        val camHelperMock = IONMediaHelperMock()
        val imgHelperMock = IONImageHelperMock()

        val cameraManager = CameraManager(
            applicationId = "someAppId",
            authority = ".camera.provider",
            exif = exifHelperMock,
            fileHelper = fileHelperMock,
            mediaHelper = camHelperMock,
            imageHelper = imgHelperMock
        )

        Mockito.`when`(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)).thenReturn(mFile)

        cameraManager.takePicture(mockActivity, 0, 0)
        cameraManager.processResultFromCamera(mockActivity, mIntent, camParameters,
            onImage = {
                fail()
            },
            onMediaResult = {
                fail()
            },
            onError = {
                assertEquals(it.code, IONError.PROCESS_IMAGE_ERROR.code)
                assertEquals(it.description, IONError.PROCESS_IMAGE_ERROR.description)
            })
    }

    @Test
    fun givenAPI28TakePictureCalledJPEGAndDataUriAndProcessOKWhenProcessResultFromCameraThenSuccess() {
        val camParameters = IONCameraParameters(
            20,
            -1,
            -1,
            0,
            0,
            allowEdit = false,
            correctOrientation = false,
            saveToPhotoAlbum = true,
            includeMetadata = false,
            latestVersion = false
        )

        val exifHelperMock = IONExifHelperMock()
        val fileHelperMock = IONFileHelperMock()
        val camHelperMock = IONMediaHelperMock()
        val imgHelperMock = IONImageHelperMock()

        val cameraManager = CameraManager(
            applicationId = "someAppId",
            authority = ".camera.provider",
            exif = exifHelperMock,
            fileHelper = fileHelperMock,
            mediaHelper = camHelperMock,
            imageHelper = imgHelperMock
        )

        Mockito.`when`(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)).thenReturn(mFile)

        imgHelperMock.processPicSuccess = true

        cameraManager.takePicture(mockActivity, 0, 0)
        cameraManager.processResultFromCamera(mockActivity, mIntent, camParameters,
            onImage = {
                assertEquals(PROCESS_SUCCESS, it)
            },
            onMediaResult = {
                fail()
            },
            onError = {
                fail()
            })
    }

    @Test
    fun givenAPI30TakePictureCalledJPEGAndDataUriAndUriNullWhenProcessResultFromCameraThenError() {
        val camParameters = IONCameraParameters(
            20,
            -1,
            -1,
            0,
            0,
            allowEdit = false,
            correctOrientation = false,
            saveToPhotoAlbum = true,
            includeMetadata = true,
            latestVersion = true
        )

        val exifHelperMock = IONExifHelperMock()
        val fileHelperMock = IONFileHelperMock()
        val camHelperMock = IONMediaHelperMock()
        val imgHelperMock = IONImageHelperMock()

        val cameraManager = CameraManager(
            applicationId = "someAppId",
            authority = ".camera.provider",
            exif = exifHelperMock,
            fileHelper = fileHelperMock,
            mediaHelper = camHelperMock,
            imageHelper = imgHelperMock
        )

        Mockito.`when`(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)).thenReturn(mFile)

        imgHelperMock.processPicSuccess = true
        fileHelperMock.isUriNull = true

        cameraManager.takePicture(mockActivity, 0, 0)
        cameraManager.processResultFromCamera(mockActivity, mIntent, camParameters,
            onImage = {
                fail()
            },
            onMediaResult = {
                fail()
            },
            onError = {
                assertEquals(it.code, IONError.TAKE_PHOTO_ERROR.code)
                assertEquals(it.description, IONError.TAKE_PHOTO_ERROR.description)
            })
    }

    @Test
    fun givenAPI30TakePictureCalledJPEGAndDataUriAndUriNotNullFileStreamNullWhenProcessResultFromCameraThenError() {
        val camParameters = IONCameraParameters(
            20,
            -1,
            -1,
            0,
            0,
            allowEdit = false,
            correctOrientation = false,
            saveToPhotoAlbum = true,
            includeMetadata = true,
            latestVersion = true
        )

        val exifHelperMock = IONExifHelperMock()
        val fileHelperMock = IONFileHelperMock()
        val camHelperMock = IONMediaHelperMock()
        val imgHelperMock = IONImageHelperMock()

        val cameraManager = CameraManager(
            applicationId = "someAppId",
            authority = ".camera.provider",
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

        cameraManager.takePicture(mockActivity, 0, 0)
        cameraManager.processResultFromCamera(mockActivity, mIntent, camParameters,
            onImage = {
                fail()
            },
            onMediaResult = {
                fail()
            },
            onError = {
                assertEquals(it.code, IONError.TAKE_PHOTO_ERROR.code)
                assertEquals(it.description, IONError.TAKE_PHOTO_ERROR.description)
            })
    }

    @Test
    fun givenAPI30TakePictureCalledJPEGAndDataUriAndUriNotNullBitmapNullWhenProcessResultFromCameraThenError() {
        val camParameters = IONCameraParameters(
            20,
            -1,
            -1,
            0,
            0,
            allowEdit = false,
            correctOrientation = false,
            saveToPhotoAlbum = true,
            includeMetadata = true,
            latestVersion = true
        )

        val exifHelperMock = IONExifHelperMock()
        val fileHelperMock = IONFileHelperMock()
        val camHelperMock = IONMediaHelperMock()
        val imgHelperMock = IONImageHelperMock()

        val cameraManager = CameraManager(
            applicationId = "someAppId",
            authority = ".camera.provider",
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

        cameraManager.takePicture(mockActivity, 0, 0)
        cameraManager.processResultFromCamera(mockActivity, mIntent, camParameters,
            onImage = {
                fail()
            },
            onMediaResult = {
                fail()
            },
            onError = {
                assertEquals(it.code, IONError.TAKE_PHOTO_ERROR.code)
                assertEquals(it.description, IONError.TAKE_PHOTO_ERROR.description)
            })
    }

    @Test
    fun givenAPI30TakePictureCalledJPEGAndDataUriAndUriNotNullBitmapNotNullWhenProcessResultFromCameraThenSuccess() {
        val camParameters = IONCameraParameters(
            20,
            -1,
            -1,
            0,
            0,
            allowEdit = false,
            correctOrientation = false,
            saveToPhotoAlbum = true,
            includeMetadata = true,
            latestVersion = true
        )

        val exifHelperMock = IONExifHelperMock()
        val fileHelperMock = IONFileHelperMock()
        val camHelperMock = IONMediaHelperMock()
        val imgHelperMock = IONImageHelperMock()

        val cameraManager = CameraManager(
            applicationId = "someAppId",
            authority = ".camera.provider",
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

        cameraManager.takePicture(mockActivity, 0, 0)
        cameraManager.processResultFromCamera(mockActivity, mIntent, camParameters,
            onImage = {
                fail()
            },
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

    @Test
    fun givenAPI28TakePictureCalledPNGAndDataUriAndProcessOKWhenProcessResultFromCameraThenSuccess() {
        val camParameters = IONCameraParameters(
            20,
            -1,
            -1,
            1,
            0,
            allowEdit = false,
            correctOrientation = false,
            saveToPhotoAlbum = true,
            includeMetadata = false,
            latestVersion = false
        )

        val exifHelperMock = IONExifHelperMock()
        val fileHelperMock = IONFileHelperMock()
        val camHelperMock = IONMediaHelperMock()
        val imgHelperMock = IONImageHelperMock()

        val cameraManager = CameraManager(
            applicationId = "someAppId",
            authority = ".camera.provider",
            exif = exifHelperMock,
            fileHelper = fileHelperMock,
            mediaHelper = camHelperMock,
            imageHelper = imgHelperMock
        )

        Mockito.`when`(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)).thenReturn(mFile)

        imgHelperMock.processPicSuccess = true

        cameraManager.takePicture(mockActivity, 0, 1)
        cameraManager.processResultFromCamera(mockActivity, mIntent, camParameters,
            onImage = {
                assertEquals(PROCESS_SUCCESS, it)
            },
            onMediaResult = {
                fail()
            },
            onError = {
                fail()
            })
    }

    @Test
    fun givenAPI28TakePictureCalledJPEGAndProcessErrorWhenProcessResultFromCameraThenError() {
        val camParameters = IONCameraParameters(
            20,
            -1,
            -1,
            0,
            0,
            allowEdit = false,
            correctOrientation = false,
            saveToPhotoAlbum = true,
            includeMetadata = false,
            latestVersion = false
        )

        val exifHelperMock = IONExifHelperMock()
        val fileHelperMock = IONFileHelperMock()
        val camHelperMock = IONMediaHelperMock()
        val imgHelperMock = IONImageHelperMock()

        val cameraManager = CameraManager(
            applicationId = "someAppId",
            authority = ".camera.provider",
            exif = exifHelperMock,
            fileHelper = fileHelperMock,
            mediaHelper = camHelperMock,
            imageHelper = imgHelperMock
        )

        Mockito.`when`(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)).thenReturn(mFile)

        imgHelperMock.processPicSuccess = false

        cameraManager.takePicture(mockActivity, 0, 0)
        cameraManager.processResultFromCamera(mockActivity, mIntent, camParameters,
            onImage = {
                fail()
            },
            onMediaResult = {
                fail()
            },
            onError = {
                assertEquals(it.code, IONError.PROCESS_IMAGE_ERROR.code)
                assertEquals(it.description, IONError.PROCESS_IMAGE_ERROR.description)
            })
    }


    @Test
    fun givenAPI28TakePictureCalledPNGAndProcessErrorWhenProcessResultFromCameraThenError() {
        val camParameters = IONCameraParameters(
            20,
            -1,
            -1,
            1,
            0,
            allowEdit = false,
            correctOrientation = false,
            saveToPhotoAlbum = true,
            includeMetadata = false,
            latestVersion = false
        )

        val exifHelperMock = IONExifHelperMock()
        val fileHelperMock = IONFileHelperMock()
        val camHelperMock = IONMediaHelperMock()
        val imgHelperMock = IONImageHelperMock()

        val cameraManager = CameraManager(
            applicationId = "someAppId",
            authority = ".camera.provider",
            exif = exifHelperMock,
            fileHelper = fileHelperMock,
            mediaHelper = camHelperMock,
            imageHelper = imgHelperMock
        )

        Mockito.`when`(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)).thenReturn(mFile)

        imgHelperMock.processPicSuccess = false

        cameraManager.takePicture(mockActivity, 0, 1)
        cameraManager.processResultFromCamera(mockActivity, mIntent, camParameters,
            onImage = {
                fail()
            },
            onMediaResult = {
                fail()
            },
            onError = {
                assertEquals(it.code, IONError.PROCESS_IMAGE_ERROR.code)
                assertEquals(it.description, IONError.PROCESS_IMAGE_ERROR.description)
            })
    }

    @Test
    fun givenTakePictureCalledJPEGAndCompressOKWhenProcessResultFromCameraThenSuccess() {
        val camParameters = IONCameraParameters(
            20,
            -1,
            -1,
            0,
            0,
            allowEdit = false,
            correctOrientation = false,
            saveToPhotoAlbum = true,
            includeMetadata = false,
            latestVersion = false
        )

        val exifHelperMock = IONExifHelperMock()
        val fileHelperMock = IONFileHelperMock()
        val camHelperMock = IONMediaHelperMock()
        val imgHelperMock = IONImageHelperMock()

        val cameraManager = CameraManager(
            applicationId = "someAppId",
            authority = ".camera.provider",
            exif = exifHelperMock,
            fileHelper = fileHelperMock,
            mediaHelper = camHelperMock,
            imageHelper = imgHelperMock
        )

        Mockito.`when`(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)).thenReturn(mFile)

        imgHelperMock.processPicSuccess = true
        fileHelperMock.getUriResult = IONFileHelperMock.GET_URI_SUCCESS

        cameraManager.takePicture(mockActivity, 0, 0)
        cameraManager.processResultFromCamera(mockActivity, mIntent, camParameters,
            onImage = {
                assertEquals(PROCESS_SUCCESS, it)
            },
            onMediaResult = {
                fail()
            },
            onError = {
                fail()
            })
    }

    @Test
    fun givenTakePictureCalledPNGAndCompressOKWhenProcessResultFromCameraThenSuccess() {
        val camParameters = IONCameraParameters(
            20,
            -1,
            -1,
            1,
            0,
            allowEdit = false,
            correctOrientation = false,
            saveToPhotoAlbum = true,
            includeMetadata = false,
            latestVersion = false
        )

        val exifHelperMock = IONExifHelperMock()
        val fileHelperMock = IONFileHelperMock()
        val camHelperMock = IONMediaHelperMock()
        val imgHelperMock = IONImageHelperMock()

        val cameraManager = CameraManager(
            applicationId = "someAppId",
            authority = ".camera.provider",
            exif = exifHelperMock,
            fileHelper = fileHelperMock,
            mediaHelper = camHelperMock,
            imageHelper = imgHelperMock
        )

        Mockito.`when`(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)).thenReturn(mFile)

        imgHelperMock.processPicSuccess = true
        fileHelperMock.getUriResult = IONFileHelperMock.GET_URI_SUCCESS

        cameraManager.takePicture(mockActivity, 0, 1)
        cameraManager.processResultFromCamera(mockActivity, mIntent, camParameters,
            onImage = {
                assertEquals(PROCESS_SUCCESS, it)
            },
            onMediaResult = {
                fail()
            },
            onError = {
                fail()
            })
    }

    @Test
    fun givenTakePictureCalledJPEGAndDoNotSaveAndCompressOKWhenProcessResultFromCameraThenSuccess() {
        val camParameters = IONCameraParameters(
            20,
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

        val exifHelperMock = IONExifHelperMock()
        val fileHelperMock = IONFileHelperMock()
        val camHelperMock = IONMediaHelperMock()
        val imgHelperMock = IONImageHelperMock()

        val cameraManager = CameraManager(
            applicationId = "someAppId",
            authority = ".camera.provider",
            exif = exifHelperMock,
            fileHelper = fileHelperMock,
            mediaHelper = camHelperMock,
            imageHelper = imgHelperMock
        )

        Mockito.`when`(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)).thenReturn(mFile)

        imgHelperMock.processPicSuccess = true
        fileHelperMock.getUriResult = IONFileHelperMock.GET_URI_SUCCESS

        cameraManager.takePicture(mockActivity, 0, 0)
        cameraManager.processResultFromCamera(mockActivity, mIntent, camParameters,
            onImage = {
                assertEquals(PROCESS_SUCCESS, it)
            },
            onMediaResult = {
                fail()
            },
            onError = {
                fail()
            })
    }

    @Test
    fun givenTakePictureCalledPNGAndDoNotSaveAndCompressOKWhenProcessResultFromCameraThenSuccess() {
        val camParameters = IONCameraParameters(
            20,
            -1,
            -1,
            1,
            0,
            allowEdit = false,
            correctOrientation = false,
            saveToPhotoAlbum = false,
            includeMetadata = false,
            latestVersion = false
        )

        val exifHelperMock = IONExifHelperMock()
        val fileHelperMock = IONFileHelperMock()
        val camHelperMock = IONMediaHelperMock()
        val imgHelperMock = IONImageHelperMock()

        val cameraManager = CameraManager(
            applicationId = "someAppId",
            authority = ".camera.provider",
            exif = exifHelperMock,
            fileHelper = fileHelperMock,
            mediaHelper = camHelperMock,
            imageHelper = imgHelperMock
        )

        Mockito.`when`(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)).thenReturn(mFile)

        imgHelperMock.processPicSuccess = true
        fileHelperMock.getUriResult = IONFileHelperMock.GET_URI_SUCCESS

        cameraManager.takePicture(mockActivity, 0, 1)
        cameraManager.processResultFromCamera(mockActivity, mIntent, camParameters,
            onImage = {
                assertEquals(PROCESS_SUCCESS, it)
            },
            onMediaResult = {
                fail()
            },
            onError = {
                fail()
            })
    }

    @Test
    fun givenTakePictureAndCustomWidthHeightJPEGAndCorrectAndProcessOKWhenProcessResultFromCameraThenSuccess() {
        val camParameters = IONCameraParameters(
            20,
            100,
            100,
            0,
            0,
            allowEdit = false,
            correctOrientation = true,
            saveToPhotoAlbum = true,
            includeMetadata = false,
            latestVersion = false
        )

        val exifHelperMock = IONExifHelperMock()
        val fileHelperMock = IONFileHelperMock()
        val camHelperMock = IONMediaHelperMock()
        val imgHelperMock = IONImageHelperMock()

        val cameraManager = CameraManager(
            applicationId = "someAppId",
            authority = ".camera.provider",
            exif = exifHelperMock,
            fileHelper = fileHelperMock,
            mediaHelper = camHelperMock,
            imageHelper = imgHelperMock
        )

        Mockito.`when`(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)).thenReturn(mFile)

        imgHelperMock.processPicSuccess = true
        imgHelperMock.areOptionsZero = false

        cameraManager.takePicture(mockActivity, 0, 0)
        cameraManager.processResultFromCamera(mockActivity, mIntent, camParameters,
            onImage = {
                assertEquals(PROCESS_SUCCESS, it)
            },
            onMediaResult = {
                fail()
            },
            onError = {
                fail()
            })
    }

    @Test
    fun givenTakePictureAndCustomWidthHeightPNGAndCorrectAndProcessOKWhenProcessResultFromCameraThenSuccess() {
        val camParameters = IONCameraParameters(
            20,
            100,
            100,
            1,
            0,
            allowEdit = false,
            correctOrientation = true,
            saveToPhotoAlbum = true,
            includeMetadata = false,
            latestVersion = false
        )

        val exifHelperMock = IONExifHelperMock()
        val fileHelperMock = IONFileHelperMock()
        val camHelperMock = IONMediaHelperMock()
        val imgHelperMock = IONImageHelperMock()

        val cameraManager = CameraManager(
            applicationId = "someAppId",
            authority = ".camera.provider",
            exif = exifHelperMock,
            fileHelper = fileHelperMock,
            mediaHelper = camHelperMock,
            imageHelper = imgHelperMock
        )

        Mockito.`when`(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)).thenReturn(mFile)

        imgHelperMock.processPicSuccess = true
        imgHelperMock.areOptionsZero = false

        cameraManager.takePicture(mockActivity, 0, 1)
        cameraManager.processResultFromCamera(mockActivity, mIntent, camParameters,
            onImage = {
                assertEquals(PROCESS_SUCCESS, it)
            },
            onMediaResult = {
                fail()
            },
            onError = {
                fail()
            })
    }

    @Test
    fun givenTakePictureAndCustomWidthHeightJPEGAndProcessErrorWhenProcessResultFromCameraThenError() {
        val camParameters = IONCameraParameters(
            20,
            100,
            100,
            0,
            0,
            allowEdit = false,
            correctOrientation = false,
            saveToPhotoAlbum = true,
            includeMetadata = false,
            latestVersion = false
        )

        val exifHelperMock = IONExifHelperMock()
        val fileHelperMock = IONFileHelperMock()
        val camHelperMock = IONMediaHelperMock()
        val imgHelperMock = IONImageHelperMock()

        val cameraManager = CameraManager(
            applicationId = "someAppId",
            authority = ".camera.provider",
            exif = exifHelperMock,
            fileHelper = fileHelperMock,
            mediaHelper = camHelperMock,
            imageHelper = imgHelperMock
        )

        Mockito.`when`(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)).thenReturn(mFile)

        imgHelperMock.processPicSuccess = false
        imgHelperMock.areOptionsZero = false

        cameraManager.takePicture(mockActivity, 0, 0)
        cameraManager.processResultFromCamera(mockActivity, mIntent, camParameters,
            onImage = {
                fail()
            },
            onMediaResult = {
                fail()
            },
            onError = {
                assertEquals(it.code, IONError.PROCESS_IMAGE_ERROR.code)
                assertEquals(it.description, IONError.PROCESS_IMAGE_ERROR.description)
            })
    }

    @Test
    fun givenTakePictureAndCustomWidthHeightPNGAndProcessErrorWhenProcessResultFromCameraThenError() {
        val camParameters = IONCameraParameters(
            20,
            100,
            100,
            1,
            0,
            allowEdit = false,
            correctOrientation = false,
            saveToPhotoAlbum = true,
            includeMetadata = false,
            latestVersion = false
        )

        val exifHelperMock = IONExifHelperMock()
        val fileHelperMock = IONFileHelperMock()
        val camHelperMock = IONMediaHelperMock()
        val imgHelperMock = IONImageHelperMock()

        val cameraManager = CameraManager(
            applicationId = "someAppId",
            authority = ".camera.provider",
            exif = exifHelperMock,
            fileHelper = fileHelperMock,
            mediaHelper = camHelperMock,
            imageHelper = imgHelperMock
        )

        Mockito.`when`(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)).thenReturn(mFile)

        imgHelperMock.processPicSuccess = false
        imgHelperMock.areOptionsZero = false

        cameraManager.takePicture(mockActivity, 0, 1)
        cameraManager.processResultFromCamera(mockActivity, mIntent, camParameters,
            onImage = {
                fail()
            },
            onMediaResult = {
                fail()
            },
            onError = {
                assertEquals(it.code, IONError.PROCESS_IMAGE_ERROR.code)
                assertEquals(it.description, IONError.PROCESS_IMAGE_ERROR.description)
            })
    }

    @Test
    fun givenTakePictureAndCustomWidthHeightJPEGAndOptionsZeroWhenProcessResultFromCameraThenError() {
        val camParameters = IONCameraParameters(
            20,
            100,
            100,
            0,
            0,
            allowEdit = false,
            correctOrientation = false,
            saveToPhotoAlbum = true,
            includeMetadata = false,
            latestVersion = false
        )

        val exifHelperMock = IONExifHelperMock()
        val fileHelperMock = IONFileHelperMock()
        val camHelperMock = IONMediaHelperMock()
        val imgHelperMock = IONImageHelperMock()

        val cameraManager = CameraManager(
            applicationId = "someAppId",
            authority = ".camera.provider",
            exif = exifHelperMock,
            fileHelper = fileHelperMock,
            mediaHelper = camHelperMock,
            imageHelper = imgHelperMock
        )

        Mockito.`when`(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)).thenReturn(mFile)

        imgHelperMock.processPicSuccess = false
        imgHelperMock.areOptionsZero = true

        cameraManager.takePicture(mockActivity, 0, 0)
        cameraManager.processResultFromCamera(mockActivity, mIntent, camParameters,
            onImage = {
                fail()
            },
            onMediaResult = {
                fail()
            },
            onError = {
                assertEquals(it.code, IONError.PROCESS_IMAGE_ERROR.code)
                assertEquals(it.description, IONError.PROCESS_IMAGE_ERROR.description)
            })
    }

    @Test
    fun givenTakePictureAndCustomWidthHeightPNGAndOptionsZeroWhenProcessResultFromCameraThenError() {
        val camParameters = IONCameraParameters(
            20,
            100,
            100,
            1,
            0,
            allowEdit = false,
            correctOrientation = false,
            saveToPhotoAlbum = true,
            includeMetadata = false,
            latestVersion = false
        )

        val exifHelperMock = IONExifHelperMock()
        val fileHelperMock = IONFileHelperMock()
        val camHelperMock = IONMediaHelperMock()
        val imgHelperMock = IONImageHelperMock()

        val cameraManager = CameraManager(
            applicationId = "someAppId",
            authority = ".camera.provider",
            exif = exifHelperMock,
            fileHelper = fileHelperMock,
            mediaHelper = camHelperMock,
            imageHelper = imgHelperMock
        )

        Mockito.`when`(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)).thenReturn(mFile)

        imgHelperMock.processPicSuccess = false
        imgHelperMock.areOptionsZero = true

        cameraManager.takePicture(mockActivity, 0, 1)
        cameraManager.processResultFromCamera(mockActivity, mIntent, camParameters,
            onImage = {
                fail()
            },
            onMediaResult = {
                fail()
            },
            onError = {
                assertEquals(it.code, IONError.PROCESS_IMAGE_ERROR.code)
                assertEquals(it.description, IONError.PROCESS_IMAGE_ERROR.description)
            })
    }

    @Test
    fun givenTakePictureAndCustomWidthHeightJPEGCompressOKWhenProcessResultFromCameraThenSuccess() {
        val camParameters = IONCameraParameters(
            20,
            100,
            100,
            0,
            0,
            allowEdit = false,
            correctOrientation = false,
            saveToPhotoAlbum = true,
            includeMetadata = false,
            latestVersion = false
        )

        val exifHelperMock = IONExifHelperMock()
        val fileHelperMock = IONFileHelperMock()
        val camHelperMock = IONMediaHelperMock()
        val imgHelperMock = IONImageHelperMock()

        val cameraManager = CameraManager(
            applicationId = "someAppId",
            authority = ".camera.provider",
            exif = exifHelperMock,
            fileHelper = fileHelperMock,
            mediaHelper = camHelperMock,
            imageHelper = imgHelperMock
        )

        Mockito.`when`(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)).thenReturn(mFile)

        imgHelperMock.processPicSuccess = true

        cameraManager.takePicture(mockActivity, 0, 0)
        cameraManager.processResultFromCamera(mockActivity, mIntent, camParameters,
            onImage = {
                assertEquals(PROCESS_SUCCESS, it)
            },
            onMediaResult = {
                fail()
            },
            onError = {
                fail()
            })
    }

    @Test
    fun givenTakePictureAndCustomWidthHeightPNGCompressOKWhenProcessResultFromCameraThenSuccess() {
        val camParameters = IONCameraParameters(
            20,
            100,
            100,
            1,
            0,
            allowEdit = false,
            correctOrientation = false,
            saveToPhotoAlbum = true,
            includeMetadata = false,
            latestVersion = false
        )

        val exifHelperMock = IONExifHelperMock()
        val fileHelperMock = IONFileHelperMock()
        val camHelperMock = IONMediaHelperMock()
        val imgHelperMock = IONImageHelperMock()

        val cameraManager = CameraManager(
            applicationId = "someAppId",
            authority = ".camera.provider",
            exif = exifHelperMock,
            fileHelper = fileHelperMock,
            mediaHelper = camHelperMock,
            imageHelper = imgHelperMock
        )

        Mockito.`when`(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)).thenReturn(mFile)

        imgHelperMock.processPicSuccess = true

        cameraManager.takePicture(mockActivity, 0, 1)
        cameraManager.processResultFromCamera(mockActivity, mIntent, camParameters,
            onImage = {
                assertEquals(PROCESS_SUCCESS, it)
            },
            onMediaResult = {
                fail()
            },
            onError = {
                fail()
            })
    }

    @Test
    fun givenTakePictureAndCustomWidthHeightJPEGCompressErrorWhenProcessResultFromCameraThenError() {
        val camParameters = IONCameraParameters(
            20,
            100,
            100,
            0,
            0,
            allowEdit = false,
            correctOrientation = false,
            saveToPhotoAlbum = true,
            includeMetadata = false,
            latestVersion = false
        )

        val exifHelperMock = IONExifHelperMock()
        val fileHelperMock = IONFileHelperMock()
        val camHelperMock = IONMediaHelperMock()
        val imgHelperMock = IONImageHelperMock()

        val cameraManager = CameraManager(
            applicationId = "someAppId",
            authority = ".camera.provider",
            exif = exifHelperMock,
            fileHelper = fileHelperMock,
            mediaHelper = camHelperMock,
            imageHelper = imgHelperMock
        )

        Mockito.`when`(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)).thenReturn(mFile)

        imgHelperMock.compressImageSuccess = false

        cameraManager.takePicture(mockActivity, 0, 0)
        cameraManager.processResultFromCamera(mockActivity, mIntent, camParameters,
            onImage = {
                fail()
            },
            onMediaResult = {
                fail()
            },
            onError = {
                assertEquals(it.code, IONError.PROCESS_IMAGE_ERROR.code)
                assertEquals(it.description, IONError.PROCESS_IMAGE_ERROR.description)
            })
    }

    @Test
    fun givenTakePictureAndCustomWidthHeightPNGCompressErrorWhenProcessResultFromCameraThenError() {
        val camParameters = IONCameraParameters(
            20,
            100,
            100,
            1,
            0,
            allowEdit = false,
            correctOrientation = false,
            saveToPhotoAlbum = true,
            includeMetadata = false,
            latestVersion = false
        )

        val exifHelperMock = IONExifHelperMock()
        val fileHelperMock = IONFileHelperMock()
        val camHelperMock = IONMediaHelperMock()
        val imgHelperMock = IONImageHelperMock()

        val cameraManager = CameraManager(
            applicationId = "someAppId",
            authority = ".camera.provider",
            exif = exifHelperMock,
            fileHelper = fileHelperMock,
            mediaHelper = camHelperMock,
            imageHelper = imgHelperMock
        )

        Mockito.`when`(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)).thenReturn(mFile)

        imgHelperMock.compressImageSuccess = false

        cameraManager.takePicture(mockActivity, 0, 1)
        cameraManager.processResultFromCamera(mockActivity, mIntent, camParameters,
            onImage = {
                fail()
            },
            onMediaResult = {
                fail()
            },
            onError = {
                assertEquals(it.code, IONError.PROCESS_IMAGE_ERROR.code)
                assertEquals(it.description, IONError.PROCESS_IMAGE_ERROR.description)
            })
    }


    @Test
    fun givenTakePictureAndCorrectJPEGAndRotate90RatioSameWhenProcessResultFromCameraThenSuccess() {
        val camParameters = IONCameraParameters(
            20,
            -1,
            -1,
            0,
            0,
            allowEdit = false,
            correctOrientation = true,
            saveToPhotoAlbum = true,
            includeMetadata = false,
            latestVersion = false
        )

        val exifHelperMock = IONExifHelperMock()
        val fileHelperMock = IONFileHelperMock()
        val camHelperMock = IONMediaHelperMock()
        val imgHelperMock = IONImageHelperMock()

        val cameraManager = CameraManager(
            applicationId = "someAppId",
            authority = ".camera.provider",
            exif = exifHelperMock,
            fileHelper = fileHelperMock,
            mediaHelper = camHelperMock,
            imageHelper = imgHelperMock
        )

        Mockito.`when`(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)).thenReturn(mFile)

        imgHelperMock.processPicSuccess = true
        imgHelperMock.areOptionsZero = false
        imgHelperMock.outHeight = 10
        imgHelperMock.outWidth = 10
        exifHelperMock.testOrientation = ExifInterface.ORIENTATION_ROTATE_90

        cameraManager.takePicture(mockActivity, 0, 0)
        cameraManager.processResultFromCamera(mockActivity, mIntent, camParameters,
            onImage = {
                assertEquals(it, PROCESS_SUCCESS)
            },
            onMediaResult = {
                fail()
            },
            onError = {
                fail()
            })
    }

    @Test
    fun givenTakePictureAndCorrectPNGAndRotate90RatioSameWhenProcessResultFromCameraThenSuccess() {
        val camParameters = IONCameraParameters(
            20,
            -1,
            -1,
            1,
            0,
            allowEdit = false,
            correctOrientation = true,
            saveToPhotoAlbum = true,
            includeMetadata = false,
            latestVersion = false
        )

        val exifHelperMock = IONExifHelperMock()
        val fileHelperMock = IONFileHelperMock()
        val camHelperMock = IONMediaHelperMock()
        val imgHelperMock = IONImageHelperMock()

        val cameraManager = CameraManager(
            applicationId = "someAppId",
            authority = ".camera.provider",
            exif = exifHelperMock,
            fileHelper = fileHelperMock,
            mediaHelper = camHelperMock,
            imageHelper = imgHelperMock
        )

        Mockito.`when`(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)).thenReturn(mFile)

        imgHelperMock.processPicSuccess = true
        imgHelperMock.areOptionsZero = false
        imgHelperMock.outHeight = 10
        imgHelperMock.outWidth = 10
        exifHelperMock.testOrientation = ExifInterface.ORIENTATION_ROTATE_90

        cameraManager.takePicture(mockActivity, 0, 1)
        cameraManager.processResultFromCamera(mockActivity, mIntent, camParameters,
            onImage = {
                assertEquals(it, PROCESS_SUCCESS)
            },
            onMediaResult = {
                fail()
            },
            onError = {
                fail()
            })
    }

    @Test
    fun givenTakePictureAndCorrectJPEGAndRotate90RatioOrigHigherWhenProcessResultFromCameraThenSuccess() {
        val camParameters = IONCameraParameters(
            20,
            -1,
            -1,
            0,
            0,
            allowEdit = false,
            correctOrientation = true,
            saveToPhotoAlbum = true,
            includeMetadata = false,
            latestVersion = false
        )

        val exifHelperMock = IONExifHelperMock()
        val fileHelperMock = IONFileHelperMock()
        val camHelperMock = IONMediaHelperMock()
        val imgHelperMock = IONImageHelperMock()

        val cameraManager = CameraManager(
            applicationId = "someAppId",
            authority = ".camera.provider",
            exif = exifHelperMock,
            fileHelper = fileHelperMock,
            mediaHelper = camHelperMock,
            imageHelper = imgHelperMock
        )

        Mockito.`when`(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)).thenReturn(mFile)

        imgHelperMock.processPicSuccess = true
        imgHelperMock.areOptionsZero = false
        imgHelperMock.outHeight = 12
        imgHelperMock.outWidth = 10
        exifHelperMock.testOrientation = ExifInterface.ORIENTATION_ROTATE_90

        cameraManager.takePicture(mockActivity, 0, 0)
        cameraManager.processResultFromCamera(mockActivity, mIntent, camParameters,
            onImage = {
                assertEquals(it, PROCESS_SUCCESS)
            },
            onMediaResult = {
                fail()
            },
            onError = {
                fail()
            })
    }

    @Test
    fun givenTakePictureAndCorrectPNGAndRotate90RatioOrigHigherWhenProcessResultFromCameraThenSuccess() {
        val camParameters = IONCameraParameters(
            20,
            -1,
            -1,
            1,
            0,
            allowEdit = false,
            correctOrientation = true,
            saveToPhotoAlbum = true,
            includeMetadata = false,
            latestVersion = false
        )

        val exifHelperMock = IONExifHelperMock()
        val fileHelperMock = IONFileHelperMock()
        val camHelperMock = IONMediaHelperMock()
        val imgHelperMock = IONImageHelperMock()

        val cameraManager = CameraManager(
            applicationId = "someAppId",
            authority = ".camera.provider",
            exif = exifHelperMock,
            fileHelper = fileHelperMock,
            mediaHelper = camHelperMock,
            imageHelper = imgHelperMock
        )

        Mockito.`when`(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)).thenReturn(mFile)

        imgHelperMock.processPicSuccess = true
        imgHelperMock.areOptionsZero = false
        imgHelperMock.outHeight = 12
        imgHelperMock.outWidth = 10
        exifHelperMock.testOrientation = ExifInterface.ORIENTATION_ROTATE_90

        cameraManager.takePicture(mockActivity, 0, 1)
        cameraManager.processResultFromCamera(mockActivity, mIntent, camParameters,
            onImage = {
                assertEquals(it, PROCESS_SUCCESS)
            },
            onMediaResult = {
                fail()
            },
            onError = {
                fail()
            })
    }

    @Test
    fun givenTakePictureAndCorrectJPEGAndRotate90RatioOrigLowerWhenProcessResultFromCameraThenSuccess() {
        val camParameters = IONCameraParameters(
            20,
            -1,
            -1,
            0,
            0,
            allowEdit = false,
            correctOrientation = true,
            saveToPhotoAlbum = true,
            includeMetadata = false,
            latestVersion = false
        )

        val exifHelperMock = IONExifHelperMock()
        val fileHelperMock = IONFileHelperMock()
        val camHelperMock = IONMediaHelperMock()
        val imgHelperMock = IONImageHelperMock()

        val cameraManager = CameraManager(
            applicationId = "someAppId",
            authority = ".camera.provider",
            exif = exifHelperMock,
            fileHelper = fileHelperMock,
            mediaHelper = camHelperMock,
            imageHelper = imgHelperMock
        )

        Mockito.`when`(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)).thenReturn(mFile)

        imgHelperMock.processPicSuccess = true
        imgHelperMock.areOptionsZero = false
        imgHelperMock.outHeight = 10
        imgHelperMock.outWidth = 12
        exifHelperMock.testOrientation = ExifInterface.ORIENTATION_ROTATE_90

        cameraManager.takePicture(mockActivity, 0, 0)
        cameraManager.processResultFromCamera(mockActivity, mIntent, camParameters,
            onImage = {
                assertEquals(it, PROCESS_SUCCESS)
            },
            onMediaResult = {
                fail()
            },
            onError = {
                fail()
            })
    }

    @Test
    fun givenTakePictureAndCorrectPNGAndRotate90RatioOrigLowerWhenProcessResultFromCameraThenSuccess() {
        val camParameters = IONCameraParameters(
            20,
            -1,
            -1,
            1,
            0,
            allowEdit = false,
            correctOrientation = true,
            saveToPhotoAlbum = true,
            includeMetadata = false,
            latestVersion = false
        )

        val exifHelperMock = IONExifHelperMock()
        val fileHelperMock = IONFileHelperMock()
        val camHelperMock = IONMediaHelperMock()
        val imgHelperMock = IONImageHelperMock()

        val cameraManager = CameraManager(
            applicationId = "someAppId",
            authority = ".camera.provider",
            exif = exifHelperMock,
            fileHelper = fileHelperMock,
            mediaHelper = camHelperMock,
            imageHelper = imgHelperMock
        )

        Mockito.`when`(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)).thenReturn(mFile)

        imgHelperMock.processPicSuccess = true
        imgHelperMock.areOptionsZero = false
        imgHelperMock.outHeight = 10
        imgHelperMock.outWidth = 12
        exifHelperMock.testOrientation = ExifInterface.ORIENTATION_ROTATE_90

        cameraManager.takePicture(mockActivity, 0, 1)
        cameraManager.processResultFromCamera(mockActivity, mIntent, camParameters,
            onImage = {
                assertEquals(it, PROCESS_SUCCESS)
            },
            onMediaResult = {
                fail()
            },
            onError = {
                fail()
            })
    }

    @Test
    fun givenTakePictureAndCorrectJPEGAndRotate90BothBelow0WhenProcessResultFromCameraThenSuccess() {
        val camParameters = IONCameraParameters(
            20,
            -1,
            -1,
            0,
            0,
            allowEdit = false,
            correctOrientation = true,
            saveToPhotoAlbum = true,
            includeMetadata = false,
            latestVersion = false
        )

        val exifHelperMock = IONExifHelperMock()
        val fileHelperMock = IONFileHelperMock()
        val camHelperMock = IONMediaHelperMock()
        val imgHelperMock = IONImageHelperMock()

        val cameraManager = CameraManager(
            applicationId = "someAppId",
            authority = ".camera.provider",
            exif = exifHelperMock,
            fileHelper = fileHelperMock,
            mediaHelper = camHelperMock,
            imageHelper = imgHelperMock
        )

        Mockito.`when`(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)).thenReturn(mFile)

        imgHelperMock.processPicSuccess = true
        imgHelperMock.areOptionsZero = false
        imgHelperMock.outHeight = -1
        imgHelperMock.outWidth = -1
        exifHelperMock.testOrientation = ExifInterface.ORIENTATION_ROTATE_90

        cameraManager.takePicture(mockActivity, 0, 0)
        cameraManager.processResultFromCamera(mockActivity, mIntent, camParameters,
            onImage = {
                assertEquals(it, PROCESS_SUCCESS)
            },
            onMediaResult = {
                fail()
            },
            onError = {
                fail()
            })
    }

    @Test
    fun givenTakePictureAndCorrectPNGAndRotate90BothBelow0WhenProcessResultFromCameraThenSuccess() {
        val camParameters = IONCameraParameters(
            20,
            -1,
            -1,
            1,
            0,
            allowEdit = false,
            correctOrientation = true,
            saveToPhotoAlbum = true,
            includeMetadata = false,
            latestVersion = false
        )

        val exifHelperMock = IONExifHelperMock()
        val fileHelperMock = IONFileHelperMock()
        val camHelperMock = IONMediaHelperMock()
        val imgHelperMock = IONImageHelperMock()

        val cameraManager = CameraManager(
            applicationId = "someAppId",
            authority = ".camera.provider",
            exif = exifHelperMock,
            fileHelper = fileHelperMock,
            mediaHelper = camHelperMock,
            imageHelper = imgHelperMock
        )

        Mockito.`when`(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)).thenReturn(mFile)

        imgHelperMock.processPicSuccess = true
        imgHelperMock.areOptionsZero = false
        imgHelperMock.outHeight = -1
        imgHelperMock.outWidth = -1
        exifHelperMock.testOrientation = ExifInterface.ORIENTATION_ROTATE_90

        cameraManager.takePicture(mockActivity, 0, 1)
        cameraManager.processResultFromCamera(mockActivity, mIntent, camParameters,
            onImage = {
                assertEquals(it, PROCESS_SUCCESS)
            },
            onMediaResult = {
                fail()
            },
            onError = {
                fail()
            })
    }

    @Test
    fun givenTakePictureAndCorrectJPEGAndRotate90HeightBelow0WhenProcessResultFromCameraThenSuccess() {
        val camParameters = IONCameraParameters(
            20,
            -1,
            -1,
            0,
            0,
            allowEdit = false,
            correctOrientation = true,
            saveToPhotoAlbum = true,
            includeMetadata = false,
            latestVersion = false
        )

        val exifHelperMock = IONExifHelperMock()
        val fileHelperMock = IONFileHelperMock()
        val camHelperMock = IONMediaHelperMock()
        val imgHelperMock = IONImageHelperMock()

        val cameraManager = CameraManager(
            applicationId = "someAppId",
            authority = ".camera.provider",
            exif = exifHelperMock,
            fileHelper = fileHelperMock,
            mediaHelper = camHelperMock,
            imageHelper = imgHelperMock
        )

        Mockito.`when`(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)).thenReturn(mFile)

        imgHelperMock.processPicSuccess = true
        imgHelperMock.areOptionsZero = false
        imgHelperMock.outHeight = -1
        imgHelperMock.outWidth = 1
        exifHelperMock.testOrientation = ExifInterface.ORIENTATION_ROTATE_90

        cameraManager.takePicture(mockActivity, 0, 0)
        cameraManager.processResultFromCamera(mockActivity, mIntent, camParameters,
            onImage = {
                assertEquals(it, PROCESS_SUCCESS)
            },
            onMediaResult = {
                fail()
            },
            onError = {
                fail()
            })
    }

    @Test
    fun givenTakePictureAndCorrectPNGAndRotate90HeightBelow0WhenProcessResultFromCameraThenSuccess() {
        val camParameters = IONCameraParameters(
            20,
            -1,
            -1,
            1,
            0,
            allowEdit = false,
            correctOrientation = true,
            saveToPhotoAlbum = true,
            includeMetadata = false,
            latestVersion = false
        )

        val exifHelperMock = IONExifHelperMock()
        val fileHelperMock = IONFileHelperMock()
        val camHelperMock = IONMediaHelperMock()
        val imgHelperMock = IONImageHelperMock()

        val cameraManager = CameraManager(
            applicationId = "someAppId",
            authority = ".camera.provider",
            exif = exifHelperMock,
            fileHelper = fileHelperMock,
            mediaHelper = camHelperMock,
            imageHelper = imgHelperMock
        )

        Mockito.`when`(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)).thenReturn(mFile)

        imgHelperMock.processPicSuccess = true
        imgHelperMock.areOptionsZero = false
        imgHelperMock.outHeight = -1
        imgHelperMock.outWidth = 1
        exifHelperMock.testOrientation = ExifInterface.ORIENTATION_ROTATE_90

        cameraManager.takePicture(mockActivity, 0, 1)
        cameraManager.processResultFromCamera(mockActivity, mIntent, camParameters,
            onImage = {
                assertEquals(it, PROCESS_SUCCESS)
            },
            onMediaResult = {
                fail()
            },
            onError = {
                fail()
            })
    }

    @Test
    fun givenTakePictureAndCorrectJPEGAndRotate90WidthBelow0WhenProcessResultFromCameraThenSuccess() {
        val camParameters = IONCameraParameters(
            20,
            -1,
            -1,
            0,
            0,
            allowEdit = false,
            correctOrientation = true,
            saveToPhotoAlbum = true,
            includeMetadata = false,
            latestVersion = false
        )

        val exifHelperMock = IONExifHelperMock()
        val fileHelperMock = IONFileHelperMock()
        val camHelperMock = IONMediaHelperMock()
        val imgHelperMock = IONImageHelperMock()

        val cameraManager = CameraManager(
            applicationId = "someAppId",
            authority = ".camera.provider",
            exif = exifHelperMock,
            fileHelper = fileHelperMock,
            mediaHelper = camHelperMock,
            imageHelper = imgHelperMock
        )

        Mockito.`when`(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)).thenReturn(mFile)

        imgHelperMock.processPicSuccess = true
        imgHelperMock.areOptionsZero = false
        imgHelperMock.outHeight = 1
        imgHelperMock.outWidth = -1
        exifHelperMock.testOrientation = ExifInterface.ORIENTATION_ROTATE_90

        cameraManager.takePicture(mockActivity, 0, 0)
        cameraManager.processResultFromCamera(mockActivity, mIntent, camParameters,
            onImage = {
                assertEquals(it, PROCESS_SUCCESS)
            },
            onMediaResult = {
                fail()
            },
            onError = {
                fail()
            })
    }

    @Test
    fun givenTakePictureAndCorrectPNGAndRotate90WidthBelow0WhenProcessResultFromCameraThenSuccess() {
        val camParameters = IONCameraParameters(
            20,
            -1,
            -1,
            1,
            0,
            allowEdit = false,
            correctOrientation = true,
            saveToPhotoAlbum = true,
            includeMetadata = false,
            latestVersion = false
        )

        val exifHelperMock = IONExifHelperMock()
        val fileHelperMock = IONFileHelperMock()
        val camHelperMock = IONMediaHelperMock()
        val imgHelperMock = IONImageHelperMock()

        val cameraManager = CameraManager(
            applicationId = "someAppId",
            authority = ".camera.provider",
            exif = exifHelperMock,
            fileHelper = fileHelperMock,
            mediaHelper = camHelperMock,
            imageHelper = imgHelperMock
        )

        Mockito.`when`(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)).thenReturn(mFile)

        imgHelperMock.processPicSuccess = true
        imgHelperMock.areOptionsZero = false
        imgHelperMock.outHeight = 1
        imgHelperMock.outWidth = -1
        exifHelperMock.testOrientation = ExifInterface.ORIENTATION_ROTATE_90

        cameraManager.takePicture(mockActivity, 0, 1)
        cameraManager.processResultFromCamera(mockActivity, mIntent, camParameters,
            onImage = {
                assertEquals(it, PROCESS_SUCCESS)
            },
            onMediaResult = {
                fail()
            },
            onError = {
                fail()
            })
    }

    @Test
    fun givenTakePictureAndCorrectJPEGAndRotate180WhenProcessResultFromCameraThenSuccess() {
        val camParameters = IONCameraParameters(
            20,
            -1,
            -1,
            0,
            0,
            allowEdit = false,
            correctOrientation = true,
            saveToPhotoAlbum = true,
            includeMetadata = false,
            latestVersion = false
        )

        val exifHelperMock = IONExifHelperMock()
        val fileHelperMock = IONFileHelperMock()
        val camHelperMock = IONMediaHelperMock()
        val imgHelperMock = IONImageHelperMock()

        val cameraManager = CameraManager(
            applicationId = "someAppId",
            authority = ".camera.provider",
            exif = exifHelperMock,
            fileHelper = fileHelperMock,
            mediaHelper = camHelperMock,
            imageHelper = imgHelperMock
        )

        Mockito.`when`(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)).thenReturn(mFile)

        imgHelperMock.processPicSuccess = true
        imgHelperMock.areOptionsZero = false
        exifHelperMock.testOrientation = ExifInterface.ORIENTATION_ROTATE_180

        cameraManager.takePicture(mockActivity, 0, 0)
        cameraManager.processResultFromCamera(mockActivity, mIntent, camParameters,
            onImage = {
                assertEquals(it, PROCESS_SUCCESS)
            },
            onMediaResult = {
                fail()
            },
            onError = {
                fail()
            })
    }

    @Test
    fun givenTakePictureAndCorrectPNGAndRotate180WhenProcessResultFromCameraThenSuccess() {
        val camParameters = IONCameraParameters(
            20,
            -1,
            -1,
            1,
            0,
            allowEdit = false,
            correctOrientation = true,
            saveToPhotoAlbum = true,
            includeMetadata = false,
            latestVersion = false
        )

        val exifHelperMock = IONExifHelperMock()
        val fileHelperMock = IONFileHelperMock()
        val camHelperMock = IONMediaHelperMock()
        val imgHelperMock = IONImageHelperMock()

        val cameraManager = CameraManager(
            applicationId = "someAppId",
            authority = ".camera.provider",
            exif = exifHelperMock,
            fileHelper = fileHelperMock,
            mediaHelper = camHelperMock,
            imageHelper = imgHelperMock
        )

        Mockito.`when`(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)).thenReturn(mFile)

        imgHelperMock.processPicSuccess = true
        imgHelperMock.areOptionsZero = false
        exifHelperMock.testOrientation = ExifInterface.ORIENTATION_ROTATE_180

        cameraManager.takePicture(mockActivity, 0, 1)
        cameraManager.processResultFromCamera(mockActivity, mIntent, camParameters,
            onImage = {
                assertEquals(it, PROCESS_SUCCESS)
            },
            onMediaResult = {
                fail()
            },
            onError = {
                fail()
            })
    }

    @Test
    fun givenTakePictureAndAllowEditWhenProcessResultFromCameraThenSuccess() {
        val camParameters = IONCameraParameters(
            20,
            -1,
            -1,
            0,
            0,
            allowEdit = true,
            correctOrientation = true,
            saveToPhotoAlbum = true,
            includeMetadata = false,
            latestVersion = false
        )

        val exifHelperMock = IONExifHelperMock()
        val fileHelperMock = IONFileHelperMock()
        val camHelperMock = IONMediaHelperMock()
        val imgHelperMock = IONImageHelperMock()

        val cameraManager = CameraManager(
            applicationId = "someAppId",
            authority = ".camera.provider",
            exif = exifHelperMock,
            fileHelper = fileHelperMock,
            mediaHelper = camHelperMock,
            imageHelper = imgHelperMock
        )

        Mockito.`when`(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)).thenReturn(mFile)

        imgHelperMock.processPicSuccess = true
        imgHelperMock.areOptionsZero = false

        cameraManager.takePicture(mockActivity, 0, 0)
        cameraManager.processResultFromCamera(mockActivity, mIntent, camParameters,
            onImage = {
                assertEquals(it, PROCESS_SUCCESS)
            },
            onMediaResult = {
                fail()
            },
            onError = {
                fail()
            })
    }

    @Test
    fun givenTakePictureAndAllowEditDoNotCorrectQuality100SaveWhenProcessResultFromCameraThenSuccess() {
        val camParameters = IONCameraParameters(
            100,
            -1,
            -1,
            0,
            0,
            allowEdit = true,
            correctOrientation = false,
            saveToPhotoAlbum = true,
            includeMetadata = false,
            latestVersion = false
        )

        val exifHelperMock = IONExifHelperMock()
        val fileHelperMock = IONFileHelperMock()
        val camHelperMock = IONMediaHelperMock()
        val imgHelperMock = IONImageHelperMock()

        val cameraManager = CameraManager(
            applicationId = "someAppId",
            authority = ".camera.provider",
            exif = exifHelperMock,
            fileHelper = fileHelperMock,
            mediaHelper = camHelperMock,
            imageHelper = imgHelperMock
        )

        Mockito.`when`(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)).thenReturn(mFile)

        imgHelperMock.processPicSuccess = true
        imgHelperMock.areOptionsZero = false

        cameraManager.takePicture(mockActivity, 0, 0)
        cameraManager.processResultFromCamera(mockActivity, mIntent, camParameters,
            onImage = {
                assertEquals(it, PROCESS_SUCCESS)
            },
            onMediaResult = {
                fail()
            },
            onError = {
                fail()
            })
    }

    @Test
    fun givenTakePictureAndAllowEditDoNotCorrectQuality100DoNotSaveWhenProcessResultFromCameraThenSuccess() {
        val camParameters = IONCameraParameters(
            100,
            -1,
            -1,
            0,
            0,
            allowEdit = true,
            correctOrientation = false,
            saveToPhotoAlbum = false,
            includeMetadata = false,
            latestVersion = false
        )

        val exifHelperMock = IONExifHelperMock()
        val fileHelperMock = IONFileHelperMock()
        val camHelperMock = IONMediaHelperMock()
        val imgHelperMock = IONImageHelperMock()

        val cameraManager = CameraManager(
            applicationId = "someAppId",
            authority = ".camera.provider",
            exif = exifHelperMock,
            fileHelper = fileHelperMock,
            mediaHelper = camHelperMock,
            imageHelper = imgHelperMock
        )

        Mockito.`when`(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)).thenReturn(mFile)

        imgHelperMock.processPicSuccess = true
        imgHelperMock.areOptionsZero = false

        cameraManager.takePicture(mockActivity, 0, 0)
        cameraManager.processResultFromCamera(mockActivity, mIntent, camParameters,
            onImage = {
                assertEquals(it, PROCESS_SUCCESS)
            },
            onMediaResult = {
                fail()
            },
            onError = {
                fail()
            })
    }

}