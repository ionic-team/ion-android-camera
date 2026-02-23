package io.ionic.libs.ioncameralib.view

import android.net.Uri
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.enableEdgeToEdge
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding

class ImageEditorActivity : ComponentActivity() {

    private val editorView by lazy { findViewById<ImageEditorView>(getResourceId("id/imageEditorView")) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(getResourceId("layout/activity_image_editor"))

        ViewCompat.setOnApplyWindowInsetsListener(editorView) { _, insets ->
            val systemBarInsets = insets.getInsets(
                WindowInsetsCompat.Type.systemBars() or WindowInsetsCompat.Type.displayCutout()
            )
            editorView.updatePadding(
                left = systemBarInsets.left,
                top = systemBarInsets.top,
                right = systemBarInsets.right,
                bottom = systemBarInsets.bottom
            )
            insets
        }

        intent.extras?.let {
            val inputUri = Uri.parse(it.getString(IMAGE_INPUT_URI_EXTRAS))
            val outputUri = Uri.parse(it.getString(IMAGE_OUTPUT_URI_EXTRAS))
            editorView.setInputImageUri(inputUri)
            editorView.setOutputImageUri(outputUri)
        }

        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.dark(window.statusBarColor),
            navigationBarStyle = SystemBarStyle.dark(window.navigationBarColor)
        )
    }

    private fun getResourceId(typeAndName: String): Int {
        return application.resources.getIdentifier(typeAndName, null, application.packageName)
    }

    companion object {
        const val IMAGE_INPUT_URI_EXTRAS = "IMAGE_EDITOR_IN_URI_EXTRAS"
        const val IMAGE_OUTPUT_URI_EXTRAS = "IMAGE_EDITOR_OUT_URI_EXTRAS"
    }
}