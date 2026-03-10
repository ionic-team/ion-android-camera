package com.outsystems.plugins.camera.model

import com.google.gson.annotations.SerializedName

data class OSCAMRMediaMetadata(
    @SerializedName("size") val size: Long? = 0,
    @SerializedName("duration") val duration: Int? = 0,
    @SerializedName("format") val format: String? = "",
    @SerializedName("resolution") val resolution: String? = "",
    @SerializedName("creationDate") val creationDate: String? = ""
)
