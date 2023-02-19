package com.abhishekslal.whatsappclone2

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth

class SplashActivity : AppCompatActivity() {
//    i) Lazy initialization was designed to prevent unnecessary initialization of objects.
//    ii) Your property will not be initialized unless you use it.
//    iii) It is initialized only once. Next time when you use it, you get the value from cache memory.
//    iv) lazy { ... } delegate can only be used for val properties
    private val auth by lazy {
        FirebaseAuth.getInstance()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if(auth.currentUser == null){
            startActivity(Intent(this,LoginActivity::class.java))
        }else{
            startActivity(Intent(this,MainActivity::class.java))
        }
        finish()

    }
}