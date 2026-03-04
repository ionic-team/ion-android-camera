/*
       Licensed to the Apache Software Foundation (ASF) under one
       or more contributor license agreements.  See the NOTICE file
       distributed with this work for additional information
       regarding copyright ownership.  The ASF licenses this file
       to you under the Apache License, Version 2.0 (the
       "License"); you may not use this file except in compliance
       with the License.  You may obtain a copy of the License at

         http://www.apache.org/licenses/LICENSE-2.0

       Unless required by applicable law or agreed to in writing,
       software distributed under the License is distributed on an
       "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
       KIND, either express or implied.  See the License for the
       specific language governing permissions and limitations
       under the License.
*/

package io.ionic.libs.ioncameralib.helper

import android.media.ExifInterface
import android.net.Uri
import java.io.IOException

class OSCAMRExifHelper : OSCAMRExifHelperInterface {

    private var aperture: String? = null
    private var datetime: String? = null
    private var exposureTime: String? = null
    private var flash: String? = null
    private var focalLength: String? = null
    private var gpsAltitude: String? = null
    private var gpsAltitudeRef: String? = null
    private var gpsDateStamp: String? = null
    private var gpsLatitude: String? = null
    private var gpsLatitudeRef: String? = null
    private var gpsLongitude: String? = null
    private var gpsLongitudeRef: String? = null
    private var gpsProcessingMethod: String? = null
    private var gpsTimestamp: String? = null
    private var iso: String? = null
    private var make: String? = null
    private var model: String? = null
    private var orientation: String? = null
    private var whiteBalance: String? = null

    private var inFile: ExifInterface? = null
    private var outFile: ExifInterface? = null

    /**
     * The file before it is compressed
     *
     * @param filePath
     * @throws IOException
     */
    @Throws(IOException::class)
    override fun createInFile(filePath: String?) {
        inFile = filePath?.let { ExifInterface(it) }
    }

    /**
     * The file after it has been compressed
     *
     * @param filePath
     * @throws IOException
     */
    @Throws(IOException::class)
    override fun createOutFile(filePath: String?) {
        outFile = filePath?.let { ExifInterface(it) }
    }

    /**
     * The file after it has been compressed
     *
     * @param filePath
     * @throws IOException
     */
    @Throws(IOException::class)
    override fun createOutFileFromUri(fileUri: Uri?) {
        outFile = fileUri?.path?.let { ExifInterface(it) }
    }

    /**
     * Reads all the EXIF data from the input file.
     */
    override fun readExifData() {
        aperture = inFile?.getAttribute(ExifInterface.TAG_APERTURE)
        datetime = inFile?.getAttribute(ExifInterface.TAG_DATETIME)
        exposureTime = inFile?.getAttribute(ExifInterface.TAG_EXPOSURE_TIME)
        flash = inFile?.getAttribute(ExifInterface.TAG_FLASH)
        focalLength = inFile?.getAttribute(ExifInterface.TAG_FOCAL_LENGTH)
        gpsAltitude = inFile?.getAttribute(ExifInterface.TAG_GPS_ALTITUDE)
        gpsAltitudeRef = inFile?.getAttribute(ExifInterface.TAG_GPS_ALTITUDE_REF)
        gpsDateStamp = inFile?.getAttribute(ExifInterface.TAG_GPS_DATESTAMP)
        gpsLatitude = inFile?.getAttribute(ExifInterface.TAG_GPS_LATITUDE)
        gpsLatitudeRef = inFile?.getAttribute(ExifInterface.TAG_GPS_LATITUDE_REF)
        gpsLongitude = inFile?.getAttribute(ExifInterface.TAG_GPS_LONGITUDE)
        gpsLongitudeRef = inFile?.getAttribute(ExifInterface.TAG_GPS_LONGITUDE_REF)
        gpsProcessingMethod = inFile?.getAttribute(ExifInterface.TAG_GPS_PROCESSING_METHOD)
        gpsTimestamp = inFile?.getAttribute(ExifInterface.TAG_GPS_TIMESTAMP)
        iso = inFile?.getAttribute(ExifInterface.TAG_ISO)
        make = inFile?.getAttribute(ExifInterface.TAG_MAKE)
        model = inFile?.getAttribute(ExifInterface.TAG_MODEL)
        orientation = inFile?.getAttribute(ExifInterface.TAG_ORIENTATION)
        whiteBalance = inFile?.getAttribute(ExifInterface.TAG_WHITE_BALANCE)
    }

    /**
     * Writes the previously stored EXIF data to the output file.
     *
     * @throws IOException
     */
    @Throws(IOException::class)
    override fun writeExifData() {
        // Don't try to write to a null file
        if (outFile == null) {
            return
        }
        if (aperture != null) {
            outFile!!.setAttribute(ExifInterface.TAG_APERTURE, aperture)
        }
        if (datetime != null) {
            outFile!!.setAttribute(ExifInterface.TAG_DATETIME, datetime)
        }
        if (exposureTime != null) {
            outFile!!.setAttribute(ExifInterface.TAG_EXPOSURE_TIME, exposureTime)
        }
        if (flash != null) {
            outFile!!.setAttribute(ExifInterface.TAG_FLASH, flash)
        }
        if (focalLength != null) {
            outFile!!.setAttribute(ExifInterface.TAG_FOCAL_LENGTH, focalLength)
        }
        if (gpsAltitude != null) {
            outFile!!.setAttribute(ExifInterface.TAG_GPS_ALTITUDE, gpsAltitude)
        }
        if (gpsAltitudeRef != null) {
            outFile!!.setAttribute(ExifInterface.TAG_GPS_ALTITUDE_REF, gpsAltitudeRef)
        }
        if (gpsDateStamp != null) {
            outFile!!.setAttribute(ExifInterface.TAG_GPS_DATESTAMP, gpsDateStamp)
        }
        if (gpsLatitude != null) {
            outFile!!.setAttribute(ExifInterface.TAG_GPS_LATITUDE, gpsLatitude)
        }
        if (gpsLatitudeRef != null) {
            outFile!!.setAttribute(ExifInterface.TAG_GPS_LATITUDE_REF, gpsLatitudeRef)
        }
        if (gpsLongitude != null) {
            outFile!!.setAttribute(ExifInterface.TAG_GPS_LONGITUDE, gpsLongitude)
        }
        if (gpsLongitudeRef != null) {
            outFile!!.setAttribute(ExifInterface.TAG_GPS_LONGITUDE_REF, gpsLongitudeRef)
        }
        if (gpsProcessingMethod != null) {
            outFile!!.setAttribute(ExifInterface.TAG_GPS_PROCESSING_METHOD, gpsProcessingMethod)
        }
        if (gpsTimestamp != null) {
            outFile!!.setAttribute(ExifInterface.TAG_GPS_TIMESTAMP, gpsTimestamp)
        }
        if (iso != null) {
            outFile!!.setAttribute(ExifInterface.TAG_ISO, iso)
        }
        if (make != null) {
            outFile!!.setAttribute(ExifInterface.TAG_MAKE, make)
        }
        if (model != null) {
            outFile!!.setAttribute(ExifInterface.TAG_MODEL, model)
        }
        if (orientation != null) {
            outFile!!.setAttribute(ExifInterface.TAG_ORIENTATION, orientation)
        }
        if (whiteBalance != null) {
            outFile!!.setAttribute(ExifInterface.TAG_WHITE_BALANCE, whiteBalance)
        }
        outFile!!.saveAttributes()
    }

    override fun getOrientation(): Int {
        val o = orientation?.toInt()
        return if (o == ExifInterface.ORIENTATION_NORMAL) {
            0
        } else if (o == ExifInterface.ORIENTATION_ROTATE_90) {
            90
        } else if (o == ExifInterface.ORIENTATION_ROTATE_180) {
            180
        } else if (o == ExifInterface.ORIENTATION_ROTATE_270) {
            270
        } else {
            0
        }
    }

    override fun resetOrientation() {
        orientation = "" + ExifInterface.ORIENTATION_NORMAL
    }

    override fun getOrientationFromExif(exif: ExifInterface): Int {
        return exif.getAttributeInt(
            ExifInterface.TAG_ORIENTATION,
            ExifInterface.ORIENTATION_UNDEFINED
        )
    }

}