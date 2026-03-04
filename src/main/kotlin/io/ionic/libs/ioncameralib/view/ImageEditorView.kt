package io.ionic.libs.ioncameralib.view

import android.app.Activity
import android.app.Activity.RESULT_CANCELED
import android.app.Activity.RESULT_OK
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Matrix
import android.graphics.Rect
import android.graphics.RectF
import android.graphics.drawable.BitmapDrawable
import androidx.exifinterface.media.ExifInterface
import android.net.Uri
import android.util.AttributeSet
import android.util.Log
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.ScaleGestureDetector
import android.widget.Button
import android.widget.FrameLayout
import android.widget.ImageButton
import android.widget.ImageView
import androidx.lifecycle.findViewTreeLifecycleOwner
import androidx.lifecycle.lifecycleScope
import io.ionic.libs.ioncameralib.helper.OSCAMRFileHelper
import io.ionic.libs.ioncameralib.imageeditor.OSCAMRImageEditorControllerInterface
import io.ionic.libs.ioncameralib.imageeditor.OSCAMRImageEditorController
import io.ionic.libs.ioncameralib.helper.OSCAMRFileHelperInterface
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Dispatchers.Default
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.InputStream
import kotlin.math.floor


class ImageEditorView @JvmOverloads constructor(
  context: Context,
  attrs: AttributeSet? = null
) : FrameLayout(context, attrs) {

  private val cropView by lazy { findViewById<ImageCropperView>(getResourceId("id/cropperView")) }
  private val imageView by lazy { findViewById<ImageView>(getResourceId("id/imageView")) }

  private val cancelButton by lazy { findViewById<Button>(getResourceId("id/cancelButton")) }
  private val okButton by lazy { findViewById<Button>(getResourceId("id/OKButton")) }

  private val rotateButton by lazy { findViewById<ImageButton>(getResourceId("id/rotateButton")) }
  private val flipButton by lazy { findViewById<ImageButton>(getResourceId("id/flipButton")) }

  private var scaleGestureDetector: ScaleGestureDetector
  private var scaleGestureListener: ScaleListener
  private var scaleFactor = 1.0f

  private var imageEditorController : OSCAMRImageEditorControllerInterface = OSCAMRImageEditorController()
  private var imageFileHelper : OSCAMRFileHelperInterface = OSCAMRFileHelper()

  private var resultUri: Uri? = null

  // Flag to check if the image is ready to be drawn
  private var isImageReady = false

  init {
    LayoutInflater.from(context).inflate(getResourceId("layout/image_editor_view"), this)

    cancelButton.setOnClickListener{
      (this.context as Activity).setResult(RESULT_CANCELED, Intent())
      (this.context as Activity).finish()
    }

    scaleGestureListener = ScaleListener()
    scaleGestureDetector = ScaleGestureDetector(this.context, scaleGestureListener)

    rotateButton.setOnClickListener{
      onRotateLeft()
    }

    okButton.setOnClickListener{
      onCrop()
    }

    flipButton.setOnClickListener{
      onFlip()
    }

  }

  fun setInputImageUri(uri: Uri) {
    val lifecycleOwner = findViewTreeLifecycleOwner()
    lifecycleOwner?.lifecycleScope?.launch {
      val bitmap = withContext(Dispatchers.IO) {
        val possiblyDownsized = loadBitmapSafely(context, uri)
        possiblyDownsized?.let { rotateBitmapIfRequired(context, it, uri) }
      }
      imageView.setImageBitmap(bitmap)
      imageView.requestLayout()
      isImageReady = true
      cropView.setImageReady() // to trigger cropView.onDraw
      invalidate() // to trigger onDraw again now that isImageReady is true
    }
  }

  fun setOutputImageUri(uri: Uri){
    resultUri = uri
  }

  override fun onDraw(canvas: Canvas) {
    super.onDraw(canvas)
    if (!isImageReady) return // we only want to draw image after it is ready
    updateCropViewLimitFrame()
  }
  override fun dispatchTouchEvent(ev: MotionEvent?): Boolean {
    return super.dispatchTouchEvent(ev)
  }

  private fun getResourceId(typeAndName: String): Int {
    val application = (context as Activity).application
    return application.resources.getIdentifier(typeAndName, null, application.packageName)
  }

  private fun onRotateLeft(){
    CoroutineScope(Default).launch {
      val sourceImage = (imageView.drawable as BitmapDrawable).bitmap
      val newImage = imageEditorController.rotateLeft(sourceImage)

      withContext(Dispatchers.Main){
        updateImageView(newImage)
      }
    }
  }
  private fun onCrop(){
      CoroutineScope(Default).launch {
        Log.d(TAG, "${cropView.getFrame()}")

        val sourceImage = (imageView.drawable as BitmapDrawable).bitmap
        val cropViewRect = cropView.getFrame()

        val imageCropRect = Rect().apply {
          left = cropViewRect.left.toInt()
          top = cropViewRect.top.toInt()
          right = left + cropViewRect.width().toInt()
          bottom = top + cropViewRect.height().toInt()
        }
        val scaledImageRect = cropView.getLimitFrame()

        Log.d(TAG, "${imageCropRect.left} ${imageCropRect.top} ${cropViewRect.width()} ${cropViewRect.height()}" )

        /* We need to create this scaledImage because the ImageView will scale with the screen, but the image
        dimensions will be the same as original.
        */
        val scaledImage = Bitmap.createScaledBitmap(sourceImage, scaledImageRect.width().toInt(), scaledImageRect.height().toInt(), false)
        val newImage = imageEditorController.crop(scaledImage, imageCropRect)

        resultUri?.let {
          imageFileHelper.saveImage(it.toString(), newImage)
        }

        val intent = Intent()
        intent.putExtra(IMAGE_OUTPUT_URI_EXTRAS, resultUri.toString())

        (context as Activity).setResult(RESULT_OK, intent)
        (context as Activity).finish()
      }
  }
  private fun onFlip(){
    CoroutineScope(Default).launch {
      val sourceImage = (imageView.drawable as BitmapDrawable).bitmap
      val newImage = imageEditorController.flip(sourceImage)

      withContext(Dispatchers.Main){
        updateImageView(newImage)
      }
    }
  }

  private fun updateImageView(image : Bitmap){
    imageView.setImageBitmap(image)
    imageView.requestLayout()
  }

  /*
    This method will find the image limits by using the ImageView and source Bitmap ratio.
    In a nutshell:
    1 - resize the image to the view Height, and then compute the proposedWidth;
    2 - resize the image to the view Width, and then compute the proposedHeight;
    3 - check which one of the proposed dimensions is small enough to fit on the view;
    4 - use this proposed dimensions to compute the limit frame;
   */
  private fun updateCropViewLimitFrame(){

    val newCropViewFrame = RectF(
      imageView.left.toFloat(),
      imageView.top.toFloat(),
      imageView.right.toFloat(),
      imageView.bottom.toFloat())

    val sourceImage = (imageView.drawable as BitmapDrawable).bitmap
    val sourceImageRatio = sourceImage.width.toFloat() / sourceImage.height.toFloat()

    val imageViewWidth = imageView.width
    val proposedHeight = imageViewWidth * (1 / sourceImageRatio)

    val imageViewHeight = imageView.height
    val proposedWidth = imageViewHeight * sourceImageRatio

    if(proposedWidth > imageViewWidth) {
      val newTopLimit = floor((imageViewHeight - proposedHeight) / 2)
      newCropViewFrame.top = newTopLimit
      newCropViewFrame.bottom = newTopLimit + proposedHeight
    }
    else {
      val newLeftLimit = floor((imageViewWidth - proposedWidth) / 2)
      newCropViewFrame.left =  newLeftLimit
      newCropViewFrame.right = newLeftLimit + proposedWidth
    }

    cropView.setLimitFrame(newCropViewFrame)
  }

    /**
     * Load a bitmap from the given URI, downsizing it if necessary.
     *
     * @param context The context to use for loading the bitmap.
     * @param uri The URI of the image to load.
     * @return The loaded bitmap, or null if loading failed.
     */
    private fun loadBitmapSafely(context: Context, uri: Uri): Bitmap? {
    return if (shouldDownsizeImage(context, uri)) {
      decodeSampledBitmapFromUri(context, uri)
    } else {
      BitmapFactory.decodeStream(openImageInputStream(context, uri))
    }
  }

  /**
   * Check if the image should be downsized based on its dimensions.
   * If the total number of pixels exceeds MAX_ALLOWED_PIXELS, it will be downsized.
   *
   * We chose 32MP as the maximum allowed size, which is a common limit for many devices.
   *
   * @param context The context to use for checking the image dimensions.
   * @param imageUri The URI of the image to check.
   * @return True if the image should be downsized, false otherwise.
   */
  private fun shouldDownsizeImage(context: Context, imageUri: Uri): Boolean {
    val options = BitmapFactory.Options().apply { inJustDecodeBounds = true }

    openImageInputStream(context, imageUri)?.use { stream ->
      BitmapFactory.decodeStream(stream, null, options)
    }

    val totalPixels = options.outWidth * options.outHeight
    return totalPixels > MAX_ALLOWED_PIXELS
  }

  /**
   * Decode a sampled bitmap from the given URI, downsizing it if necessary.
   * Maximum dimensions are defined by REQUIRED_WIDTH and REQUIRED_HEIGHT,
   * which are set to 1080 and 1920 respectively.
   *
   * @param context The context to use for decoding.
   * @param imageUri The URI of the image to decode.
   * @return The decoded bitmap, or null if decoding failed.
   */
  private fun decodeSampledBitmapFromUri(context: Context, imageUri: Uri): Bitmap? {
    val contentResolver = context.contentResolver

    // use inJustDecodeBounds=true to get dimensions
    val options = BitmapFactory.Options().apply {
      inJustDecodeBounds = true
    }

    openImageInputStream(context, imageUri)?.use { stream ->
      BitmapFactory.decodeStream(stream, null, options)
    }

    val imageWidth = options.outWidth
    val imageHeight = options.outHeight

    return if (imageWidth > REQUIRED_WIDTH || imageHeight > REQUIRED_HEIGHT) {
      // downsize image if needed using inSampleSize
      options.inSampleSize = calculateInSampleSize(options)
      options.inJustDecodeBounds = false

      openImageInputStream(context, imageUri)?.use { stream ->
        BitmapFactory.decodeStream(stream, null, options)
      }
    } else {
      // load full image if small enough
      val fullSizeOptions = BitmapFactory.Options().apply {
        inJustDecodeBounds = false
      }
      openImageInputStream(context, imageUri)?.use { stream ->
        BitmapFactory.decodeStream(stream, null, fullSizeOptions)
      }
    }
  }

  private fun openImageInputStream(context: Context, imageUri: Uri): InputStream? =
    imageFileHelper.getInputStreamFromUriString(imageUri.toString(), context)

    /**
     * Calculate the inSampleSize value based on the desired dimensions. In other words,
     * this function determines how much to downsize the image.
     *
     * inSampleSize is a power of 2, to scale down an image while maintaining its aspect ratio,
     * and to do it as efficiently as possible, according to the documentation of BitmapFactory.
     *
     * This value is used by the BitmapFactory to load a smaller version of the image into memory.
     *
     * The function ensures that the image is scaled down just enough to fit within the required dimensions,
     * avoiding unnecessary loss of quality.
     *
     * @param options The BitmapFactory.Options object containing the image dimensions.
     * @return The calculated inSampleSize value.
     */
  private fun calculateInSampleSize(options: BitmapFactory.Options): Int {
    val (height: Int, width: Int) = options.run { outHeight to outWidth }
    var inSampleSize = 1

    if (height > REQUIRED_HEIGHT || width > REQUIRED_WIDTH) {
      val halfHeight: Int = height / 2
      val halfWidth: Int = width / 2

      while ((halfHeight / inSampleSize) >= REQUIRED_HEIGHT && (halfWidth / inSampleSize) >= REQUIRED_WIDTH) {
        inSampleSize *= 2
      }
    }
    return inSampleSize
  }

    /**
     * Rotate the bitmap if required based on the EXIF orientation data.
     *
     * @param context The context to use for decoding.
     * @param bitmap The bitmap to rotate.
     * @param imageUri The URI of the image to decode.
     * @return The rotated bitmap, or the original bitmap if no rotation is needed.
     */
  private fun rotateBitmapIfRequired(context: Context, bitmap: Bitmap, imageUri: Uri): Bitmap {
    val inputStream = openImageInputStream(context, imageUri)
    val exif = inputStream?.use {
      ExifInterface(it)
    }

    val orientation = exif?.getAttributeInt(
      ExifInterface.TAG_ORIENTATION,
      ExifInterface.ORIENTATION_NORMAL
    ) ?: return bitmap

    val matrix = Matrix()
    when (orientation) {
      ExifInterface.ORIENTATION_ROTATE_90 -> matrix.postRotate(90f)
      ExifInterface.ORIENTATION_ROTATE_180 -> matrix.postRotate(180f)
      ExifInterface.ORIENTATION_ROTATE_270 -> matrix.postRotate(270f)
      else -> return bitmap // no rotation needed
    }

    return Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
  }

  private inner class ScaleListener : ScaleGestureDetector.SimpleOnScaleGestureListener() {

    override fun onScale(scaleGestureDetector: ScaleGestureDetector): Boolean {
      scaleFactor *= scaleGestureDetector.scaleFactor
      imageView.scaleX = scaleFactor
      imageView.scaleY = scaleFactor
      imageView.requestLayout()
      return true
    }
  }

  companion object {
    private const val TAG = "ImageEditorView"
    private const val IMAGE_OUTPUT_URI_EXTRAS = "IMAGE_EDITOR_OUT_URI_EXTRAS"
    private const val REQUIRED_WIDTH = 1080
    private const val REQUIRED_HEIGHT = 1920
    private const val MAX_ALLOWED_PIXELS = 32000000 // 32MP
  }

}
