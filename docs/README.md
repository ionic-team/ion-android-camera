# IONCameraLib

A modern, flexible and feature-rich camera and media library for Android apps. Includes advanced photo, video, and gallery management with easy integration for Kotlin and Android projects.

## Installation

### Gradle

Add the following to your module's `build.gradle` file:

```groovy
dependencies {
    implementation 'io.ionic.libs:ioncameralib:1.0.0'
}
```

Make sure you have the appropriate Maven repository declared in your project-level `build.gradle` or `settings.gradle`:

```groovy
repositories {
    google()
    mavenCentral()
}
```

## Usage

### Basic Camera Operations

```kotlin
import io.ionic.libs.ioncameralib.manager.IONCAMRCameraManager
import io.ionic.libs.ioncameralib.manager.IONCAMRGalleryManager
import io.ionic.libs.ioncameralib.manager.IONCAMREditManager
import io.ionic.libs.ioncameralib.manager.IONCAMRVideoManager
import io.ionic.libs.ioncameralib.model.IONCAMRCameraParameters
import io.ionic.libs.ioncameralib.model.IONCAMREditParameters
import io.ionic.libs.ioncameralib.model.IONCAMRVideoParameters
import io.ionic.libs.ioncameralib.model.IONCAMRMediaType

class MainActivity : AppCompatActivity() {

    private lateinit var cameraManager: IONCAMRCameraManager
    private lateinit var galleryManager: IONCAMRGalleryManager
    private lateinit var editManager: IONCAMREditManager
    private lateinit var videoManager: IONCAMRVideoManager

    // Store parameters to use in launcher callbacks
    private var cameraParameters: IONCAMRCameraParameters? = null
    private var videoParameters: IONCAMRVideoParameters? = null
    private var editParameters: IONCAMREditParameters? = null

    // Launchers must be registered at class level (before the activity starts)
    private val cameraLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val params = cameraParameters ?: return@registerForActivityResult
        cameraManager.processResultFromCamera(
            activity = this,
            intent = result.data,
            camParameters = params,
            onMediaResult = { mediaResult -> println("Photo captured: ${mediaResult.uri}") },
            onError = { error -> println("Error: ${error.description}") }
        )
    }

    private val videoLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val params = videoParameters ?: return@registerForActivityResult
        lifecycleScope.launch {
            cameraManager.processResultFromVideo(
                activity = this@MainActivity,
                uri = result.data?.data,
                fromGallery = params.saveToGallery,
                isPersistent = params.isPersistent,
                includeMetadata = params.includeMetadata,
                onSuccess = { mediaResult -> println("Video captured: ${mediaResult.uri}") },
                onError = { error -> println("Error: ${error.description}") }
            )
        }
    }

    private val galleryLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        lifecycleScope.launch {
            galleryManager.onChooseFromGalleryResult(
                activity = this@MainActivity,
                resultCode = result.resultCode,
                intent = result.data,
                includeMetadata = false,
                onSuccess = { results -> println("Selected ${results.size} item(s)") },
                onError = { error -> println("Error: ${error.description}") }
            )
        }
    }

    private val editLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val params = editParameters ?: IONCAMREditParameters(
            editURI = null, fromUri = false, saveToGallery = false, includeMetadata = false
        )
        editManager.processResultFromEdit(
            activity = this,
            intent = result.data,
            editParameters = params,
            onImage = { base64 -> println("Edited image (base64): $base64") },
            onMediaResult = { mediaResult -> println("Edited image URI: ${mediaResult.uri}") },
            onError = { error -> println("Error: ${error.description}") }
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setupManagers()
    }

    private fun setupManagers() {
        val exifHelper = IONCAMRExifHelper()
        val fileHelper = IONCAMRFileHelper()
        val mediaHelper = IONCAMRMediaHelper()
        val imageHelper = IONCAMRImageHelper()

        cameraManager = IONCAMRCameraManager(
            applicationId = packageName,
            exif = exifHelper,
            fileHelper = fileHelper,
            mediaHelper = mediaHelper,
            imageHelper = imageHelper
        )
        galleryManager = IONCAMRGalleryManager(
            exif = exifHelper,
            fileHelper = fileHelper,
            mediaHelper = mediaHelper,
            imageHelper = imageHelper
        )
        editManager = IONCAMREditManager(
            applicationId = packageName,
            exif = exifHelper,
            fileHelper = fileHelper,
            mediaHelper = mediaHelper,
            imageHelper = imageHelper
        )
        videoManager = IONCAMRVideoManager(fileHelper = fileHelper)
    }

    // Take a photo
    fun capturePhoto() {
        cameraParameters = IONCAMRCameraParameters(
            mQuality = 80,
            targetWidth = 1024,
            targetHeight = 768,
            encodingType = 0, // 0 = JPEG, 1 = PNG
            mediaType = 0,
            allowEdit = true,
            correctOrientation = true,
            saveToPhotoAlbum = false,
            includeMetadata = false
        )
        cameraManager.takePhoto(
            activity = this,
            encodingType = cameraParameters!!.encodingType,
            launcher = cameraLauncher
        )
    }

    // Record a video
    fun recordVideo() {
        videoParameters = IONCAMRVideoParameters(
            saveToGallery = false,
            includeMetadata = false,
            isPersistent = true
        )
        cameraManager.recordVideo(
            activity = this,
            saveVideoToGallery = videoParameters!!.saveToGallery,
            launcher = videoLauncher,
            onError = { error -> println("Error: ${error.description}") }
        )
    }

    // Choose from gallery
    fun chooseFromGallery() {
        galleryManager.chooseFromGallery(
            activity = this,
            mediaType = IONCAMRMediaType.PICTURE,
            allowMultiSelect = false,
            limit = 1,
            launcher = galleryLauncher
        )
    }

    // Edit an image from URI
    fun editURIPhoto(filePath: String) {
        editParameters = IONCAMREditParameters(
            editURI = filePath,
            fromUri = true,
            saveToGallery = false,
            includeMetadata = false
        )
        editManager.editURIPicture(
            activity = this,
            pictureFilePath = filePath,
            launcher = editLauncher,
            onError = { error -> println("Error: ${error.description}") }
        )
    }

    // Edit an image from Base64 string
    fun editBase64Image(base64Image: String) {
        editParameters = null
        editManager.editImage(
            activity = this,
            image = base64Image,
            launcher = editLauncher
        )
    }

    // Play a video
    fun playVideo(videoUri: String) {
        videoManager.playVideo(
            activity = this,
            videoUri = videoUri,
            onSuccess = { println("Video playback started") },
            onError = { error -> println("Video playback error: ${error.description}") }
        )
    }

    // Clean temporary video files
    fun cleanupTemporaryFiles() {
        cameraManager.deleteVideoFilesFromCache(this)
    }

    override fun onDestroy() {
        super.onDestroy()
        cameraManager.onDestroy(this)
    }
}
```

### Advanced Usage Examples

```kotlin
// Multiple selection from gallery (video or photo)
fun selectMultipleMedia() {
    galleryManager.chooseFromGallery(
        activity = this,
        mediaType = IONCAMRMediaType.ALL,
        allowMultiSelect = true,
        limit = 10,
        launcher = galleryLauncher
    )
}

// High quality photo with metadata
fun takeHighQualityPhoto() {
    cameraParameters = IONCAMRCameraParameters(
        mQuality = 100,
        targetWidth = 2048,
        targetHeight = 1536,
        encodingType = 0, // JPEG
        mediaType = 0,
        allowEdit = false,
        correctOrientation = true,
        saveToPhotoAlbum = true,
        includeMetadata = true
    )
    cameraManager.takePhoto(
        activity = this,
        encodingType = cameraParameters!!.encodingType,
        launcher = cameraLauncher
    )
}

// Video recording saved to gallery
fun recordVideoToGallery() {
    videoParameters = IONCAMRVideoParameters(
        saveToGallery = true,
        includeMetadata = false,
        isPersistent = true
    )
    cameraManager.recordVideo(
        activity = this,
        saveVideoToGallery = videoParameters!!.saveToGallery,
        launcher = videoLauncher,
        onError = { error -> println("Error: ${error.description}") }
    )
}
```

## Key Components

### Manager Classes

- **`IONCAMRCameraManager`**: Handles photo capture and video recording via the device camera
- **`IONCAMRGalleryManager`**: Handles media selection from the device gallery
- **`IONCAMREditManager`**: Handles photo editing and cropping
- **`IONCAMRVideoManager`**: Handles video playback

### Configuration Parameters

- **`IONCAMRCameraParameters`**: Configures photo capture settings (quality, dimensions, encoding type, media type, edit, orientation correction, album saving, metadata)
- **`IONCAMRVideoParameters`**: Configures video recording settings (save to gallery, include metadata, persistence)
- **`IONCAMREditParameters`**: Configures image editing settings (source URI, save to gallery, metadata)

### Result Model

All successful media operations return `IONCAMRMediaResult` objects:

```kotlin
data class IONCAMRMediaResult(
    val type: Int,          // Media type (image or video)
    val uri: String,        // File URI of the captured or selected media item
    val thumbnail: String?, // Base64 thumbnail (if available)
    val metadata: IONCAMRMediaMetadata?, // File metadata (if requested)
    val saved: Boolean      // Whether the file was saved to the gallery
)

data class IONCAMRMediaMetadata(
    val size: Long?,         // File size in bytes
    val duration: Int?,      // Duration in seconds (video only)
    val format: String?,     // File format (e.g. "jpeg", "mp4")
    val resolution: String?, // Image/video resolution (e.g. "1920x1080")
    val creationDate: String? // File creation date
)
```

### Error Handling

All manager methods expose an `onError` callback that receives an `IONCAMRError`. Each error carries a numeric `code` and a human-readable `description`. Errors are grouped into categories: permission errors, user cancellations, file errors, and general processing errors. See `IONCAMRError` for the full list.

```kotlin
onError = { error ->
    // error.code — numeric error code (e.g. for logging)
    // error.description — human-readable message
    showErrorAlert("Error ${error.code}: ${error.description}")
}
```

## Permissions

Add the following permissions to your app's `AndroidManifest.xml`:

```xml
<uses-permission android:name="android.permission.CAMERA" />
<uses-permission android:name="android.permission.READ_MEDIA_IMAGES" />
<uses-permission android:name="android.permission.READ_MEDIA_VIDEO" />

<!-- Required for devices running Android 9 (API 28) and below -->
<uses-permission
    android:name="android.permission.READ_EXTERNAL_STORAGE"
    android:maxSdkVersion="28" />
<uses-permission
    android:name="android.permission.WRITE_EXTERNAL_STORAGE"
    android:maxSdkVersion="28" />
```

You must also request camera and storage permissions at runtime before invoking camera or gallery operations. Use Android's standard permission request flow or a library such as [ActivityResultContracts.RequestPermission](https://developer.android.com/reference/androidx/activity/result/contract/ActivityResultContracts.RequestPermission).

## Requirements

- Android API 24+
- Android Gradle Plugin 8.7.3+
- Kotlin 1.9.24+
- Java 17+

## License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## Contributing

1. Fork the project
2. Create your feature branch (`git checkout -b feature/AmazingFeature`)
3. Commit your changes (`git commit -m 'Add some AmazingFeature'`)
4. Push to the branch (`git push origin feature/AmazingFeature`)
5. Open a Pull Request

## Support

- Report issues on our [Issue Tracker](https://github.com/ionic-team/ion-android-camera/issues)
