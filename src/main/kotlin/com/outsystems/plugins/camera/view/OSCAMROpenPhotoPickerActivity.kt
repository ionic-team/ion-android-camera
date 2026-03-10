package com.outsystems.plugins.camera.view

import android.content.ClipData
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import com.outsystems.plugins.camera.model.OSCAMRMediaType

class OSCAMROpenPhotoPickerActivity : ComponentActivity() {

    companion object {
        private const val ALLOW_MULTIPLE = "allowMultiple"
        private const val MEDIA_TYPE = "mediaType"
    }

    // Registers a photo picker activity launcher in single-select mode.
    private val pickMediaSingle = registerForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
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

    // Registers a photo picker activity launcher in multiple-select mode.
    private val pickMediaMultiple = registerForActivityResult(ActivityResultContracts.PickMultipleVisualMedia()) { uriList ->
        if (uriList.isNotEmpty()) {
            Log.d("PhotoPicker", "Selected URIs: $uriList")

            // send URI inside intent
            val intentWithUri = Intent()
            if (uriList.size > 1) { // multiple selection
                // Create a ClipData object and add each Uri to it
                val clipData = ClipData.newUri(contentResolver, "Selected Media", uriList[0])
                makeUriPermissionPersistable(uriList[0])
                for (i in 1 until uriList.size) {
                    clipData.addItem(ClipData.Item(uriList[i]))
                    makeUriPermissionPersistable(uriList[i])
                }
                intentWithUri.clipData = clipData
            } else { // single selection
                intentWithUri.data = uriList[0]
                makeUriPermissionPersistable(uriList[0])
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val allowMultiple = intent.getBooleanExtra(ALLOW_MULTIPLE, false)
        val mediaTypeFromIntent = intent.getStringExtra(MEDIA_TYPE)

        val mediaType = when (mediaTypeFromIntent) {
            OSCAMRMediaType.IMAGE.mimeType -> ActivityResultContracts.PickVisualMedia.ImageOnly
            OSCAMRMediaType.VIDEO.mimeType -> ActivityResultContracts.PickVisualMedia.VideoOnly
            else -> ActivityResultContracts.PickVisualMedia.ImageAndVideo
        }

        if (allowMultiple) {
            pickMediaMultiple.launch(PickVisualMediaRequest(mediaType))
        } else {
            pickMediaSingle.launch(PickVisualMediaRequest(mediaType))
        }
    }

    private fun makeUriPermissionPersistable(uri: Uri) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) { // only for Android >= 11
            applicationContext.contentResolver.takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
    }

}