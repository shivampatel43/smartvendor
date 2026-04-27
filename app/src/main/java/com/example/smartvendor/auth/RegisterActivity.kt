package com.example.smartvendor.auth

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.example.smartvendor.MainActivity
import com.example.smartvendor.databinding.ActivityRegisterBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import java.util.UUID
import kotlin.random.Random

class RegisterActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRegisterBinding
    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()
    private val storage = FirebaseStorage.getInstance()
    private var selectedImageUri: Uri? = null

    private val imagePickerLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        if (uri != null) {
            selectedImageUri = uri
            binding.ivShopImage.setImageURI(uri)
            binding.ivShopImage.setPadding(0, 0, 0, 0) // Remove padding when image is set
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Show image picker only for vendors
        binding.rgRole.setOnCheckedChangeListener { _, checkedId ->
            binding.cvImagePicker.visibility = if (checkedId == binding.rbVendor.id) View.VISIBLE else View.GONE
        }

        binding.cvImagePicker.setOnClickListener {
            imagePickerLauncher.launch("image/*")
        }

        binding.btnRegister.setOnClickListener {
            registerUser()
        }

        binding.tvLogin.setOnClickListener {
            finish()
        }
    }

    private fun registerUser() {
        val name = binding.etName.text.toString()
        val email = binding.etEmail.text.toString()
        val password = binding.etPassword.text.toString()
        val role = if (binding.rbVendor.isChecked) "vendor" else "customer"

        if (name.isEmpty() || email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
            return
        }

        if (role == "vendor" && selectedImageUri == null) {
            Toast.makeText(this, "Please select a shop image", Toast.LENGTH_SHORT).show()
            return
        }

        binding.btnRegister.isEnabled = false
        binding.btnRegister.text = "Creating Account..."

        auth.createUserWithEmailAndPassword(email, password)
            .addOnSuccessListener { result ->
                val uid = result.user?.uid ?: return@addOnSuccessListener
                
                if (role == "vendor" && selectedImageUri != null) {
                    uploadImageAndSaveUser(uid, name, email, role)
                } else {
                    saveUserData(uid, name, email, role, "")
                }
            }
            .addOnFailureListener {
                binding.btnRegister.isEnabled = true
                binding.btnRegister.text = "Create Account"
                Toast.makeText(this, "Registration Failed: ${it.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun uploadImageAndSaveUser(uid: String, name: String, email: String, role: String) {
        val fileName = "vendor_images/${UUID.randomUUID()}.jpg"
        val storageRef = storage.reference.child(fileName)

        storageRef.putFile(selectedImageUri!!)
            .addOnSuccessListener {
                storageRef.downloadUrl.addOnSuccessListener { uri ->
                    saveUserData(uid, name, email, role, uri.toString())
                }
            }
            .addOnFailureListener {
                saveUserData(uid, name, email, role, "") // Save even if image fails
            }
    }

    private fun saveUserData(uid: String, name: String, email: String, role: String, imageUrl: String) {
        // Default mock location for new vendors (near Delhi)
        val lat = 28.6139 + (Random.nextDouble() - 0.5) * 0.1
        val lon = 77.2090 + (Random.nextDouble() - 0.5) * 0.1

        val userData = hashMapOf(
            "uid" to uid,
            "name" to name,
            "email" to email,
            "role" to role,
            "latitude" to lat,
            "longitude" to lon,
            "location" to "Delhi, India",
            "imageUrl" to imageUrl,
            "rating" to 0.0,
            "numRatings" to 0
        )

        db.collection("users").document(uid).set(userData)
            .addOnSuccessListener {
                startActivity(Intent(this, MainActivity::class.java))
                finish()
            }
            .addOnFailureListener {
                binding.btnRegister.isEnabled = true
                binding.btnRegister.text = "Create Account"
                Toast.makeText(this, "Failed to save user data", Toast.LENGTH_SHORT).show()
            }
    }
}
