package com.outsystems.plugins.camera

import android.app.Activity
import android.content.Intent
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Environment
import android.util.Base64
import android.util.Log
import com.outsystems.plugins.camera.controller.OSCAMRController
import com.outsystems.plugins.camera.mocks.OSCAMRMediaHelperMock
import com.outsystems.plugins.camera.mocks.OSCAMRExifHelperMock
import com.outsystems.plugins.camera.mocks.OSCAMRFileHelperMock
import com.outsystems.plugins.camera.mocks.OSCAMRImageHelperMock
import com.outsystems.plugins.camera.model.OSCAMREditParameters
import com.outsystems.plugins.camera.model.OSCAMRError
import com.outsystems.plugins.camera.model.OSCAMRParameters
import com.outsystems.plugins.camera.view.ImageEditorActivity
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
class EditPictureTests {

    private val mIntent = Mockito.mock(Intent::class.java)
    private val mUri = Mockito.mock(Uri::class.java)
    private val mFile = Mockito.mock(File::class.java)
    private val mockUri = Mockito.mock(Uri::class.java)
    private val mockDrawable = Mockito.mock(Drawable::class.java)

    private lateinit var mLog: MockedStatic<Log>
    private lateinit var mEnvironment: MockedStatic<Environment>
    private lateinit var mUriStatic: MockedStatic<Uri>
    private lateinit var mBase64: MockedStatic<Base64>
    private lateinit var mDrawable: MockedStatic<Drawable>
    private lateinit var mockActivity: Activity

    private var editOptions = OSCAMREditParameters(
        "",
        fromUri = false,
        saveToGallery = false,
        includeMetadata = false
    )

    companion object{
        private const val PROCESS_SUCCESS = "myImage"
        private const val FILE_LOCATION = "file://content/storage/emulated/sampleFileLocation"
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
        mUriStatic = Mockito.mockStatic(Uri::class.java)
        mBase64 = Mockito.mockStatic(Base64::class.java)
        mDrawable = Mockito.mockStatic(Drawable::class.java)
        mockActivity = Mockito.mock(Activity::class.java)
        Mockito.`when`(Uri.parse(anyString())).thenReturn(mockUri)
        Mockito.`when`(Drawable.createFromPath(anyString())).thenReturn(mockDrawable)
    }

    @After
    fun after() {
        mEnvironment.close()
        mLog.close()
        mUriStatic.close()
        mBase64.close()
        mDrawable.close()
    }

    @Test
    fun givenEditPictureFromClientActionWhenEditPictureProcessResultThenSuccess() {

        val exifHelperMock = OSCAMRExifHelperMock()
        val fileHelperMock = OSCAMRFileHelperMock()
        val camHelperMock = OSCAMRMediaHelperMock()
        val imgHelperMock = OSCAMRImageHelperMock()

        val camController = OSCAMRController("someAppId", exifHelperMock, fileHelperMock, camHelperMock, imgHelperMock)

        Mockito.`when`(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)).thenReturn(mFile)

        val byteArray = ByteArray(1)
        Mockito.`when`(Base64.decode("imageInBinary", Base64.NO_WRAP)).thenReturn(byteArray)

        Mockito.doReturn(FILE_LOCATION).`when`(mIntent).getStringExtra(ImageEditorActivity.IMAGE_OUTPUT_URI_EXTRAS)

        imgHelperMock.bitmapToBase64Success = true

        camController.editImage(null, "imageInBinary", null, null)
        camController.processResultFromEdit(mockActivity, mIntent, editOptions,
            {
                assertEquals(it, OSCAMRImageHelperMock.SAMPLE_BASE64_THUMBNAIL)
            },
            {
                fail()
            },
            {
                fail()
            })
    }

    @Test
    fun givenEditPictureFromClientActionWhenEditPictureProcessErrorResultThenError() {

        val exifHelperMock = OSCAMRExifHelperMock()
        val fileHelperMock = OSCAMRFileHelperMock()
        val camHelperMock = OSCAMRMediaHelperMock()
        val imgHelperMock = OSCAMRImageHelperMock()

        val camController = OSCAMRController("someAppId", exifHelperMock, fileHelperMock, camHelperMock, imgHelperMock)

        Mockito.`when`(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)).thenReturn(mFile)

        val byteArray = ByteArray(1)
        Mockito.`when`(Base64.decode("imageInBinary", Base64.NO_WRAP)).thenReturn(byteArray)

        imgHelperMock.bitmapToBase64Success = false

        camController.editImage(null, "imageInBinary", null, null)
        camController.processResultFromEdit(mockActivity, mIntent, editOptions,
            {
                fail()
            },
            {
                fail()
            },
            {
                assertEquals(it.code, OSCAMRError.EDIT_IMAGE_ERROR.code)
                assertEquals(it.description, OSCAMRError.EDIT_IMAGE_ERROR.description)
            })
    }

    @Test
    fun givenTakePictureAllowEditWhenEditProcessSuccessThenSuccess() {

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
        camController.openCropActivity(null, mUri, 1, 1)
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
    fun givenTakePictureAllowEditWhenEditProcessErrorThenError() {

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

        imgHelperMock.processPicSuccess = false
        imgHelperMock.areOptionsZero = false

        camController.takePicture(mockActivity, 0, 0)
        camController.openCropActivity(null, mUri, 1, 1)
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
    fun givenFileDoesNotExistWhenEditURIPictureThenError() {

        val exifHelperMock = OSCAMRExifHelperMock()
        val fileHelperMock = OSCAMRFileHelperMock()
        val camHelperMock = OSCAMRMediaHelperMock()
        val imgHelperMock = OSCAMRImageHelperMock()
        val camController = OSCAMRController("someAppId", exifHelperMock, fileHelperMock, camHelperMock, imgHelperMock)
        fileHelperMock.fileExists = false

        camController.editURIPicture(mockActivity, FILE_LOCATION, null, null
        ) {
            assertEquals(it.code, OSCAMRError.FILE_DOES_NOT_EXIST_ERROR.code)
            assertEquals(it.description, OSCAMRError.FILE_DOES_NOT_EXIST_ERROR.description)
        }
    }

    @Test
    fun givenResultImagePathNullWhenEditURIPictureThenError() {

        val exifHelperMock = OSCAMRExifHelperMock()
        val fileHelperMock = OSCAMRFileHelperMock()
        val camHelperMock = OSCAMRMediaHelperMock()
        val imgHelperMock = OSCAMRImageHelperMock()

        val camController = OSCAMRController("someAppId", exifHelperMock, fileHelperMock, camHelperMock, imgHelperMock)

        Mockito.`when`(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)).thenReturn(mFile)

        val byteArray = ByteArray(1)
        Mockito.`when`(Base64.decode("imageInBinary", Base64.NO_WRAP)).thenReturn(byteArray)

        Mockito.doReturn(null).`when`(mIntent).getStringExtra(ImageEditorActivity.IMAGE_OUTPUT_URI_EXTRAS)

        imgHelperMock.bitmapToBase64Success = true
        fileHelperMock.fileExists = true

        val options = OSCAMREditParameters(
            FILE_LOCATION,
            true,
            saveToGallery = true,
            includeMetadata = true
        )

        fileHelperMock.fileExists = true
        camController.editURIPicture(mockActivity, FILE_LOCATION, null, null
        ) {
            fail()
        }
        camController.processResultFromEdit(mockActivity, mIntent, options,
            {
                fail()
            },
            {
                fail()
            },
            {
                assertEquals(it.code, OSCAMRError.EDIT_IMAGE_ERROR.code)
                assertEquals(it.description, OSCAMRError.EDIT_IMAGE_ERROR.description)
            })
    }

    @Test
    fun givenResultImagePathEmptyWhenEditURIPictureThenError() {

        val exifHelperMock = OSCAMRExifHelperMock()
        val fileHelperMock = OSCAMRFileHelperMock()
        val camHelperMock = OSCAMRMediaHelperMock()
        val imgHelperMock = OSCAMRImageHelperMock()

        val camController = OSCAMRController("someAppId", exifHelperMock, fileHelperMock, camHelperMock, imgHelperMock)

        Mockito.`when`(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)).thenReturn(mFile)

        val byteArray = ByteArray(1)
        Mockito.`when`(Base64.decode("imageInBinary", Base64.NO_WRAP)).thenReturn(byteArray)

        Mockito.doReturn("").`when`(mIntent).getStringExtra(ImageEditorActivity.IMAGE_OUTPUT_URI_EXTRAS)

        imgHelperMock.bitmapToBase64Success = true
        fileHelperMock.fileExists = true

        val options = OSCAMREditParameters(
            FILE_LOCATION,
            true,
            saveToGallery = true,
            includeMetadata = true
        )

        fileHelperMock.fileExists = true
        camController.editURIPicture(mockActivity, FILE_LOCATION, null, null
        ) {
            fail()
        }
        camController.processResultFromEdit(mockActivity, mIntent, options,
            {
                fail()
            },
            {
                fail()
            },
            {
                assertEquals(it.code, OSCAMRError.EDIT_IMAGE_ERROR.code)
                assertEquals(it.description, OSCAMRError.EDIT_IMAGE_ERROR.description)
            })
    }

    @Test
    fun givenImageURINullWhenEditURIPictureThenError() {

        val exifHelperMock = OSCAMRExifHelperMock()
        val fileHelperMock = OSCAMRFileHelperMock()
        val camHelperMock = OSCAMRMediaHelperMock()
        val imgHelperMock = OSCAMRImageHelperMock()

        val camController = OSCAMRController("someAppId", exifHelperMock, fileHelperMock, camHelperMock, imgHelperMock)

        Mockito.`when`(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)).thenReturn(mFile)

        val byteArray = ByteArray(1)
        Mockito.`when`(Base64.decode("imageInBinary", Base64.NO_WRAP)).thenReturn(byteArray)

        Mockito.doReturn(FILE_LOCATION).`when`(mIntent).getStringExtra(ImageEditorActivity.IMAGE_OUTPUT_URI_EXTRAS)

        imgHelperMock.bitmapToBase64Success = true
        fileHelperMock.fileExists = true
        fileHelperMock.isUriNull = true

        val options = OSCAMREditParameters(
            FILE_LOCATION,
            true,
            saveToGallery = true,
            includeMetadata = true
        )

        fileHelperMock.fileExists = true
        camController.editURIPicture(mockActivity, FILE_LOCATION, null, null
        ) {
            fail()
        }
        camController.processResultFromEdit(mockActivity, mIntent, options,
            {
                fail()
            },
            {
                fail()
            },
            {
                assertEquals(it.code, OSCAMRError.EDIT_IMAGE_ERROR.code)
                assertEquals(it.description, OSCAMRError.EDIT_IMAGE_ERROR.description)
            })
    }

    @Test
    fun givenImageURICreatesNullDrawableWhenEditURIPictureThenError() {
        val exifHelperMock = OSCAMRExifHelperMock()
        val fileHelperMock = OSCAMRFileHelperMock()
        val camHelperMock = OSCAMRMediaHelperMock()
        val imgHelperMock = OSCAMRImageHelperMock()

        val camController = OSCAMRController("someAppId", exifHelperMock, fileHelperMock, camHelperMock, imgHelperMock)

        Mockito.`when`(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)).thenReturn(mFile)

        val byteArray = ByteArray(1)
        Mockito.`when`(Base64.decode("imageInBinary", Base64.NO_WRAP)).thenReturn(byteArray)

        Mockito.doReturn(FILE_LOCATION).`when`(mIntent).getStringExtra(ImageEditorActivity.IMAGE_OUTPUT_URI_EXTRAS)

        imgHelperMock.bitmapToBase64Success = true
        fileHelperMock.fileExists = true
        fileHelperMock.isUriNull = false

        Mockito.`when`(Drawable.createFromPath(anyString())).thenReturn(null)

        fileHelperMock.fileExists = true
        camController.editURIPicture(mockActivity, FILE_LOCATION, null, null
        ) {
            assertEquals(it.code, OSCAMRError.FETCH_IMAGE_FROM_URI_ERROR.code)
            assertEquals(it.description, OSCAMRError.FETCH_IMAGE_FROM_URI_ERROR.description)
        }
    }

    @Test
    fun givenMediaResultNullWhenEditURIPictureThenError() {

        val exifHelperMock = OSCAMRExifHelperMock()
        val fileHelperMock = OSCAMRFileHelperMock()
        val camHelperMock = OSCAMRMediaHelperMock()
        val imgHelperMock = OSCAMRImageHelperMock()

        val camController = OSCAMRController("someAppId", exifHelperMock, fileHelperMock, camHelperMock, imgHelperMock)

        Mockito.`when`(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)).thenReturn(mFile)

        val byteArray = ByteArray(1)
        Mockito.`when`(Base64.decode("imageInBinary", Base64.NO_WRAP)).thenReturn(byteArray)

        Mockito.doReturn(FILE_LOCATION).`when`(mIntent).getStringExtra(ImageEditorActivity.IMAGE_OUTPUT_URI_EXTRAS)

        imgHelperMock.bitmapToBase64Success = true
        fileHelperMock.fileExists = true
        fileHelperMock.isUriNull = false
        imgHelperMock.isBitmapNull = true

        val options = OSCAMREditParameters(
            FILE_LOCATION,
            true,
            saveToGallery = true,
            includeMetadata = true
        )

        camController.editURIPicture(mockActivity, FILE_LOCATION, null, null
        ) {
            fail()
        }
        camController.processResultFromEdit(mockActivity, mIntent, options,
            {
                fail()
            },
            {
                fail()
            },
            {
                assertEquals(it.code, OSCAMRError.EDIT_IMAGE_ERROR.code)
                assertEquals(it.description, OSCAMRError.EDIT_IMAGE_ERROR.description)
            })
    }

    @Test
    fun givenProcessEditSuccessWhenEditURIPictureWithSaveToGalleryAndMetadataThenSuccess() {

        val exifHelperMock = OSCAMRExifHelperMock()
        val fileHelperMock = OSCAMRFileHelperMock()
        val camHelperMock = OSCAMRMediaHelperMock()
        val imgHelperMock = OSCAMRImageHelperMock()

        val camController = OSCAMRController("someAppId", exifHelperMock, fileHelperMock, camHelperMock, imgHelperMock)

        Mockito.`when`(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)).thenReturn(mFile)

        val byteArray = ByteArray(1)
        Mockito.`when`(Base64.decode("imageInBinary", Base64.NO_WRAP)).thenReturn(byteArray)

        Mockito.doReturn(FILE_LOCATION).`when`(mIntent).getStringExtra(ImageEditorActivity.IMAGE_OUTPUT_URI_EXTRAS)

        imgHelperMock.bitmapToBase64Success = true
        fileHelperMock.fileExists = true
        fileHelperMock.isUriNull = false
        imgHelperMock.isBitmapNull = false
        fileHelperMock.fileSize = METADATA_SIZE
        fileHelperMock.creationDateTime = METADATA_CREATION_DATE
        fileHelperMock.fileExtension = METADATA_FORMAT

        val options = OSCAMREditParameters(
            FILE_LOCATION,
            true,
            saveToGallery = true,
            includeMetadata = true
        )

        camController.editURIPicture(mockActivity, FILE_LOCATION, null, null
        ) {
            fail()
        }
        camController.processResultFromEdit(mockActivity, mIntent, options,
            {
                fail()
            },
            {
                assertEquals(it.uri, FILE_LOCATION)
                assertEquals(it.type, 0)
                assertEquals(it.thumbnail, BASE_64)
                assertEquals(it.metadata?.creationDate, METADATA_CREATION_DATE)
                assertEquals(it.metadata?.resolution, METADATA_RESOLUTION)
                assertEquals(it.metadata?.duration, null)
                assertEquals(it.metadata?.format, METADATA_FORMAT)
                assertEquals(it.metadata?.size, METADATA_SIZE)
            },
            {
                fail()
            })
    }

    @Test
    fun givenProcessEditSuccessWhenEditURIPictureWithoutSaveToGalleryAndNoMetadataThenSuccess() {

        val exifHelperMock = OSCAMRExifHelperMock()
        val fileHelperMock = OSCAMRFileHelperMock()
        val camHelperMock = OSCAMRMediaHelperMock()
        val imgHelperMock = OSCAMRImageHelperMock()

        val camController = OSCAMRController("someAppId", exifHelperMock, fileHelperMock, camHelperMock, imgHelperMock)

        Mockito.`when`(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)).thenReturn(mFile)

        val byteArray = ByteArray(1)
        Mockito.`when`(Base64.decode("imageInBinary", Base64.NO_WRAP)).thenReturn(byteArray)

        Mockito.doReturn(FILE_LOCATION).`when`(mIntent).getStringExtra(ImageEditorActivity.IMAGE_OUTPUT_URI_EXTRAS)

        imgHelperMock.bitmapToBase64Success = true
        fileHelperMock.fileExists = true
        fileHelperMock.isUriNull = false
        imgHelperMock.isBitmapNull = false

        val options = OSCAMREditParameters(
            FILE_LOCATION,
            true,
            saveToGallery = false,
            includeMetadata = false
        )

        camController.editURIPicture(mockActivity, FILE_LOCATION, null, null
        ) {
            fail()
        }
        camController.processResultFromEdit(mockActivity, mIntent, options,
            {
                fail()
            },
            {
                assertEquals(it.uri, FILE_LOCATION)
                assertEquals(it.type, 0)
                assertEquals(it.thumbnail, BASE_64)
                assertEquals(it.metadata, null)
            },
            {
                fail()
            })
    }

}