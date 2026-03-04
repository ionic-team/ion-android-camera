package io.ionic.libs.ioncameralib.model

import com.google.gson.annotations.SerializedName

data class IONMediaMetadata(
    @SerializedName("size") val size: Long? = 0,
    @SerializedName("duration") val duration: Long? = 0,
    @SerializedName("format") val format: String? = "",
    @SerializedName("resolution") val resolution: String? = "",
    @SerializedName("creationDate") val creationDate: String? = ""
)

