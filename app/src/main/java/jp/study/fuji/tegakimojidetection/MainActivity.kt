package jp.study.fuji.tegakimojidetection

import android.graphics.Bitmap
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {
    companion object {
        private const val TAG = "MainActivity"
        private val IMAGE_MEAN = 128
        private val IMAGE_STD = 128.0f
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        bt_clear.setOnClickListener {
            canvas.clear()
            text.text = ""
        }



        val loader = TfAssetLoader(assets)
        val labels = loader.loadTfLabelList()
        Log.v(TAG, labels[0] + ":"+ labels.size)

        val mapper = loader.loadTfModelFile()
        Log.v(TAG, mapper.toString())

        bt_detection.setOnClickListener {
            val b = canvas.getBitmap()

            image.setImageBitmap(b)
            val classify = TegakiMojiClassifier(mapper, labels).classifyFrame(b)
            text.text = classify
        }
    }
}
