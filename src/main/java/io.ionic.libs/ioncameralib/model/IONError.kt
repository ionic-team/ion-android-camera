package io.ionic.libs.ioncameralib.model

enum class IONError(val code: Int, val description: String) {
    CAMERA_PERMISSION_DENIED_ERROR(3, "Couldn't access camera. Check your camera permissions and try again."),
    NO_IMAGE_SELECTED_ERROR(5, "No image selected."),
    GALLERY_PERMISSION_DENIED_ERROR(6, "Couldn't access your photo gallery because access wasn't provided. Check its permissions and try again."),
    NO_PICTURE_TAKEN_ERROR(7, "No picture captured."),
    NO_CAMERA_AVAILABLE_ERROR(8, "No camera available."),
    EDIT_IMAGE_ERROR (10, "Couldn't edit image."),
    TAKE_PHOTO_ERROR(11, "Couldn't capture picture."),
    GET_IMAGE_ERROR(12, "Couldn't get image from the gallery."),
    PROCESS_IMAGE_ERROR(13, "Couldn't process image."),
    EDIT_CANCELLED_ERROR(14, "Couldn't edit picture because the process was canceled."),
    CAPTURE_VIDEO_ERROR(18, "Couldn't capture video."),
    CAPTURE_VIDEO_CANCELLED_ERROR(19, "Couldn't capture video because the process was canceled."),
    GENERIC_CHOOSE_MULTIMEDIA_ERROR(21, "Couldn't choose media from the gallery."),
    CHOOSE_MULTIMEDIA_CANCELLED_ERROR(23, "Couldn't choose media from the gallery because the process was canceled."),
    MEDIA_PATH_ERROR(24, "Couldn't get media file path."),
    PLAY_VIDEO_GENERAL_ERROR(26, "Couldn't play video."),
    EDIT_PICTURE_EMPTY_URI_ERROR(27, "URI parameter cannot be empty."),
    FILE_DOES_NOT_EXIST_ERROR(30, "The selected file doesn't exist."),
    FETCH_IMAGE_FROM_URI_ERROR(31, "Couldn't retrieve image from the URI."),

    // Overall Android specific
    CONTEXT_ERROR(202, "Unable to get the context.")
}