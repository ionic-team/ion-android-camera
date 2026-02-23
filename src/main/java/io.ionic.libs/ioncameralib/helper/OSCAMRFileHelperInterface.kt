package io.ionic.libs.ioncameralib.helper

import android.app.Activity
import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import java.io.File
import java.io.InputStream

interface OSCAMRFileHelperInterface {
    fun getRealPath(uri: Uri?, context: Context?): String?
    fun getRealPath(uriString: String, context: Context): String?
    fun getUriFromString(uriString: String): Uri
    fun getInputStreamFromUriString(uriString: String, context: Context?): InputStream?
    fun stripFileProtocol(uriString: String): String?
    fun getMimeType(uriString: String, activity: Activity?): String?
    fun writeUncompressedImage(activity: Activity?, fis: InputStream?, dest: Uri?)
    fun refreshGallery(activity: Activity?, contentUri: Uri?)
    fun createCaptureFile(activity: Activity?, fileName: String): File
    fun getUriForFile(activity: Activity?, authority: String, file: File): Uri?
    fun whichContentStore(): Uri?
    fun getUriString(uri: Uri?): String
    fun getTempDirectoryPath(activity: Activity?): String?
    fun saveImage(path: String, bitmap: Bitmap)
    fun deleteFileFromCache(context: Context?, cacheFileName: String)
    fun saveStringSharedPreferences(activity: Activity, key: String, value: String)
    fun getMimeType(url: String): String?
    fun getAbsoluteCachedFilePath(activity: Activity, fileName: String): String
    fun fileExists(file: File): Boolean
    fun getFileSizeFromUri(activity: Activity, uri: Uri): Long?
    fun getFileExtension(filePath: String): String?
    fun getFileCreationDate(file: File): String
    fun storeFileNameInPrefs(fileName: String, context: Context)
    fun getCachedFileNames(context: Context) : Map<String, *>?
    fun removeFileNameFromPrefs(fileName: String, context: Context)
    fun getImagePathFromInputStreamUri(activity: Activity, uri: Uri): String?
}