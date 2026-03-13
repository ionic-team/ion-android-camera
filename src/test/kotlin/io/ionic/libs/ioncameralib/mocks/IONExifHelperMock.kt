package  io.ionic.libs.ioncameralib.mocks

import android.media.ExifInterface
import android.net.Uri
import io.ionic.libs.ioncameralib.helper.IONExifHelperInterface

class IONExifHelperMock: IONExifHelperInterface {

    var orientationNormal = false
    var testOrientation = ExifInterface.ORIENTATION_ROTATE_90

    override fun createInFile(filePath: String?) {
        // do nothing
    }

    override fun createOutFile(filePath: String?) {
        // do nothing
    }

    override fun createOutFileFromUri(fileUri: Uri?) {
        // do nothing
    }

    override fun readExifData() {
        // do nothing
    }

    override fun writeExifData() {
        // do nothing
    }

    override fun getOrientation(): Int {
        if(orientationNormal){
            return ExifInterface.ORIENTATION_NORMAL
        }
        return ExifInterface.ORIENTATION_FLIP_HORIZONTAL
    }

    override fun resetOrientation() {
        // do nothing
    }

    override fun getOrientationFromExif(exif: ExifInterface): Int {
        return testOrientation
    }
}