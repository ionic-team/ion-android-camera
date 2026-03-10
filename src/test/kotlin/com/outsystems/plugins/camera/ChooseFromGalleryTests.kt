package com.outsystems.plugins.camera

import android.app.Activity
import android.content.Intent
import android.net.Uri
import com.outsystems.plugins.camera.controller.OSCAMRController
import com.outsystems.plugins.camera.mocks.OSCAMRExifHelperMock
import com.outsystems.plugins.camera.mocks.OSCAMRFileHelperMock
import com.outsystems.plugins.camera.mocks.OSCAMRImageHelperMock
import com.outsystems.plugins.camera.mocks.OSCAMRMediaHelperMock
import com.outsystems.plugins.camera.model.OSCAMRMediaType
import com.outsystems.plugins.camera.model.OSCAMRError
import kotlinx.coroutines.runBlocking
import org.junit.Assert
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito
import org.mockito.junit.MockitoJUnitRunner

@RunWith(MockitoJUnitRunner::class)
class ChooseFromGalleryTests {

    private val mIntent = Mockito.mock(Intent::class.java)
    private val mockActivity = Mockito.mock(Activity::class.java)
    private val mUri = Mockito.mock(Uri::class.java)

    @Test
    fun givenChoosePictureFromGalleryWhenSingleSelectSuccessThenSuccess() {

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

        imgHelperMock.bitmapToBase64Success = true
        fileHelperMock.fileLocationNotNull = true
        fileHelperMock.mimeType = OSCAMRMediaType.IMAGE.mimeType
        imgHelperMock.intentUris = listOf(
            Uri.parse(SAMPLE_IMAGE_URI)
        )
        fileHelperMock.fileExists = true

        camController.chooseFromGallery(
            mockActivity,
            OSCAMRMediaType.IMAGE,
            false,
            0
        )

        runBlocking {
            camController.onChooseFromGalleryResult(
                mockActivity,
                RESULT_OK,
                mIntent,
                false,
                false,
                OSCAMRMediaType.IMAGE,
                {
                    Assert.assertEquals(1, it.size)
                    Assert.assertEquals(OSCAMRFileHelperMock.FILE_LOCATION, it.first().uri)
                    Assert.assertEquals(
                        OSCAMRImageHelperMock.SAMPLE_BASE64_THUMBNAIL,
                        it.first().thumbnail
                    )
                    Assert.assertEquals(OSCAMRMediaType.IMAGE.type, it.first().type)
                },
                {
                    Assert.fail()
                })
        }
    }

    @Test
    fun givenChoosePictureFromGalleryWhenSingleSelectWithMetadataSuccessThenSuccess() {

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

        imgHelperMock.bitmapToBase64Success = true
        fileHelperMock.fileLocationNotNull = true
        fileHelperMock.mimeType = OSCAMRMediaType.IMAGE.mimeType
        imgHelperMock.intentUris = listOf(
            mUri
        )
        fileHelperMock.fileExists = true

        camController.chooseFromGallery(
            mockActivity,
            OSCAMRMediaType.IMAGE,
            false,
            0
        )

        runBlocking {
            camController.onChooseFromGalleryResult(
                mockActivity,
                RESULT_OK,
                mIntent,
                true,
                false,
                OSCAMRMediaType.IMAGE,
                {
                    Assert.assertEquals(1, it.size)
                    Assert.assertEquals(OSCAMRFileHelperMock.FILE_LOCATION, it.first().uri)
                    Assert.assertEquals(
                        OSCAMRImageHelperMock.SAMPLE_BASE64_THUMBNAIL,
                        it.first().thumbnail
                    )
                    Assert.assertEquals(OSCAMRMediaType.IMAGE.type, it.first().type)
                    Assert.assertEquals(it.first().metadata?.size, METADATA_SIZE)
                    Assert.assertEquals(it.first().metadata?.format, METADATA_FORMAT)
                    Assert.assertEquals(it.first().metadata?.duration, null)
                    Assert.assertEquals(it.first().metadata?.resolution, METADATA_RESOLUTION)
                    Assert.assertEquals(it.first().metadata?.creationDate, METADATA_CREATION_DATE)
                },
                {
                    Assert.fail()
                })
        }
    }

    @Test
    fun givenChoosePictureFromGalleryWhenFileDoesNotExistThenError() {

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

        imgHelperMock.bitmapToBase64Success = true
        fileHelperMock.fileLocationNotNull = true
        fileHelperMock.mimeType = OSCAMRMediaType.IMAGE.mimeType
        imgHelperMock.intentUris = listOf(
            mUri
        )
        fileHelperMock.fileExists = false

        camController.chooseFromGallery(
            mockActivity,
            OSCAMRMediaType.IMAGE,
            false,
            0
        )

        runBlocking {
            camController.onChooseFromGalleryResult(
                mockActivity,
                RESULT_OK,
                mIntent,
                true,
                false,
                OSCAMRMediaType.IMAGE,
                {
                    Assert.fail()
                },
                {
                    Assert.assertEquals(it.code, OSCAMRError.GENERIC_CHOOSE_MULTIMEDIA_ERROR.code)
                    Assert.assertEquals(
                        it.description,
                        OSCAMRError.GENERIC_CHOOSE_MULTIMEDIA_ERROR.description
                    )
                })
        }
    }

    @Test
    fun givenChooseVideoFromGalleryWhenFileDoesNotExistThenSuccess() {

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

        imgHelperMock.bitmapToBase64Success = true
        fileHelperMock.fileLocationNotNull = true
        fileHelperMock.mimeType = OSCAMRMediaType.VIDEO.mimeType
        imgHelperMock.intentUris = listOf(
            mUri,
            mUri
        )
        fileHelperMock.mUri = mUri
        fileHelperMock.fileExists = false

        camController.chooseFromGallery(
            mockActivity,
            OSCAMRMediaType.VIDEO,
            true,
            0
        )

        runBlocking {
            camController.onChooseFromGalleryResult(
                mockActivity,
                RESULT_OK,
                mIntent,
                true,
                false,
                OSCAMRMediaType.VIDEO,
                {
                    Assert.fail()
                },
                {
                    Assert.assertEquals(it.code, OSCAMRError.GENERIC_CHOOSE_MULTIMEDIA_ERROR.code)
                    Assert.assertEquals(
                        it.description,
                        OSCAMRError.GENERIC_CHOOSE_MULTIMEDIA_ERROR.description
                    )
                })

        }
    }

    @Test
    fun givenChooseVideoFromGalleryWhenMultipleSelectWithMetadataSuccessThenSuccess() {

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

        imgHelperMock.bitmapToBase64Success = true
        fileHelperMock.fileLocationNotNull = true
        fileHelperMock.mimeType = OSCAMRMediaType.VIDEO.mimeType
        imgHelperMock.intentUris = listOf(
            mUri,
            mUri
        )
        fileHelperMock.mUri = mUri
        fileHelperMock.fileExists = true

        camController.chooseFromGallery(
            mockActivity,
            OSCAMRMediaType.VIDEO,
            true,
            0
        )

        runBlocking {
            camController.onChooseFromGalleryResult(
                mockActivity,
                RESULT_OK,
                mIntent,
                true,
                false,
                OSCAMRMediaType.VIDEO,
                {
                    Assert.assertEquals(2, it.size)
                    Assert.assertEquals(
                        OSCAMRImageHelperMock.SAMPLE_BASE64_THUMBNAIL,
                        it.first().thumbnail
                    )
                    Assert.assertEquals(OSCAMRMediaType.VIDEO.type, it.first().type)
                    Assert.assertEquals(it.first().metadata?.size, METADATA_SIZE)
                    Assert.assertEquals(it.first().metadata?.format, METADATA_FORMAT)
                    Assert.assertEquals(it.first().metadata?.duration, METADATA_DURATION)
                    Assert.assertEquals(it.first().metadata?.resolution, METADATA_RESOLUTION)
                    Assert.assertEquals(it.first().metadata?.creationDate, METADATA_CREATION_DATE)
                },
                {
                    Assert.fail()
                })

        }
    }

    @Test
    fun givenChooseVideoFromGalleryWhenMultipleSelectSuccessThenSuccess() {

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

        imgHelperMock.bitmapToBase64Success = true
        fileHelperMock.fileLocationNotNull = true
        fileHelperMock.mimeType = OSCAMRMediaType.VIDEO.mimeType
        imgHelperMock.intentUris = listOf(
            Uri.parse(SAMPLE_VIDEO_URI),
            Uri.parse(SAMPLE_VIDEO_URI)
        )
        fileHelperMock.mUri = mUri
        fileHelperMock.fileExists = true

        camController.chooseFromGallery(
            mockActivity,
            OSCAMRMediaType.VIDEO,
            true,
            0
        )

        runBlocking {
            camController.onChooseFromGalleryResult(
                mockActivity,
                RESULT_OK,
                mIntent,
                false,
                false,
                OSCAMRMediaType.VIDEO,
                {
                    Assert.assertEquals(2, it.size)
                    Assert.assertEquals(
                        OSCAMRImageHelperMock.SAMPLE_BASE64_THUMBNAIL,
                        it.first().thumbnail
                    )
                    Assert.assertEquals(OSCAMRMediaType.VIDEO.type, it.first().type)
                },
                {
                    Assert.fail()
                })

        }
    }

    @Test
    fun givenChoosePictureFromGalleryWhenCancelledThenCancelledError() {

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

        camController.chooseFromGallery(
            mockActivity,
            OSCAMRMediaType.IMAGE,
            false,
            0
        )

        runBlocking {
            camController.onChooseFromGalleryResult(
                mockActivity,
                RESULT_CANCELLED,
                mIntent,
                false,
                false,
                OSCAMRMediaType.IMAGE,
                {
                    Assert.fail()
                },
                {
                    Assert.assertEquals(it.code, OSCAMRError.CHOOSE_MULTIMEDIA_CANCELLED_ERROR.code)
                    Assert.assertEquals(
                        it.description,
                        OSCAMRError.CHOOSE_MULTIMEDIA_CANCELLED_ERROR.description
                    )
                })
        }
    }

    @Test
    fun givenChoosePictureFromGalleryWhenSomeErrorThenGenericError() {

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

        camController.chooseFromGallery(
            mockActivity,
            OSCAMRMediaType.IMAGE,
            false,
            0
        )
        runBlocking {
            camController.onChooseFromGalleryResult(
                mockActivity,
                RESULT_NOK,
                mIntent,
                false,
                false,
                OSCAMRMediaType.IMAGE,
                {
                    Assert.fail()
                },
                {
                    Assert.assertEquals(it.code, OSCAMRError.GENERIC_CHOOSE_MULTIMEDIA_ERROR.code)
                    Assert.assertEquals(
                        it.description,
                        OSCAMRError.GENERIC_CHOOSE_MULTIMEDIA_ERROR.description
                    )
                })
        }
    }

    @Test
    fun givenChoosePictureFromGalleryCreateBase64ThumbnailErrorThenGenericError() {

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

        imgHelperMock.bitmapToBase64Success = false
        fileHelperMock.fileLocationNotNull = true
        fileHelperMock.mimeType = OSCAMRMediaType.IMAGE.mimeType
        imgHelperMock.intentUris = listOf(
            Uri.parse(SAMPLE_IMAGE_URI)
        )

        camController.chooseFromGallery(
            mockActivity,
            OSCAMRMediaType.IMAGE,
            false,
            0
        )

        runBlocking {
            camController.onChooseFromGalleryResult(
                mockActivity,
                RESULT_OK,
                mIntent,
                false,
                false,
                OSCAMRMediaType.IMAGE,
                {
                    Assert.fail()
                },
                {
                    Assert.assertEquals(it.code, OSCAMRError.GENERIC_CHOOSE_MULTIMEDIA_ERROR.code)
                    Assert.assertEquals(
                        it.description,
                        OSCAMRError.GENERIC_CHOOSE_MULTIMEDIA_ERROR.description
                    )
                })
        }
    }

    @Test
    fun givenChooseImageFromGalleryWhenSingleSelectAndEditSuccessThenSuccess() {

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

        imgHelperMock.bitmapToBase64Success = true
        fileHelperMock.fileLocationNotNull = true
        fileHelperMock.mimeType = OSCAMRMediaType.IMAGE.mimeType
        imgHelperMock.intentUris = listOf(
            Uri.parse(SAMPLE_IMAGE_URI)
        )
        fileHelperMock.mUri = mUri
        fileHelperMock.fileExists = true
        camController.chooseFromGallery(
            mockActivity,
            OSCAMRMediaType.IMAGE,
            false,
            0
        )

        runBlocking {
            camController.chooseFromGallery(
                mockActivity,
                OSCAMRMediaType.IMAGE,
                false,
                0)

            camController.onChooseFromGalleryResult(
                mockActivity,
                RESULT_OK,
                mIntent,
                true,
                true,
                OSCAMRMediaType.IMAGE,
                {
                    Assert.assertEquals(1, it.size)
                    Assert.assertEquals(
                        OSCAMRImageHelperMock.SAMPLE_BASE64_THUMBNAIL,
                        it.first().thumbnail
                    )
                    Assert.assertEquals(OSCAMRMediaType.IMAGE.type, it.first().type)
                },
                {
                    Assert.fail()
                })

            camController.onChooseFromGalleryEditResult(
                mockActivity,
                RESULT_OK,
                mIntent,
                true,
                {
                    Assert.assertEquals(1, it.size)
                    Assert.assertEquals(
                        OSCAMRImageHelperMock.SAMPLE_BASE64_THUMBNAIL,
                        it.first().thumbnail
                    )
                    Assert.assertEquals(OSCAMRMediaType.IMAGE.type, it.first().type)
                },
                {
                    Assert.fail()
                })
        }
    }

    @Test
    fun givenChooseImageFromGalleryWhenSingleSelectAndEditCancelThenCancelError() {

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

        imgHelperMock.bitmapToBase64Success = true
        fileHelperMock.fileLocationNotNull = true
        fileHelperMock.mimeType = OSCAMRMediaType.IMAGE.mimeType
        imgHelperMock.intentUris = listOf(
            Uri.parse(SAMPLE_IMAGE_URI)
        )
        fileHelperMock.mUri = mUri
        fileHelperMock.fileExists = true
        camController.chooseFromGallery(
            mockActivity,
            OSCAMRMediaType.IMAGE,
            false,
            0
        )

        runBlocking {
            camController.onChooseFromGalleryEditResult(
                mockActivity,
                RESULT_CANCELLED,
                mIntent,
                true,
                {
                    Assert.fail()
                },
                {
                    Assert.assertEquals(it.code, OSCAMRError.EDIT_CANCELLED_ERROR.code)
                    Assert.assertEquals(it.description, OSCAMRError.EDIT_CANCELLED_ERROR.description)
                })
        }
    }

    @Test
    fun givenChooseImageFromGalleryWhenSingleSelectAndEditErrorThenEditError() {

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

        imgHelperMock.bitmapToBase64Success = true
        fileHelperMock.fileLocationNotNull = true
        fileHelperMock.mimeType = OSCAMRMediaType.IMAGE.mimeType
        imgHelperMock.intentUris = listOf(
            Uri.parse(SAMPLE_IMAGE_URI)
        )
        fileHelperMock.mUri = mUri
        fileHelperMock.fileExists = true
        camController.chooseFromGallery(
            mockActivity,
            OSCAMRMediaType.IMAGE,
            false,
            0
        )

        runBlocking {
            camController.onChooseFromGalleryEditResult(
                mockActivity,
                RESULT_NOK,
                mIntent,
                true,
                {
                    Assert.fail()
                },
                {
                    Assert.assertEquals(it.code, OSCAMRError.EDIT_IMAGE_ERROR.code)
                    Assert.assertEquals(it.description, OSCAMRError.EDIT_IMAGE_ERROR.description)
                })
        }
    }

    @Test
    fun givenChooseImageFromGalleryWhenNullIntentThenEditError() {

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

        runBlocking {
            camController.onChooseFromGalleryResult(
                mockActivity,
                RESULT_OK,
                null,
                galleryMediaType = OSCAMRMediaType.IMAGE,
                onSuccess = {
                    Assert.fail()
                },
                onError = {
                    Assert.assertEquals(it.code, OSCAMRError.CHOOSE_MULTIMEDIA_CANCELLED_ERROR.code)
                    Assert.assertEquals(it.description, OSCAMRError.CHOOSE_MULTIMEDIA_CANCELLED_ERROR.description)
                }
            )

            camController.onChooseFromGalleryEditResult(
                mockActivity,
                RESULT_OK,
                null,
                onSuccess = {
                    Assert.fail()
                },
                onError = {
                    Assert.assertEquals(it.code, OSCAMRError.EDIT_IMAGE_ERROR.code)
                    Assert.assertEquals(it.description, OSCAMRError.EDIT_IMAGE_ERROR.description)
                })
        }
    }

    @Test
    fun givenChooseImageFromGalleryWhenRemoteFileThenSuccess() {

        val exifHelperMock = OSCAMRExifHelperMock()
        val fileHelperMock = OSCAMRFileHelperMock()
        val camHelperMock = OSCAMRMediaHelperMock()
        val imgHelperMock = OSCAMRImageHelperMock()

        fileHelperMock.fileLocationNotNull = false
        imgHelperMock.intentUris = listOf(
            mUri
        )
        fileHelperMock.mUri = mUri
        fileHelperMock.fileExists = true

        val camController = OSCAMRController(
            "someAppId",
            exifHelperMock,
            fileHelperMock,
            camHelperMock,
            imgHelperMock
        )

        runBlocking {
            camController.onChooseFromGalleryResult(
                mockActivity,
                RESULT_OK,
                mIntent,
                false,
                false,
                OSCAMRMediaType.IMAGE,
                {
                    Assert.assertEquals(1, it.size)
                },
                {
                    Assert.fail()
                })
        }
    }

    companion object {
        private const val RESULT_OK = -1
        private const val RESULT_NOK = 2
        private const val RESULT_CANCELLED = 0
        private const val SAMPLE_IMAGE_URI = "file://image.bmp"
        private const val SAMPLE_VIDEO_URI = "file://video.mp4"
        private const val METADATA_SIZE = 23456L
        private const val METADATA_DURATION = 2
        private const val METADATA_FORMAT = "mp4"
        private const val METADATA_RESOLUTION = "1920x1080"
        private const val METADATA_CREATION_DATE = "2023-03-30T09:01:27Z"
    }
}