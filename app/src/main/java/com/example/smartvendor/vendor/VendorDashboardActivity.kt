package com.example.smartvendor.vendor

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.smartvendor.auth.LoginActivity
import com.example.smartvendor.data.model.Product
import com.example.smartvendor.databinding.ActivityVendorDashboardBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class VendorDashboardActivity : AppCompatActivity() {

    private lateinit var binding: ActivityVendorDashboardBinding
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private val products = mutableListOf<Product>()
    private lateinit var adapter: ProductAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityVendorDashboardBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupRecyclerView()
        loadProducts()

        binding.fabAddProduct.setOnClickListener {
            startActivity(Intent(this, AddProductActivity::class.java))
        }

        // Setup Logout
        binding.btnLogout.setOnClickListener {
            auth.signOut()
            val intent = Intent(this, LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
        }
    }

    private fun setupRecyclerView() {
        adapter = ProductAdapter(products)
        binding.rvProducts.layoutManager = LinearLayoutManager(this)
        binding.rvProducts.adapter = adapter
    }

    private fun loadProducts() {
        val uid = auth.currentUser?.uid ?: return
        db.collection("products")
            .whereEqualTo("vendorId", uid)
            .addSnapshotListener { value, error ->
                if (error != null) return@addSnapshotListener
                
                products.clear()
                value?.documents?.forEach { doc ->
                    val product = doc.toObject(Product::class.java)
                    if (product != null) {
                        products.add(product.copy(productId = doc.id))
                    }
                }
                adapter.notifyDataSetChanged()
                binding.tvEmptyState.visibility = if (products.isEmpty()) View.VISIBLE else View.GONE
            }
    }
}
