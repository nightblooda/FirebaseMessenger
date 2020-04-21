package com.himanshu.firebasemessenger

import android.content.Intent
import android.os.Bundle
import android.os.PersistableBundle
import android.util.Log
import android.widget.Toast
import android.widget.Toast.LENGTH_SHORT
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.activity_login.*

class LoginActivity: AppCompatActivity(){

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        Log.d("AppDebug", "Entered in loginactivity")

        login_button.setOnClickListener {
            loginUser()
        }

        dont_have_account_textveiw.setOnClickListener {
            Log.d("AppDebug", "LoginActivity: Directed to MainActivity")
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish()
        }


    }

    private fun loginUser(){
        val email = email_login.text.toString()
        val password = password_login.text.toString()
        if(email.isEmpty() || password.isEmpty()){
            Toast.makeText(this, "Enter email and password.", Toast.LENGTH_SHORT).show()
            return
        }
        Log.d("AppDebug", "LoginActivity: email: ${email}, password: ${password}")
        FirebaseAuth.getInstance().signInWithEmailAndPassword(email, password)
            .addOnCompleteListener {
                if(!it.isSuccessful){
                    return@addOnCompleteListener
                }
                Toast.makeText(this, "User is logged in with uid: ${it.result?.user?.uid}", LENGTH_SHORT).show()
                val intent = Intent(this, LatestMessageActivity::class.java)
                startActivity(intent)
                finish()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Error: ${it.message}", LENGTH_SHORT).show()
            }
    }
}