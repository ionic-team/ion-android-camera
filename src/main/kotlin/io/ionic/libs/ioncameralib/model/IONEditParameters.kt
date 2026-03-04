package io.ionic.libs.ioncameralib.model

import com.google.gson.annotations.SerializedName

data class IONEditParameters(
    @SerializedName("editURI") var editURI: String?,
    @SerializedName("fromUri") var fromUri: Boolean,
    @SerializedName("saveToGallery") val saveToGallery: Boolean,
    @SerializedName("includeMetadata") val includeMetadata: Boolean
)