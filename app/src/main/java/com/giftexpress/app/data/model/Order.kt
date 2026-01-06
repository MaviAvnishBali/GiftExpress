package com.giftexpress.app.data.model

data class Order(
    val id: String,
    val status: String, // e.g., "Order Placed", "Delivered", "Order Cancelled"
    val date: String, // e.g., "Delivery by Web, 07 Jan", "Mon, 24 Jan"
    val productName: String, // Not explicitly shown in list but good to have, or maybe the "Order Placed" text is the status and the product name is implicit or not shown? 
    // Looking at the screenshot:
    // Title: "Order Placed" / "Delivered" / "Order Cancelled" (Status)
    // Subtitle: "Delivery by Web, 07 Jan" / "Mon, 24 Jan" / "As per your request" (Date/Info)
    // Size: "Size: 3.4 oz (10 ML)"
    // Qty: "Qty: 1"
    // Image
    
    val statusText: String,
    val dateText: String,
    val sizeText: String,
    val quantity: Int,
    val imageUrl: String? = null,
    val statusColor: Int,
    
    // Details
    val orderIdDisplay: String = "",
    val fullProductName: String = "",
    val timeline: List<OrderTimelineStep> = emptyList(),
    val address: OrderAddress? = null,
    val price: OrderPrice? = null,
    val paymentMethod: String = ""
)

data class OrderTimelineStep(
    val title: String,
    val date: String,
    val isCompleted: Boolean,
    val isCurrent: Boolean = false
)

data class OrderAddress(
    val name: String,
    val address: String,
    val phone: String
)

data class OrderPrice(
    val listingPrice: String,
    val specialPrice: String,
    val discount: String,
    val totalAmount: String
)
