package com.himanshu.firebasemessenger

import android.app.Activity
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import kotlinx.android.synthetic.main.activity_main.*
import java.util.*

class MainActivity : AppCompatActivity() {

    private val TAG: String = "AppDebug"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        image_btn_register.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK)
            intent.type = "image/*"
            startActivityForResult(intent, 0)
        }


        register_button.setOnClickListener {
            registerUser()
        }

        already_have_account_textview.setOnClickListener{
            Log.d(TAG, "MainActivity: Directed to LoginActivity")
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
            finish()
        }
    }

    var selectedImgUri: Uri? = null
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(requestCode == 0 && resultCode == Activity.RESULT_OK && data != null){
            Log.d("AppDebug", "MainActivity: Photo selected.")
            selectedImgUri = data.data
            val bitmap = MediaStore.Images.Media.getBitmap(contentResolver, selectedImgUri)
            image_register.setImageBitmap(bitmap)

            image_btn_register.alpha = 0f
        }
    }

    private fun registerUser(){
        val username = username_register.text.toString()
        val email = email_register.text.toString()
        val password = password_register.text.toString()
        if(email.isEmpty() || password.isEmpty()){
            Toast.makeText(this, "Enter the email and password.", Toast.LENGTH_SHORT).show()
            return
        }
        Log.d(TAG, "MainActivity: username: ${username}, email: ${email}, password: ${password}")
        FirebaseAuth.getInstance().createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener {
                if(!it.isSuccessful){
                    return@addOnCompleteListener
                }
                Toast.makeText(this, "User is registered with uid: ${it.result?.user?.uid}", Toast.LENGTH_SHORT).show()
                uploadImgToFireStorage()
            }
            .addOnFailureListener {
                Log.e("AppDebug", "MainAcivity: ${it.message}")
                Toast.makeText(this, "Error: ${it.message}", Toast.LENGTH_SHORT).show()
//                return@addOnFailureListener
            }
    }

    private fun uploadImgToFireStorage(){
        if(selectedImgUri == null) return
        val filename = UUID.randomUUID().toString()
        val ref = FirebaseStorage.getInstance().getReference("/image/$filename")
        ref.putFile(selectedImgUri!!)
            .addOnSuccessListener {
                Log.d("AppDebug", "MainActivity: Successfully uploaded image: ${it.metadata?.path}")

                ref.downloadUrl.addOnSuccessListener {
                    Log.d("AppDebug", "MainActivity: Location of image: ${it}")
                    saveToFirebaseDatabase(it.toString())
                }
            }
            .addOnFailureListener{
                Log.d("AppDebug", "MainActivity: ${it.message}")
            }
    }

    private fun saveToFirebaseDatabase(imgUrl: String){
        var uid = FirebaseAuth.getInstance().uid ?: ""
        var ref = FirebaseDatabase.getInstance().getReference("/users/${uid}")

        val user = User(uid, username_register.text.toString(), imgUrl)

        ref.setValue(user)
            .addOnSuccessListener {
                Log.d("AppDebug", "MainActivity: Data is stored in Database")
                val intent = Intent(this, LatestMessageActivity::class.java)
                startActivity(intent)
                finish()
            }
            .addOnFailureListener{
                Log.d("AppDebug", "MainActivity: ${it.message}")
            }

    }


}


