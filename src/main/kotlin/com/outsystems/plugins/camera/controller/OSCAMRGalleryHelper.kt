package com.outsystems.plugins.camera.controller

class OSCAMRGalleryHelper(val picturesDirectory: String?, val galleryFileName: String?) {

    var galleryPath = this.picturesDirectory + "/" + this.galleryFileName

}