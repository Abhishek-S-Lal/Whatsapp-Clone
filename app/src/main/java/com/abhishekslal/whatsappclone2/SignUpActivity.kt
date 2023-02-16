package com.abhishekslal.whatsappclone2

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.abhishekslal.whatsappclone2.databinding.ActivitySignUpBinding
import com.google.android.gms.tasks.Continuation
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.UploadTask
import com.abhishekslal.whatsappclone2.models.User

class SignUpActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySignUpBinding
    private val storage by lazy {
        FirebaseStorage.getInstance()
    }
    private val auth by lazy {
        FirebaseAuth.getInstance()
    }
    private val database by lazy {
        FirebaseFirestore.getInstance()
    }
    private lateinit var downloadUrl: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignUpBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        //Assignment 2 - Add Crop Image option along with a thumbnail using firebase extension
        binding.userImgView.setOnClickListener {
            checkPermissionForImage()
        }
        binding.nextBtn.setOnClickListener {
            val name = binding.nameEt.text.toString()
            if (!::downloadUrl.isInitialized) {
                toast("Photo cannot be empty")
            } else if (name.isEmpty()) {
                toast("Name cannot be empty")
            } else {
                val user = User(name, downloadUrl, downloadUrl/*Needs to thumbnai url*/, auth.uid!!)
                Log.d("SignUpActivity", user.toString())
                database.collection("users").document(auth.uid!!).set(user).addOnSuccessListener {
                    Toast.makeText(this,"Successfully Registered", Toast.LENGTH_SHORT).show()
                    val intent = Intent(this, MainActivity::class.java)
                    startActivity(intent)
                    finish()
                }.addOnFailureListener {
                    binding.nextBtn.isEnabled = true
                    Toast.makeText(this,it.message, Toast.LENGTH_SHORT).show()
                }
            }
        }

    }

    override fun onBackPressed() {

    }


    private fun checkPermissionForImage() {
        if ((checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED)
            && (checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED)
        ) {
            Toast.makeText(this,"If case", Toast.LENGTH_SHORT).show()

            val permission = arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE)
            val permissionWrite = arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE)
            val permissionReadImages = arrayOf(Manifest.permission.READ_MEDIA_IMAGES)

            requestPermissions(
                permission,
                1001
            ) // GIVE AN INTEGER VALUE FOR PERMISSION_CODE_READ LIKE 1001
            requestPermissions(
                permissionWrite,
                1002
            ) // GIVE AN INTEGER VALUE FOR PERMISSION_CODE_WRITE LIKE 1002
            requestPermissions(
                permissionReadImages,
                1003
            ) // GIVE AN INTEGER VALUE FOR PERMISSION_CODE_WRITE LIKE 1003

            pickImageFromGallery()
        } else {
            pickImageFromGallery()
        }
    }



    private fun pickImageFromGallery() {
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        startActivityForResult(
            intent,
            1000
        ) // GIVE AN INTEGER VALUE FOR IMAGE_PICK_CODE LIKE 1000
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK && requestCode == 1000) {
            data?.data?.let {
                binding.userImgView.setImageURI(it)
                startUpload(it)
            }
        }
    }

    private fun startUpload(filePath: Uri) {
        binding.nextBtn.isEnabled = false
        val ref = storage.reference.child("uploads/" + auth.uid.toString())
        val uploadTask = ref.putFile(filePath)
        uploadTask.continueWithTask(Continuation<UploadTask.TaskSnapshot, Task<Uri>> { task ->
            if (!task.isSuccessful) {
                task.exception?.let {
                    throw it
                }
            }
            return@Continuation ref.downloadUrl
        }).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                downloadUrl = task.result.toString()
                binding.nextBtn.isEnabled = true
            } else {
                binding.nextBtn.isEnabled = true
                // Handle failures
            }
        }.addOnFailureListener {

        }
    }

}