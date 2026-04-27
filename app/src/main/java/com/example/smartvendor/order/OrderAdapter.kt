package com.example.smartvendor.order

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.smartvendor.data.model.Order
import com.example.smartvendor.databinding.ItemOrderBinding
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class OrderAdapter(
    private val orders: List<Order>,
    private val isVendor: Boolean,
    private val onUpdateStatus: (Order, String) -> Unit
) : RecyclerView.Adapter<OrderAdapter.OrderViewHolder>() {

    class OrderViewHolder(val binding: ItemOrderBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OrderViewHolder {
        val binding = ItemOrderBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return OrderViewHolder(binding)
    }

    override fun onBindViewHolder(holder: OrderViewHolder, position: Int) {
        val order = orders[position]
        holder.binding.tvOrderTitle.text = if (isVendor) order.customerName else order.vendorName
        holder.binding.chipStatus.text = order.status
        
        val itemsSummary = order.items.joinToString { "${it.productName} x${it.quantity}" }
        holder.binding.tvOrderItems.text = itemsSummary
        
        val dateFormat = SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault())
        holder.binding.tvOrderDate.text = dateFormat.format(Date(order.timestamp))
        
        holder.binding.tvOrderTotal.text = String.format(Locale.getDefault(), "$ %.2f", order.totalAmount)

        if (isVendor && order.status == "Pending") {
            holder.binding.llVendorActions.visibility = View.VISIBLE
            holder.binding.btnUpdateStatus.text = "Accept"
            holder.binding.btnUpdateStatus.setOnClickListener { onUpdateStatus(order, "Accepted") }
            holder.binding.btnDecline.setOnClickListener { onUpdateStatus(order, "Cancelled") }
        } else if (isVendor && order.status == "Accepted") {
            holder.binding.llVendorActions.visibility = View.VISIBLE
            holder.binding.btnUpdateStatus.text = "Mark Prepared"
            holder.binding.btnDecline.visibility = View.GONE
            holder.binding.btnUpdateStatus.setOnClickListener { onUpdateStatus(order, "Preparing") }
        } else if (isVendor && order.status == "Preparing") {
            holder.binding.llVendorActions.visibility = View.VISIBLE
            holder.binding.btnUpdateStatus.text = "Deliver"
            holder.binding.btnUpdateStatus.setOnClickListener { onUpdateStatus(order, "Delivered") }
        } else {
            holder.binding.llVendorActions.visibility = View.GONE
        }
    }

    override fun getItemCount() = orders.size
}
