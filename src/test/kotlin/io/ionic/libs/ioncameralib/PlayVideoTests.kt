package io.ionic.libs.ioncameralib

import android.app.Activity
import io.ionic.libs.ioncameralib.manager.VideoManager
import io.ionic.libs.ioncameralib.mocks.IONFileHelperMock
import io.ionic.libs.ioncameralib.model.IONError
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Before
import org.junit.Test
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

        val fileHelperMock = IONFileHelperMock()

        val videoManager = VideoManager(
            "authority",
            fileHelperMock,
        )

        fileHelperMock.fileExists = true

        videoManager.playVideo(mockActivity, VIDEO_URI,
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

        val fileHelperMock = IONFileHelperMock()

        val videoManager = VideoManager(
            "authority",
            fileHelperMock,
        )

        fileHelperMock.fileExists = false

        videoManager.playVideo(mockActivity, VIDEO_URI,
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

        val fileHelperMock = IONFileHelperMock()

        val videoManager = VideoManager(
            "authority",
            fileHelperMock,
        )

        fileHelperMock.fileExists = true
        fileHelperMock.mimeType = null

        videoManager.playVideo(mockActivity, VIDEO_URI,
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

        val fileHelperMock = IONFileHelperMock()

        val videoManager = VideoManager(
            "authority",
            fileHelperMock,
        )

        fileHelperMock.fileExists = true
        fileHelperMock.mimeType = ""

        videoManager.playVideo(mockActivity, VIDEO_URI,
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