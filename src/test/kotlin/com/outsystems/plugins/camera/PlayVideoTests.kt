package com.outsystems.plugins.camera

import android.app.Activity
import com.outsystems.plugins.camera.controller.OSCAMRController
import com.outsystems.plugins.camera.mocks.OSCAMRMediaHelperMock
import com.outsystems.plugins.camera.mocks.OSCAMRExifHelperMock
import com.outsystems.plugins.camera.mocks.OSCAMRFileHelperMock
import com.outsystems.plugins.camera.mocks.OSCAMRImageHelperMock
import com.outsystems.plugins.camera.model.OSCAMRError
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

        fileHelperMock.fileExists = false

        camController.playVideo(mockActivity, VIDEO_URI,
            {
                fail()
            },
            {
                assertEquals(it.code, OSCAMRError.FILE_DOES_NOT_EXIST_ERROR.code)
                assertEquals(it.description, OSCAMRError.FILE_DOES_NOT_EXIST_ERROR.description)
            }
        )
    }

    @Test
    fun givenNullMimeTypeWhenPlayVideoThenError() {

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

        fileHelperMock.fileExists = true
        fileHelperMock.mimeType = null

        camController.playVideo(mockActivity, VIDEO_URI,
            {
                fail()
            },
            {
                assertEquals(it.code, OSCAMRError.MEDIA_PATH_ERROR.code)
                assertEquals(it.description, OSCAMRError.MEDIA_PATH_ERROR.description)
            }
        )
    }

    @Test
    fun givenEmptyMimeTypeWhenPlayVideoThenError() {

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

        fileHelperMock.fileExists = true
        fileHelperMock.mimeType = ""

        camController.playVideo(mockActivity, VIDEO_URI,
            {
                fail()
            },
            {
                assertEquals(it.code, OSCAMRError.MEDIA_PATH_ERROR.code)
                assertEquals(it.description, OSCAMRError.MEDIA_PATH_ERROR.description)
            }
        )
    }


}