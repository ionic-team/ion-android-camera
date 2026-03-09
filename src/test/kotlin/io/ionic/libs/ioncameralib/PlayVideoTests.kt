package io.ionic.libs.ioncameralib

import android.app.Activity
import io.ionic.libs.ioncameralib.manager.OSCAMRController
import io.ionic.libs.ioncameralib.mocks.IONMediaHelperMock
import io.ionic.libs.ioncameralib.mocks.IONExifHelperMock
import io.ionic.libs.ioncameralib.mocks.IONFileHelperMock
import io.ionic.libs.ioncameralib.mocks.IONImageHelperMock
import io.ionic.libs.ioncameralib.model.IONError
import org.junit.*
import org.junit.Assert.*
import org.junit.runner.RunWith
import org.mockito.Mockito
import org.mockito.junit.MockitoJUnitRunner

/**
 * Unit tests for the Play Video feature
 *
 */
@RunWith(MockitoJUnitRunner::class)
class PlayVideoTests {

    private lateinit var mockActivity: Activity

    companion object {
        private const val VIDEO_URI = "directory/myVideo.mp4"
    }

    @Before
    fun before() {
        mockActivity = Mockito.mock(Activity::class.java)
    }

    @Test
    fun givenFileExistsWhenPlayVideoThenSuccess() {

        val exifHelperMock = IONExifHelperMock()
        val fileHelperMock = IONFileHelperMock()
        val camHelperMock = IONMediaHelperMock()
        val imgHelperMock = IONImageHelperMock()

        val camController = OSCAMRController(
            "someAppId",
            exifHelperMock,
            fileHelperMock,
            camHelperMock,
            imgHelperMock
        )

        fileHelperMock.fileExists = true

        camController.playVideo(mockActivity, VIDEO_URI,
            {
                assertTrue(true)
            },
            {
                fail()
            }
        )
    }

    @Test
    fun givenFileDoesNotExistWhenPlayVideoThenError() {

        val exifHelperMock = IONExifHelperMock()
        val fileHelperMock = IONFileHelperMock()
        val camHelperMock = IONMediaHelperMock()
        val imgHelperMock = IONImageHelperMock()

        val camController = OSCAMRController(
            "someAppId",
            exifHelperMock,
            fileHelperMock,
            camHelperMock,
            imgHelperMock
        )

        fileHelperMock.fileExists = false

        camController.playVideo(mockActivity, VIDEO_URI,
            {
                fail()
            },
            {
                assertEquals(it.code, IONError.FILE_DOES_NOT_EXIST_ERROR.code)
                assertEquals(it.description, IONError.FILE_DOES_NOT_EXIST_ERROR.description)
            }
        )
    }

    @Test
    fun givenNullMimeTypeWhenPlayVideoThenError() {

        val exifHelperMock = IONExifHelperMock()
        val fileHelperMock = IONFileHelperMock()
        val camHelperMock = IONMediaHelperMock()
        val imgHelperMock = IONImageHelperMock()

        val camController = OSCAMRController(
            "someAppId",
            exifHelperMock,
            fileHelperMock,
            camHelperMock,
            imgHelperMock
        )

        fileHelperMock.fileExists = true
        fileHelperMock.mimeType = null

        camController.playVideo(mockActivity, VIDEO_URI,
            {
                fail()
            },
            {
                assertEquals(it.code, IONError.MEDIA_PATH_ERROR.code)
                assertEquals(it.description, IONError.MEDIA_PATH_ERROR.description)
            }
        )
    }

    @Test
    fun givenEmptyMimeTypeWhenPlayVideoThenError() {

        val exifHelperMock = IONExifHelperMock()
        val fileHelperMock = IONFileHelperMock()
        val camHelperMock = IONMediaHelperMock()
        val imgHelperMock = IONImageHelperMock()

        val camController = OSCAMRController(
            "someAppId",
            exifHelperMock,
            fileHelperMock,
            camHelperMock,
            imgHelperMock
        )

        fileHelperMock.fileExists = true
        fileHelperMock.mimeType = ""

        camController.playVideo(mockActivity, VIDEO_URI,
            {
                fail()
            },
            {
                assertEquals(it.code, IONError.MEDIA_PATH_ERROR.code)
                assertEquals(it.description, IONError.MEDIA_PATH_ERROR.description)
            }
        )
    }


}