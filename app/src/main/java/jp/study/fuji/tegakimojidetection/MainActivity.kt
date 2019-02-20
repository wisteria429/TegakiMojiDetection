package jp.study.fuji.tegakimojidetection

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import io.reactivex.disposables.CompositeDisposable
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

    private val mainActDisposable = CompositeDisposable()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        bt_clear.setOnClickListener {
            canvas.clear()
            text.text = ""
            image.setImageBitmap(null)
        }

        bt_save_7.setOnClickListener {
            save7()
        }

        canvas.onActionUpListener = {
            classify()
        }

    }

    private fun classify() {
            val b = canvas.getBitmap()

            classifier.classify(b) {
                l ->
                    text.text = formatText(l)
                    image.setImageBitmap(b)
            }
    }

    private fun save7() {
        val repo = FirebaseStorageRepository()
        val d = repo.upload(canvas.getBitmap(), "7").subscribe(
            {Log.v(TAG, "complete")}, {Log.v(TAG, it.message)}
        )
        mainActDisposable.add(d)
    }

    private fun formatText(results:List<TegakiMojiClassifier.Result>):String {
        val top3 = results.subList(0, 3)
        return buildString {
            for(r in top3) {
                append("%s : (%.2f)\n".format(r.label, r.score))
            }
        }

    }

}
