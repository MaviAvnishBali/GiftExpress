package com.giftexpress.app.ui.orders

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.giftexpress.app.data.model.Order
import com.giftexpress.app.databinding.ItemOrderBinding

class OrdersAdapter(private val onOrderClick: (Order) -> Unit) : ListAdapter<Order, OrdersAdapter.OrderViewHolder>(OrderDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OrderViewHolder {
        val binding = ItemOrderBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return OrderViewHolder(binding, onOrderClick)
    }

    override fun onBindViewHolder(holder: OrderViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class OrderViewHolder(
        private val binding: ItemOrderBinding,
        private val onOrderClick: (Order) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(order: Order) {
            binding.root.setOnClickListener { onOrderClick(order) }
            binding.tvStatus.text = order.statusText
            binding.tvStatus.setTextColor(order.statusColor)
            binding.tvDate.text = order.dateText
            binding.tvSize.text = order.sizeText
            binding.tvQuantity.text = "Qty: ${order.quantity}"
            
            // Load image if needed, for now just placeholder or resource
            // Glide.with(binding.root).load(order.imageUrl).into(binding.ivProduct)
        }
    }

    class OrderDiffCallback : DiffUtil.ItemCallback<Order>() {
        override fun areItemsTheSame(oldItem: Order, newItem: Order): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Order, newItem: Order): Boolean {
            return oldItem == newItem
        }
    }
}
