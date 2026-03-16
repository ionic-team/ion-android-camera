package io.ionic.libs.ioncameralib.model

import com.google.gson.annotations.SerializedName

data class IONCAMRVideoParameters(
    @SerializedName("saveToGallery") val saveToGallery: Boolean,
    @SerializedName("includeMetadata") val includeMetadata: Boolean
)
