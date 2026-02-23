package io.ionic.libs.ioncameralib.helper

import android.media.ExifInterface
import android.net.Uri

interface OSCAMRExifHelperInterface {

    fun createInFile(filePath: String?)
    fun createOutFile(filePath: String?)
    fun createOutFileFromUri(fileUri: Uri?)
    fun readExifData()
    fun writeExifData()
    fun getOrientation(): Int
    fun resetOrientation()
    fun getOrientationFromExif(exif: ExifInterface): Int
}