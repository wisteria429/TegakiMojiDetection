package jp.study.fuji.tegakimojidetection

import android.graphics.Bitmap
import android.util.Log
import org.tensorflow.lite.Interpreter
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.MappedByteBuffer
import java.util.*

class TegakiMojiClassifier(model:MappedByteBuffer, val labels:List<String>) {

    companion object {
        private const val RESULT_TO_SHOW = 3

        private const val DIM_BATCH_SIZE = 1
        private const val DIM_PIXEL_SIZE = 3

        const val DIM_IMG_SIZE_X = 224
        const val DIM_IMG_SIZE_Y = 224

    }

    private val tfLite: Interpreter = Interpreter(model)
    private val imgData : ByteBuffer
    private val intValues : IntArray = IntArray(DIM_IMG_SIZE_X * DIM_IMG_SIZE_Y)
    private val labelProbArray : Array<FloatArray>

    private val sortedLabels : PriorityQueue<Map.Entry<String, Float>> = PriorityQueue(
        RESULT_TO_SHOW,
        kotlin.Comparator { t1, t2-> (t1.value).compareTo(t2.value) }
    )

    init {
        imgData = ByteBuffer.allocateDirect(
            4 * DIM_BATCH_SIZE * DIM_IMG_SIZE_X * DIM_IMG_SIZE_Y * DIM_PIXEL_SIZE
        )
        imgData.order(ByteOrder.nativeOrder())

        labelProbArray = Array(1) { FloatArray(labels.size)}
    }

    fun classifyFrame(bitmap : Bitmap):String {
        convertBitmapToByteBuffer(bitmap)

        tfLite.run(imgData, labelProbArray)

        return printTopKLables()

    }


    fun convertBitmapToByteBuffer(bitmap : Bitmap) {
        imgData.rewind()
        bitmap.getPixels(intValues, 0, bitmap.width, 0, 0, bitmap.width, bitmap.height)

        var pixel = 0
        for (i in 0..(DIM_IMG_SIZE_X -1)) {
            for (j in 0..(DIM_IMG_SIZE_Y -1)) {
                val value = intValues[pixel++]
                imgData.putFloat(((value shr 16) and 0xFF).toFloat() / 255)
                imgData.putFloat(((value shr 8) and 0xFF).toFloat() / 255)
                imgData.putFloat((value and 0xFF).toFloat() / 255)
            }
        }
    }

    fun printTopKLables():String {
        for (i in 0..(labels.size -1)) {
            sortedLabels.add(AbstractMap.SimpleEntry(labels.get(i), labelProbArray[0][i]))
            if (sortedLabels.size > RESULT_TO_SHOW) {
                sortedLabels.poll()
            }
        }
        val textToShow = StringBuilder()
        val size = sortedLabels.size
        for (i in 0..(size - 1)) {
            val label = sortedLabels.poll()
            textToShow.insert(0, "%s: %4.2f\n".format(label.key,label.value))
        }
        return textToShow.toString()
    }
}