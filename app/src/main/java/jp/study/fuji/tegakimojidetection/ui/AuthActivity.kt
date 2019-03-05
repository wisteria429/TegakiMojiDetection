package jp.study.fuji.tegakimojidetection.ui

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import jp.study.fuji.tegakimojidetection.R
import jp.study.fuji.tegakimojidetection.Transition
import jp.study.fuji.tegakimojidetection.databinding.ActivityAuthBinding
import jp.study.fuji.tegakimojidetection.viewmodel.AuthViewModel


class AuthActivity : AppCompatActivity() {

    companion object {
        private const val TAG = "AuthActivity"
    }



    private lateinit var viewModel: AuthViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.v(TAG, "onCreate")

        viewModel = ViewModelProviders.of(this).get(AuthViewModel::class.java)
        val binding = DataBindingUtil
            .setContentView(this, R.layout.activity_auth) as ActivityAuthBinding

        binding.lifecycleOwner = this
        binding.viewmodel = viewModel

        viewModel.loginTransitionIntent.observe(this, Observer<Transition?> {
            it?.let(this@AuthActivity::transition)
        })

        supportActionBar?.apply {
            title = "ログイン"
            setDisplayHomeAsUpEnabled(true)
        }
    }

    override fun onResume() {
        super.onResume()
        viewModel.onResume()
    }

    private fun transition(transition : Transition) {
        startActivityForResult(transition.intent, transition.reqCode)
    }




    public override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        viewModel.onActivityResult(requestCode, resultCode, data)


    }


}
