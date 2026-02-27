/*
       Licensed to the Apache Software Foundation (ASF) under one
       or more contributor license agreements.  See the NOTICE file
       distributed with this work for additional information
       regarding copyright ownership.  The ASF licenses this file
       to you under the Apache License, Version 2.0 (the
       "License"); you may not use this file except in compliance
       with the License.  You may obtain a copy of the License at
         http://www.apache.org/licenses/LICENSE-2.0
       Unless required by applicable law or agreed to in writing,
       software distributed under the License is distributed on an
       "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
       KIND, either express or implied.  See the License for the
       specific language governing permissions and limitations
       under the License.
 */

package io.ionic.libs.ioncameralib.helper

import android.app.Activity
import android.content.ContentUris
import android.content.Context
import android.content.Intent
import android.database.Cursor
import android.graphics.Bitmap
import android.net.Uri
import android.os.Environment
import android.provider.DocumentsContract
import android.provider.MediaStore
import android.provider.OpenableColumns
import android.util.Log
import android.webkit.MimeTypeMap
import androidx.core.content.FileProvider
import java.io.*
import java.nio.file.Files
import java.nio.file.attribute.BasicFileAttributes
import java.util.*

class OSCAMRFileHelper: OSCAMRFileHelperInterface {

    companion object {
        private const val LOG_TAG = "OSCAMRFileHelper"
        private const val EXTERNAL_STORAGE = "com.android.externalstorage.documents"
        private const val DOWNLOADS_DOCUMENTS = "com.android.providers.downloads.documents"
        private const val PROVIDERS_MEDIA = "com.android.providers.media.documents"
        private const val STORE = "CameraStore"
        private const val CACHED_VIDEOS_STORE = "CachedVideosStore"
    }

    /**
     * Returns the real path of the given URI string.
     * If the given URI string represents a content:// URI, the real path is retrieved from the media store.
     *
     * @param uri the URI of the audio/image/video
     * @param context the current application context
     * @return the full path to the file
     */
    override fun getRealPath(uri: Uri?, context: Context?): String? {
        if(context != null && uri != null){
            return getRealPathFromURI(context, uri)
        }
        return null
    }

    /**
     * Returns the real path of the given URI.
     * If the given URI is a content:// URI, the real path is retrieved from the media store.
     *
     * @param uriString the URI string of the audio/image/video
     * @param context the current application context
     * @return the full path to the file
     */
    override fun getRealPath(uriString: String, context: Context): String? {
        return getRealPath(Uri.parse(uriString), context)
    }

    override fun getUriFromString(uriString: String): Uri {
        return Uri.parse(uriString)
    }

    private fun getRealPathFromURI(context: Context, uri: Uri): String? {

            // DocumentProvider
            if (DocumentsContract.isDocumentUri(context, uri)) {

                // ExternalStorageProvider
                if (isExternalStorageDocument(uri)) {
                    val docId = DocumentsContract.getDocumentId(uri)
                    val split = docId.split(":").toTypedArray()
                    val type = split[0]
                    if ("primary".equals(type, ignoreCase = true)) {
                        return Environment.getExternalStorageDirectory().toString() + "/" + split[1]
                    }

                    // TODO handle non-primary volumes
                } else if (isDownloadsDocument(uri)) {
                    val id = DocumentsContract.getDocumentId(uri)
                    return if (id != null && id.isNotEmpty()) {
                        if (id.startsWith("raw:")) {
                            return id.replaceFirst("raw:".toRegex(), "")
                        }
                        try {
                            val contentUri = ContentUris.withAppendedId(
                                Uri.parse("content://downloads/public_downloads"),
                                java.lang.Long.valueOf(id)
                            )
                            getDataColumn(
                                context,
                                contentUri,
                                null,
                                null
                            )
                        } catch (e: NumberFormatException) {
                            null
                        }
                    } else {
                        null
                    }
                } else if (isMediaDocument(uri)) {
                    val docId = DocumentsContract.getDocumentId(uri)
                    val split = docId.split(":").toTypedArray()
                    val type = split[0]
                    var contentUri: Uri? = null
                    if ("image" == type) {
                        contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI
                    } else if ("video" == type) {
                        contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI
                    } else if ("audio" == type) {
                        contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
                    }
                    val selection = "_id=?"
                    val selectionArgs = arrayOf(
                        split[1]
                    )
                    return getDataColumn(
                        context,
                        contentUri,
                        selection,
                        selectionArgs
                    )
                }
            } else if ("content".equals(uri.scheme, ignoreCase = true)) {

                // Return the remote address
                return getDataColumn(
                    context,
                    uri,
                    null,
                    null
                )
            } else if ("file".equals(uri.scheme, ignoreCase = true)) {
                return uri.path
            }
            return null
        }

    /**
     * Returns an input stream based on given URI string.
     *
     * @param uriString the URI string from which to obtain the input stream
     * @param context the current application context
     * @return an input stream into the data at the given URI or null if given an invalid URI string
     * @throws IOException
     */
    @Throws(IOException::class)
    override fun getInputStreamFromUriString(uriString: String, context: Context?): InputStream? {

        if(context == null){
            return null
        }

        var uriString = uriString
        var returnValue: InputStream? = null
        if (uriString.startsWith("content")) {
            val uri = Uri.parse(uriString)
            returnValue = context.contentResolver.openInputStream(uri)
        } else if (uriString.startsWith("file://")) {
            val question = uriString.indexOf("?")
            if (question > -1) {
                uriString = uriString.substring(0, question)
            }
            if (uriString.startsWith("file:///android_asset/")) {
                val uri = Uri.parse(uriString)
                val relativePath = uri.path?.substring(15)
                returnValue = relativePath?.let { context.assets.open(it) }
            } else {
                // might still be content so try that first
                returnValue = try {
                    context.contentResolver.openInputStream(Uri.parse(uriString))
                } catch (e: Exception) {
                    null
                }
                if (returnValue == null) {
                    returnValue = FileInputStream(
                        getRealPath(
                            uriString,
                            context
                        )
                    )
                }
            }
        } else {
            returnValue = FileInputStream(uriString)
        }
        return returnValue
    }

    /**
     * Removes the "file://" prefix from the given URI string, if applicable.
     * If the given URI string doesn't have a "file://" prefix, it is returned unchanged.
     *
     * @param uriString the URI string to operate on
     * @return a path without the "file://" prefix
     */
    override fun stripFileProtocol(uriString: String): String? {
        var uriString = uriString
        if (uriString.startsWith("file://")) {
            uriString = uriString.substring(7)
        }
        return uriString
    }

    private fun getMimeTypeForExtension(path: String): String? {
        var extension = path
        val lastDot = extension.lastIndexOf('.')
        if (lastDot != -1) {
            extension = extension.substring(lastDot + 1)
        }
        // Convert the URI string to lower case to ensure compatibility with MimeTypeMap (see CB-2185).
        extension = extension.lowercase(Locale.getDefault())
        return if (extension == "3ga") {
            "audio/3gpp"
        } else MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension)
    }

    /**
     * Returns the mime type of the data specified by the given URI string.
     *
     * @param uriString the URI string of the data
     * @return the mime type of the specified data
     */
    override fun getMimeType(uriString: String, activity: Activity?): String? {
        var mimeType: String? = null
        val uri = Uri.parse(uriString)
        mimeType = if (uriString.startsWith("content://")) {
            activity?.contentResolver?.getType(uri)
        } else {
            uri.path?.let { getMimeTypeForExtension(it) }
        }
        return mimeType
    }

    /**
     * Write an input stream to local disk
     *
     * @param fis - The InputStream to write
     * @param dest - Destination on disk to write to
     * @throws FileNotFoundException
     * @throws IOException
     */
    @Throws(FileNotFoundException::class, IOException::class)
    override fun writeUncompressedImage(activity: Activity?, fis: InputStream?, dest: Uri?) {
        var os: OutputStream? = null
        try {
            os = dest?.let { activity?.contentResolver?.openOutputStream(it) }
            val buffer = ByteArray(4096)
            var len: Int
            while (fis!!.read(buffer).also { len = it } != -1) {
                os?.write(buffer, 0, len)
            }
            os?.flush()
        } finally {
            if (os != null) {
                try {
                    os.close()
                } catch (e: IOException) {
                    Log.d(
                        LOG_TAG,
                        "Exception while closing output stream."
                    )
                }
            }
            if (fis != null) {
                try {
                    fis.close()
                } catch (e: IOException) {
                    Log.d(
                        LOG_TAG,
                        "Exception while closing file input stream."
                    )
                }
            }
        }
    }

    override fun refreshGallery(activity: Activity?, contentUri: Uri?) {
        val mediaScanIntent = Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE)
        // Starting from Android Q, working with the ACTION_MEDIA_SCANNER_SCAN_FILE intent is deprecated
        mediaScanIntent.data = contentUri
        activity?.sendBroadcast(mediaScanIntent)
    }

    override fun createCaptureFile(activity: Activity?, fileName: String): File {
        return File(activity?.let { getTempDirectoryPath(it) }, fileName)
    }

    override fun getUriForFile(activity: Activity?, authority: String, file: File): Uri? {
        return activity?.let {
            FileProvider.getUriForFile(
                it,
                authority,
                file
            )
        }
    }

    /**
     * Determine if we are storing the images in internal or external storage
     *
     * @return Uri
     */
    override fun whichContentStore(): Uri? {
        return if (Environment.getExternalStorageState() == Environment.MEDIA_MOUNTED) {
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI
        } else {
            MediaStore.Images.Media.INTERNAL_CONTENT_URI
        }
    }

    override fun getUriString(uri: Uri?): String {
        return uri.toString()
    }

    override fun getTempDirectoryPath(activity: Activity?): String? {
        activity?.let {
            val cache: File = it.cacheDir
            // Create the cache directory if it doesn't exist
            cache.mkdirs()
            return cache.absolutePath
        }
        return null
    }

    override fun saveImage(path: String, bitmap: Bitmap) {
        val file = File(path)
        var fOut = FileOutputStream(file)

        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fOut)

        fOut.flush()
        fOut.close()
    }

    override fun deleteFileFromCache(context: Context?, cacheFileName: String) {
        val cacheFile = File(context?.cacheDir?.absolutePath + "/" + cacheFileName)
        if (cacheFile.exists()) {
            cacheFile.delete()
        }
    }

    override fun saveStringSharedPreferences(activity: Activity, key: String, value: String) {
        activity.getSharedPreferences(STORE, Context.MODE_PRIVATE)
            .edit()
            .putString(key, value)
            .apply()
    }

    override fun getAbsoluteCachedFilePath(activity: Activity, fileName: String): String {
        val cacheDir: String = activity.cacheDir.absolutePath
        val videoFile = File(cacheDir, fileName)
        return videoFile.absolutePath
    }

    override fun getMimeType(url: String): String? {
        var type: String? = null
        val extension = MimeTypeMap.getFileExtensionFromUrl(url)
        if (extension != null) {
            type = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension)
        }
        return type
    }

    override fun fileExists(file: File): Boolean {
        return file.exists()
    }

    override fun getFileSizeFromUri(activity: Activity, uri: Uri): Long? {
        activity.applicationContext.contentResolver.query(uri, null, null, null, null)
            ?.use { cursor ->
                val sizeIndex = cursor.getColumnIndex(OpenableColumns.SIZE)
                if (cursor.moveToFirst()) {
                    return cursor.getLong(sizeIndex)
                }
            }
        return null
    }

    override fun getFileExtension(filePath: String): String? {
        return MimeTypeMap.getFileExtensionFromUrl(filePath)
    }

    override fun getFileCreationDate(file: File): String {
        val attr = Files.readAttributes(file.toPath(), BasicFileAttributes::class.java)
        return attr.creationTime().toString()
    }

    override fun storeFileNameInPrefs(fileName: String, context: Context) {
        context.getSharedPreferences(CACHED_VIDEOS_STORE, Context.MODE_PRIVATE)
            .edit()
            .putString(fileName, fileName)
            .apply()
    }

    override fun getCachedFileNames(context: Context) : Map<String, *>? {
        return context.getSharedPreferences(CACHED_VIDEOS_STORE, Context.MODE_PRIVATE).all
    }

    override fun removeFileNameFromPrefs(fileName: String, context: Context) {
        val cacheFile = File(context?.cacheDir?.absolutePath + "/" + fileName)
        if (!cacheFile.exists()) {
            context.getSharedPreferences(CACHED_VIDEOS_STORE, Context.MODE_PRIVATE).edit().remove(fileName).apply()
        }
    }

    override fun getImagePathFromInputStreamUri(activity: Activity, uri: Uri): String? {
        var inputStream: InputStream? = null
        var filePath: String? = null
        if (uri.authority != null) {
            try {
                inputStream = activity.contentResolver.openInputStream(uri) ?: return null
                val mimeType = getMimeType(uri.toString(), activity)
                val photoFile = downloadCacheFileFromInputStream(activity, inputStream, mimeType)
                filePath = photoFile?.path
            } catch (e: Exception) {
                // does nothing. returns null and continues iterating.
            } finally {
                try {
                    inputStream?.close()
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }
        }
        return filePath
    }

    /**
     * Get the value of the data column for this Uri. This is useful for
     * MediaStore Uris, and other file-based ContentProviders.
     *
     * @param context The context.
     * @param uri The Uri to query.
     * @param selection (Optional) Filter used in the query.
     * @param selectionArgs (Optional) Selection arguments used in the query.
     * @return The value of the _data column, which is typically a file path.
     */
    private fun getDataColumn(
        context: Context, uri: Uri?, selection: String?,
        selectionArgs: Array<String>?
    ): String? {
        var cursor: Cursor? = null
        val column = "_data"
        val projection = arrayOf(
            column
        )
        try {
            cursor = context
                .contentResolver
                .query(
                    uri!!,
                    projection,
                    selection,
                    selectionArgs,
                    null)
            if (cursor != null && cursor.moveToFirst()) {
                val columnIndex = cursor.getColumnIndexOrThrow(column)
                return cursor.getString(columnIndex)
            }
        } catch (e: Exception) {
            return null
        } finally {
            cursor?.close()
        }
        return null
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is ExternalStorageProvider.
     */
    private fun isExternalStorageDocument(uri: Uri): Boolean {
        return EXTERNAL_STORAGE == uri.authority
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is DownloadsProvider.
     */
    private fun isDownloadsDocument(uri: Uri): Boolean {
        return DOWNLOADS_DOCUMENTS == uri.authority
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is MediaProvider.
     */
    private fun isMediaDocument(uri: Uri): Boolean {
        return PROVIDERS_MEDIA == uri.authority
    }

    private fun downloadCacheFileFromInputStream(context: Activity, inputStream: InputStream, mimeType: String?): File? {
        var read: Int
        val buffer = ByteArray(8 * 1024)
        val extension = MimeTypeMap.getSingleton().getExtensionFromMimeType(mimeType) ?: "dat"
        val targetFile = createCaptureFile(context, "file.${extension}")
        val outputStream: OutputStream = FileOutputStream(targetFile)
        while (inputStream.read(buffer).also { read = it } != -1) {
            outputStream.write(buffer, 0, read)
        }
        outputStream.flush()
        try {
            outputStream.close()
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return targetFile
    }
}