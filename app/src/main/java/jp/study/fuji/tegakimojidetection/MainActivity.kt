package jp.study.fuji.tegakimojidetection

import android.graphics.Bitmap
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.crashlytics.android.Crashlytics
import kotlinx.android.synthetic.main.activity_main.*
import java.nio.MappedByteBuffer

class MainActivity : AppCompatActivity() {
    companion object {
        private const val TAG = "MainActivity"
    }

    private val labels:List<String> by lazy {
        val loader = TfAssetLoader(assets)
        loader.loadTfLabelList()

    }
    private val model:MappedByteBuffer by lazy {
        val loader = TfAssetLoader(assets)
        loader.loadTfModelFile()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        bt_clear.setOnClickListener {
            canvas.clear()
            text.text = ""
            image.setImageBitmap(null)
        }


        canvas.onActionUpListener = {
            classify()
        }

    }

    private fun classify() {
            val b = canvas.getBitmap()

            image.setImageBitmap(b)
            val classify = TegakiMojiClassifier(model, labels).classifyFrame(b)
            text.text = classify
    }
}
