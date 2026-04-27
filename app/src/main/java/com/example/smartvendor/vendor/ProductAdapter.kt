package com.example.smartvendor.vendor

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.example.smartvendor.R
import com.example.smartvendor.data.model.Product
import com.example.smartvendor.databinding.ItemProductBinding

class ProductAdapter(private val products: List<Product>) :
    RecyclerView.Adapter<ProductAdapter.ProductViewHolder>() {

    class ProductViewHolder(val binding: ItemProductBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProductViewHolder {
        val binding = ItemProductBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ProductViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ProductViewHolder, position: Int) {
        val product = products[position]
        holder.binding.tvProductName.text = product.name
        holder.binding.tvProductDescription.text = product.description
        holder.binding.tvProductPrice.text = "$${product.price}"
        
        if (product.imageUrl.isNotEmpty()) {
            holder.binding.ivProduct.load(product.imageUrl) {
                crossfade(true)
                placeholder(android.R.drawable.ic_menu_gallery)
                error(android.R.drawable.ic_menu_report_image)
            }
        } else {
            holder.binding.ivProduct.setImageResource(android.R.drawable.ic_menu_report_image)
        }
    }

    override fun getItemCount() = products.size
}
