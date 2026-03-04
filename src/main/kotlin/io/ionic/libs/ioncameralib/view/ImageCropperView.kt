package io.ionic.libs.ioncameralib.view

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import android.view.View
import kotlin.math.sqrt

class ImageCropperView @JvmOverloads constructor(
  context: Context,
  attrs: AttributeSet? = null
) : View(context, attrs)  {

  private var borders: Array<RectangleBorder> = arrayOf()
  private var corners: Array<RectangleCorner> = arrayOf()

  private lateinit var leftBorder: RectangleBorder
  private lateinit var topBorder: RectangleBorder
  private lateinit var rightBorder: RectangleBorder
  private lateinit var bottomBorder: RectangleBorder

  private lateinit var topLeftCorner: RectangleCorner
  private lateinit var topRightCorner: RectangleCorner
  private lateinit var bottomRightCorner: RectangleCorner
  private lateinit var bottomLeftCorner: RectangleCorner

  private var rectangleMinHeight: Int = MIN_CROP_HEIGHT
  private var rectangleMinWidth: Int = MIN_CROP_WIDTH

  private var touchStartPoint: Point = Point(0, 0)
  private var selectedCorner : RectangleCorner? = null
  private var selectedBorder : RectangleBorder? = null

  private val backgroundPaint: Paint
  private val rectanglePaint: Paint
  private val borderPaint: Paint
  private val cornerPaint: Paint

  private var limitsFrame: RectF = RectF()

  // Flag to check if the image is ready to be cropped, that is, if frame can be drawn around it
  private var isImageReady: Boolean = false

  companion object {
    private const val TAG = "ImageCropperView"

    private const val BORDER_WIDTH = 2
    private const val DEFAULT_BACKGROUND_ALPHA = 0.7f
    private const val COLOR_DENSITY = 255f

    private const val CORNER_WIDTH = 6
    private const val CORNER_HEIGHT = 60
    private const val CORNER_TOUCH_RADIUS = 120

    private const val BORDER_HEIGHT = 60
    private const val BORDER_TOUCH_RADIUS = 120

    private const val MIN_CROP_WIDTH = CORNER_TOUCH_RADIUS * 3
    private const val MIN_CROP_HEIGHT = CORNER_TOUCH_RADIUS * 3
  }

  init {

    val backgroundAlpha: Float = DEFAULT_BACKGROUND_ALPHA

    setWillNotDraw(false)
    setLayerType(LAYER_TYPE_HARDWARE, null)

    backgroundPaint = Paint().apply {
      color = Color.BLACK
      alpha = (backgroundAlpha * COLOR_DENSITY).toInt()
    }

    rectanglePaint = Paint().apply {
      xfermode = PorterDuffXfermode(PorterDuff.Mode.CLEAR)
    }

    borderPaint = Paint().apply {
      strokeWidth = BORDER_WIDTH.toFloat()
      style = Paint.Style.FILL_AND_STROKE
      color = Color.WHITE
    }

    cornerPaint = Paint().apply {
      style = Paint.Style.FILL_AND_STROKE
      color = Color.WHITE
    }

  }

  override fun onDraw(canvas: Canvas) {
    super.onDraw(canvas)

    if (!isImageReady) return

    if (corners.isEmpty()) setupCorners()

    if (borders.isEmpty()) setupBorders()

    adjustTouchAreas()

    drawBackground(canvas)
    drawCrop(canvas)
    drawCorners(canvas)
    drawBorders(canvas)
    drawGridLines(canvas)
    drawBorder(canvas)
  }

  override fun onTouchEvent(event: MotionEvent): Boolean {

    val eventAction = event.action
    val eventX = event.x.toInt()
    val eventY = event.y.toInt()

    when (eventAction) {
      MotionEvent.ACTION_DOWN -> {
        touchStartPoint.x = eventX
        touchStartPoint.y = eventY

        selectedCorner = null
        selectedBorder = null

        corners.forEach {
          if(it.intersectsWith(eventX, eventY)){
            selectedCorner = it
            invalidate()
          }
        }

        borders.forEach {
          if (it.intersectsWith(eventX, eventY)) {
            selectedBorder = it
            invalidate()
          }
        }

        limitRectangleSize()
      }
      MotionEvent.ACTION_MOVE -> {
        if (selectedCorner != null) {
          moveCorner(eventX, eventY)
          limitRectangleSize()
        }
        else if(selectedBorder != null){
          moveBorder(eventX, eventY)
          limitRectangleSize()
        }
        else if(isInsideRectangle(eventX, eventY)) {
          moveRectangle(eventX, eventY)
        }

      }
    }
    // redraw canvas
    invalidate()
    return true
  }

  /**
   * Sets the image as ready to be cropped
   */
  fun setImageReady() {
    isImageReady = true
    invalidate()
  }

  private fun setupCorners() {

    val left = limitsFrame.left.toInt()
    val top = limitsFrame.top.toInt()
    val right = limitsFrame.right.toInt()
    val bottom = limitsFrame.bottom.toInt()

    topLeftCorner = RectangleCorner(Point().apply {
      x = left
      y = top
    })
    topRightCorner = RectangleCorner(Point().apply {
      x = right
      y = top
    })
    bottomRightCorner = RectangleCorner(Point().apply {
      x = right
      y = bottom
    })
    bottomLeftCorner = RectangleCorner(Point().apply {
      x = left
      y = bottom
    })

    topLeftCorner.adjacentX = bottomLeftCorner
    topLeftCorner.adjacentY = topRightCorner

    topRightCorner.adjacentX = bottomRightCorner
    topRightCorner.adjacentY = topLeftCorner

    bottomRightCorner.adjacentX = topRightCorner
    bottomRightCorner.adjacentY = bottomLeftCorner

    bottomLeftCorner.adjacentX = topLeftCorner
    bottomLeftCorner.adjacentY = bottomRightCorner

    corners = arrayOf(topLeftCorner, topRightCorner, bottomRightCorner, bottomLeftCorner)

  }

  private fun setupBorders() {

    leftBorder = RectangleBorder(topLeftCorner, bottomLeftCorner)
    rightBorder = RectangleBorder(topRightCorner, bottomRightCorner)
    topBorder = RectangleBorder(topLeftCorner, topRightCorner)
    bottomBorder = RectangleBorder(bottomLeftCorner, bottomRightCorner)

    borders = arrayOf(leftBorder, rightBorder, topBorder, bottomBorder)

  }

  fun setLimitFrame(frame: RectF) {
    this.limitsFrame = frame
    this.rectangleMinHeight = this.rectangleMinHeight.coerceAtMost((frame.height() * 0.4).toInt())
    this.rectangleMinWidth = this.rectangleMinWidth.coerceAtMost((frame.width() * 0.4).toInt())

    limitRectangleToLimits()
  }

  fun getLimitFrame(): RectF {
    return limitsFrame
  }

  fun getFrame(): RectF {
    return RectF().apply {
      top = topLeftCorner.y.toFloat() - limitsFrame.top
      left = topLeftCorner.x.toFloat() - limitsFrame.left
      bottom = bottomRightCorner.y.toFloat() - limitsFrame.top
      right = bottomRightCorner.x.toFloat() - limitsFrame.left
    }
  }

  /**
   * Updates and adjusts the corner and border touching areas to be
   * 1/3 do the size of the cropper view size
   */
  private fun adjustTouchAreas() {
    val width = topRightCorner.x - topLeftCorner.x
    val height = bottomRightCorner.y - topLeftCorner.y

    // update touch area to keep within a 1/3 of the min size
    // 1/3 was chosen because the crop viewer is divided into a 3x3 grid.
    // since we want the radius, we further divide by 2, resulting in dividing by 6.
    val newCircleRadius = width.coerceAtMost(height).toFloat() / 6
    corners.forEach {
      it.touchRadius = newCircleRadius
    }
    borders.forEach {
      it.touchRadius = newCircleRadius
    }
  }

  private fun moveCorner(x: Int, y: Int) {
    selectedCorner?.let {
      it.x = x
      it.adjacentX?.x = x

      it.y = y
      it.adjacentY?.y = y

      limitRectangleToHorizontalLimits()
      limitRectangleToVerticalLimits()
    }
  }

  private fun moveBorder(x: Int, y: Int){

    selectedBorder?.let {

      if(it.typeRectangle == RectangleBorderType.X){
        it.lowestAdjacentXY.x = x
        it.highestAdjacentXY.x = x
        limitRectangleToHorizontalLimits()
      }
      else {
        it.lowestAdjacentXY.y = y
        it.highestAdjacentXY.y = y
        limitRectangleToVerticalLimits()
      }
    }
  }

  private fun moveRectangle(x: Int, y: Int) {

    Log.d(TAG, "TopLeftCorner: ${topLeftCorner.x - limitsFrame.left} ${topLeftCorner.y - limitsFrame.top}")

    var dx = -(touchStartPoint.x - x)
    var dy = -(touchStartPoint.y - y)

    if(topLeftCorner.x + dx <= limitsFrame.left || bottomRightCorner.x + dx >= limitsFrame.right){
      dx = 0
    }
    if(topLeftCorner.y + dy <= limitsFrame.top || bottomRightCorner.y + dy >= limitsFrame.bottom){
      dy = 0
    }

    corners.forEach {
      it.x += dx
      it.y += dy
    }

    touchStartPoint.x = x
    touchStartPoint.y = y

  }

  private fun limitRectangleToHorizontalLimits(){
    val leftBorderMinLimit = limitsFrame.left.toInt()
    val leftBorderMaxLimit = limitsFrame.right.toInt() - rectangleMinWidth
    leftBorder.clampXCoordinate(leftBorderMinLimit, leftBorderMaxLimit)

    val rightBorderMinLimit = limitsFrame.left.toInt() + rectangleMinWidth
    val rightBorderMaxLimit = limitsFrame.right.toInt()
    rightBorder.clampXCoordinate(rightBorderMinLimit, rightBorderMaxLimit)
  }

  private fun limitRectangleToVerticalLimits(){
    val topBorderMinLimit = limitsFrame.top.toInt()
    val topBorderMaxLimit = limitsFrame.bottom.toInt() - rectangleMinHeight
    topBorder.clampYCoordinate(topBorderMinLimit, topBorderMaxLimit)

    val bottomBorderMinLimit = limitsFrame.top.toInt() + rectangleMinHeight
    val bottomBorderMaxLimit = limitsFrame.bottom.toInt()
    bottomBorder.clampYCoordinate(bottomBorderMinLimit, bottomBorderMaxLimit)
  }

  private fun limitRectangleToLimits(){
    if(corners.isEmpty()) return
    limitRectangleToHorizontalLimits()
    limitRectangleToVerticalLimits()
    invalidate()
  }

  private fun limitRectangleSize(){
    // apply minimum size and adjust to new resolution
    val currentWidth = bottomRightCorner.x - topLeftCorner.x
    val widthDeficit = this.rectangleMinWidth - currentWidth
    if (widthDeficit > 0){
      limitWidthFromLeft(widthDeficit)
      limitWidthFromRight(widthDeficit)
    }

    val currentHeight = bottomRightCorner.y - topLeftCorner.y
    val heightDeficit = this.rectangleMinHeight - currentHeight
    if (heightDeficit > 0){
      limitHeightFromTop(heightDeficit)
      limitHeightFromBottom(heightDeficit)
    }

    //adjustCropperToWithinImage()

  }

  private fun limitWidthFromLeft(widthDeficit: Int){
    limitWidth(topLeftCorner, bottomLeftCorner, leftBorder, -widthDeficit)
  }

  private fun limitWidthFromRight(widthDeficit: Int){
    limitWidth(topRightCorner, bottomRightCorner, rightBorder, widthDeficit)
  }

  private fun limitHeightFromTop(heightDeficit: Int) {
    limitHeight(topLeftCorner, topRightCorner, topBorder, -heightDeficit)
  }

  private fun limitHeightFromBottom(heightDeficit: Int) {
    limitHeight(bottomLeftCorner, bottomRightCorner, bottomBorder, heightDeficit)
  }

  private fun limitWidth(c0: RectangleCorner, c1: RectangleCorner, border: RectangleBorder, widthDeficit : Int){
    if(selectedCorner == c0 || selectedCorner == c1 || selectedBorder == border){
      c0.x += widthDeficit
      c1.x += widthDeficit
    }
  }

  private fun limitHeight(c0: RectangleCorner, c1: RectangleCorner, border: RectangleBorder, widthDeficit : Int){
    if(selectedCorner == c0 || selectedCorner == c1 || selectedBorder == border){
      c0.y += widthDeficit
      c1.y += widthDeficit
    }
  }

  private fun isInsideRectangle(x: Int, y: Int): Boolean{
    val checkLeft = x > topLeftCorner.x
    val checkRight = x < topRightCorner.x
    val checkTop = y > topLeftCorner.y
    val checkBottom = y < bottomLeftCorner.y
    return checkLeft && checkRight && checkTop && checkBottom
  }

  private fun drawBackground(canvas: Canvas) {
    canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), backgroundPaint)
  }

  private fun drawCrop(canvas: Canvas) {
    val left = topLeftCorner.x.toFloat()
    val top = topLeftCorner.y.toFloat()
    val right = bottomRightCorner.x.toFloat()
    val bottom = bottomRightCorner.y.toFloat()
    canvas.drawRect(left, top, right, bottom, rectanglePaint)

  }

  private fun drawBorder(canvas: Canvas) {
    val left = topLeftCorner.x.toFloat()
    val top = topLeftCorner.y.toFloat()
    val right = bottomRightCorner.x.toFloat()
    val bottom = bottomRightCorner.y.toFloat()

    canvas.drawLine(left, top, right, top, borderPaint)
    canvas.drawLine(left, bottom, right, bottom, borderPaint)
    canvas.drawLine(left, top, left, bottom, borderPaint)
    canvas.drawLine(right, top, right, bottom, borderPaint)
  }

  private fun drawGridLines(canvas: Canvas){

    val frameWidth = topRightCorner.x - topLeftCorner.x
    val frameHeight = bottomLeftCorner.y - topLeftCorner.y
    val borderHeight = frameHeight / 3
    val borderWidth = frameWidth / 3

    val left = topLeftCorner.x.toFloat()
    val top = topLeftCorner.y.toFloat()
    val right = bottomRightCorner.x.toFloat()
    val bottom = bottomRightCorner.y.toFloat()

    canvas.drawLine(left + borderWidth, top, left + borderWidth, bottom, borderPaint)
    canvas.drawLine(left + borderWidth * 2, top, left + borderWidth * 2, bottom, borderPaint)
    canvas.drawLine(left, top + borderHeight * 2, right, top + borderHeight * 2, borderPaint)
    canvas.drawLine(left, top + borderHeight, right, top + borderHeight, borderPaint)


  }

  private fun drawCorners(canvas: Canvas){
    //TODO: I should probably think of a better way to do this and move the draw logic inside the class itself.
    drawTopLeftCorner(canvas)
    drawBottomLeftCorner(canvas)
    drawTopRightCorner(canvas)
    drawBottomRightCorner(canvas)
  }

  private fun drawBorders(canvas: Canvas){
    borders.forEach {
      it.draw(canvas)
    }
  }

  private fun drawTopLeftCorner(canvas : Canvas){
    val corner = topLeftCorner
    //Vertical
    canvas.drawRect(
      (corner.x - corner.width).toFloat(),
      (corner.y - corner.width).toFloat(),
      (corner.x + corner.width).toFloat(),
      (corner.y + corner.height).toFloat(),
      cornerPaint
    )
    //Horizontal
    canvas.drawRect(
      (corner.x - corner.width).toFloat(),
      (corner.y - corner.width).toFloat(),
      (corner.x + corner.height).toFloat(),
      (corner.y + corner.width).toFloat(),
      cornerPaint
    )
  }

  private fun drawBottomLeftCorner(canvas : Canvas){

    val corner = bottomLeftCorner
    //Vertical
    canvas.drawRect(
      (corner.x - corner.width).toFloat(),
      (corner.y - corner.height).toFloat(),
      (corner.x + corner.width).toFloat(),
      (corner.y + corner.width).toFloat(),
      cornerPaint
    )
    //Horizontal
    canvas.drawRect(
      (corner.x - corner.width).toFloat(),
      (corner.y - corner.width).toFloat(),
      (corner.x + corner.height).toFloat(),
      (corner.y + corner.width).toFloat(),
      cornerPaint
    )
  }

  private fun drawTopRightCorner(canvas : Canvas){

    val corner = topRightCorner
    //Vertical
    canvas.drawRect(
      (corner.x - corner.width).toFloat(),
      (corner.y - corner.width).toFloat(),
      (corner.x + corner.width).toFloat(),
      (corner.y + corner.height).toFloat(),
      cornerPaint
    )
    //Horizontal
    canvas.drawRect(
      (corner.x - corner.height).toFloat(),
      (corner.y - corner.width).toFloat(),
      (corner.x + corner.width).toFloat(),
      (corner.y + corner.width).toFloat(),
      cornerPaint
    )
  }

  private fun drawBottomRightCorner(canvas : Canvas){

    val corner = bottomRightCorner
    //Vertical
    canvas.drawRect(
      (corner.x - corner.width).toFloat(),
      (corner.y - corner.height).toFloat(),
      (corner.x + corner.width).toFloat(),
      (corner.y + corner.width).toFloat(),
      cornerPaint
    )
    //Horizontal
    canvas.drawRect(
      (corner.x - corner.height).toFloat(),
      (corner.y - corner.width).toFloat(),
      (corner.x + corner.width).toFloat(),
      (corner.y + corner.width).toFloat(),
      cornerPaint
    )
  }

  private inner class RectangleCorner(point : Point){

    var touchRadius: Float = CORNER_TOUCH_RADIUS.toFloat()
    var adjacentX: RectangleCorner? = null
    var adjacentY: RectangleCorner? = null

    val width: Int
      get() = CORNER_WIDTH
    val height: Int
      get() = CORNER_HEIGHT

    var x: Int = point.x
    var y: Int = point.y

    fun intersectsWith(eventX : Int, eventY: Int): Boolean {
      val radCircle = sqrt(((x - eventX) * (x - eventX) + (y - eventY) * (y - eventY)).toDouble())
      return radCircle < this.touchRadius
    }

    fun clampXCoordinate(min: Int, max: Int) {
      x = x.coerceAtLeast(min).coerceAtMost(max)
    }

    fun clampYCoordinate(min: Int, max: Int) {
      y = y.coerceAtLeast(min).coerceAtMost(max)
    }

  }

  enum class RectangleBorderType {
    X,
    Y
  }

  private inner class RectangleBorder(lowest: RectangleCorner, highest: RectangleCorner) {

    var touchRadius: Float = CORNER_TOUCH_RADIUS.toFloat()
    var lowestAdjacentXY:  RectangleCorner = lowest
    var highestAdjacentXY: RectangleCorner = highest
    var typeRectangle : RectangleBorderType = RectangleBorderType.X

    init {
      if(lowestAdjacentXY.x == highestAdjacentXY.x){
        typeRectangle = RectangleBorderType.X
      }
      else if(lowestAdjacentXY.y == highestAdjacentXY.y){
        typeRectangle = RectangleBorderType.Y
      }
    }

    val width: Int
      get() = CORNER_WIDTH
    val height: Int
      get() = BORDER_HEIGHT

    val x: Int
      get() {
        return if(typeRectangle == RectangleBorderType.X){
          lowestAdjacentXY.x
        } else {
          lowestAdjacentXY.x + ((highestAdjacentXY.x - lowestAdjacentXY.x) / 2)
        }
      }

    val y: Int
      get() {
        return if(typeRectangle == RectangleBorderType.X){
          lowestAdjacentXY.y + ((highestAdjacentXY.y - lowestAdjacentXY.y) / 2)
        } else {
          lowestAdjacentXY.y
        }
      }

    fun intersectsWith(eventX : Int, eventY: Int): Boolean {
      val radCircle = sqrt(((x - eventX) * (x - eventX) + (y - eventY) * (y - eventY)).toDouble())
      return radCircle < this.touchRadius
    }

    fun draw(canvas: Canvas){
      if(typeRectangle == RectangleBorderType.X){

        canvas.drawRect(
          (x - width).toFloat(),
          (y - height).toFloat(),
          (x + width).toFloat(),
          (y + height).toFloat(),
          cornerPaint
        )

      }
      else {
        canvas.drawRect(
          (x - height).toFloat(),
          (y - width).toFloat(),
          (x + height).toFloat(),
          (y + width).toFloat(),
          cornerPaint
        )
      }
    }

    fun clampXCoordinate(min: Int, max: Int) {
      if(typeRectangle == RectangleBorderType.X) {
        lowestAdjacentXY.clampXCoordinate(min, max)
        highestAdjacentXY.clampXCoordinate(min, max)
      }
    }

    fun clampYCoordinate(min: Int, max: Int) {
      if(typeRectangle == RectangleBorderType.Y) {
        lowestAdjacentXY.clampYCoordinate(min, max)
        highestAdjacentXY.clampYCoordinate(min, max)
      }
    }

  }
}
