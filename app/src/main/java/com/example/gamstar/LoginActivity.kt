package com.example.gamstar

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.facebook.AccessToken
import com.facebook.CallbackManager
import com.facebook.FacebookCallback
import com.facebook.FacebookException
import com.facebook.login.LoginManager
import com.facebook.login.LoginResult
import com.google.android.gms.auth.api.Auth
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.FacebookAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.android.synthetic.main.activity_login.*
import java.util.*


class LoginActivity : AppCompatActivity() {

    var auth : FirebaseAuth? = null

    var googleSignInClient: GoogleSignInClient? = null

    var callbackManager : CallbackManager? = null

    val GOOGLE_LOGIN_CODE = 9001


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        auth = FirebaseAuth.getInstance()

        var gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()

        googleSignInClient = GoogleSignIn.getClient(this, gso)
        callbackManager = CallbackManager.Factory.create()

        google_SigninButton.setOnClickListener { googleLogin() }
        facebook_SigninButton.setOnClickListener { facebookLogin() }
        email_SigninButton.setOnClickListener { emainLogin() }

        email_SignupText.setOnClickListener { moveSignUpPage() }
    }
    fun moveSignUpPage(){
        startActivity(Intent(this, SignUpActivity()::class.java))
        finish()
    }

    fun moveMainPage(user: FirebaseUser?){
        if(user != null){
            //progress_bar.visibility = View.VISIBLE
            Toast.makeText(this, getString(R.string.signin_complete), Toast.LENGTH_SHORT).show()
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }
    }

    fun googleLogin() {
        progress_bar.visibility = View.VISIBLE
        var signInIntent = googleSignInClient?.signInIntent
        startActivityForResult(signInIntent, GOOGLE_LOGIN_CODE)
    }


    fun facebookLogin() {
        progress_bar.visibility = View.VISIBLE

        LoginManager.getInstance().logInWithReadPermissions(this, Arrays.asList("public_profile", "email"))
        LoginManager.getInstance().registerCallback(callbackManager, object : FacebookCallback<LoginResult>{
            override fun onSuccess(result: LoginResult) {
                handleFacebookAccessToken(result.accessToken)
            }

            override fun onCancel() {
                progress_bar.visibility = View.GONE
            }

            override fun onError(error: FacebookException?) {
                progress_bar.visibility = View.GONE
            }
        })
    }

    fun handleFacebookAccessToken(token: AccessToken){
        val credential = FacebookAuthProvider.getCredential(token.token)
        auth?.signInWithCredential(credential)
            ?.addOnCompleteListener { task ->
                progress_bar.visibility = View.GONE
                if(task.isSuccessful){
                    moveMainPage(auth?.currentUser)
                }
            }
    }

    fun emainLogin(){
        if(email_editText.text.toString().isNullOrEmpty() || password_editText.text.toString().isNullOrEmpty()){
            Toast.makeText(this, "아이디 혹은 패스워드를 입력해주세요.", Toast.LENGTH_SHORT).show()
        } else{
            signinEmail()
        }
    }




    fun signinEmail() {
        auth?.signInWithEmailAndPassword(email_editText.text.toString(), password_editText.text.toString())
            ?.addOnCompleteListener {
                    task ->
                        if (task.isSuccessful) {
                            Toast.makeText(this, "로그인 성공", Toast.LENGTH_SHORT).show()
                            moveMainPage(task.result?.user)
                        } else {
                            //Toast.makeText(this, task.exception?.message, Toast.LENGTH_SHORT).show()
                            Toast.makeText(this, "존재하지 않는 계정입니다.", Toast.LENGTH_SHORT).show()
                        }
            }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        // 페이스북 SDK로 Data 넘겨주기
        callbackManager?.onActivityResult(requestCode, resultCode, data)


        //구글에서 Accepted Data 가져오기
        if (requestCode == GOOGLE_LOGIN_CODE && resultCode == Activity.RESULT_OK) {
            val result = Auth.GoogleSignInApi.getSignInResultFromIntent(data)

            if (result!!.isSuccess) {
                val account = result.signInAccount
                firebaseAuthWithGoogle(account!!)
            } else {
                progress_bar.visibility = View.GONE
            }
        }
    }

    private fun firebaseAuthWithGoogle(account: GoogleSignInAccount) {
        val credential = GoogleAuthProvider.getCredential(account.idToken, null)
        auth?.signInWithCredential(credential)
            ?.addOnCompleteListener { task->
                progress_bar.visibility = View.GONE
                if(task.isSuccessful){
                    moveMainPage(auth?.currentUser)
                }
            }
    }

    override fun onStart() {
        super.onStart()

        moveMainPage(auth?.currentUser)
    }

}
