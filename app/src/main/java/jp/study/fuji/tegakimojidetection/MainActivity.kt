package jp.study.fuji.tegakimojidetection

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {
    companion object {
        private const val TAG = "MainActivity"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        bt_clear.setOnClickListener {
            canvas.clear()
        }

        val loader = TfAssetLoader(assets)
        val labels = loader.loadTfLabelList()
        Log.v(TAG, labels[0] + ":"+ labels.size)

        val mapper = loader.loadTfModelFile()
        Log.v(TAG, mapper.toString())
    }
}
