package jp.study.fuji.tegakimojidetection

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import io.reactivex.disposables.CompositeDisposable
import jp.study.fuji.tegakimojidetection.ui.AuthActivity
import kotlinx.android.synthetic.main.activity_main.*
import java.nio.MappedByteBuffer

class MainActivity : AppCompatActivity() {
    companion object {
        private const val TAG = "MainActivity"
    }

    private val labels: List<String> by lazy {
        val loader = TfAssetLoader(assets)
        loader.loadTfLabelList()

    }
    private val model: MappedByteBuffer by lazy {
        val loader = TfAssetLoader(assets)
        loader.loadTfModelFile()
    }

    private val classifier: TegakiMojiClassifier by lazy {
        TegakiMojiClassifier(labels)
    }

    private var candidate: InputCandidate? = null


    private val mainActDisposable = CompositeDisposable()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)
        getCandidate()

        bt_clear.setOnClickListener {
            canvas.clear()
            text.text = ""
            image.setImageBitmap(null)
        }

        bt_save_7.setOnClickListener {
            save7()
        }

        bt_login.setOnClickListener {

            startActivity(Intent(this, AuthActivity::class.java))
        }

        canvas.onActionUpListener = {
            classify()
        }

        supportActionBar?.title = getInputTheme()

    }

    private fun getInputTheme(): String {
        var rtn = "0"
        candidate?.let {
            rtn = it.candidate.random()
        }

        return rtn
    }

    private fun getCandidate() {
        val config = FirebaseRemoteConfig.getInstance()
        config.setConfigSettings(
            FirebaseRemoteConfigSettings.Builder()
                .setDeveloperModeEnabled(true).build()
        )
        config.setDefaults(R.xml.remote_config_defaults)
        config.fetch().addOnSuccessListener {
            val adapter = Moshi.Builder()
                .add(KotlinJsonAdapterFactory())
                .build().adapter(InputCandidate::class.java)
            candidate = adapter.fromJson(config.getString("input_candidate"))
            supportActionBar?.title = getInputTheme()
        }

    }

    private fun classify() {
        val b = canvas.getBitmap()

        classifier.classify(b) { l ->
            text.text = formatText(l)
            image.setImageBitmap(b)
        }
    }

    private fun save7() {
        val repo = FirebaseStorageRepository()
        val d = repo.upload(canvas.getBitmap(), "7").subscribe(
            { Log.v(TAG, "complete") }, { Log.v(TAG, it.message) }
        )
        mainActDisposable.add(d)
    }

    private fun formatText(results: List<TegakiMojiClassifier.Result>): String {
        val top3 = results.subList(0, 3)
        return buildString {
            for (r in top3) {
                append("%s : (%.2f)\n".format(r.label, r.score))
            }
        }

    }

}
