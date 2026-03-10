package com.outsystems.plugins.camera

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Environment
import android.util.Base64
import android.util.Log
import com.outsystems.plugins.camera.controller.OSCAMRController
import com.outsystems.plugins.camera.controller.helper.OSCAMRMediaHelper
import com.outsystems.plugins.camera.mocks.OSCAMRMediaHelperMock
import com.outsystems.plugins.camera.mocks.OSCAMRExifHelperMock
import com.outsystems.plugins.camera.mocks.OSCAMRFileHelperMock
import com.outsystems.plugins.camera.mocks.OSCAMRImageHelperMock
import com.outsystems.plugins.camera.model.OSCAMRError
import kotlinx.coroutines.runBlocking
import org.junit.*
import org.junit.Assert.*
import org.junit.runner.RunWith
import org.mockito.Mockito
import org.mockito.junit.MockitoJUnitRunner
import java.io.File

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
@RunWith(MockitoJUnitRunner::class)
class CaptureVideoTests {

    private val mIntent = Mockito.mock(Intent::class.java)
    private val mEnvironment = Mockito.mockStatic(Environment::class.java)
    private val mLog = Mockito.mockStatic(Log::class.java)
    private val mUriStatic = Mockito.mockStatic(Uri::class.java)
    private lateinit var mUri: Uri
    private val mFile = Mockito.mock(File::class.java)
    private val mBase64 = Mockito.mockStatic(Base64::class.java)


    private lateinit var mockActivity: Activity

    companion object {
        private const val VIDEO_URI = "videoUri"
        private const val VIDEO_URI_GALLERY = "videoUriFromGallery"
        private const val BASE_64 = "base64"
        private const val METADATA_SIZE = 23456L
        private const val METADATA_DURATION = 2
        private const val METADATA_FORMAT = "mp4"
        private const val METADATA_RESOLUTION = "1920x1080"
        private const val METADATA_CREATION_DATE = "2023-03-30T09:01:27Z"
    }

    @Before
    fun before() {
        mockActivity = Mockito.mock(Activity::class.java)
        mUri = Mockito.mock(Uri::class.java)
        Mockito.doReturn("path").`when`(mUri).path
    }

    @After
    fun after() {
        mEnvironment.close()
        mLog.close()
        mUriStatic.close()
        mBase64.close()
    }

    @Test
    fun givenCaptureVideoOkWhenProcessResultThenSuccess() {

        val exifHelperMock = OSCAMRExifHelperMock()
        val fileHelperMock = OSCAMRFileHelperMock()
        val camHelperMock = OSCAMRMediaHelperMock()
        val imgHelperMock = OSCAMRImageHelperMock()

        fileHelperMock.fileExists = true

        val camController = OSCAMRController(
            "someAppId",
            exifHelperMock,
            fileHelperMock,
            camHelperMock,
            imgHelperMock
        )
        fileHelperMock.inputStreamUriFilePath = VIDEO_URI_GALLERY

        Mockito.`when`(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES))
            .thenReturn(mFile)

        camController.captureVideo(mockActivity) {
            // do nothing
        }

        runBlocking {
            camController.processResultFromVideo(mockActivity,
                mUri,
                fromGallery = false,
                isPersistent = false,
                includeMetadata = false,
                onSuccess = {
                    assertEquals(it.type, 1)
                    assertEquals(it.uri, VIDEO_URI)
                    assertEquals(it.thumbnail, BASE_64)
                    assertEquals(it.metadata, null)
                },
                onError = {
                    fail()
                }
            )
            camController.processResultFromVideo(mockActivity,
                mUri,
                fromGallery = true,
                isPersistent = false,
                includeMetadata = false,
                onSuccess = {
                    assertEquals(it.type, 1)
                    assertEquals(it.uri, VIDEO_URI_GALLERY)
                    assertEquals(it.thumbnail, BASE_64)
                    assertEquals(it.metadata, null)
                },
                onError = {
                    fail()
                }
            )
        }
    }

    @Test
    fun givenCaptureVideoWithMetadataOkWhenProcessResultThenSuccess() {

        val exifHelperMock = OSCAMRExifHelperMock()
        val fileHelperMock = OSCAMRFileHelperMock()
        val camHelperMock = OSCAMRMediaHelperMock()
        val imgHelperMock = OSCAMRImageHelperMock()

        fileHelperMock.fileExists = true
        fileHelperMock.fileSize = METADATA_SIZE
        fileHelperMock.fileExtension = METADATA_FORMAT
        fileHelperMock.creationDateTime = METADATA_CREATION_DATE
        camHelperMock.resolution = Pair(1920, 1080)
        camHelperMock.duration = 2222

        val camController = OSCAMRController(
            "someAppId",
            exifHelperMock,
            fileHelperMock,
            camHelperMock,
            imgHelperMock
        )
        fileHelperMock.inputStreamUriFilePath = VIDEO_URI_GALLERY

        Mockito.`when`(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES))
            .thenReturn(mFile)

        camController.captureVideo(mockActivity) {
            // do nothing
        }

        runBlocking {
            camController.processResultFromVideo(mockActivity,
                mUri,
                fromGallery = false,
                isPersistent = false,
                includeMetadata = true,
                onSuccess = {
                    assertEquals(it.type, 1)
                    assertEquals(it.uri, VIDEO_URI)
                    assertEquals(it.thumbnail, BASE_64)
                    assertNotEquals(it.metadata, null)
                    assertEquals(it.metadata?.size, METADATA_SIZE)
                    assertEquals(it.metadata?.duration, METADATA_DURATION)
                    assertEquals(it.metadata?.resolution, METADATA_RESOLUTION)
                    assertEquals(it.metadata?.format, METADATA_FORMAT)
                    assertEquals(it.metadata?.creationDate, METADATA_CREATION_DATE)
                },
                onError = {
                    fail()
                }
            )
            camController.processResultFromVideo(mockActivity,
                mUri,
                fromGallery = true,
                isPersistent = false,
                includeMetadata = true,
                onSuccess = {
                    assertEquals(it.type, 1)
                    assertEquals(it.uri, VIDEO_URI_GALLERY)
                    assertEquals(it.thumbnail, BASE_64)
                    assertNotEquals(it.metadata, null)
                    assertEquals(it.metadata?.size, METADATA_SIZE)
                    assertEquals(it.metadata?.duration, METADATA_DURATION)
                    assertEquals(it.metadata?.resolution, METADATA_RESOLUTION)
                    assertEquals(it.metadata?.format, METADATA_FORMAT)
                    assertEquals(it.metadata?.creationDate, METADATA_CREATION_DATE)
                },
                onError = {
                    fail()
                }
            )
        }
    }

    @Test
    fun givenNoCameraWhenCaptureVideoThenError() {

        val exifHelperMock = OSCAMRExifHelperMock()
        val fileHelperMock = OSCAMRFileHelperMock()
        val camHelperMock = OSCAMRMediaHelperMock()
        val imgHelperMock = OSCAMRImageHelperMock()

        val camController = OSCAMRController(
            "someAppId",
            exifHelperMock,
            fileHelperMock,
            camHelperMock,
            imgHelperMock
        )

        Mockito.`when`(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES))
            .thenReturn(mFile)

        camHelperMock.existsActivity = false

        camController.captureVideo(mockActivity) {
            assertEquals(it.code, OSCAMRError.NO_CAMERA_AVAILABLE_ERROR.code)
            assertEquals(it.description, OSCAMRError.NO_CAMERA_AVAILABLE_ERROR.description)
        }

    }

    @Test
    fun givenCaptureVideoFilePathNullWhenProcessResultThenError() {

        val exifHelperMock = OSCAMRExifHelperMock()
        val fileHelperMock = OSCAMRFileHelperMock()
        val camHelperMock = OSCAMRMediaHelperMock()
        val imgHelperMock = OSCAMRImageHelperMock()

        val camController = OSCAMRController(
            "someAppId",
            exifHelperMock,
            fileHelperMock,
            camHelperMock,
            imgHelperMock
        )

        Mockito.`when`(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES))
            .thenReturn(mFile)

        camController.captureVideo(mockActivity) {
            // do nothing
        }
        runBlocking {
            camController.processResultFromVideo(mockActivity, mUri,
                fromGallery = false,
                isPersistent = false,
                includeMetadata = false,
                onSuccess = {
                    fail()
                },
                onError = {
                    assertEquals(it.code, OSCAMRError.MEDIA_PATH_ERROR.code)
                    assertEquals(it.description, OSCAMRError.MEDIA_PATH_ERROR.description)
                }
            )
            camController.processResultFromVideo(mockActivity, mUri,
                fromGallery = true,
                isPersistent = false,
                includeMetadata = false,
                onSuccess = {
                    assert(true)
                },
                onError = {
                    assertEquals(it.code, OSCAMRError.MEDIA_PATH_ERROR.code)
                    assertEquals(it.description, OSCAMRError.MEDIA_PATH_ERROR.description)
                }
            )
        }
    }

    @Test
    fun givenCaptureVideoFilePathEmptyWhenProcessResultThenError() {

        val exifHelperMock = OSCAMRExifHelperMock()
        val fileHelperMock = OSCAMRFileHelperMock()
        val camHelperMock = OSCAMRMediaHelperMock()
        val imgHelperMock = OSCAMRImageHelperMock()

        val camController = OSCAMRController(
            "someAppId",
            exifHelperMock,
            fileHelperMock,
            camHelperMock,
            imgHelperMock
        )

        Mockito.`when`(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES))
            .thenReturn(mFile)

        camController.captureVideo(mockActivity) {
            // do nothing
        }
        runBlocking {
            camController.processResultFromVideo(mockActivity, mUri,
                fromGallery = false,
                isPersistent = false,
                includeMetadata = false,
                onSuccess = {
                    fail()
                },
                onError = {
                    assertEquals(it.code, OSCAMRError.MEDIA_PATH_ERROR.code)
                    assertEquals(it.description, OSCAMRError.MEDIA_PATH_ERROR.description)
                }
            )
            camController.processResultFromVideo(mockActivity, mUri,
                fromGallery = true,
                isPersistent = false,
                includeMetadata = false,
                onSuccess = {
                    fail()
                },
                onError = {
                    assertEquals(it.code, OSCAMRError.MEDIA_PATH_ERROR.code)
                    assertEquals(it.description, OSCAMRError.MEDIA_PATH_ERROR.description)
                }
            )
        }
    }

    @Test
    fun givenCaptureVideoOkAndProcessResultOkWhenDeleteThenSuccess() {
        val exifHelperMock = OSCAMRExifHelperMock()
        val fileHelperMock = OSCAMRFileHelperMock()
        val camHelperMock = OSCAMRMediaHelperMock()
        val imgHelperMock = OSCAMRImageHelperMock()

        fileHelperMock.fileExists = true

        val camController = OSCAMRController(
            "someAppId",
            exifHelperMock,
            fileHelperMock,
            camHelperMock,
            imgHelperMock
        )
        fileHelperMock.inputStreamUriFilePath = VIDEO_URI_GALLERY

        Mockito.`when`(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES))
            .thenReturn(mFile)

        camController.captureVideo(mockActivity) {
            // do nothing
        }

        runBlocking {
            camController.processResultFromVideo(mockActivity, mUri,
                fromGallery = false,
                isPersistent = false,
                includeMetadata = false,
                onSuccess = {
                    assertEquals(it.type, 1)
                    assertEquals(it.uri, VIDEO_URI)
                    assertEquals(it.thumbnail, BASE_64)
                    assertEquals(it.metadata, null)
                },
                onError = {
                    fail()
                }
            )
            camController.processResultFromVideo(mockActivity, mUri,
                fromGallery = true,
                isPersistent = false,
                includeMetadata = false,
                onSuccess = {
                    assertEquals(it.type, 1)
                    assertEquals(it.uri, VIDEO_URI_GALLERY)
                    assertEquals(it.thumbnail, BASE_64)
                    assertEquals(it.metadata, null)
                },
                onError = {
                    fail()
                }
            )
        }
        camController.deleteVideoFilesFromCache(mockActivity)
        assertEquals(2, fileHelperMock.deleteCallsCount)
    }

    @Test
    fun givenCaptureMultipleVideosAndProcessResultOKWhenDeleteThenSuccess() {
        val exifHelperMock = OSCAMRExifHelperMock()
        val fileHelperMock = OSCAMRFileHelperMock()
        val camHelperMock = OSCAMRMediaHelperMock()
        val imgHelperMock = OSCAMRImageHelperMock()

        fileHelperMock.fileExists = true

        val camController = OSCAMRController(
            "someAppId",
            exifHelperMock,
            fileHelperMock,
            camHelperMock,
            imgHelperMock
        )
        fileHelperMock.inputStreamUriFilePath = VIDEO_URI_GALLERY

        Mockito.`when`(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES))
            .thenReturn(mFile)

        camController.captureVideo(mockActivity) {
            // do nothing
        }
        runBlocking {
            camController.processResultFromVideo(mockActivity, mUri,
                fromGallery = false,
                isPersistent = false,
                includeMetadata = false,
                onSuccess = {
                    assertEquals(it.type, 1)
                    assertEquals(it.uri, VIDEO_URI)
                    assertEquals(it.thumbnail, BASE_64)
                    assertEquals(it.metadata, null)
                },
                onError = {
                    fail()
                }
            )
            camController.processResultFromVideo(mockActivity, mUri,
                fromGallery = true,
                isPersistent = false,
                includeMetadata = false,
                onSuccess = {
                    assertEquals(it.type, 1)
                    assertEquals(it.uri, VIDEO_URI_GALLERY)
                    assertEquals(it.thumbnail, BASE_64)
                    assertEquals(it.metadata, null)
                },
                onError = {
                    fail()
                }
            )
        }

        camController.captureVideo(mockActivity) {
            // do nothing
        }

        runBlocking {
            camController.processResultFromVideo(mockActivity, mUri,
                fromGallery = false,
                isPersistent = false,
                includeMetadata = false,
                onSuccess = {
                    assertEquals(it.type, 1)
                    assertEquals(it.uri, VIDEO_URI)
                    assertEquals(it.thumbnail, BASE_64)
                    assertEquals(it.metadata, null)
                },
                onError = {
                    fail()
                }
            )
        }
        camController.deleteVideoFilesFromCache(mockActivity)
        assertEquals(2, fileHelperMock.deleteCallsCount)
    }

    @Test
    fun givenDifferentSizeReturnNewSizeWhen480p() {
        val helper = OSCAMRMediaHelper()
        val result = helper.defineNewDimensionSize(2160, 3840)
        assertEquals(480, result.first)
        assertEquals(853, result.second)
        val result2 = helper.defineNewDimensionSize(3840, 2160)
        assertEquals(853, result2.first)
        assertEquals(480, result2.second)
        val result3 = helper.defineNewDimensionSize(3840, 2880)
        assertEquals(640, result3.first)
        assertEquals(480, result3.second)
        val result4 = helper.defineNewDimensionSize(2880, 3840)
        assertEquals(480, result4.first)
        assertEquals(640, result4.second)
    }

    @Test
    fun givenSomeErrorWhenCaptureVideoThenDeleteNotCalled() {

        val exifHelperMock = OSCAMRExifHelperMock()
        val fileHelperMock = OSCAMRFileHelperMock()
        val camHelperMock = OSCAMRMediaHelperMock()
        val imgHelperMock = OSCAMRImageHelperMock()

        val camController = OSCAMRController(
            "someAppId",
            exifHelperMock,
            fileHelperMock,
            camHelperMock,
            imgHelperMock
        )

        Mockito.`when`(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES))
            .thenReturn(mFile)

        camHelperMock.existsActivity = false

        camController.captureVideo(mockActivity) {
            assertEquals(it.code, OSCAMRError.NO_CAMERA_AVAILABLE_ERROR.code)
            assertEquals(it.description, OSCAMRError.NO_CAMERA_AVAILABLE_ERROR.description)
        }

        assertEquals(fileHelperMock.deleteCallsCount, 0)
    }

    @Test
    fun givenSomeErrorWhenProcessResultThenDeleteNotCalled() {

        val exifHelperMock = OSCAMRExifHelperMock()
        val fileHelperMock = OSCAMRFileHelperMock()
        val camHelperMock = OSCAMRMediaHelperMock()
        val imgHelperMock = OSCAMRImageHelperMock()

        val camController = OSCAMRController(
            "someAppId",
            exifHelperMock,
            fileHelperMock,
            camHelperMock,
            imgHelperMock
        )

        Mockito.`when`(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES))
            .thenReturn(mFile)

        Mockito.doReturn(null).`when`(mIntent).data
        val mUri = mIntent.data

        camController.captureVideo(mockActivity) {
            // do nothing
        }
        runBlocking {
            camController.processResultFromVideo(mockActivity, mUri,
                fromGallery = false,
                isPersistent = false,
                includeMetadata = false,
                onSuccess = {
                    fail()
                },
                onError = {
                    assertEquals(it.code, OSCAMRError.CAPTURE_VIDEO_ERROR.code)
                    assertEquals(it.description, OSCAMRError.CAPTURE_VIDEO_ERROR.description)
                }
            )
        }

        assertEquals(fileHelperMock.deleteCallsCount, 0)
    }

}