package jp.study.fuji.tegakimojidetection.viewmodel

import android.app.Application
import android.content.Intent
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import jp.study.fuji.tegakimojidetection.BuildConfig
import jp.study.fuji.tegakimojidetection.Transition

class AuthViewModel(application: Application) : AndroidViewModel(application) {

    val displayName = MutableLiveData<String>()

    val loginState = MutableLiveData<LoginState>()

    val loginTransitionIntent = MutableLiveData<Transition>()

    companion object {
        private const val TAG = "AuthViewModel"
        private const val RC_SIGN_IN = 9001
    }

    private val auth = FirebaseAuth.getInstance()

    private val googleSignInClient: GoogleSignInClient by lazy {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(BuildConfig.GOOGLE_API_CLIENT_ID)
            .requestEmail()
            .build()

        GoogleSignIn.getClient(application, gso)
    }

    fun onResume() {
        updateState()


    }

    fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == RC_SIGN_IN) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                // Google Sign In was successful, authenticate with Firebase
                val account = task.getResult(ApiException::class.java)
                account?.let {
                    firebaseAuthWithGoogle(it)
                }
            } catch (e: ApiException) {
                // Google Sign In failed, update UI appropriately
                Log.w(TAG, "Google sign in failed", e)
                logout()
                // ...
            }

        }
    }

    fun login() {
        Log.v(TAG, "login")
        val signInIntent = googleSignInClient.signInIntent
        loginTransitionIntent.postValue(Transition(signInIntent, RC_SIGN_IN))
    }

    fun logout() {
        Log.v(TAG, "logout")

        auth.signOut()
        googleSignInClient.signOut()
            .addOnCompleteListener { updateState() }

    }

    private fun firebaseAuthWithGoogle(acct: GoogleSignInAccount) {
        Log.d(TAG, "firebaseAuthWithGoogle:" + acct.id!!)

        val credential = GoogleAuthProvider.getCredential(acct.idToken, null)
        auth.signInWithCredential(credential)
            .addOnCompleteListener { task ->
                if (!task.isSuccessful) return@addOnCompleteListener

                updateState()

            }

    }

    private fun updateState() {
        displayName.postValue(
            auth.currentUser?.displayName ?: "未ログイン")

        if (auth.currentUser == null) {
            loginState.postValue(LoginState.LOGOUT)
        } else {
            loginState.postValue(LoginState.LOGIN)
        }
    }

    enum class LoginState {
        LOGIN, LOGOUT, PROGRESS
    }
}