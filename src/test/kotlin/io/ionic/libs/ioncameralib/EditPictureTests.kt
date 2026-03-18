package io.ionic.libs.ioncameralib

import android.app.Activity
import android.content.Intent
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Environment
import android.util.Base64
import android.util.Log
import androidx.activity.result.ActivityResultLauncher
import io.ionic.libs.ioncameralib.manager.IONCAMRCameraManager
import io.ionic.libs.ioncameralib.manager.IONCAMREditManager
import io.ionic.libs.ioncameralib.mocks.IONExifHelperMock
import io.ionic.libs.ioncameralib.mocks.IONCAMRFileHelperMock
import io.ionic.libs.ioncameralib.mocks.IONCAMRImageHelperMock
import io.ionic.libs.ioncameralib.mocks.IONCAMRMediaHelperMock
import io.ionic.libs.ioncameralib.model.IONCAMRCameraParameters
import io.ionic.libs.ioncameralib.model.IONCAMREditParameters
import io.ionic.libs.ioncameralib.model.IONCAMRError
import io.ionic.libs.ioncameralib.view.IONCAMRImageEditorActivity
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.fail
import org.junit.Before
import org.junit.Test
import org.mockito.ArgumentMatchers.anyString
import org.mockito.MockedStatic
import org.mockito.Mockito
import org.mockito.kotlin.mock
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
    private val mActivityLauncher = mock<ActivityResultLauncher<Intent>>()

    private lateinit var mLog: MockedStatic<Log>
    private lateinit var mEnvironment: MockedStatic<Environment>
    private lateinit var mUriStatic: MockedStatic<Uri>
    private lateinit var mBase64: MockedStatic<Base64>
    private lateinit var mDrawable: MockedStatic<Drawable>
    private lateinit var mockActivity: Activity

    private var editOptions = IONCAMREditParameters(
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

        val exifHelperMock = IONExifHelperMock()
        val fileHelperMock = IONCAMRFileHelperMock()
        val camHelperMock = IONCAMRMediaHelperMock()
        val imgHelperMock = IONCAMRImageHelperMock()

        val IONCAMREditManager = IONCAMREditManager(
            "someAppId",
            exifHelperMock,
            fileHelperMock,
            camHelperMock,
            imgHelperMock
        )

        Mockito.`when`(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)).thenReturn(mFile)

        val byteArray = ByteArray(1)
        Mockito.`when`(Base64.decode("imageInBinary", Base64.NO_WRAP)).thenReturn(byteArray)

        Mockito.doReturn(FILE_LOCATION).`when`(mIntent).getStringExtra(IONCAMRImageEditorActivity.IMAGE_OUTPUT_URI_EXTRAS)

        imgHelperMock.bitmapToBase64Success = true

        IONCAMREditManager.editImage(null, "imageInBinary", mActivityLauncher)
        IONCAMREditManager.processResultFromEdit(mockActivity, mIntent, editOptions,
            {
                assertEquals(it, IONCAMRImageHelperMock.SAMPLE_BASE64_THUMBNAIL)
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

        val exifHelperMock = IONExifHelperMock()
        val fileHelperMock = IONCAMRFileHelperMock()
        val camHelperMock = IONCAMRMediaHelperMock()
        val imgHelperMock = IONCAMRImageHelperMock()

        val IONCAMREditManager = IONCAMREditManager(
            "someAppId",
            exifHelperMock,
            fileHelperMock,
            camHelperMock,
            imgHelperMock
        )

        Mockito.`when`(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)).thenReturn(mFile)

        val byteArray = ByteArray(1)
        Mockito.`when`(Base64.decode("imageInBinary", Base64.NO_WRAP)).thenReturn(byteArray)

        imgHelperMock.bitmapToBase64Success = false

        IONCAMREditManager.editImage(null, "imageInBinary", mActivityLauncher)
        IONCAMREditManager.processResultFromEdit(mockActivity, mIntent, editOptions,
            {
                fail()
            },
            {
                fail()
            },
            {
                assertEquals(it.code, IONCAMRError.EDIT_IMAGE_ERROR.code)
                assertEquals(it.description, IONCAMRError.EDIT_IMAGE_ERROR.description)
            })
    }

    @Test
    fun givenTakePictureAllowEditWhenEditProcessSuccessThenSuccess() {

        val camParameters = IONCAMRCameraParameters(
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
        val fileHelperMock = IONCAMRFileHelperMock()
        val camHelperMock = IONCAMRMediaHelperMock()
        val imgHelperMock = IONCAMRImageHelperMock()

        val IONCAMRCameraManager = IONCAMRCameraManager(
            "someAppId",
            exifHelperMock,
            fileHelperMock,
            camHelperMock,
            imgHelperMock
        )
        val IONCAMREditManager = IONCAMREditManager(
            "someAppId",
            exifHelperMock,
            fileHelperMock,
            camHelperMock,
            imgHelperMock
        )

        Mockito.`when`(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)).thenReturn(mFile)

        imgHelperMock.processPicSuccess = true
        imgHelperMock.areOptionsZero = false

        IONCAMRCameraManager.takePicture(mockActivity, 0, 0)
        IONCAMREditManager.openCropActivity(null, mUri, mActivityLauncher)
        IONCAMRCameraManager.processResultFromCamera(mockActivity, mIntent, camParameters,
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

        val camParameters = IONCAMRCameraParameters(
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
        val fileHelperMock = IONCAMRFileHelperMock()
        val camHelperMock = IONCAMRMediaHelperMock()
        val imgHelperMock = IONCAMRImageHelperMock()

        val IONCAMRCameraManager = IONCAMRCameraManager(
            "someAppId",
            exifHelperMock,
            fileHelperMock,
            camHelperMock,
            imgHelperMock
        )
        val IONCAMREditManager = IONCAMREditManager(
            "someAppId",
            exifHelperMock,
            fileHelperMock,
            camHelperMock,
            imgHelperMock
        )

        Mockito.`when`(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)).thenReturn(mFile)

        imgHelperMock.processPicSuccess = false
        imgHelperMock.areOptionsZero = false

        IONCAMRCameraManager.takePicture(mockActivity, 0, 0)
        IONCAMREditManager.openCropActivity(null, mUri, mActivityLauncher)
        IONCAMRCameraManager.processResultFromCamera(mockActivity, mIntent, camParameters,
            onImage = {
                fail()
            },
            onMediaResult = {
                fail()
            },
            onError = {
                assertEquals(it.code, IONCAMRError.PROCESS_IMAGE_ERROR.code)
                assertEquals(it.description, IONCAMRError.PROCESS_IMAGE_ERROR.description)
            })
    }

    @Test
    fun givenFileDoesNotExistWhenEditURIPictureThenError() {

        val exifHelperMock = IONExifHelperMock()
        val fileHelperMock = IONCAMRFileHelperMock()
        val camHelperMock = IONCAMRMediaHelperMock()
        val imgHelperMock = IONCAMRImageHelperMock()
        val IONCAMREditManager = IONCAMREditManager(
            "someAppId",
            exifHelperMock,
            fileHelperMock,
            camHelperMock,
            imgHelperMock
        )
        fileHelperMock.fileExists = false

        IONCAMREditManager.editURIPicture(mockActivity, FILE_LOCATION, mActivityLauncher) {
            assertEquals(it.code, IONCAMRError.FILE_DOES_NOT_EXIST_ERROR.code)
            assertEquals(it.description, IONCAMRError.FILE_DOES_NOT_EXIST_ERROR.description)
        }
    }

    @Test
    fun givenResultImagePathNullWhenEditURIPictureThenError() {

        val exifHelperMock = IONExifHelperMock()
        val fileHelperMock = IONCAMRFileHelperMock()
        val camHelperMock = IONCAMRMediaHelperMock()
        val imgHelperMock = IONCAMRImageHelperMock()

        val IONCAMREditManager = IONCAMREditManager(
            "someAppId",
            exifHelperMock,
            fileHelperMock,
            camHelperMock,
            imgHelperMock
        )

        Mockito.`when`(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)).thenReturn(mFile)

        val byteArray = ByteArray(1)
        Mockito.`when`(Base64.decode("imageInBinary", Base64.NO_WRAP)).thenReturn(byteArray)

        Mockito.doReturn(null).`when`(mIntent).getStringExtra(IONCAMRImageEditorActivity.IMAGE_OUTPUT_URI_EXTRAS)

        imgHelperMock.bitmapToBase64Success = true
        fileHelperMock.fileExists = true

        val options = IONCAMREditParameters(
            FILE_LOCATION,
            true,
            saveToGallery = true,
            includeMetadata = true
        )

        fileHelperMock.fileExists = true
        IONCAMREditManager.editURIPicture(mockActivity, FILE_LOCATION, mActivityLauncher) {
            fail()
        }
        IONCAMREditManager.processResultFromEdit(mockActivity, mIntent, options,
            {
                fail()
            },
            {
                fail()
            },
            {
                assertEquals(it.code, IONCAMRError.EDIT_IMAGE_ERROR.code)
                assertEquals(it.description, IONCAMRError.EDIT_IMAGE_ERROR.description)
            })
    }

    @Test
    fun givenResultImagePathEmptyWhenEditURIPictureThenError() {

        val exifHelperMock = IONExifHelperMock()
        val fileHelperMock = IONCAMRFileHelperMock()
        val camHelperMock = IONCAMRMediaHelperMock()
        val imgHelperMock = IONCAMRImageHelperMock()

        val IONCAMREditManager = IONCAMREditManager(
            "someAppId",
            exifHelperMock,
            fileHelperMock,
            camHelperMock,
            imgHelperMock
        )

        Mockito.`when`(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)).thenReturn(mFile)

        val byteArray = ByteArray(1)
        Mockito.`when`(Base64.decode("imageInBinary", Base64.NO_WRAP)).thenReturn(byteArray)

        Mockito.doReturn("").`when`(mIntent).getStringExtra(IONCAMRImageEditorActivity.IMAGE_OUTPUT_URI_EXTRAS)

        imgHelperMock.bitmapToBase64Success = true
        fileHelperMock.fileExists = true

        val options = IONCAMREditParameters(
            FILE_LOCATION,
            true,
            saveToGallery = true,
            includeMetadata = true
        )

        fileHelperMock.fileExists = true
        IONCAMREditManager.editURIPicture(mockActivity, FILE_LOCATION, mActivityLauncher) {
            fail()
        }
        IONCAMREditManager.processResultFromEdit(mockActivity, mIntent, options,
            {
                fail()
            },
            {
                fail()
            },
            {
                assertEquals(it.code, IONCAMRError.EDIT_IMAGE_ERROR.code)
                assertEquals(it.description, IONCAMRError.EDIT_IMAGE_ERROR.description)
            })
    }

    @Test
    fun givenImageURINullWhenEditURIPictureThenError() {

        val exifHelperMock = IONExifHelperMock()
        val fileHelperMock = IONCAMRFileHelperMock()
        val camHelperMock = IONCAMRMediaHelperMock()
        val imgHelperMock = IONCAMRImageHelperMock()

        val IONCAMREditManager = IONCAMREditManager(
            "someAppId",
            exifHelperMock,
            fileHelperMock,
            camHelperMock,
            imgHelperMock
        )

        Mockito.`when`(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)).thenReturn(mFile)

        val byteArray = ByteArray(1)
        Mockito.`when`(Base64.decode("imageInBinary", Base64.NO_WRAP)).thenReturn(byteArray)

        Mockito.doReturn(FILE_LOCATION).`when`(mIntent).getStringExtra(IONCAMRImageEditorActivity.IMAGE_OUTPUT_URI_EXTRAS)

        imgHelperMock.bitmapToBase64Success = true
        fileHelperMock.fileExists = true
        fileHelperMock.isUriNull = true

        val options = IONCAMREditParameters(
            FILE_LOCATION,
            true,
            saveToGallery = true,
            includeMetadata = true
        )

        fileHelperMock.fileExists = true
        IONCAMREditManager.editURIPicture(mockActivity, FILE_LOCATION, mActivityLauncher) {
            fail()
        }
        IONCAMREditManager.processResultFromEdit(mockActivity, mIntent, options,
            {
                fail()
            },
            {
                fail()
            },
            {
                assertEquals(it.code, IONCAMRError.EDIT_IMAGE_ERROR.code)
                assertEquals(it.description, IONCAMRError.EDIT_IMAGE_ERROR.description)
            })
    }

    @Test
    fun givenImageURICreatesNullDrawableWhenEditURIPictureThenError() {
        val exifHelperMock = IONExifHelperMock()
        val fileHelperMock = IONCAMRFileHelperMock()
        val camHelperMock = IONCAMRMediaHelperMock()
        val imgHelperMock = IONCAMRImageHelperMock()

        val IONCAMREditManager = IONCAMREditManager(
            "someAppId",
            exifHelperMock,
            fileHelperMock,
            camHelperMock,
            imgHelperMock
        )

        Mockito.`when`(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)).thenReturn(mFile)

        val byteArray = ByteArray(1)
        Mockito.`when`(Base64.decode("imageInBinary", Base64.NO_WRAP)).thenReturn(byteArray)

        Mockito.doReturn(FILE_LOCATION).`when`(mIntent).getStringExtra(IONCAMRImageEditorActivity.IMAGE_OUTPUT_URI_EXTRAS)

        imgHelperMock.bitmapToBase64Success = true
        fileHelperMock.fileExists = true
        fileHelperMock.isUriNull = false

        Mockito.`when`(Drawable.createFromPath(anyString())).thenReturn(null)

        fileHelperMock.fileExists = true
        IONCAMREditManager.editURIPicture(mockActivity, FILE_LOCATION, mActivityLauncher) {
            assertEquals(it.code, IONCAMRError.FETCH_IMAGE_FROM_URI_ERROR.code)
            assertEquals(it.description, IONCAMRError.FETCH_IMAGE_FROM_URI_ERROR.description)
        }
    }

    @Test
    fun givenImageBitmapNullWhenEditURIPictureThenError() {

        val exifHelperMock = IONExifHelperMock()
        val fileHelperMock = IONCAMRFileHelperMock()
        val camHelperMock = IONCAMRMediaHelperMock()
        val imgHelperMock = IONCAMRImageHelperMock()

        val IONCAMREditManager = IONCAMREditManager(
            "someAppId",
            exifHelperMock,
            fileHelperMock,
            camHelperMock,
            imgHelperMock
        )

        Mockito.`when`(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)).thenReturn(mFile)

        val byteArray = ByteArray(1)
        Mockito.`when`(Base64.decode("imageInBinary", Base64.NO_WRAP)).thenReturn(byteArray)

        Mockito.doReturn(FILE_LOCATION).`when`(mIntent).getStringExtra(IONCAMRImageEditorActivity.IMAGE_OUTPUT_URI_EXTRAS)

        imgHelperMock.bitmapToBase64Success = true
        fileHelperMock.fileExists = true
        fileHelperMock.isUriNull = false
        imgHelperMock.isBitmapNull = true

        val options = IONCAMREditParameters(
            FILE_LOCATION,
            true,
            saveToGallery = true,
            includeMetadata = true
        )

        IONCAMREditManager.editURIPicture(mockActivity, FILE_LOCATION, mActivityLauncher) {
            fail()
        }
        IONCAMREditManager.processResultFromEdit(mockActivity, mIntent, options,
            {
                fail()
            },
            {
                fail()
            },
            {
                assertEquals(it.code, IONCAMRError.EDIT_IMAGE_ERROR.code)
                assertEquals(it.description, IONCAMRError.EDIT_IMAGE_ERROR.description)
            })
    }

    @Test
    fun givenProcessEditSuccessWhenEditURIPictureWithSaveToGalleryAndMetadataThenSuccess() {

        val exifHelperMock = IONExifHelperMock()
        val fileHelperMock = IONCAMRFileHelperMock()
        val camHelperMock = IONCAMRMediaHelperMock()
        val imgHelperMock = IONCAMRImageHelperMock()

        val IONCAMREditManager = IONCAMREditManager(
            "someAppId",
            exifHelperMock,
            fileHelperMock,
            camHelperMock,
            imgHelperMock
        )

        Mockito.`when`(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)).thenReturn(mFile)

        val byteArray = ByteArray(1)
        Mockito.`when`(Base64.decode("imageInBinary", Base64.NO_WRAP)).thenReturn(byteArray)

        Mockito.doReturn(FILE_LOCATION).`when`(mIntent).getStringExtra(IONCAMRImageEditorActivity.IMAGE_OUTPUT_URI_EXTRAS)

        imgHelperMock.bitmapToBase64Success = true
        fileHelperMock.fileExists = true
        fileHelperMock.isUriNull = false
        imgHelperMock.isBitmapNull = false
        fileHelperMock.fileSize = METADATA_SIZE
        fileHelperMock.creationDateTime = METADATA_CREATION_DATE
        fileHelperMock.fileExtension = METADATA_FORMAT

        val options = IONCAMREditParameters(
            FILE_LOCATION,
            true,
            saveToGallery = true,
            includeMetadata = true
        )

        IONCAMREditManager.editURIPicture(mockActivity, FILE_LOCATION, mActivityLauncher) {
            fail()
        }
        IONCAMREditManager.processResultFromEdit(mockActivity, mIntent, options,
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

        val exifHelperMock = IONExifHelperMock()
        val fileHelperMock = IONCAMRFileHelperMock()
        val camHelperMock = IONCAMRMediaHelperMock()
        val imgHelperMock = IONCAMRImageHelperMock()

        val IONCAMREditManager = IONCAMREditManager(
            "someAppId",
            exifHelperMock,
            fileHelperMock,
            camHelperMock,
            imgHelperMock
        )

        Mockito.`when`(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)).thenReturn(mFile)

        val byteArray = ByteArray(1)
        Mockito.`when`(Base64.decode("imageInBinary", Base64.NO_WRAP)).thenReturn(byteArray)

        Mockito.doReturn(FILE_LOCATION).`when`(mIntent).getStringExtra(IONCAMRImageEditorActivity.IMAGE_OUTPUT_URI_EXTRAS)

        imgHelperMock.bitmapToBase64Success = true
        fileHelperMock.fileExists = true
        fileHelperMock.isUriNull = false
        imgHelperMock.isBitmapNull = false

        val options = IONCAMREditParameters(
            FILE_LOCATION,
            true,
            saveToGallery = false,
            includeMetadata = false
        )

        IONCAMREditManager.editURIPicture(mockActivity, FILE_LOCATION, mActivityLauncher) {
            fail()
        }
        IONCAMREditManager.processResultFromEdit(mockActivity, mIntent, options,
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