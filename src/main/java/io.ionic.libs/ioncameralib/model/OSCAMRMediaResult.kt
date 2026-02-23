package io.ionic.libs.ioncameralib.model

import com.google.gson.annotations.SerializedName

data class OSCAMRMediaResult(
    @SerializedName("type") val type: Int,
    @SerializedName("uri") val uri: String,
    @SerializedName("thumbnail") val thumbnail: String,
    @SerializedName("metadata") val metadata: IONMediaMetadata?
)