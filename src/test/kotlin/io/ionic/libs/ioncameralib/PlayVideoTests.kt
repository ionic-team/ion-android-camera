package io.ionic.libs.ioncameralib

import android.app.Activity
import io.ionic.libs.ioncameralib.manager.IONCAMRVideoManager
import io.ionic.libs.ioncameralib.mocks.IONCAMRFileHelperMock
import io.ionic.libs.ioncameralib.model.IONCAMRError
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

        val fileHelperMock = IONCAMRFileHelperMock()

        val IONCAMRVideoManager = IONCAMRVideoManager(
            fileHelperMock,
        )

        fileHelperMock.fileExists = true

        IONCAMRVideoManager.playVideo(mockActivity, VIDEO_URI,
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

        val fileHelperMock = IONCAMRFileHelperMock()

        val IONCAMRVideoManager = IONCAMRVideoManager(
            fileHelperMock,
        )

        fileHelperMock.fileExists = false

        IONCAMRVideoManager.playVideo(mockActivity, VIDEO_URI,
            {
                fail()
            },
            {
                assertEquals(it.code, IONCAMRError.FILE_DOES_NOT_EXIST_ERROR.code)
                assertEquals(it.description, IONCAMRError.FILE_DOES_NOT_EXIST_ERROR.description)
            }
        )
    }

    @Test
    fun givenNullMimeTypeWhenPlayVideoThenError() {

        val fileHelperMock = IONCAMRFileHelperMock()

        val IONCAMRVideoManager = IONCAMRVideoManager(
            fileHelperMock,
        )

        fileHelperMock.fileExists = true
        fileHelperMock.mimeType = null

        IONCAMRVideoManager.playVideo(mockActivity, VIDEO_URI,
            {
                fail()
            },
            {
                assertEquals(it.code, IONCAMRError.MEDIA_PATH_ERROR.code)
                assertEquals(it.description, IONCAMRError.MEDIA_PATH_ERROR.description)
            }
        )
    }

    @Test
    fun givenEmptyMimeTypeWhenPlayVideoThenError() {

        val fileHelperMock = IONCAMRFileHelperMock()

        val IONCAMRVideoManager = IONCAMRVideoManager(
            fileHelperMock,
        )

        fileHelperMock.fileExists = true
        fileHelperMock.mimeType = ""

        IONCAMRVideoManager.playVideo(mockActivity, VIDEO_URI,
            {
                fail()
            },
            {
                assertEquals(it.code, IONCAMRError.MEDIA_PATH_ERROR.code)
                assertEquals(it.description, IONCAMRError.MEDIA_PATH_ERROR.description)
            }
        )
    }


}