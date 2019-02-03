package jp.study.fuji.tegakimojidetection

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
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

    private val classifier:TegakiMojiClassifier by lazy {
        TegakiMojiClassifier(labels)
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

            classifier.classify(b) {
                l ->
                    text.text = l
                    image.setImageBitmap(b)
            }
    }

}
