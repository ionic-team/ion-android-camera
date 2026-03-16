package io.ionic.libs.ioncameralib.mocks

import android.app.Activity
import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import io.ionic.libs.ioncameralib.helper.IONCAMRFileHelperInterface
import org.mockito.Mockito
import java.io.File
import java.io.InputStream

class IONCAMRFileHelperMock: IONCAMRFileHelperInterface {

    var getUriResult = GET_URI_SUCCESS
    var fileLocationNotNull = false
    var mimeType : String? = "notAMimeType"
    var deleteCallsCount = 0
    var fileExists = false
    lateinit var mUri: Uri
    val mockInputStream = Mockito.mock(InputStream::class.java)
    var fileSize = 23456L
    var fileExtension = "mp4"
    var creationDateTime = "2023-03-30T09:01:26Z"
    var isUriNull = false
    var isFileStreamNull = false

    companion object {
        const val GET_URI_SUCCESS = 0
        const val GET_URI_EMPTY = 1
        const val GET_URI_EXCEPTION = 2
        const val FILE_LOCATION = "file://content/storage/emulated/sampleFileLocation"
    }

    override fun getRealPath(uri: Uri?, context: Context?): String? {
        return if (fileLocationNotNull) FILE_LOCATION else null
    }

    override fun getRealPath(uriString: String, context: Context): String? {
        return uriString
    }

    override fun getUriFromString(uriString: String): Uri {
        return mUri
    }

    override fun getInputStreamFromUriString(uriString: String, context: Context?): InputStream? {
        return if (isFileStreamNull) {
            null
        } else {
            mockInputStream
        }
    }

    override fun stripFileProtocol(uriString: String): String? {
        return uriString
    }

    override fun getMimeType(uriString: String, activity: Activity?): String? {
        return mimeType
    }

    override fun writeUncompressedImage(activity: Activity?, fis: InputStream?, dest: Uri?) {
        // do nothing
    }

    override fun refreshGallery(activity: Activity?, contentUri: Uri?) {
        // do nothing
    }

    override fun createCaptureFile(activity: Activity?, fileName: String): File {
        return File("somePath", "myFile")
    }

    override fun getUriForFile(activity: Activity?, authority: String, file: File): Uri? {
        return if (isUriNull) {
            null
        } else {
            Uri.parse("uriString")
        }
    }

    override fun whichContentStore(): Uri? {
        return null
    }

    override fun getUriString(uri: Uri?): String {
        return when (getUriResult){
            GET_URI_EMPTY -> ""
            GET_URI_EXCEPTION -> throw Exception()
            else -> "file://myUriString"
        }

    }

    override fun getTempDirectoryPath(activity: Activity?): String? {
        return "myPath"
    }

    override fun saveImage(path: String, bitmap: Bitmap) {
        // do nothing
    }

    override fun deleteFileFromCache(context: Context?, cacheFileName: String) {
        deleteCallsCount++
    }

    override fun saveStringSharedPreferences(activity: Activity, key: String, value: String) {
        // do nothing
    }

    override fun getAbsoluteCachedFilePath(activity: Activity, fileName: String): String {
        return "videoUri"
    }

    override fun fileExists(file: File): Boolean {
        return fileExists
    }

    override fun getFileSizeFromUri(activity: Activity, uri: Uri): Long? {
        return fileSize
    }

    override fun getFileExtension(filePath: String): String? {
        return fileExtension
    }

    override fun getFileCreationDate(file: File): String {
        return creationDateTime
    }

    override fun storeFileNameInPrefs(fileName: String, context: Context) {
        // do nothing
    }

    override fun getCachedFileNames(context: Context): Map<String, *>? {
        return mapOf(Pair("myFile", "myFile"), Pair("myOtherFile", "myOtherFile"))
    }

    override fun removeFileNameFromPrefs(fileName: String, context: Context) {
        // do nothing
    }

    override fun getImagePathFromInputStreamUri(activity: Activity, uri: Uri): String? {
        return FILE_LOCATION
    }

    override fun getMimeType(url: String): String? {
        return mimeType
    }
}