package com.outsystems.plugins.camera.model

enum class OSCAMRMediaType(val type: Int, val mimeType: String) {
    IMAGE(0, "image/*"),
    VIDEO(1, "video/*"),
    IMAGE_AND_VIDEO(2, "*/*");

    companion object {
        fun fromValue(value: Int) = values().first { it.type == value }
    }
}