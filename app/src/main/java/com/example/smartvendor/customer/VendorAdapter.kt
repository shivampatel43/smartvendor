package com.example.smartvendor.customer

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import coil.load
import coil.transform.CircleCropTransformation
import com.example.smartvendor.R
import com.example.smartvendor.data.model.Vendor
import com.example.smartvendor.databinding.ItemVendorBinding
import java.util.Locale

class VendorAdapter(
    private val vendors: List<Vendor>,
    private val onVisitClick: (Vendor) -> Unit
) : RecyclerView.Adapter<VendorAdapter.VendorViewHolder>() {

    class VendorViewHolder(val binding: ItemVendorBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VendorViewHolder {
        val binding = ItemVendorBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return VendorViewHolder(binding)
    }

    override fun onBindViewHolder(holder: VendorViewHolder, position: Int) {
        val vendor = vendors[position]
        holder.binding.tvVendorName.text = vendor.name
        holder.binding.tvVendorCategory.text = vendor.category
        holder.binding.tvVendorLocation.text = vendor.location
        
        // Load Shop Image
        if (vendor.imageUrl.isNotEmpty()) {
            holder.binding.ivVendor.load(vendor.imageUrl) {
                crossfade(true)
                placeholder(android.R.drawable.ic_menu_myplaces)
                error(android.R.drawable.ic_menu_myplaces)
                transformations(CircleCropTransformation())
            }
        } else {
            holder.binding.ivVendor.setImageResource(android.R.drawable.ic_menu_myplaces)
        }
        
        // Show Rating
        holder.binding.tvRating.text = String.format(Locale.getDefault(), "%.1f (%d)", vendor.rating, vendor.numRatings)
        
        // Bind the calculated distance
        if (vendor.distance.isNotEmpty()) {
            holder.binding.tvDistance.visibility = View.VISIBLE
            holder.binding.tvDistance.text = vendor.distance
        } else {
            holder.binding.tvDistance.visibility = View.GONE
        }
        
        holder.binding.btnViewStore.setOnClickListener {
            onVisitClick(vendor)
        }
    }

    override fun getItemCount() = vendors.size
}
