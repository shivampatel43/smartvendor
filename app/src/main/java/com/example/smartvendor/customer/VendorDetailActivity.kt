package com.example.smartvendor.customer

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.smartvendor.data.model.Review
import com.example.smartvendor.data.model.Vendor
import com.example.smartvendor.databinding.ActivityVendorDetailBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import java.util.UUID

class VendorDetailActivity : AppCompatActivity() {

    private lateinit var binding: ActivityVendorDetailBinding
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private var vendorId: String? = null
    private val reviews = mutableListOf<Review>()
    private lateinit var adapter: ReviewAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityVendorDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        vendorId = intent.getStringExtra("VENDOR_ID")
        if (vendorId == null) {
            finish()
            return
        }

        setupToolbar()
        setupRecyclerView()
        loadVendorDetails()
        loadReviews()

        binding.btnSubmitReview.setOnClickListener {
            submitReview()
        }
    }

    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        binding.toolbar.setNavigationOnClickListener { onBackPressed() }
    }

    private fun setupRecyclerView() {
        adapter = ReviewAdapter(reviews)
        binding.rvReviews.adapter = adapter
    }

    private fun loadVendorDetails() {
        vendorId?.let { id ->
            db.collection("users").document(id).get()
                .addOnSuccessListener { doc ->
                    val name = doc.getString("name") ?: ""
                    val category = doc.getString("category") ?: ""
                    val rating = doc.getDouble("rating") ?: 0.0
                    val numRatings = doc.getLong("numRatings")?.toInt() ?: 0

                    binding.tvDetailName.text = name
                    binding.tvDetailCategory.text = category
                    binding.avgRatingBar.rating = rating.toFloat()
                    binding.tvAvgRating.text = String.format("%.1f (%d reviews)", rating, numRatings)
                }
        }
    }

    private fun loadReviews() {
        vendorId?.let { id ->
            db.collection("reviews")
                .whereEqualTo("vendorId", id)
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .addSnapshotListener { value, error ->
                    if (error != null) return@addSnapshotListener
                    
                    reviews.clear()
                    value?.documents?.forEach { doc ->
                        doc.toObject(Review::class.java)?.let { reviews.add(it) }
                    }
                    adapter.notifyDataSetChanged()
                }
        }
    }

    private fun submitReview() {
        val rating = binding.userRatingBar.rating
        val comment = binding.etComment.text.toString().trim()
        val userId = auth.currentUser?.uid ?: return
        val userName = auth.currentUser?.displayName ?: "Anonymous"

        if (rating == 0f) {
            Toast.makeText(this, "Please select a rating", Toast.LENGTH_SHORT).show()
            return
        }

        val reviewId = UUID.randomUUID().toString()
        val review = Review(
            reviewId = reviewId,
            vendorId = vendorId!!,
            userId = userId,
            userName = userName,
            rating = rating,
            comment = comment
        )

        db.collection("reviews").document(reviewId).set(review)
            .addOnSuccessListener {
                updateVendorRating(rating.toDouble())
                binding.userRatingBar.rating = 0f
                binding.etComment.text?.clear()
                Toast.makeText(this, "Review submitted!", Toast.LENGTH_SHORT).show()
            }
    }

    private fun updateVendorRating(newRating: Double) {
        vendorId?.let { id ->
            val vendorRef = db.collection("users").document(id)
            db.runTransaction { transaction ->
                val snapshot = transaction.get(vendorRef)
                val currentRating = snapshot.getDouble("rating") ?: 0.0
                val currentNumRatings = snapshot.getLong("numRatings") ?: 0L
                
                val newNumRatings = currentNumRatings + 1
                val newAvgRating = ((currentRating * currentNumRatings) + newRating) / newNumRatings
                
                transaction.update(vendorRef, "rating", newAvgRating)
                transaction.update(vendorRef, "numRatings", newNumRatings)
            }.addOnSuccessListener {
                loadVendorDetails() // Refresh UI
            }
        }
    }
}
