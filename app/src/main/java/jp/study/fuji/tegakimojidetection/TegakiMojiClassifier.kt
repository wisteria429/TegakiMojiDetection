package jp.study.fuji.tegakimojidetection

import android.graphics.Bitmap
import android.graphics.Color
import android.os.Build
import android.util.Log
import com.google.firebase.ml.custom.*
import com.google.firebase.ml.custom.model.FirebaseCloudModelSource
import com.google.firebase.ml.custom.model.FirebaseLocalModelSource
import com.google.firebase.ml.custom.model.FirebaseModelDownloadConditions
import java.util.*
import android.util.TimingLogger
import java.nio.ByteBuffer
import java.nio.ByteOrder


class TegakiMojiClassifier(val labels:List<String>) {

    companion object {
        private const val TAG = "TegakiMojiClassifier"

        private const val RESULT_TO_SHOW = 3

        private const val DIM_BATCH_SIZE = 1
        private const val DIM_PIXEL_SIZE = 3

        const val DIM_IMG_SIZE_X = 224
        const val DIM_IMG_SIZE_Y = 224

        private const val CLOUD_MODEL_NAME = "tegaki-moji-detector"
        private const val LOCAL_MODEL_NAME = "local-tegaki-moji-detector"
        private const val LOCAL_MODEL_ASSET_FILE_PATH = "my_model.lite"


    }


    private val imgData:ByteBuffer

    private val sortedLabels : PriorityQueue<Map.Entry<String, Float>> = PriorityQueue(
        RESULT_TO_SHOW,
        kotlin.Comparator { t1, t2-> (t1.value).compareTo(t2.value) }
    )

    init {
        imgData = ByteBuffer.allocateDirect(
            4 * DIM_BATCH_SIZE * DIM_IMG_SIZE_X * DIM_IMG_SIZE_Y * DIM_PIXEL_SIZE
        )
        imgData.order(ByteOrder.nativeOrder())
    }

    private val interpreter: FirebaseModelInterpreter by lazy {
        val options = buildFirebaseModelOptions()
        FirebaseModelInterpreter.getInstance(options)!!
    }


    private val inputOutputOptions : FirebaseModelInputOutputOptions  = buildFirebaseModelInputOutputOptions()



    fun printTopKLables(labelProbArray : Array<FloatArray>):String {
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


    private fun buildFirebaseModelOptions() : FirebaseModelOptions {


        val cloudModelSource = buildCloudSource(CLOUD_MODEL_NAME, buildConditions())
        val localModelSource = buildLocalSource(LOCAL_MODEL_NAME, LOCAL_MODEL_ASSET_FILE_PATH)

        FirebaseModelManager.getInstance().registerCloudModelSource(cloudModelSource)
        FirebaseModelManager.getInstance().registerLocalModelSource(localModelSource)

        return FirebaseModelOptions.Builder()
            .setCloudModelName(CLOUD_MODEL_NAME)
            .setLocalModelName(LOCAL_MODEL_NAME)
            .build()


    }

    private fun  buildLocalSource(
        modelName:String, localModelFileName:String): FirebaseLocalModelSource {
        return FirebaseLocalModelSource.Builder(modelName) // Assign a name to this model
            .setAssetFilePath(localModelFileName)
            .build()
    }

    private fun buildCloudSource(
        modelName:String, conditions: FirebaseModelDownloadConditions): FirebaseCloudModelSource {
        return FirebaseCloudModelSource.Builder(modelName)
            .enableModelUpdates(true)
            .setInitialDownloadConditions(conditions)
            .setUpdatesDownloadConditions(conditions)
            .build()
    }

    private fun buildConditions(): FirebaseModelDownloadConditions {
        var conditionsBuilder: FirebaseModelDownloadConditions.Builder =
            FirebaseModelDownloadConditions.Builder()
                .requireWifi()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            // Enable advanced conditions on Android Nougat and newer.
            conditionsBuilder = conditionsBuilder
                .requireCharging()
                .requireDeviceIdle()
        }
        return conditionsBuilder.build()
    }

    private fun buildFirebaseModelInputOutputOptions() : FirebaseModelInputOutputOptions {
        return FirebaseModelInputOutputOptions.Builder()
            .setInputFormat(0, FirebaseModelDataType.FLOAT32, intArrayOf(1, DIM_IMG_SIZE_X, DIM_IMG_SIZE_Y, DIM_PIXEL_SIZE))
            .setOutputFormat(0, FirebaseModelDataType.FLOAT32, intArrayOf(1, 10))
            .build()
    }

    fun classify(bitmap:Bitmap, callback : (String) -> Unit) {

        val inputs = FirebaseModelInputs.Builder()
            .add(convertBitmapToByteBuffer(bitmap))
            .build()

        interpreter.run(inputs, inputOutputOptions)
            .addOnSuccessListener {
                val output = it.getOutput<Array<FloatArray>>(0)
                val labels = printTopKLables(output)
                callback.invoke(labels)
            }
            .addOnFailureListener { e ->
                Log.v(TAG, "interpreter failure")
            }

    }

    private fun convertBitmapToByteBuffer(bitmap: Bitmap): ByteBuffer {
        val logger = TimingLogger("TAG_TEST", "testTimingLogger")

        imgData.rewind()
        logger.addSplit("create input")


        val intArray = IntArray(DIM_IMG_SIZE_X * DIM_IMG_SIZE_Y)
        bitmap.getPixels(intArray, 0, bitmap.width, 0, 0, bitmap.width, bitmap.height)

        for (i in 0..(intArray.size -1)) {
            val pixel = intArray[i]
            imgData.putFloat((Color.red(pixel) - 127) / 255.0f)
            imgData.putFloat((Color.green(pixel) - 127) / 255.0f)
            imgData.putFloat((Color.blue(pixel) - 127) / 255.0f)
        }

        logger.dumpToLog()
        return imgData
    }
}