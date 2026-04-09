package io.ionic.libs.ioncameralib.model

enum class IONCAMRError(val code: Int, val description: String) {
    CAMERA_PERMISSION_DENIED_ERROR(3, "Couldn't access camera. Check your camera permissions and try again."),
    GALLERY_PERMISSION_DENIED_ERROR(5, "Couldn't access your photo gallery because access wasn't provided."),
    NO_PICTURE_TAKEN_ERROR(6, "Couldn't take photo because the process was canceled."),
    NO_CAMERA_AVAILABLE_ERROR(7, "No camera available."),
    EDIT_IMAGE_ERROR (9, "Couldn't edit image."),
    TAKE_PHOTO_ERROR(10, "Couldn't take photo."),
    PROCESS_IMAGE_ERROR(12, "Couldn't process image."),
    EDIT_CANCELLED_ERROR(13, "Couldn't edit photo because the process was canceled."),
    CAPTURE_VIDEO_ERROR(16, "Couldn't record video."),
    CAPTURE_VIDEO_CANCELLED_ERROR(17, "Couldn't record video because the process was canceled."),
    GENERIC_CHOOSE_MULTIMEDIA_ERROR(18, "Couldn't choose media from the gallery."),
    CHOOSE_MULTIMEDIA_CANCELLED_ERROR(20, "Couldn't choose media from the gallery because the process was canceled."),
    MEDIA_PATH_ERROR(21, "Couldn't get media file path."),
    PLAY_VIDEO_GENERAL_ERROR(23, "Couldn't play video."),
    EDIT_PICTURE_EMPTY_URI_ERROR(24, "URI parameter cannot be empty."),
    FILE_DOES_NOT_EXIST_ERROR(27, "The selected file doesn't exist."),
    FETCH_IMAGE_FROM_URI_ERROR(28, "Couldn't retrieve image from the URI."),
    INVALID_ARGUMENT_ERROR(31, "Invalid argument provided to plugin method."),

    // Overall Android specific
    CONTEXT_ERROR(33, "Unable to get the context.")
}