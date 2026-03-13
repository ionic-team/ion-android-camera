package io.ionic.libs.ioncameralib

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Environment
import android.util.Base64
import android.util.Log
import io.ionic.libs.ioncameralib.manager.GalleryManager
import io.ionic.libs.ioncameralib.mocks.IONExifHelperMock
import io.ionic.libs.ioncameralib.mocks.IONFileHelperMock
import io.ionic.libs.ioncameralib.mocks.IONImageHelperMock
import io.ionic.libs.ioncameralib.mocks.IONMediaHelperMock
import io.ionic.libs.ioncameralib.model.IONCameraParameters
import io.ionic.libs.ioncameralib.model.IONEditParameters
import io.ionic.libs.ioncameralib.model.IONError
import io.ionic.libs.ioncameralib.view.IONImageEditorActivity
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.fail
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito
import java.io.File

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
class ChoosePictureTests {

    private val mIntent = Mockito.mock(Intent::class.java)
    private val mEnvironment = Mockito.mockStatic(Environment::class.java)
    private val mLog = Mockito.mockStatic(Log::class.java)
    private val mUriStatic = Mockito.mockStatic(Uri::class.java)
    private val mUri = Mockito.mock(Uri::class.java)
    private val mFile = Mockito.mock(File::class.java)
    private val mBase64 = Mockito.mockStatic(Base64::class.java)
    private lateinit var mockActivity: Activity

    private var editOptions = IONEditParameters(
        "",
        fromUri = false,
        saveToGallery = false,
        includeMetadata = false
    )

    companion object{
        private const val PROCESS_SUCCESS = "myImage"
        private const val FILE_LOCATION = "file://content/storage/emulated/sampleFileLocation"
        private const val PNG_MIME_TYPE = "image/png"
        private const val JPEG_MIME_TYPE = "image/jpeg"

    }

    @Before
    fun before() {
        mockActivity = Mockito.mock(Activity::class.java)
    }

    @After
    fun after() {
        mEnvironment.close()
        mLog.close()
        mUriStatic.close()
        mBase64.close()
    }

    @Test
    fun givenGetPictureWhenProcessResultNoMimeTypeSuccessThenSuccess() {

        val camParameters = IONCameraParameters(
            60,
            1600,
            1600,
            0,
            0,
            allowEdit = false,
            correctOrientation = true,
            saveToPhotoAlbum = false,
            includeMetadata = false,
            latestVersion = false
        )

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

        Mockito.`when`(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)).thenReturn(mFile)

        val byteArray = ByteArray(1)
        Mockito.`when`(Base64.decode("imageInBinary", Base64.NO_WRAP)).thenReturn(byteArray)

        imgHelperMock.processPicSuccess = true
        fileHelperMock.fileLocationNotNull = true

        galleryManager.getImage(null, 0, 0, camParameters)
        galleryManager.processResultFromGallery(null, mIntent, camParameters,
            {
                assertEquals(it, PROCESS_SUCCESS)
            },
            {
                fail()
            })
    }

    @Test
    fun givenGetPictureWhenProcessResultVideoSuccessThenSuccess() {

        val camParameters = IONCameraParameters(
            60,
            1600,
            1600,
            0,
            1,
            allowEdit = false,
            correctOrientation = true,
            saveToPhotoAlbum = false,
            includeMetadata = false,
            latestVersion = false
        )

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

        Mockito.`when`(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)).thenReturn(mFile)

        val byteArray = ByteArray(1)
        Mockito.`when`(Base64.decode("imageInBinary", Base64.NO_WRAP)).thenReturn(byteArray)

        imgHelperMock.processPicSuccess = true
        fileHelperMock.fileLocationNotNull = true

        galleryManager.getImage(null, 0, 0, camParameters)
        galleryManager.processResultFromGallery(null, mIntent, camParameters,
            {
                assertEquals(it, PROCESS_SUCCESS)
            },
            {
                fail()
            })
    }

    @Test
    fun givenGetPictureWhenProcessResultAllMediaSuccessThenSuccess() {

        val camParameters = IONCameraParameters(
            60,
            1600,
            1600,
            0,
            2,
            allowEdit = false,
            correctOrientation = true,
            saveToPhotoAlbum = false,
            includeMetadata = false,
            latestVersion = false
        )

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

        Mockito.`when`(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)).thenReturn(mFile)

        val byteArray = ByteArray(1)
        Mockito.`when`(Base64.decode("imageInBinary", Base64.NO_WRAP)).thenReturn(byteArray)

        imgHelperMock.processPicSuccess = true
        fileHelperMock.fileLocationNotNull = true

        galleryManager.getImage(null, 0, 0, camParameters)
        galleryManager.processResultFromGallery(null, mIntent, camParameters,
            {
                assertEquals(it, PROCESS_SUCCESS)
            },
            {
                fail()
            })
    }

    @Test
    fun givenNegativeDimensionsGetPictureWhenProcessResultSuccessThenSuccess() {

        val camParameters = IONCameraParameters(
            60,
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

        Mockito.`when`(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)).thenReturn(mFile)

        val byteArray = ByteArray(1)
        Mockito.`when`(Base64.decode("imageInBinary", Base64.NO_WRAP)).thenReturn(byteArray)

        imgHelperMock.processPicSuccess = true
        fileHelperMock.fileLocationNotNull = true
        fileHelperMock.mimeType = JPEG_MIME_TYPE

        galleryManager.getImage(null, 0, 0, camParameters)
        galleryManager.processResultFromGallery(null, mIntent, camParameters,
            {
                assertEquals(it, PROCESS_SUCCESS)
            },
            {
                fail()
            })
    }

    @Test
    fun givenGetPictureWhenProcessResultMimeTypeJPEGSuccessThenSuccess() {

        val camParameters = IONCameraParameters(
            60,
            1600,
            1600,
            0,
            0,
            allowEdit = false,
            correctOrientation = true,
            saveToPhotoAlbum = false,
            includeMetadata = false,
            latestVersion = false
        )

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

        Mockito.`when`(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)).thenReturn(mFile)

        val byteArray = ByteArray(1)
        Mockito.`when`(Base64.decode("imageInBinary", Base64.NO_WRAP)).thenReturn(byteArray)

        imgHelperMock.bitmapToBase64Success = true
        imgHelperMock.processPicSuccess = true
        fileHelperMock.fileLocationNotNull = true
        fileHelperMock.mimeType = JPEG_MIME_TYPE

        galleryManager.getImage(null, 0, 0, camParameters)
        galleryManager.processResultFromGallery(null, mIntent, camParameters,
            {
                assertEquals(it, PROCESS_SUCCESS)
            },
            {
                fail()
            })
    }

    @Test
    fun givenGetPictureWhenProcessFailsResultMimeTypeJPEGErrorThenError() {

        val camParameters = IONCameraParameters(
            60,
            1600,
            1600,
            0,
            0,
            allowEdit = false,
            correctOrientation = true,
            saveToPhotoAlbum = false,
            includeMetadata = false,
            latestVersion = false
        )

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

        Mockito.`when`(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)).thenReturn(mFile)

        val byteArray = ByteArray(1)
        Mockito.`when`(Base64.decode("imageInBinary", Base64.NO_WRAP)).thenReturn(byteArray)

        imgHelperMock.bitmapToBase64Success = true
        imgHelperMock.processPicSuccess = false
        fileHelperMock.fileLocationNotNull = true
        fileHelperMock.mimeType = JPEG_MIME_TYPE

        galleryManager.getImage(null, 0, 0, camParameters)
        galleryManager.processResultFromGallery(null, mIntent, camParameters,
            {
                fail()
            },
            {
                assertEquals(it.code, IONError.PROCESS_IMAGE_ERROR.code)
                assertEquals(it.description, IONError.PROCESS_IMAGE_ERROR.description)
            })
    }

    @Test
    fun givenGetPictureWhenProcessResultMimeTypePNGSuccessThenSuccess() {

        val camParameters = IONCameraParameters(
            60,
            1600,
            1600,
            1,
            0,
            allowEdit = false,
            correctOrientation = true,
            saveToPhotoAlbum = false,
            includeMetadata = false,
            latestVersion = false
        )

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

        Mockito.`when`(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)).thenReturn(mFile)

        val byteArray = ByteArray(1)
        Mockito.`when`(Base64.decode("imageInBinary", Base64.NO_WRAP)).thenReturn(byteArray)

        imgHelperMock.processPicSuccess = true
        imgHelperMock.bitmapToBase64Success = true
        fileHelperMock.fileLocationNotNull = true
        fileHelperMock.mimeType = PNG_MIME_TYPE

        galleryManager.getImage(null, 0, 0, camParameters)
        galleryManager.processResultFromGallery(null, mIntent, camParameters,
            {
                assertEquals(it, PROCESS_SUCCESS)
            },
            {
                fail()
            })
    }

    @Test
    fun givenGetPictureWhenProcessFailsResultMimeTypePNGErrorThenError() {

        val camParameters = IONCameraParameters(
            60,
            1600,
            1600,
            1,
            0,
            allowEdit = false,
            correctOrientation = true,
            saveToPhotoAlbum = false,
            includeMetadata = false,
            latestVersion = false
        )

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

        Mockito.`when`(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)).thenReturn(mFile)

        val byteArray = ByteArray(1)
        Mockito.`when`(Base64.decode("imageInBinary", Base64.NO_WRAP)).thenReturn(byteArray)

        imgHelperMock.processPicSuccess = false
        imgHelperMock.bitmapToBase64Success = true
        fileHelperMock.fileLocationNotNull = true
        fileHelperMock.mimeType = PNG_MIME_TYPE

        galleryManager.getImage(null, 0, 0, camParameters)
        galleryManager.processResultFromGallery(null, mIntent, camParameters,
            {
                fail()
            },
            {
                assertEquals(it.code, IONError.PROCESS_IMAGE_ERROR.code)
                assertEquals(it.description, IONError.PROCESS_IMAGE_ERROR.description)
            })
    }

    @Test
    fun givenGetPictureAllowEditWhenProcessResultSuccessThenSuccess() {
        val camParameters = IONCameraParameters(
            60,
            1600,
            1600,
            1,
            0,
            allowEdit = true,
            correctOrientation = true,
            saveToPhotoAlbum = false,
            includeMetadata = false,
            latestVersion = false
        )

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

        Mockito.`when`(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)).thenReturn(mFile)

        val byteArray = ByteArray(1)
        Mockito.`when`(Base64.decode("imageInBinary", Base64.NO_WRAP)).thenReturn(byteArray)

        Mockito.doReturn(FILE_LOCATION).`when`(mIntent).getStringExtra(IONImageEditorActivity.IMAGE_OUTPUT_URI_EXTRAS)

        imgHelperMock.processPicSuccess = true
        imgHelperMock.bitmapToBase64Success = true
        fileHelperMock.fileLocationNotNull = true
        fileHelperMock.mimeType = PNG_MIME_TYPE

        galleryManager.getImage(null, 0, 0, camParameters)
        galleryManager.openCropActivity(null, mUri, 1, 1)
        galleryManager.processResultFromEdit(mockActivity, mIntent, editOptions,
            {
                assertEquals(it, IONImageHelperMock.SAMPLE_BASE64_THUMBNAIL)
            },
            ".camera.provider",
            {
                fail()
            },
            {
                fail()
            })
    }

    @Test
    fun givenGetPictureAllowEditWhenProcessResultErrorThenError() {

        val camParameters = IONCameraParameters(
            60,
            1600,
            1600,
            1,
            0,
            allowEdit = true,
            correctOrientation = true,
            saveToPhotoAlbum = false,
            includeMetadata = false,
            latestVersion = false
        )

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

        Mockito.`when`(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)).thenReturn(mFile)

        val byteArray = ByteArray(1)
        Mockito.`when`(Base64.decode("imageInBinary", Base64.NO_WRAP)).thenReturn(byteArray)

        imgHelperMock.processPicSuccess = true
        imgHelperMock.bitmapToBase64Success = false
        fileHelperMock.fileLocationNotNull = true
        fileHelperMock.mimeType = PNG_MIME_TYPE

        galleryManager.getImage(null, 0, 0, camParameters)
        galleryManager.openCropActivity(null, mUri, 1, 1)
        galleryManager.processResultFromEdit(mockActivity, mIntent, editOptions,
            {
                fail()
            },
            ".camera.provider",
            {
                fail()
            },
            {
                assertEquals(it.code, IONError.EDIT_IMAGE_ERROR.code)
                assertEquals(it.description, IONError.EDIT_IMAGE_ERROR.description)
            })
    }

}