package com.example.myapitest

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.myapitest.databinding.ActivityLoginBinding
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import com.google.firebase.FirebaseException
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.PhoneAuthCredential
import java.util.concurrent.TimeUnit

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private lateinit var auth: FirebaseAuth
    private var verificationId = ""


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()

        skipLogin() //ONLY FOR TESTS
        setupView()

    }

    override fun onResume() {
        super.onResume()
        verifyLoggedUser()
    }

    private fun verifyLoggedUser() {
        if (auth.currentUser != null) {
            navigateToMainActivity()
        }
    }

    private fun navigateToMainActivity() {
        startActivity(MainActivity.newIntent(this))
        finish()
    }

    private fun skipLogin() {
        binding.btnSkipLogin.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            //finish()
        }
    }

    private fun setupView() {
        binding.btnSendSms.setOnClickListener {
            sendVerificationCode()
        }

        binding.btnVerifySms.setOnClickListener {
            verifyCode()
        }
    }

    fun sendVerificationCode() {
        val phoneNumber = binding.cellphone.text.toString()
        val options = PhoneAuthOptions.newBuilder(auth)
            .setPhoneNumber(phoneNumber)
            .setTimeout(45L, TimeUnit.SECONDS)
            .setActivity(this)
            .setCallbacks(object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {

                override fun onVerificationCompleted(credential: PhoneAuthCredential) {
                    signInWithPhoneAuthCredential(credential)
                }

                override fun onVerificationFailed(exception: FirebaseException) {
                    Toast.makeText(
                        this@LoginActivity,
                        getString(R.string.error_generic, exception.message),
                        Toast.LENGTH_SHORT
                    ).show()
                }

                override fun onCodeSent(
                    verificationId: String,
                    token: PhoneAuthProvider.ForceResendingToken
                ) {
                    this@LoginActivity.verificationId = verificationId
                    binding.verifyCode.visibility = View.VISIBLE
                    binding.btnVerifySms.visibility = View.VISIBLE
                    Toast.makeText(this@LoginActivity, getString(R.string.msg_code_sent), Toast.LENGTH_SHORT).show()
                }
            })
            .build()
        PhoneAuthProvider.verifyPhoneNumber(options)
    }

    fun verifyCode() {
        val code = binding.verifyCode.text.toString()
        if (verificationId.isNotEmpty() && code.isNotEmpty()) {
            val credential = PhoneAuthProvider.getCredential(verificationId, code)
            signInWithPhoneAuthCredential(credential)
        }
    }

    private fun signInWithPhoneAuthCredential(credential: PhoneAuthCredential) {
        auth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                onCredentialCompleteListener(task)
            }
    }

    private fun onCredentialCompleteListener(task: Task<AuthResult>) {
        if (task.isSuccessful) {
            navigateToMainActivity()
        } else {
            Toast.makeText(this, getString(R.string.error_generic, task.exception?.message), Toast.LENGTH_SHORT).show()
        }
    }

    companion object {
        fun newIntent(context: Context) =
            Intent(context, LoginActivity::class.java)
    }
}
