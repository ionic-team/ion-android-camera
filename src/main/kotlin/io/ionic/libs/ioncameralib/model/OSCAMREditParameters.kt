package io.ionic.libs.ioncameralib.model

import com.google.gson.annotations.SerializedName

data class OSCAMREditParameters(
    @SerializedName("editURI") var editURI: String?,
    @SerializedName("fromUri") var fromUri: Boolean,
    @SerializedName("saveToGallery") val saveToGallery: Boolean,
    @SerializedName("includeMetadata") val includeMetadata: Boolean
)
