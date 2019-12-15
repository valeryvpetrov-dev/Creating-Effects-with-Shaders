package dev.valeryvpetrov.findme

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.MotionEvent
import androidx.appcompat.widget.AppCompatImageView
import kotlin.math.floor
import kotlin.random.Random

class SpotLightImageView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : AppCompatImageView(context, attrs, defStyleAttr) {

    private var paint = Paint()
    private var shouldDrawSpotlight = false
    private var isGameOver = false

    private lateinit var winnerRect: RectF
    private var androidBitmapX = 0f
    private var androidBitmapY = 0f

    private val bitmapAndroid = BitmapFactory.decodeResource(resources, R.drawable.android)
    private val bitmapSpotlight = BitmapFactory.decodeResource(resources, R.drawable.mask)

    private var shader: Shader

    private val shaderMatrix = Matrix()

    init {
        val bitmap = Bitmap.createBitmap(
            bitmapSpotlight.width,
            bitmapSpotlight.height,
            Bitmap.Config.ARGB_8888
        )

        val canvas = Canvas(bitmap)
        val shaderPaint = Paint(Paint.ANTI_ALIAS_FLAG)
        shaderPaint.color = Color.BLACK

        // Draw a black rectangle
        canvas.drawRect(0.0f, 0.0f,
            bitmapSpotlight.width.toFloat(), bitmapSpotlight.height.toFloat(),
            shaderPaint
        )

        // Use the DST_OUT compositing mode to mask out the spotlight from the black rectangle
        shaderPaint.xfermode = PorterDuffXfermode(PorterDuff.Mode.DST_OUT)
        canvas.drawBitmap(bitmapSpotlight, 0.0f, 0.0f, shaderPaint)

        // Fill remaining space with bitmap edge pattern (black color)
        shader = BitmapShader(bitmap, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP)
        paint.shader = shader
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        setupWinnerRect()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        canvas.drawColor(Color.WHITE)
        canvas.drawBitmap(bitmapAndroid, androidBitmapX, androidBitmapY, paint)
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        val motionEventX = event.x
        val motionEventY = event.y

        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                shouldDrawSpotlight = true
                if (isGameOver) {   // Restart the game.
                    isGameOver = false
                    setupWinnerRect()
                }
            }
            MotionEvent.ACTION_UP -> {
                shouldDrawSpotlight = false
                isGameOver = winnerRect.contains(motionEventX, motionEventY)
            }
        }

        // Replace spotlight.
        shaderMatrix.setTranslate(
            motionEventX - bitmapSpotlight.width / 2.0f,
            motionEventY - bitmapSpotlight.height / 2.0f
        )
        shader.setLocalMatrix(shaderMatrix)

        invalidate()    // Redraw.
        return true
    }

    /**
     * Generates random location of android image.
     * Defines winner area according to android image position.
     */
    private fun setupWinnerRect() {
        androidBitmapX = floor(Random.nextFloat() * (width - bitmapAndroid.width))
        androidBitmapY = floor(Random.nextFloat() * (height - bitmapAndroid.height))

        winnerRect = RectF(
            androidBitmapX,
            androidBitmapY,
            androidBitmapX + bitmapAndroid.width,
            androidBitmapY + bitmapAndroid.height
        )
    }
}