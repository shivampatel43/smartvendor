package com.example.smartvendor.vendor

import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.example.smartvendor.data.model.Product
import com.example.smartvendor.databinding.ActivityAddProductBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import java.util.*

class AddProductActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAddProductBinding
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private val storage = FirebaseStorage.getInstance() // Uses default bucket from google-services.json
    
    private var selectedImageUri: Uri? = null

    private val imagePicker = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        if (uri != null) {
            selectedImageUri = uri
            binding.ivProductImage.setImageURI(uri)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddProductBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnSelectImage.setOnClickListener {
            imagePicker.launch("image/*")
        }

        binding.btnAddProduct.setOnClickListener {
            saveFullProduct()
        }
        
        binding.toolbar.setNavigationOnClickListener { finish() }
    }

    private fun saveFullProduct() {
        val name = binding.etProductName.text.toString()
        val priceStr = binding.etPrice.text.toString()

        if (name.isEmpty() || priceStr.isEmpty()) {
            Toast.makeText(this, "Name and price are required", Toast.LENGTH_SHORT).show()
            return
        }

        if (selectedImageUri != null) {
            uploadImageAndSave()
        } else {
            finalizeSave("")
        }
    }

    private fun uploadImageAndSave() {
        binding.progressBar.visibility = View.VISIBLE
        binding.btnAddProduct.isEnabled = false
        
        val fileName = "product_${System.currentTimeMillis()}.jpg"
        val ref = storage.reference.child("images/$fileName")

        ref.putFile(selectedImageUri!!)
            .addOnSuccessListener {
                ref.downloadUrl.addOnSuccessListener { uri ->
                    finalizeSave(uri.toString())
                }.addOnFailureListener { e ->
                    binding.progressBar.visibility = View.GONE
                    binding.btnAddProduct.isEnabled = true
                    Toast.makeText(this, "Failed to get download URL: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener { e ->
                binding.progressBar.visibility = View.GONE
                binding.btnAddProduct.isEnabled = true
                Toast.makeText(this, "Upload Failed. Check if Storage is enabled in Firebase Console: ${e.message}", Toast.LENGTH_LONG).show()
            }
    }

    private fun finalizeSave(imageUrl: String) {
        val vendorId = auth.currentUser?.uid ?: return
        val name = binding.etProductName.text.toString()
        val price = binding.etPrice.text.toString().toDoubleOrNull() ?: 0.0
        val description = binding.etDescription.text.toString()

        val product = Product(
            vendorId = vendorId,
            name = name,
            price = price,
            description = description,
            imageUrl = imageUrl
        )

        db.collection("products").add(product)
            .addOnSuccessListener {
                Toast.makeText(this, "Product added successfully!", Toast.LENGTH_SHORT).show()
                finish()
            }
            .addOnFailureListener { e ->
                binding.progressBar.visibility = View.GONE
                binding.btnAddProduct.isEnabled = true
                Toast.makeText(this, "Database Error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }
}
