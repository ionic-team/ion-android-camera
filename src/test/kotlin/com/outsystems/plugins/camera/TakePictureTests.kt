package com.outsystems.plugins.camera

import android.app.Activity
import android.content.Intent
import android.media.ExifInterface
import android.net.Uri
import android.os.Environment
import android.util.Log
import com.outsystems.plugins.camera.controller.OSCAMRController
import com.outsystems.plugins.camera.mocks.OSCAMRMediaHelperMock
import com.outsystems.plugins.camera.mocks.OSCAMRExifHelperMock
import com.outsystems.plugins.camera.mocks.OSCAMRFileHelperMock
import com.outsystems.plugins.camera.mocks.OSCAMRImageHelperMock
import com.outsystems.plugins.camera.model.OSCAMRError
import com.outsystems.plugins.camera.model.OSCAMRParameters
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

        val camParameters = OSCAMRParameters(
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

        val exifHelperMock = OSCAMRExifHelperMock()
        val fileHelperMock = OSCAMRFileHelperMock()
        val camHelperMock = OSCAMRMediaHelperMock()
        val imgHelperMock = OSCAMRImageHelperMock()

        val camController = OSCAMRController("someAppId", exifHelperMock, fileHelperMock, camHelperMock, imgHelperMock)

        Mockito.`when`(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)).thenReturn(mFile)

        camController.processResultFromCamera(mockActivity, mIntent, camParameters,
            onImage = {
                fail()
            },
            onMediaResult = {
                fail()
            },
            onError = {
                assertEquals(it.code, OSCAMRError.PROCESS_IMAGE_ERROR.code)
                assertEquals(it.description, OSCAMRError.PROCESS_IMAGE_ERROR.description)
            })
    }

    @Test
    fun givenTakePictureNotCalledPNGWhenProcessResultFromCameraThenTakePhotoError() {

        val camParameters = OSCAMRParameters(
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

        val exifHelperMock = OSCAMRExifHelperMock()
        val fileHelperMock = OSCAMRFileHelperMock()
        val camHelperMock = OSCAMRMediaHelperMock()
        val imgHelperMock = OSCAMRImageHelperMock()

        val camController = OSCAMRController("someAppId", exifHelperMock, fileHelperMock, camHelperMock, imgHelperMock)

        Mockito.`when`(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)).thenReturn(mFile)

        camController.processResultFromCamera(mockActivity, mIntent, camParameters,
            onImage = {
                fail()
            },
            onMediaResult = {
                fail()
            },
            onError = {
                assertEquals(it.code, OSCAMRError.PROCESS_IMAGE_ERROR.code)
                assertEquals(it.description, OSCAMRError.PROCESS_IMAGE_ERROR.description)
            })
    }

    @Test
    fun givenAPI28TakePictureCalledBitmapNullWhenProcessResultFromCameraThenError() {

        val camParameters = OSCAMRParameters(
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

        val exifHelperMock = OSCAMRExifHelperMock()
        val fileHelperMock = OSCAMRFileHelperMock()
        val camHelperMock = OSCAMRMediaHelperMock()
        val imgHelperMock = OSCAMRImageHelperMock()

        val camController = OSCAMRController("someAppId", exifHelperMock, fileHelperMock, camHelperMock, imgHelperMock)

        Mockito.`when`(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)).thenReturn(mFile)

        camController.takePicture(mockActivity, 0, 0)
        camController.processResultFromCamera(mockActivity, mIntent, camParameters,
            onImage = {
                fail()
            },
            onMediaResult = {
                fail()
            },
            onError = {
                assertEquals(it.code, OSCAMRError.PROCESS_IMAGE_ERROR.code)
                assertEquals(it.description, OSCAMRError.PROCESS_IMAGE_ERROR.description)
            })
    }

    @Test
    fun givenAPI28TakePictureCalledJPEGAndDataUriAndProcessOKWhenProcessResultFromCameraThenSuccess() {
        val camParameters = OSCAMRParameters(
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

        val exifHelperMock = OSCAMRExifHelperMock()
        val fileHelperMock = OSCAMRFileHelperMock()
        val camHelperMock = OSCAMRMediaHelperMock()
        val imgHelperMock = OSCAMRImageHelperMock()

        val camController = OSCAMRController("someAppId", exifHelperMock, fileHelperMock, camHelperMock, imgHelperMock)

        Mockito.`when`(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)).thenReturn(mFile)

        imgHelperMock.processPicSuccess = true

        camController.takePicture(mockActivity, 0, 0)
        camController.processResultFromCamera(mockActivity, mIntent, camParameters,
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
        val camParameters = OSCAMRParameters(
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

        val exifHelperMock = OSCAMRExifHelperMock()
        val fileHelperMock = OSCAMRFileHelperMock()
        val camHelperMock = OSCAMRMediaHelperMock()
        val imgHelperMock = OSCAMRImageHelperMock()

        val camController = OSCAMRController("someAppId", exifHelperMock, fileHelperMock, camHelperMock, imgHelperMock)

        Mockito.`when`(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)).thenReturn(mFile)

        imgHelperMock.processPicSuccess = true
        fileHelperMock.isUriNull = true

        camController.takePicture(mockActivity, 0, 0)
        camController.processResultFromCamera(mockActivity, mIntent, camParameters,
            onImage = {
                fail()
            },
            onMediaResult = {
                fail()
            },
            onError = {
                assertEquals(it.code, OSCAMRError.TAKE_PHOTO_ERROR.code)
                assertEquals(it.description, OSCAMRError.TAKE_PHOTO_ERROR.description)
            })
    }

    @Test
    fun givenAPI30TakePictureCalledJPEGAndDataUriAndUriNotNullFileStreamNullWhenProcessResultFromCameraThenError() {
        val camParameters = OSCAMRParameters(
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

        val exifHelperMock = OSCAMRExifHelperMock()
        val fileHelperMock = OSCAMRFileHelperMock()
        val camHelperMock = OSCAMRMediaHelperMock()
        val imgHelperMock = OSCAMRImageHelperMock()

        val camController = OSCAMRController("someAppId", exifHelperMock, fileHelperMock, camHelperMock, imgHelperMock)

        Mockito.`when`(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)).thenReturn(mFile)

        imgHelperMock.processPicSuccess = true
        fileHelperMock.fileExists = true
        fileHelperMock.isUriNull = false
        fileHelperMock.isFileStreamNull = true

        camController.takePicture(mockActivity, 0, 0)
        camController.processResultFromCamera(mockActivity, mIntent, camParameters,
            onImage = {
                fail()
            },
            onMediaResult = {
                fail()
            },
            onError = {
                assertEquals(it.code, OSCAMRError.TAKE_PHOTO_ERROR.code)
                assertEquals(it.description, OSCAMRError.TAKE_PHOTO_ERROR.description)
            })
    }

    @Test
    fun givenAPI30TakePictureCalledJPEGAndDataUriAndUriNotNullBitmapNullWhenProcessResultFromCameraThenError() {
        val camParameters = OSCAMRParameters(
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

        val exifHelperMock = OSCAMRExifHelperMock()
        val fileHelperMock = OSCAMRFileHelperMock()
        val camHelperMock = OSCAMRMediaHelperMock()
        val imgHelperMock = OSCAMRImageHelperMock()

        val camController = OSCAMRController("someAppId", exifHelperMock, fileHelperMock, camHelperMock, imgHelperMock)

        Mockito.`when`(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)).thenReturn(mFile)

        imgHelperMock.processPicSuccess = true
        fileHelperMock.fileExists = true
        fileHelperMock.isUriNull = false
        fileHelperMock.isFileStreamNull = false
        imgHelperMock.isBitmapNull = true

        camController.takePicture(mockActivity, 0, 0)
        camController.processResultFromCamera(mockActivity, mIntent, camParameters,
            onImage = {
                fail()
            },
            onMediaResult = {
                fail()
            },
            onError = {
                assertEquals(it.code, OSCAMRError.TAKE_PHOTO_ERROR.code)
                assertEquals(it.description, OSCAMRError.TAKE_PHOTO_ERROR.description)
            })
    }

    @Test
    fun givenAPI30TakePictureCalledJPEGAndDataUriAndUriNotNullBitmapNotNullWhenProcessResultFromCameraThenSuccess() {
        val camParameters = OSCAMRParameters(
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

        val exifHelperMock = OSCAMRExifHelperMock()
        val fileHelperMock = OSCAMRFileHelperMock()
        val camHelperMock = OSCAMRMediaHelperMock()
        val imgHelperMock = OSCAMRImageHelperMock()

        val camController = OSCAMRController("someAppId", exifHelperMock, fileHelperMock, camHelperMock, imgHelperMock)

        Mockito.`when`(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)).thenReturn(mFile)

        imgHelperMock.processPicSuccess = true
        fileHelperMock.fileExists = true
        fileHelperMock.isUriNull = false
        fileHelperMock.isFileStreamNull = false
        imgHelperMock.isBitmapNull = false
        imgHelperMock.bitmapToBase64Success = true
        fileHelperMock.fileExtension = "jpg"

        camController.takePicture(mockActivity, 0, 0)
        camController.processResultFromCamera(mockActivity, mIntent, camParameters,
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
        val camParameters = OSCAMRParameters(
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

        val exifHelperMock = OSCAMRExifHelperMock()
        val fileHelperMock = OSCAMRFileHelperMock()
        val camHelperMock = OSCAMRMediaHelperMock()
        val imgHelperMock = OSCAMRImageHelperMock()

        val camController = OSCAMRController("someAppId", exifHelperMock, fileHelperMock, camHelperMock, imgHelperMock)

        Mockito.`when`(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)).thenReturn(mFile)

        imgHelperMock.processPicSuccess = true

        camController.takePicture(mockActivity, 0, 1)
        camController.processResultFromCamera(mockActivity, mIntent, camParameters,
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
        val camParameters = OSCAMRParameters(
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

        val exifHelperMock = OSCAMRExifHelperMock()
        val fileHelperMock = OSCAMRFileHelperMock()
        val camHelperMock = OSCAMRMediaHelperMock()
        val imgHelperMock = OSCAMRImageHelperMock()

        val camController = OSCAMRController("someAppId", exifHelperMock, fileHelperMock, camHelperMock, imgHelperMock)

        Mockito.`when`(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)).thenReturn(mFile)

        imgHelperMock.processPicSuccess = false

        camController.takePicture(mockActivity, 0, 0)
        camController.processResultFromCamera(mockActivity, mIntent, camParameters,
            onImage = {
                fail()
            },
            onMediaResult = {
                fail()
            },
            onError = {
                assertEquals(it.code, OSCAMRError.PROCESS_IMAGE_ERROR.code)
                assertEquals(it.description, OSCAMRError.PROCESS_IMAGE_ERROR.description)
            })
    }


    @Test
    fun givenAPI28TakePictureCalledPNGAndProcessErrorWhenProcessResultFromCameraThenError() {
        val camParameters = OSCAMRParameters(
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

        val exifHelperMock = OSCAMRExifHelperMock()
        val fileHelperMock = OSCAMRFileHelperMock()
        val camHelperMock = OSCAMRMediaHelperMock()
        val imgHelperMock = OSCAMRImageHelperMock()

        val camController = OSCAMRController("someAppId", exifHelperMock, fileHelperMock, camHelperMock, imgHelperMock)

        Mockito.`when`(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)).thenReturn(mFile)

        imgHelperMock.processPicSuccess = false

        camController.takePicture(mockActivity, 0, 1)
        camController.processResultFromCamera(mockActivity, mIntent, camParameters,
            onImage = {
                fail()
            },
            onMediaResult = {
                fail()
            },
            onError = {
                assertEquals(it.code, OSCAMRError.PROCESS_IMAGE_ERROR.code)
                assertEquals(it.description, OSCAMRError.PROCESS_IMAGE_ERROR.description)
            })
    }

    @Test
    fun givenTakePictureCalledJPEGAndCompressOKWhenProcessResultFromCameraThenSuccess() {
        val camParameters = OSCAMRParameters(
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

        val exifHelperMock = OSCAMRExifHelperMock()
        val fileHelperMock = OSCAMRFileHelperMock()
        val camHelperMock = OSCAMRMediaHelperMock()
        val imgHelperMock = OSCAMRImageHelperMock()

        val camController = OSCAMRController("someAppId", exifHelperMock, fileHelperMock, camHelperMock, imgHelperMock)

        Mockito.`when`(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)).thenReturn(mFile)

        imgHelperMock.processPicSuccess = true
        fileHelperMock.getUriResult = OSCAMRFileHelperMock.GET_URI_SUCCESS

        camController.takePicture(mockActivity, 0, 0)
        camController.processResultFromCamera(mockActivity, mIntent, camParameters,
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
        val camParameters = OSCAMRParameters(
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

        val exifHelperMock = OSCAMRExifHelperMock()
        val fileHelperMock = OSCAMRFileHelperMock()
        val camHelperMock = OSCAMRMediaHelperMock()
        val imgHelperMock = OSCAMRImageHelperMock()

        val camController = OSCAMRController("someAppId", exifHelperMock, fileHelperMock, camHelperMock, imgHelperMock)

        Mockito.`when`(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)).thenReturn(mFile)

        imgHelperMock.processPicSuccess = true
        fileHelperMock.getUriResult = OSCAMRFileHelperMock.GET_URI_SUCCESS

        camController.takePicture(mockActivity, 0, 1)
        camController.processResultFromCamera(mockActivity, mIntent, camParameters,
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
        val camParameters = OSCAMRParameters(
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

        val exifHelperMock = OSCAMRExifHelperMock()
        val fileHelperMock = OSCAMRFileHelperMock()
        val camHelperMock = OSCAMRMediaHelperMock()
        val imgHelperMock = OSCAMRImageHelperMock()

        val camController = OSCAMRController("someAppId", exifHelperMock, fileHelperMock, camHelperMock, imgHelperMock)

        Mockito.`when`(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)).thenReturn(mFile)

        imgHelperMock.processPicSuccess = true
        fileHelperMock.getUriResult = OSCAMRFileHelperMock.GET_URI_SUCCESS

        camController.takePicture(mockActivity, 0, 0)
        camController.processResultFromCamera(mockActivity, mIntent, camParameters,
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
        val camParameters = OSCAMRParameters(
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

        val exifHelperMock = OSCAMRExifHelperMock()
        val fileHelperMock = OSCAMRFileHelperMock()
        val camHelperMock = OSCAMRMediaHelperMock()
        val imgHelperMock = OSCAMRImageHelperMock()

        val camController = OSCAMRController("someAppId", exifHelperMock, fileHelperMock, camHelperMock, imgHelperMock)

        Mockito.`when`(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)).thenReturn(mFile)

        imgHelperMock.processPicSuccess = true
        fileHelperMock.getUriResult = OSCAMRFileHelperMock.GET_URI_SUCCESS

        camController.takePicture(mockActivity, 0, 1)
        camController.processResultFromCamera(mockActivity, mIntent, camParameters,
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
        val camParameters = OSCAMRParameters(
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

        val exifHelperMock = OSCAMRExifHelperMock()
        val fileHelperMock = OSCAMRFileHelperMock()
        val camHelperMock = OSCAMRMediaHelperMock()
        val imgHelperMock = OSCAMRImageHelperMock()

        val camController = OSCAMRController("someAppId", exifHelperMock, fileHelperMock, camHelperMock, imgHelperMock)

        Mockito.`when`(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)).thenReturn(mFile)

        imgHelperMock.processPicSuccess = true
        imgHelperMock.areOptionsZero = false

        camController.takePicture(mockActivity, 0, 0)
        camController.processResultFromCamera(mockActivity, mIntent, camParameters,
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
        val camParameters = OSCAMRParameters(
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

        val exifHelperMock = OSCAMRExifHelperMock()
        val fileHelperMock = OSCAMRFileHelperMock()
        val camHelperMock = OSCAMRMediaHelperMock()
        val imgHelperMock = OSCAMRImageHelperMock()

        val camController = OSCAMRController("someAppId", exifHelperMock, fileHelperMock, camHelperMock, imgHelperMock)

        Mockito.`when`(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)).thenReturn(mFile)

        imgHelperMock.processPicSuccess = true
        imgHelperMock.areOptionsZero = false

        camController.takePicture(mockActivity, 0, 1)
        camController.processResultFromCamera(mockActivity, mIntent, camParameters,
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
        val camParameters = OSCAMRParameters(
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

        val exifHelperMock = OSCAMRExifHelperMock()
        val fileHelperMock = OSCAMRFileHelperMock()
        val camHelperMock = OSCAMRMediaHelperMock()
        val imgHelperMock = OSCAMRImageHelperMock()

        val camController = OSCAMRController("someAppId", exifHelperMock, fileHelperMock, camHelperMock, imgHelperMock)

        Mockito.`when`(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)).thenReturn(mFile)

        imgHelperMock.processPicSuccess = false
        imgHelperMock.areOptionsZero = false

        camController.takePicture(mockActivity, 0, 0)
        camController.processResultFromCamera(mockActivity, mIntent, camParameters,
            onImage = {
                fail()
            },
            onMediaResult = {
                fail()
            },
            onError = {
                assertEquals(it.code, OSCAMRError.PROCESS_IMAGE_ERROR.code)
                assertEquals(it.description, OSCAMRError.PROCESS_IMAGE_ERROR.description)
            })
    }

    @Test
    fun givenTakePictureAndCustomWidthHeightPNGAndProcessErrorWhenProcessResultFromCameraThenError() {
        val camParameters = OSCAMRParameters(
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

        val exifHelperMock = OSCAMRExifHelperMock()
        val fileHelperMock = OSCAMRFileHelperMock()
        val camHelperMock = OSCAMRMediaHelperMock()
        val imgHelperMock = OSCAMRImageHelperMock()

        val camController = OSCAMRController("someAppId", exifHelperMock, fileHelperMock, camHelperMock, imgHelperMock)

        Mockito.`when`(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)).thenReturn(mFile)

        imgHelperMock.processPicSuccess = false
        imgHelperMock.areOptionsZero = false

        camController.takePicture(mockActivity, 0, 1)
        camController.processResultFromCamera(mockActivity, mIntent, camParameters,
            onImage = {
                fail()
            },
            onMediaResult = {
                fail()
            },
            onError = {
                assertEquals(it.code, OSCAMRError.PROCESS_IMAGE_ERROR.code)
                assertEquals(it.description, OSCAMRError.PROCESS_IMAGE_ERROR.description)
            })
    }

    @Test
    fun givenTakePictureAndCustomWidthHeightJPEGAndOptionsZeroWhenProcessResultFromCameraThenError() {
        val camParameters = OSCAMRParameters(
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

        val exifHelperMock = OSCAMRExifHelperMock()
        val fileHelperMock = OSCAMRFileHelperMock()
        val camHelperMock = OSCAMRMediaHelperMock()
        val imgHelperMock = OSCAMRImageHelperMock()

        val camController = OSCAMRController("someAppId", exifHelperMock, fileHelperMock, camHelperMock, imgHelperMock)

        Mockito.`when`(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)).thenReturn(mFile)

        imgHelperMock.processPicSuccess = false
        imgHelperMock.areOptionsZero = true

        camController.takePicture(mockActivity, 0, 0)
        camController.processResultFromCamera(mockActivity, mIntent, camParameters,
            onImage = {
                fail()
            },
            onMediaResult = {
                fail()
            },
            onError = {
                assertEquals(it.code, OSCAMRError.PROCESS_IMAGE_ERROR.code)
                assertEquals(it.description, OSCAMRError.PROCESS_IMAGE_ERROR.description)
            })
    }

    @Test
    fun givenTakePictureAndCustomWidthHeightPNGAndOptionsZeroWhenProcessResultFromCameraThenError() {
        val camParameters = OSCAMRParameters(
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

        val exifHelperMock = OSCAMRExifHelperMock()
        val fileHelperMock = OSCAMRFileHelperMock()
        val camHelperMock = OSCAMRMediaHelperMock()
        val imgHelperMock = OSCAMRImageHelperMock()

        val camController = OSCAMRController("someAppId", exifHelperMock, fileHelperMock, camHelperMock, imgHelperMock)

        Mockito.`when`(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)).thenReturn(mFile)

        imgHelperMock.processPicSuccess = false
        imgHelperMock.areOptionsZero = true

        camController.takePicture(mockActivity, 0, 1)
        camController.processResultFromCamera(mockActivity, mIntent, camParameters,
            onImage = {
                fail()
            },
            onMediaResult = {
                fail()
            },
            onError = {
                assertEquals(it.code, OSCAMRError.PROCESS_IMAGE_ERROR.code)
                assertEquals(it.description, OSCAMRError.PROCESS_IMAGE_ERROR.description)
            })
    }

    @Test
    fun givenTakePictureAndCustomWidthHeightJPEGCompressOKWhenProcessResultFromCameraThenSuccess() {
        val camParameters = OSCAMRParameters(
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

        val exifHelperMock = OSCAMRExifHelperMock()
        val fileHelperMock = OSCAMRFileHelperMock()
        val camHelperMock = OSCAMRMediaHelperMock()
        val imgHelperMock = OSCAMRImageHelperMock()

        val camController = OSCAMRController("someAppId", exifHelperMock, fileHelperMock, camHelperMock, imgHelperMock)

        Mockito.`when`(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)).thenReturn(mFile)

        imgHelperMock.processPicSuccess = true

        camController.takePicture(mockActivity, 0, 0)
        camController.processResultFromCamera(mockActivity, mIntent, camParameters,
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
        val camParameters = OSCAMRParameters(
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

        val exifHelperMock = OSCAMRExifHelperMock()
        val fileHelperMock = OSCAMRFileHelperMock()
        val camHelperMock = OSCAMRMediaHelperMock()
        val imgHelperMock = OSCAMRImageHelperMock()

        val camController = OSCAMRController("someAppId", exifHelperMock, fileHelperMock, camHelperMock, imgHelperMock)

        Mockito.`when`(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)).thenReturn(mFile)

        imgHelperMock.processPicSuccess = true

        camController.takePicture(mockActivity, 0, 1)
        camController.processResultFromCamera(mockActivity, mIntent, camParameters,
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
        val camParameters = OSCAMRParameters(
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

        val exifHelperMock = OSCAMRExifHelperMock()
        val fileHelperMock = OSCAMRFileHelperMock()
        val camHelperMock = OSCAMRMediaHelperMock()
        val imgHelperMock = OSCAMRImageHelperMock()

        val camController = OSCAMRController("someAppId", exifHelperMock, fileHelperMock, camHelperMock, imgHelperMock)

        Mockito.`when`(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)).thenReturn(mFile)

        imgHelperMock.compressImageSuccess = false

        camController.takePicture(mockActivity, 0, 0)
        camController.processResultFromCamera(mockActivity, mIntent, camParameters,
            onImage = {
                fail()
            },
            onMediaResult = {
                fail()
            },
            onError = {
                assertEquals(it.code, OSCAMRError.PROCESS_IMAGE_ERROR.code)
                assertEquals(it.description, OSCAMRError.PROCESS_IMAGE_ERROR.description)
            })
    }

    @Test
    fun givenTakePictureAndCustomWidthHeightPNGCompressErrorWhenProcessResultFromCameraThenError() {
        val camParameters = OSCAMRParameters(
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

        val exifHelperMock = OSCAMRExifHelperMock()
        val fileHelperMock = OSCAMRFileHelperMock()
        val camHelperMock = OSCAMRMediaHelperMock()
        val imgHelperMock = OSCAMRImageHelperMock()

        val camController = OSCAMRController("someAppId", exifHelperMock, fileHelperMock, camHelperMock, imgHelperMock)

        Mockito.`when`(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)).thenReturn(mFile)

        imgHelperMock.compressImageSuccess = false

        camController.takePicture(mockActivity, 0, 1)
        camController.processResultFromCamera(mockActivity, mIntent, camParameters,
            onImage = {
                fail()
            },
            onMediaResult = {
                fail()
            },
            onError = {
                assertEquals(it.code, OSCAMRError.PROCESS_IMAGE_ERROR.code)
                assertEquals(it.description, OSCAMRError.PROCESS_IMAGE_ERROR.description)
            })
    }


    @Test
    fun givenTakePictureAndCorrectJPEGAndRotate90RatioSameWhenProcessResultFromCameraThenSuccess() {
        val camParameters = OSCAMRParameters(
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

        val exifHelperMock = OSCAMRExifHelperMock()
        val fileHelperMock = OSCAMRFileHelperMock()
        val camHelperMock = OSCAMRMediaHelperMock()
        val imgHelperMock = OSCAMRImageHelperMock()

        val camController = OSCAMRController("someAppId", exifHelperMock, fileHelperMock, camHelperMock, imgHelperMock)

        Mockito.`when`(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)).thenReturn(mFile)

        imgHelperMock.processPicSuccess = true
        imgHelperMock.areOptionsZero = false
        imgHelperMock.outHeight = 10
        imgHelperMock.outWidth = 10
        exifHelperMock.testOrientation = ExifInterface.ORIENTATION_ROTATE_90

        camController.takePicture(mockActivity, 0, 0)
        camController.processResultFromCamera(mockActivity, mIntent, camParameters,
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
        val camParameters = OSCAMRParameters(
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

        val exifHelperMock = OSCAMRExifHelperMock()
        val fileHelperMock = OSCAMRFileHelperMock()
        val camHelperMock = OSCAMRMediaHelperMock()
        val imgHelperMock = OSCAMRImageHelperMock()

        val camController = OSCAMRController("someAppId", exifHelperMock, fileHelperMock, camHelperMock, imgHelperMock)

        Mockito.`when`(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)).thenReturn(mFile)

        imgHelperMock.processPicSuccess = true
        imgHelperMock.areOptionsZero = false
        imgHelperMock.outHeight = 10
        imgHelperMock.outWidth = 10
        exifHelperMock.testOrientation = ExifInterface.ORIENTATION_ROTATE_90

        camController.takePicture(mockActivity, 0, 1)
        camController.processResultFromCamera(mockActivity, mIntent, camParameters,
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
        val camParameters = OSCAMRParameters(
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

        val exifHelperMock = OSCAMRExifHelperMock()
        val fileHelperMock = OSCAMRFileHelperMock()
        val camHelperMock = OSCAMRMediaHelperMock()
        val imgHelperMock = OSCAMRImageHelperMock()

        val camController = OSCAMRController("someAppId", exifHelperMock, fileHelperMock, camHelperMock, imgHelperMock)

        Mockito.`when`(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)).thenReturn(mFile)

        imgHelperMock.processPicSuccess = true
        imgHelperMock.areOptionsZero = false
        imgHelperMock.outHeight = 12
        imgHelperMock.outWidth = 10
        exifHelperMock.testOrientation = ExifInterface.ORIENTATION_ROTATE_90

        camController.takePicture(mockActivity, 0, 0)
        camController.processResultFromCamera(mockActivity, mIntent, camParameters,
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
        val camParameters = OSCAMRParameters(
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

        val exifHelperMock = OSCAMRExifHelperMock()
        val fileHelperMock = OSCAMRFileHelperMock()
        val camHelperMock = OSCAMRMediaHelperMock()
        val imgHelperMock = OSCAMRImageHelperMock()

        val camController = OSCAMRController("someAppId", exifHelperMock, fileHelperMock, camHelperMock, imgHelperMock)

        Mockito.`when`(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)).thenReturn(mFile)

        imgHelperMock.processPicSuccess = true
        imgHelperMock.areOptionsZero = false
        imgHelperMock.outHeight = 12
        imgHelperMock.outWidth = 10
        exifHelperMock.testOrientation = ExifInterface.ORIENTATION_ROTATE_90

        camController.takePicture(mockActivity, 0, 1)
        camController.processResultFromCamera(mockActivity, mIntent, camParameters,
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
        val camParameters = OSCAMRParameters(
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

        val exifHelperMock = OSCAMRExifHelperMock()
        val fileHelperMock = OSCAMRFileHelperMock()
        val camHelperMock = OSCAMRMediaHelperMock()
        val imgHelperMock = OSCAMRImageHelperMock()

        val camController = OSCAMRController("someAppId", exifHelperMock, fileHelperMock, camHelperMock, imgHelperMock)

        Mockito.`when`(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)).thenReturn(mFile)

        imgHelperMock.processPicSuccess = true
        imgHelperMock.areOptionsZero = false
        imgHelperMock.outHeight = 10
        imgHelperMock.outWidth = 12
        exifHelperMock.testOrientation = ExifInterface.ORIENTATION_ROTATE_90

        camController.takePicture(mockActivity, 0, 0)
        camController.processResultFromCamera(mockActivity, mIntent, camParameters,
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
        val camParameters = OSCAMRParameters(
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

        val exifHelperMock = OSCAMRExifHelperMock()
        val fileHelperMock = OSCAMRFileHelperMock()
        val camHelperMock = OSCAMRMediaHelperMock()
        val imgHelperMock = OSCAMRImageHelperMock()

        val camController = OSCAMRController("someAppId", exifHelperMock, fileHelperMock, camHelperMock, imgHelperMock)

        Mockito.`when`(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)).thenReturn(mFile)

        imgHelperMock.processPicSuccess = true
        imgHelperMock.areOptionsZero = false
        imgHelperMock.outHeight = 10
        imgHelperMock.outWidth = 12
        exifHelperMock.testOrientation = ExifInterface.ORIENTATION_ROTATE_90

        camController.takePicture(mockActivity, 0, 1)
        camController.processResultFromCamera(mockActivity, mIntent, camParameters,
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
        val camParameters = OSCAMRParameters(
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

        val exifHelperMock = OSCAMRExifHelperMock()
        val fileHelperMock = OSCAMRFileHelperMock()
        val camHelperMock = OSCAMRMediaHelperMock()
        val imgHelperMock = OSCAMRImageHelperMock()

        val camController = OSCAMRController("someAppId", exifHelperMock, fileHelperMock, camHelperMock, imgHelperMock)

        Mockito.`when`(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)).thenReturn(mFile)

        imgHelperMock.processPicSuccess = true
        imgHelperMock.areOptionsZero = false
        imgHelperMock.outHeight = -1
        imgHelperMock.outWidth = -1
        exifHelperMock.testOrientation = ExifInterface.ORIENTATION_ROTATE_90

        camController.takePicture(mockActivity, 0, 0)
        camController.processResultFromCamera(mockActivity, mIntent, camParameters,
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
        val camParameters = OSCAMRParameters(
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

        val exifHelperMock = OSCAMRExifHelperMock()
        val fileHelperMock = OSCAMRFileHelperMock()
        val camHelperMock = OSCAMRMediaHelperMock()
        val imgHelperMock = OSCAMRImageHelperMock()

        val camController = OSCAMRController("someAppId", exifHelperMock, fileHelperMock, camHelperMock, imgHelperMock)

        Mockito.`when`(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)).thenReturn(mFile)

        imgHelperMock.processPicSuccess = true
        imgHelperMock.areOptionsZero = false
        imgHelperMock.outHeight = -1
        imgHelperMock.outWidth = -1
        exifHelperMock.testOrientation = ExifInterface.ORIENTATION_ROTATE_90

        camController.takePicture(mockActivity, 0, 1)
        camController.processResultFromCamera(mockActivity, mIntent, camParameters,
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
        val camParameters = OSCAMRParameters(
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

        val exifHelperMock = OSCAMRExifHelperMock()
        val fileHelperMock = OSCAMRFileHelperMock()
        val camHelperMock = OSCAMRMediaHelperMock()
        val imgHelperMock = OSCAMRImageHelperMock()

        val camController = OSCAMRController("someAppId", exifHelperMock, fileHelperMock, camHelperMock, imgHelperMock)

        Mockito.`when`(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)).thenReturn(mFile)

        imgHelperMock.processPicSuccess = true
        imgHelperMock.areOptionsZero = false
        imgHelperMock.outHeight = -1
        imgHelperMock.outWidth = 1
        exifHelperMock.testOrientation = ExifInterface.ORIENTATION_ROTATE_90

        camController.takePicture(mockActivity, 0, 0)
        camController.processResultFromCamera(mockActivity, mIntent, camParameters,
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
        val camParameters = OSCAMRParameters(
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

        val exifHelperMock = OSCAMRExifHelperMock()
        val fileHelperMock = OSCAMRFileHelperMock()
        val camHelperMock = OSCAMRMediaHelperMock()
        val imgHelperMock = OSCAMRImageHelperMock()

        val camController = OSCAMRController("someAppId", exifHelperMock, fileHelperMock, camHelperMock, imgHelperMock)

        Mockito.`when`(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)).thenReturn(mFile)

        imgHelperMock.processPicSuccess = true
        imgHelperMock.areOptionsZero = false
        imgHelperMock.outHeight = -1
        imgHelperMock.outWidth = 1
        exifHelperMock.testOrientation = ExifInterface.ORIENTATION_ROTATE_90

        camController.takePicture(mockActivity, 0, 1)
        camController.processResultFromCamera(mockActivity, mIntent, camParameters,
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
        val camParameters = OSCAMRParameters(
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

        val exifHelperMock = OSCAMRExifHelperMock()
        val fileHelperMock = OSCAMRFileHelperMock()
        val camHelperMock = OSCAMRMediaHelperMock()
        val imgHelperMock = OSCAMRImageHelperMock()

        val camController = OSCAMRController("someAppId", exifHelperMock, fileHelperMock, camHelperMock, imgHelperMock)

        Mockito.`when`(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)).thenReturn(mFile)

        imgHelperMock.processPicSuccess = true
        imgHelperMock.areOptionsZero = false
        imgHelperMock.outHeight = 1
        imgHelperMock.outWidth = -1
        exifHelperMock.testOrientation = ExifInterface.ORIENTATION_ROTATE_90

        camController.takePicture(mockActivity, 0, 0)
        camController.processResultFromCamera(mockActivity, mIntent, camParameters,
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
        val camParameters = OSCAMRParameters(
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

        val exifHelperMock = OSCAMRExifHelperMock()
        val fileHelperMock = OSCAMRFileHelperMock()
        val camHelperMock = OSCAMRMediaHelperMock()
        val imgHelperMock = OSCAMRImageHelperMock()

        val camController = OSCAMRController("someAppId", exifHelperMock, fileHelperMock, camHelperMock, imgHelperMock)

        Mockito.`when`(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)).thenReturn(mFile)

        imgHelperMock.processPicSuccess = true
        imgHelperMock.areOptionsZero = false
        imgHelperMock.outHeight = 1
        imgHelperMock.outWidth = -1
        exifHelperMock.testOrientation = ExifInterface.ORIENTATION_ROTATE_90

        camController.takePicture(mockActivity, 0, 1)
        camController.processResultFromCamera(mockActivity, mIntent, camParameters,
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
        val camParameters = OSCAMRParameters(
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

        val exifHelperMock = OSCAMRExifHelperMock()
        val fileHelperMock = OSCAMRFileHelperMock()
        val camHelperMock = OSCAMRMediaHelperMock()
        val imgHelperMock = OSCAMRImageHelperMock()

        val camController = OSCAMRController("someAppId", exifHelperMock, fileHelperMock, camHelperMock, imgHelperMock)

        Mockito.`when`(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)).thenReturn(mFile)

        imgHelperMock.processPicSuccess = true
        imgHelperMock.areOptionsZero = false
        exifHelperMock.testOrientation = ExifInterface.ORIENTATION_ROTATE_180

        camController.takePicture(mockActivity, 0, 0)
        camController.processResultFromCamera(mockActivity, mIntent, camParameters,
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
        val camParameters = OSCAMRParameters(
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

        val exifHelperMock = OSCAMRExifHelperMock()
        val fileHelperMock = OSCAMRFileHelperMock()
        val camHelperMock = OSCAMRMediaHelperMock()
        val imgHelperMock = OSCAMRImageHelperMock()

        val camController = OSCAMRController("someAppId", exifHelperMock, fileHelperMock, camHelperMock, imgHelperMock)

        Mockito.`when`(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)).thenReturn(mFile)

        imgHelperMock.processPicSuccess = true
        imgHelperMock.areOptionsZero = false
        exifHelperMock.testOrientation = ExifInterface.ORIENTATION_ROTATE_180

        camController.takePicture(mockActivity, 0, 1)
        camController.processResultFromCamera(mockActivity, mIntent, camParameters,
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
        val camParameters = OSCAMRParameters(
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

        val exifHelperMock = OSCAMRExifHelperMock()
        val fileHelperMock = OSCAMRFileHelperMock()
        val camHelperMock = OSCAMRMediaHelperMock()
        val imgHelperMock = OSCAMRImageHelperMock()

        val camController = OSCAMRController("someAppId", exifHelperMock, fileHelperMock, camHelperMock, imgHelperMock)

        Mockito.`when`(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)).thenReturn(mFile)

        imgHelperMock.processPicSuccess = true
        imgHelperMock.areOptionsZero = false

        camController.takePicture(mockActivity, 0, 0)
        camController.processResultFromCamera(mockActivity, mIntent, camParameters,
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
        val camParameters = OSCAMRParameters(
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

        val exifHelperMock = OSCAMRExifHelperMock()
        val fileHelperMock = OSCAMRFileHelperMock()
        val camHelperMock = OSCAMRMediaHelperMock()
        val imgHelperMock = OSCAMRImageHelperMock()

        val camController = OSCAMRController("someAppId", exifHelperMock, fileHelperMock, camHelperMock, imgHelperMock)

        Mockito.`when`(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)).thenReturn(mFile)

        imgHelperMock.processPicSuccess = true
        imgHelperMock.areOptionsZero = false

        camController.takePicture(mockActivity, 0, 0)
        camController.processResultFromCamera(mockActivity, mIntent, camParameters,
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
        val camParameters = OSCAMRParameters(
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

        val exifHelperMock = OSCAMRExifHelperMock()
        val fileHelperMock = OSCAMRFileHelperMock()
        val camHelperMock = OSCAMRMediaHelperMock()
        val imgHelperMock = OSCAMRImageHelperMock()

        val camController = OSCAMRController("someAppId", exifHelperMock, fileHelperMock, camHelperMock, imgHelperMock)

        Mockito.`when`(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)).thenReturn(mFile)

        imgHelperMock.processPicSuccess = true
        imgHelperMock.areOptionsZero = false

        camController.takePicture(mockActivity, 0, 0)
        camController.processResultFromCamera(mockActivity, mIntent, camParameters,
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