package io.ionic.libs.ioncameralib.model

import com.google.gson.annotations.SerializedName

data class IONVideoParameters(
    @SerializedName("saveToGallery") val saveToGallery: Boolean,
    @SerializedName("includeMetadata") val includeMetadata: Boolean
)
