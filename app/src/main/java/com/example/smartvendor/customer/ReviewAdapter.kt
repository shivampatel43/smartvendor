package com.example.smartvendor.customer

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.smartvendor.data.model.Review
import com.example.smartvendor.databinding.ItemReviewBinding
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ReviewAdapter(private val reviews: List<Review>) : RecyclerView.Adapter<ReviewAdapter.ReviewViewHolder>() {

    class ReviewViewHolder(val binding: ItemReviewBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ReviewViewHolder {
        val binding = ItemReviewBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ReviewViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ReviewViewHolder, position: Int) {
        val review = reviews[position]
        holder.binding.tvReviewerName.text = review.userName
        holder.binding.reviewRatingBar.rating = review.rating
        holder.binding.tvReviewComment.text = review.comment
        
        val dateFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
        holder.binding.tvReviewDate.text = dateFormat.format(Date(review.timestamp))
    }

    override fun getItemCount() = reviews.size
}
