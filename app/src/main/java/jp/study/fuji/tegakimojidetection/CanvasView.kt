package jp.study.fuji.tegakimojidetection

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View

class CanvasView: View {
    private val path : Path = Path()
    private val paint : Paint = Paint()
    var onActionUpListener: (() -> Unit)? = null


    init {
        paint.apply {
            color = Color.rgb(0,0,0)
            style = Paint.Style.STROKE
            strokeJoin = Paint.Join.ROUND
            strokeCap = Paint.Cap.ROUND
            strokeWidth = 80f
        }

    }

    constructor(context: Context) : super(context, null)
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)


    override fun onDraw(canvas: Canvas?) {
        canvas?.apply {
            drawPath(path, paint)
        }

        super.onDraw(canvas)

    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        event?.apply {
            when(action) {
                MotionEvent.ACTION_DOWN -> {
                    path.moveTo(x, y)
                    invalidate()
                }
                MotionEvent.ACTION_MOVE -> {
                    path.lineTo(x, y)
                    invalidate()
                }
                MotionEvent.ACTION_UP -> {
                    path.lineTo(x, y)
                    invalidate()
                    onActionUpListener?.invoke()
                }
            }
            return true
        }

        return false
    }

    fun clear() {
        path.reset()
        invalidate()
    }

    fun getBitmap() :Bitmap {
        val b = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565)

        val c = Canvas(b)
        draw(c)

        return Bitmap.createScaledBitmap(b, TegakiMojiClassifier.DIM_IMG_SIZE_X, TegakiMojiClassifier.DIM_IMG_SIZE_Y, false)

    }

}