package com.tonial.apicarros

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.Task
import com.google.firebase.FirebaseException
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import com.tonial.apicarros.databinding.ActivityLoginBinding
import java.util.concurrent.TimeUnit

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var googleSignInClient: GoogleSignInClient
    private var verificationId = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()

        // Configuração do Google Sign-In (usa o default_web_client_id gerado pelo google-services.json)
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)//informação padrao (id)
            .requestIdToken(getString(R.string.default_web_client_id))//Pede token pra validar no firebase
            .requestEmail()
            .build()

        googleSignInClient = GoogleSignIn.getClient(this, gso)

        setupView()
    }

    override fun onResume() {
        super.onResume()
        verifyUserLogged()
    }

    private fun verifyUserLogged() {
        if (auth.currentUser != null) {
            navigateToMainActivity()
        }
    }

    private fun setupView() {
        binding.btnSendSms.setOnClickListener {
            sendVerificationCode()
        }
        binding.btnVerifySms.setOnClickListener {
            verifyCode()
        }
        // Configura o clique do botão do Google
        binding.googleSignInButton.setOnClickListener {
            signInWithGoogle()
        }
    }

    //valida se deu certo a credencial
    private fun onCredentialCompleteListener(task: Task<AuthResult>) {
        if (task.isSuccessful) {
            navigateToMainActivity()
        } else {
            Toast.makeText(this, task.exception?.message, Toast.LENGTH_SHORT).show()
        }
    }

    private fun navigateToMainActivity() {
        startActivity(MainActivity.newIntent(this))
        finish()
    }

    //region autentica com sms

    //manda verificação pelo celular
    private fun sendVerificationCode() {
        val phoneNumber = binding.cellphone.text.toString()
        val options = PhoneAuthOptions.newBuilder(auth)
            .setPhoneNumber(phoneNumber)
            .setTimeout(45L, TimeUnit.SECONDS)
            .setActivity(this)
            .setCallbacks(
                object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
                    override fun onVerificationCompleted(credential: PhoneAuthCredential) {
                    }

                    override fun onVerificationFailed(exception: FirebaseException) {
                        Toast.makeText(
                            this@LoginActivity,
                            "${exception.message}",
                            Toast.LENGTH_LONG
                        ).show()
                    }

                    override fun onCodeSent(verificationId: String, token: PhoneAuthProvider.ForceResendingToken) {
                        this@LoginActivity.verificationId = verificationId
                        binding.veryfyCode.visibility = View.VISIBLE
                        binding.btnVerifySms.visibility = View.VISIBLE
                        Toast.makeText(
                            this@LoginActivity,
                            "Código de verificação enviado via SMS",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                })
            .build()
        PhoneAuthProvider.verifyPhoneNumber(options)
    }

    //valida codigo enviado
    private fun verifyCode() {
        val code = binding.veryfyCode.text.toString()
        val credential = PhoneAuthProvider.getCredential(verificationId, code)
        auth.signInWithCredential(credential)
            .addOnCompleteListener { onCredentialCompleteListener(it) }
    }

    //endregion autentica com sms

    //region autenticar com email google

    // Launcher para capturar o resultado da tela de seleção de conta do Google
    // Ele que valida o token e passa para o firebaseAuthWithGoogle
    private val googleSignInLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
            try {
                val account = task.getResult(ApiException::class.java)!!
                firebaseAuthWithGoogle(account.idToken!!)
            } catch (e: ApiException) {
                Toast.makeText(this, "Falha no Google Sign-In: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    //abre a tela dos emails
    private fun signInWithGoogle() {
        val signInIntent = googleSignInClient.signInIntent
        googleSignInLauncher.launch(signInIntent)
    }

    //comunica firebase e google
    //manda o token e cria a credencial
    private fun firebaseAuthWithGoogle(idToken: String) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        auth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                onCredentialCompleteListener(task)
            }
    }

    //endregion autenticar com email google

    companion object {
        fun newIntent(context: Context): Intent {
            return Intent(context, LoginActivity::class.java)
        }
    }
}
