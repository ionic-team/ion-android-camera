package io.ionic.libs.ioncameralib

import android.app.Activity
import android.content.Intent
import android.net.Uri
import androidx.activity.result.ActivityResultLauncher
import io.ionic.libs.ioncameralib.manager.GalleryManager
import io.ionic.libs.ioncameralib.mocks.IONExifHelperMock
import io.ionic.libs.ioncameralib.mocks.IONFileHelperMock
import io.ionic.libs.ioncameralib.mocks.IONImageHelperMock
import io.ionic.libs.ioncameralib.mocks.IONMediaHelperMock
import io.ionic.libs.ioncameralib.model.IONError
import io.ionic.libs.ioncameralib.model.IONMediaType
import kotlinx.coroutines.runBlocking
import org.junit.Assert
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito
import org.mockito.junit.MockitoJUnitRunner
import org.mockito.kotlin.mock

@RunWith(MockitoJUnitRunner::class)
class ChooseFromGalleryTests {

    private val mIntent = Mockito.mock(Intent::class.java)
    private val mockActivity = Mockito.mock(Activity::class.java)
    private val mUri = Mockito.mock(Uri::class.java)
    private val mActivityLauncher = mock<ActivityResultLauncher<Intent>>()

    @Test
    fun givenChoosePictureFromGalleryWhenSingleSelectSuccessThenSuccess() {

        val exifHelperMock = IONExifHelperMock()
        val fileHelperMock = IONFileHelperMock()
        val camHelperMock = IONMediaHelperMock()
        val imgHelperMock = IONImageHelperMock()

        val galleryManager = GalleryManager(
            exif = exifHelperMock,
            fileHelper = fileHelperMock,
            mediaHelper = camHelperMock,
            imageHelper = imgHelperMock
        )

        imgHelperMock.bitmapToBase64Success = true
        fileHelperMock.fileLocationNotNull = true
        fileHelperMock.mimeType = IONMediaType.PICTURE.mimeType
        imgHelperMock.intentUris = listOf(
            mUri
        )
        fileHelperMock.mUri = mUri
        fileHelperMock.fileExists = true

        galleryManager.chooseFromGallery(
            mockActivity,
            IONMediaType.PICTURE,
            false,
            mActivityLauncher
        )

        runBlocking {
            galleryManager.onChooseFromGalleryResult(
                mockActivity,
                RESULT_OK,
                mIntent,
                false,
                {
                    Assert.assertEquals(1, it.size)
                    Assert.assertEquals(IONFileHelperMock.FILE_LOCATION, it.first().uri)
                    Assert.assertEquals(
                        IONImageHelperMock.SAMPLE_BASE64_THUMBNAIL,
                        it.first().thumbnail
                    )
                    Assert.assertEquals(IONMediaType.PICTURE.type, it.first().type)
                },
                {
                    Assert.fail()
                })
        }
    }

    @Test
    fun givenChoosePictureFromGalleryWhenSingleSelectWithMetadataSuccessThenSuccess() {

        val exifHelperMock = IONExifHelperMock()
        val fileHelperMock = IONFileHelperMock()
        val camHelperMock = IONMediaHelperMock()
        val imgHelperMock = IONImageHelperMock()

        fileHelperMock.fileExists = true
        fileHelperMock.fileSize = METADATA_SIZE
        fileHelperMock.fileExtension = METADATA_FORMAT
        fileHelperMock.creationDateTime = METADATA_CREATION_DATE
        camHelperMock.resolution = Pair(1920, 1080)
        camHelperMock.duration = 2222

        val galleryManager = GalleryManager(
            exif = exifHelperMock,
            fileHelper = fileHelperMock,
            mediaHelper = camHelperMock,
            imageHelper = imgHelperMock
        )

        imgHelperMock.bitmapToBase64Success = true
        fileHelperMock.fileLocationNotNull = true
        fileHelperMock.mimeType = IONMediaType.PICTURE.mimeType
        imgHelperMock.intentUris = listOf(
            mUri
        )
        fileHelperMock.mUri = mUri
        fileHelperMock.fileExists = true

        galleryManager.chooseFromGallery(
            mockActivity,
            IONMediaType.PICTURE,
            false,
            mActivityLauncher
        )

        runBlocking {
            galleryManager.onChooseFromGalleryResult(
                mockActivity,
                RESULT_OK,
                mIntent,
                true,
                {
                    Assert.assertEquals(1, it.size)
                    Assert.assertEquals(IONFileHelperMock.FILE_LOCATION, it.first().uri)
                    Assert.assertEquals(
                        IONImageHelperMock.SAMPLE_BASE64_THUMBNAIL,
                        it.first().thumbnail
                    )
                    Assert.assertEquals(IONMediaType.PICTURE.type, it.first().type)
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

        val exifHelperMock = IONExifHelperMock()
        val fileHelperMock = IONFileHelperMock()
        val camHelperMock = IONMediaHelperMock()
        val imgHelperMock = IONImageHelperMock()

        fileHelperMock.fileExists = true
        fileHelperMock.fileSize = METADATA_SIZE
        fileHelperMock.fileExtension = METADATA_FORMAT
        fileHelperMock.creationDateTime = METADATA_CREATION_DATE
        camHelperMock.resolution = Pair(1920, 1080)
        camHelperMock.duration = 2222

        val galleryManager = GalleryManager(
            exif = exifHelperMock,
            fileHelper = fileHelperMock,
            mediaHelper = camHelperMock,
            imageHelper = imgHelperMock
        )

        imgHelperMock.bitmapToBase64Success = true
        fileHelperMock.fileLocationNotNull = true
        fileHelperMock.mimeType = IONMediaType.PICTURE.mimeType
        imgHelperMock.intentUris = listOf(
            mUri
        )
        fileHelperMock.mUri = mUri
        fileHelperMock.fileExists = false

        galleryManager.chooseFromGallery(
            mockActivity,
            IONMediaType.PICTURE,
            false,
            mActivityLauncher
        )

        runBlocking {
            galleryManager.onChooseFromGalleryResult(
                mockActivity,
                RESULT_OK,
                mIntent,
                true,
                {
                    Assert.fail()
                },
                {
                    Assert.assertEquals(it.code, IONError.GENERIC_CHOOSE_MULTIMEDIA_ERROR.code)
                    Assert.assertEquals(
                        it.description,
                        IONError.GENERIC_CHOOSE_MULTIMEDIA_ERROR.description
                    )
                })
        }
    }

    @Test
    fun givenChooseVideoFromGalleryWhenFileDoesNotExistThenError() {

        val exifHelperMock = IONExifHelperMock()
        val fileHelperMock = IONFileHelperMock()
        val camHelperMock = IONMediaHelperMock()
        val imgHelperMock = IONImageHelperMock()

        fileHelperMock.fileExists = true
        fileHelperMock.fileSize = METADATA_SIZE
        fileHelperMock.fileExtension = METADATA_FORMAT
        fileHelperMock.creationDateTime = METADATA_CREATION_DATE
        camHelperMock.resolution = Pair(1920, 1080)
        camHelperMock.duration = 2222

        val galleryManager = GalleryManager(
            exif = exifHelperMock,
            fileHelper = fileHelperMock,
            mediaHelper = camHelperMock,
            imageHelper = imgHelperMock
        )

        imgHelperMock.bitmapToBase64Success = true
        fileHelperMock.fileLocationNotNull = true
        fileHelperMock.mimeType = IONMediaType.VIDEO.mimeType
        imgHelperMock.intentUris = listOf(
            mUri,
            mUri
        )
        fileHelperMock.mUri = mUri
        fileHelperMock.fileExists = false

        galleryManager.chooseFromGallery(
            mockActivity,
            IONMediaType.VIDEO,
            true,
            mActivityLauncher
        )

        runBlocking {
            galleryManager.onChooseFromGalleryResult(
                mockActivity,
                RESULT_OK,
                mIntent,
                true,
                {
                    Assert.fail()
                },
                {
                    Assert.assertEquals(it.code, IONError.GENERIC_CHOOSE_MULTIMEDIA_ERROR.code)
                    Assert.assertEquals(
                        it.description,
                        IONError.GENERIC_CHOOSE_MULTIMEDIA_ERROR.description
                    )
                })

        }
    }

    @Test
    fun givenChooseVideoFromGalleryWhenMultipleSelectWithMetadataSuccessThenSuccess() {

        val exifHelperMock = IONExifHelperMock()
        val fileHelperMock = IONFileHelperMock()
        val camHelperMock = IONMediaHelperMock()
        val imgHelperMock = IONImageHelperMock()

        fileHelperMock.fileExists = true
        fileHelperMock.fileSize = METADATA_SIZE
        fileHelperMock.fileExtension = METADATA_FORMAT
        fileHelperMock.creationDateTime = METADATA_CREATION_DATE
        camHelperMock.resolution = Pair(1920, 1080)
        camHelperMock.duration = 2222

        val galleryManager = GalleryManager(
            exif = exifHelperMock,
            fileHelper = fileHelperMock,
            mediaHelper = camHelperMock,
            imageHelper = imgHelperMock
        )

        imgHelperMock.bitmapToBase64Success = true
        fileHelperMock.fileLocationNotNull = true
        fileHelperMock.mimeType = IONMediaType.VIDEO.mimeType
        imgHelperMock.intentUris = listOf(
            mUri,
            mUri
        )
        fileHelperMock.mUri = mUri
        fileHelperMock.fileExists = true

        galleryManager.chooseFromGallery(
            mockActivity,
            IONMediaType.VIDEO,
            true,
            mActivityLauncher
        )

        runBlocking {
            galleryManager.onChooseFromGalleryResult(
                mockActivity,
                RESULT_OK,
                mIntent,
                true,
                {
                    Assert.assertEquals(2, it.size)
                    Assert.assertEquals(
                        IONImageHelperMock.SAMPLE_BASE64_THUMBNAIL,
                        it.first().thumbnail
                    )
                    Assert.assertEquals(IONMediaType.VIDEO.type, it.first().type)
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

        val exifHelperMock = IONExifHelperMock()
        val fileHelperMock = IONFileHelperMock()
        val camHelperMock = IONMediaHelperMock()
        val imgHelperMock = IONImageHelperMock()

        val galleryManager = GalleryManager(
            exif = exifHelperMock,
            fileHelper = fileHelperMock,
            mediaHelper = camHelperMock,
            imageHelper = imgHelperMock
        )

        imgHelperMock.bitmapToBase64Success = true
        fileHelperMock.fileLocationNotNull = true
        fileHelperMock.mimeType = IONMediaType.VIDEO.mimeType
        imgHelperMock.intentUris = listOf(
            mUri,
            mUri
        )
        fileHelperMock.mUri = mUri
        fileHelperMock.fileExists = true

        galleryManager.chooseFromGallery(
            mockActivity,
            IONMediaType.VIDEO,
            true,
            mActivityLauncher
        )

        runBlocking {
            galleryManager.onChooseFromGalleryResult(
                mockActivity,
                RESULT_OK,
                mIntent,
                false,
                {
                    Assert.assertEquals(2, it.size)
                    Assert.assertEquals(
                        IONImageHelperMock.SAMPLE_BASE64_THUMBNAIL,
                        it.first().thumbnail
                    )
                    Assert.assertEquals(IONMediaType.VIDEO.type, it.first().type)
                },
                {
                    Assert.fail()
                })

        }
    }

    @Test
    fun givenChoosePictureFromGalleryWhenCancelledThenCancelledError() {

        val exifHelperMock = IONExifHelperMock()
        val fileHelperMock = IONFileHelperMock()
        val camHelperMock = IONMediaHelperMock()
        val imgHelperMock = IONImageHelperMock()

        val galleryManager = GalleryManager(
            exif = exifHelperMock,
            fileHelper = fileHelperMock,
            mediaHelper = camHelperMock,
            imageHelper = imgHelperMock
        )

        galleryManager.chooseFromGallery(
            mockActivity,
            IONMediaType.PICTURE,
            false,
            mActivityLauncher
        )

        runBlocking {
            galleryManager.onChooseFromGalleryResult(
                mockActivity,
                RESULT_CANCELLED,
                mIntent,
                false,
                {
                    Assert.fail()
                },
                {
                    Assert.assertEquals(it.code, IONError.CHOOSE_MULTIMEDIA_CANCELLED_ERROR.code)
                    Assert.assertEquals(
                        it.description,
                        IONError.CHOOSE_MULTIMEDIA_CANCELLED_ERROR.description
                    )
                })
        }
    }

    @Test
    fun givenChoosePictureFromGalleryWhenSomeErrorThenGenericError() {

        val exifHelperMock = IONExifHelperMock()
        val fileHelperMock = IONFileHelperMock()
        val camHelperMock = IONMediaHelperMock()
        val imgHelperMock = IONImageHelperMock()

        val galleryManager = GalleryManager(
            exif = exifHelperMock,
            fileHelper = fileHelperMock,
            mediaHelper = camHelperMock,
            imageHelper = imgHelperMock
        )

        galleryManager.chooseFromGallery(
            mockActivity,
            IONMediaType.PICTURE,
            false,
            mActivityLauncher
        )
        runBlocking {
            galleryManager.onChooseFromGalleryResult(
                mockActivity,
                RESULT_NOK,
                mIntent,
                false,
                {
                    Assert.fail()
                },
                {
                    Assert.assertEquals(it.code, IONError.GENERIC_CHOOSE_MULTIMEDIA_ERROR.code)
                    Assert.assertEquals(
                        it.description,
                        IONError.GENERIC_CHOOSE_MULTIMEDIA_ERROR.description
                    )
                })
        }
    }

    @Test
    fun givenChoosePictureFromGalleryCreateBase64ThumbnailErrorThenGenericError() {

        val exifHelperMock = IONExifHelperMock()
        val fileHelperMock = IONFileHelperMock()
        val camHelperMock = IONMediaHelperMock()
        val imgHelperMock = IONImageHelperMock()

        val galleryManager = GalleryManager(
            exif = exifHelperMock,
            fileHelper = fileHelperMock,
            mediaHelper = camHelperMock,
            imageHelper = imgHelperMock
        )

        imgHelperMock.bitmapToBase64Success = false
        fileHelperMock.fileLocationNotNull = true
        fileHelperMock.mimeType = IONMediaType.PICTURE.mimeType
        imgHelperMock.intentUris = listOf(
            mUri
        )
        fileHelperMock.mUri = mUri
        fileHelperMock.fileExists = true

        galleryManager.chooseFromGallery(
            mockActivity,
            IONMediaType.PICTURE,
            false,
            mActivityLauncher
        )

        runBlocking {
            galleryManager.onChooseFromGalleryResult(
                mockActivity,
                RESULT_OK,
                mIntent,
                false,
                {
                    Assert.fail()
                },
                {
                    Assert.assertEquals(it.code, IONError.GENERIC_CHOOSE_MULTIMEDIA_ERROR.code)
                    Assert.assertEquals(
                        it.description,
                        IONError.GENERIC_CHOOSE_MULTIMEDIA_ERROR.description
                    )
                })
        }
    }

    @Test
    fun givenChooseImageFromGalleryWhenSingleSelectAndEditSuccessThenSuccess() {

        val exifHelperMock = IONExifHelperMock()
        val fileHelperMock = IONFileHelperMock()
        val camHelperMock = IONMediaHelperMock()
        val imgHelperMock = IONImageHelperMock()

        val galleryManager = GalleryManager(
            exif = exifHelperMock,
            fileHelper = fileHelperMock,
            mediaHelper = camHelperMock,
            imageHelper = imgHelperMock
        )

        imgHelperMock.bitmapToBase64Success = true
        fileHelperMock.fileLocationNotNull = true
        fileHelperMock.mimeType = IONMediaType.PICTURE.mimeType
        imgHelperMock.intentUris = listOf(
            mUri
        )
        fileHelperMock.mUri = mUri
        fileHelperMock.fileExists = true
        galleryManager.chooseFromGallery(
            mockActivity,
            IONMediaType.PICTURE,
            false,
            mActivityLauncher
        )

        runBlocking {
            // Test onChooseFromGalleryResult
            galleryManager.onChooseFromGalleryResult(
                mockActivity,
                RESULT_OK,
                mIntent,
                true,
                {
                    Assert.assertEquals(1, it.size)
                    Assert.assertEquals(
                        IONImageHelperMock.SAMPLE_BASE64_THUMBNAIL,
                        it.first().thumbnail
                    )
                    Assert.assertEquals(IONMediaType.PICTURE.type, it.first().type)
                },
                {
                    Assert.fail()
                })

            // Test onChooseFromGalleryEditResult
            galleryManager.onChooseFromGalleryEditResult(
                mockActivity,
                RESULT_OK,
                mIntent,
                true,
                {
                    Assert.assertEquals(1, it.size)
                    Assert.assertEquals(
                        IONImageHelperMock.SAMPLE_BASE64_THUMBNAIL,
                        it.first().thumbnail
                    )
                    Assert.assertEquals(IONMediaType.PICTURE.type, it.first().type)
                },
                {
                    Assert.fail()
                })
        }
    }

    @Test
    fun givenChooseImageFromGalleryWhenSingleSelectAndEditCancelThenCancelError() {

        val exifHelperMock = IONExifHelperMock()
        val fileHelperMock = IONFileHelperMock()
        val camHelperMock = IONMediaHelperMock()
        val imgHelperMock = IONImageHelperMock()

        val galleryManager = GalleryManager(
            exif = exifHelperMock,
            fileHelper = fileHelperMock,
            mediaHelper = camHelperMock,
            imageHelper = imgHelperMock
        )

        imgHelperMock.bitmapToBase64Success = true
        fileHelperMock.fileLocationNotNull = true
        fileHelperMock.mimeType = IONMediaType.PICTURE.mimeType
        imgHelperMock.intentUris = listOf(
            mUri
        )
        fileHelperMock.mUri = mUri
        fileHelperMock.fileExists = true
        galleryManager.chooseFromGallery(
            mockActivity,
            IONMediaType.PICTURE,
            false,
            mActivityLauncher
        )

        runBlocking {
            galleryManager.onChooseFromGalleryEditResult(
                mockActivity,
                RESULT_CANCELLED,
                mIntent,
                true,
                {
                    Assert.fail()
                },
                {
                    Assert.assertEquals(it.code, IONError.EDIT_CANCELLED_ERROR.code)
                    Assert.assertEquals(it.description, IONError.EDIT_CANCELLED_ERROR.description)
                })
        }
    }

    @Test
    fun givenChooseImageFromGalleryWhenSingleSelectAndEditErrorThenEditError() {

        val exifHelperMock = IONExifHelperMock()
        val fileHelperMock = IONFileHelperMock()
        val camHelperMock = IONMediaHelperMock()
        val imgHelperMock = IONImageHelperMock()

        val galleryManager = GalleryManager(
            exif = exifHelperMock,
            fileHelper = fileHelperMock,
            mediaHelper = camHelperMock,
            imageHelper = imgHelperMock
        )

        imgHelperMock.bitmapToBase64Success = true
        fileHelperMock.fileLocationNotNull = true
        fileHelperMock.mimeType = IONMediaType.PICTURE.mimeType
        imgHelperMock.intentUris = listOf(
            mUri
        )
        fileHelperMock.mUri = mUri
        fileHelperMock.fileExists = true
        galleryManager.chooseFromGallery(
            mockActivity,
            IONMediaType.PICTURE,
            false,
            mActivityLauncher
        )

        runBlocking {
            galleryManager.onChooseFromGalleryEditResult(
                mockActivity,
                RESULT_NOK,
                mIntent,
                true,
                {
                    Assert.fail()
                },
                {
                    Assert.assertEquals(it.code, IONError.EDIT_IMAGE_ERROR.code)
                    Assert.assertEquals(it.description, IONError.EDIT_IMAGE_ERROR.description)
                })
        }
    }

    @Test
    fun givenChooseImageFromGalleryWhenNullIntentThenEditError() {

        val exifHelperMock = IONExifHelperMock()
        val fileHelperMock = IONFileHelperMock()
        val camHelperMock = IONMediaHelperMock()
        val imgHelperMock = IONImageHelperMock()

        val galleryManager = GalleryManager(
            exif = exifHelperMock,
            fileHelper = fileHelperMock,
            mediaHelper = camHelperMock,
            imageHelper = imgHelperMock
        )

        runBlocking {
            galleryManager.onChooseFromGalleryResult(
                mockActivity,
                RESULT_OK,
                null,
                onSuccess = {
                    Assert.fail()
                },
                onError = {
                    Assert.assertEquals(it.code, IONError.CHOOSE_MULTIMEDIA_CANCELLED_ERROR.code)
                    Assert.assertEquals(it.description, IONError.CHOOSE_MULTIMEDIA_CANCELLED_ERROR.description)
                }
            )

            galleryManager.onChooseFromGalleryEditResult(
                mockActivity,
                RESULT_OK,
                null,
                onSuccess = {
                    Assert.fail()
                },
                onError = {
                    Assert.assertEquals(it.code, IONError.EDIT_IMAGE_ERROR.code)
                    Assert.assertEquals(it.description, IONError.EDIT_IMAGE_ERROR.description)
                })
        }
    }

    @Test
    fun givenChooseImageFromGalleryWhenRemoteFileThenSuccess() {

        val exifHelperMock = IONExifHelperMock()
        val fileHelperMock = IONFileHelperMock()
        val camHelperMock = IONMediaHelperMock()
        val imgHelperMock = IONImageHelperMock()

        fileHelperMock.fileLocationNotNull = false
        imgHelperMock.intentUris = listOf(
            mUri
        )
        fileHelperMock.mUri = mUri
        fileHelperMock.fileExists = true

        val galleryManager = GalleryManager(
            exif = exifHelperMock,
            fileHelper = fileHelperMock,
            mediaHelper = camHelperMock,
            imageHelper = imgHelperMock
        )

        runBlocking {
            galleryManager.onChooseFromGalleryResult(
                mockActivity,
                RESULT_OK,
                mIntent,
                false,
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
        private const val METADATA_SIZE = 23456L
        private const val METADATA_DURATION = 2
        private const val METADATA_FORMAT = "mp4"
        private const val METADATA_RESOLUTION = "1920x1080"
        private const val METADATA_CREATION_DATE = "2023-03-30T09:01:27Z"
    }
}