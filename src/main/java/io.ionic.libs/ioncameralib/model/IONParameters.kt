package io.ionic.libs.ioncameralib.model

import com.google.gson.annotations.SerializedName

data class IONParameters(
    @SerializedName("mQuality") val mQuality: Int,
    @SerializedName("targetWidth") var targetWidth: Int,
    @SerializedName("targetHeight") var targetHeight: Int,
    @SerializedName("encodingType") val encodingType: Int,
    @SerializedName("mediaType") val mediaType: Int,
    @SerializedName("allowEdit") val allowEdit: Boolean,
    @SerializedName("correctOrientation") val correctOrientation: Boolean,
    @SerializedName("saveToPhotoAlbum") val saveToPhotoAlbum: Boolean,
    @SerializedName("includeMetadata") val includeMetadata: Boolean,
    @SerializedName("latestVersion") val latestVersion: Boolean
)
