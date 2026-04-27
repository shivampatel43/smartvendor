package com.example.smartvendor.customer

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.smartvendor.ai.AiAssistantActivity
import com.example.smartvendor.auth.LoginActivity
import com.example.smartvendor.data.model.Vendor
import com.example.smartvendor.databinding.ActivityCustomerHomeBinding
import com.example.smartvendor.network.RetrofitClient
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch
import kotlin.math.*
import kotlin.random.Random

class CustomerHomeActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCustomerHomeBinding
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private val firebaseVendors = mutableListOf<Vendor>()
    private val googleVendors = mutableListOf<Vendor>()
    private val allVendors = mutableListOf<Vendor>()
    private val filteredVendors = mutableListOf<Vendor>()
    private lateinit var adapter: VendorAdapter
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private var currentCategory = "All"
    
    // NOTE: Replace with your actual Google Maps API Key
    private val MAPS_API_KEY = "YOUR_GOOGLE_MAPS_API_KEY"

    private val locationPermissionRequest = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        when {
            permissions.getOrDefault(Manifest.permission.ACCESS_FINE_LOCATION, false) -> {
                startNearbySensing()
            }
            permissions.getOrDefault(Manifest.permission.ACCESS_COARSE_LOCATION, false) -> {
                startNearbySensing()
            }
            else -> {
                Toast.makeText(this, "Location permission denied", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCustomerHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        setupRecyclerView()
        setupCategoryChips()
        loadFirebaseVendors()

        binding.btnAiAssistant.setOnClickListener {
            startActivity(Intent(this, AiAssistantActivity::class.java))
        }

        binding.btnNearby.setOnClickListener {
            checkLocationPermissions()
        }

        binding.btnLogout.setOnClickListener {
            auth.signOut()
            val intent = Intent(this, LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
        }
    }

    private fun setupCategoryChips() {
        binding.chipGroupCategories.setOnCheckedChangeListener { group, checkedId ->
            currentCategory = when (checkedId) {
                binding.chipFastFood.id -> "Fast Food"
                binding.chipSupermarket.id -> "Supermarket"
                binding.chipRestaurant.id -> "Restaurant"
                binding.chipBakery.id -> "Bakery"
                else -> "All"
            }
            applyFilters()
        }
    }

    private fun applyFilters() {
        filteredVendors.clear()
        val combined = (firebaseVendors + googleVendors).distinctBy { it.name }
        if (currentCategory == "All") {
            filteredVendors.addAll(combined)
        } else {
            filteredVendors.addAll(combined.filter { it.category.contains(currentCategory, ignoreCase = true) })
        }
        adapter.notifyDataSetChanged()
    }

    private fun setupRecyclerView() {
        adapter = VendorAdapter(filteredVendors) { vendor ->
            val intent = Intent(this, VendorDetailActivity::class.java)
            intent.putExtra("VENDOR_ID", vendor.vendorId)
            intent.putExtra("VENDOR_NAME", vendor.name)
            startActivity(intent)
        }
        binding.rvVendors.layoutManager = LinearLayoutManager(this)
        binding.rvVendors.adapter = adapter
    }

    private fun loadFirebaseVendors() {
        db.collection("users")
            .whereEqualTo("role", "vendor")
            .addSnapshotListener { value, error ->
                if (error != null) return@addSnapshotListener
                
                firebaseVendors.clear()
                value?.documents?.forEach { doc ->
                    val vendor = Vendor(
                        vendorId = doc.id,
                        name = doc.getString("name") ?: "",
                        category = doc.getString("category") ?: "General",
                        location = doc.getString("location") ?: "Unknown",
                        imageUrl = doc.getString("imageUrl") ?: "",
                        latitude = doc.getDouble("latitude") ?: 0.0,
                        longitude = doc.getDouble("longitude") ?: 0.0,
                        rating = doc.getDouble("rating") ?: 0.0,
                        numRatings = doc.getLong("numRatings")?.toInt() ?: 0
                    )
                    firebaseVendors.add(vendor)
                }
                applyFilters()
            }
    }

    private fun checkLocationPermissions() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            locationPermissionRequest.launch(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION))
        } else {
            startNearbySensing()
        }
    }

    private fun startNearbySensing() {
        binding.rvVendors.visibility = View.GONE
        binding.progressScanning.visibility = View.VISIBLE
        binding.toolbar.title = "Scanning Google Maps + Local..."

        fusedLocationClient.lastLocation.addOnSuccessListener { location: Location? ->
            val userLoc = location ?: Location("test").apply {
                latitude = 28.6139
                longitude = 77.2090
            }
            
            // 1. Fetch from Google Places API
            fetchGooglePlaces(userLoc)
            
            // 2. Process Firebase Vendors
            processFirebaseVendors(userLoc)
            
            Handler(Looper.getMainLooper()).postDelayed({
                binding.progressScanning.visibility = View.GONE
                binding.rvVendors.visibility = View.VISIBLE
                applyFilters()
            }, 3000)
        }
    }

    private fun fetchGooglePlaces(location: Location) {
        lifecycleScope.launch {
            try {
                val locString = "${location.latitude},${location.longitude}"
                val response = RetrofitClient.placesApi.getNearbyPlaces(locString, 10000, "store", MAPS_API_KEY)
                
                googleVendors.clear()
                response.results.forEach { place ->
                    val imageUrl = if (!place.photos.isNullOrEmpty()) {
                        "https://maps.googleapis.com/maps/api/place/photo?maxwidth=400&photoreference=${place.photos[0].photoReference}&key=$MAPS_API_KEY"
                    } else ""

                    val vendor = Vendor(
                        vendorId = place.placeId,
                        name = place.name,
                        location = place.vicinity,
                        category = place.types.firstOrNull()?.replace("_", " ")?.capitalize() ?: "Store",
                        imageUrl = imageUrl,
                        latitude = place.geometry.location.lat,
                        longitude = place.geometry.location.lng,
                        rating = place.rating ?: 0.0,
                        numRatings = place.userRatingsTotal ?: 0
                    )
                    
                    val distance = calculateDistance(location.latitude, location.longitude, vendor.latitude, vendor.longitude)
                    vendor.distance = String.format("%.1f km away", distance)
                    googleVendors.add(vendor)
                }
            } catch (e: Exception) {
                Log.e("PlacesAPI", "Error fetching places", e)
            }
        }
    }

    private fun processFirebaseVendors(userLocation: Location) {
        firebaseVendors.forEach { vendor ->
            if (vendor.latitude != 0.0) {
                val distance = calculateDistance(userLocation.latitude, userLocation.longitude, vendor.latitude, vendor.longitude)
                vendor.distance = if (distance <= 10.0) String.format("%.1f km away", distance) else ""
            }
        }
    }

    private fun calculateDistance(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        val r = 6371 // km
        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)
        val a = sin(dLat / 2).pow(2) + cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) * sin(dLon / 2).pow(2)
        val c = 2 * atan2(sqrt(a), sqrt(1 - a))
        return r * c
    }
}
