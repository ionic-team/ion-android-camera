package io.ionic.libs.ioncameralib.view

import android.content.ClipData
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import io.ionic.libs.ioncameralib.model.IONMediaType


class IONOpenPhotoPickerActivity : ComponentActivity() {

    companion object {
        private const val ALLOW_MULTIPLE = "allowMultiple"
        private const val MEDIA_TYPE = "mediaType"
        private const val MEDIA_LIMIT = "limit"
    }

    // Registers a photo picker activity launcher in single-select mode.
    private fun launchSinglePicker(mediaType: ActivityResultContracts.PickVisualMedia.VisualMediaType) {
        val launcher = registerForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
            if (uri != null) {
                Log.d("PhotoPicker", "Selected URI: $uri")
                // send URI inside intent
                val intentWithUri = Intent().apply {
                    data = uri
                    flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
                }
                makeUriPermissionPersistable(uri)
                setResult(RESULT_OK, intentWithUri)
                finish()
            } else {
                Log.d("PhotoPicker", "No media selected")
                setResult(RESULT_CANCELED)
                finish()
            }
        }

        launcher.launch(PickVisualMediaRequest(mediaType))
    }

    private fun launchMultiplePicker(mediaType: ActivityResultContracts.PickVisualMedia.VisualMediaType, limit: Int) {

        var safeLimit = limit
        // Ensure limit does not exceed system max limit
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val maxLimit = MediaStore.getPickImagesMaxLimit()
            if (safeLimit > maxLimit) {
                safeLimit = maxLimit
            }
        }

        val contract =
            if (safeLimit > 1)
                ActivityResultContracts.PickMultipleVisualMedia(safeLimit)
            else
                ActivityResultContracts.PickMultipleVisualMedia()

        val launcher = registerForActivityResult(contract) { uriList ->
            if (uriList.isNotEmpty()) {
                // Safety check in case system ignores limit
                val safeList = if (safeLimit > 0) uriList.take(safeLimit) else uriList
                Log.d("PhotoPicker", "Selected URIs: $safeList")
                val intentWithUri = Intent()
                if (safeList.size > 1) { // multiple selection
                    val clipData = ClipData.newUri(contentResolver, "Selected Media", safeList[0])
                    makeUriPermissionPersistable(safeList[0])
                    for (i in 1 until safeList.size) {
                        clipData.addItem(ClipData.Item(safeList[i]))
                        makeUriPermissionPersistable(safeList[i])
                    }
                    intentWithUri.clipData = clipData
                } else { // single selection
                    intentWithUri.data = safeList[0]
                    makeUriPermissionPersistable(safeList[0])
                }
                intentWithUri.flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
                setResult(RESULT_OK, intentWithUri)
                finish()
            } else {
                Log.d("PhotoPicker", "No media selected")
                setResult(RESULT_CANCELED)
                finish()
            }
        }

        launcher.launch(PickVisualMediaRequest(mediaType))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val allowMultiple = intent.getBooleanExtra(ALLOW_MULTIPLE, false)
        val mediaTypeFromIntent = intent.getStringExtra(MEDIA_TYPE)
        val limit = intent.getIntExtra(MEDIA_LIMIT, 0)

        val mediaType = when (mediaTypeFromIntent) {
            IONMediaType.PICTURE.mimeType -> ActivityResultContracts.PickVisualMedia.ImageOnly
            IONMediaType.VIDEO.mimeType -> ActivityResultContracts.PickVisualMedia.VideoOnly
            else -> ActivityResultContracts.PickVisualMedia.ImageAndVideo
        }

        if (allowMultiple) {
            launchMultiplePicker(mediaType, limit)
        } else {
            launchSinglePicker(mediaType)
        }
    }

    private fun makeUriPermissionPersistable(uri: Uri) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) { // only for Android >= 11
            applicationContext.contentResolver.takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
    }

}