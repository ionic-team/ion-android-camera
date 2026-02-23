package io.ionic.libs.ioncameralib.model

enum class IONMediaType(val type: Int, val mimeType: String) {
    PICTURE(0, "image/*"),
    VIDEO(1, "video/*"),
    ALL(2, "*/*");

    companion object Companion {
        fun fromValue(value: Int) = values().first { it.type == value }
    }
}