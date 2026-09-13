package com.mezban.pos.data

data class ReceiptItem(val name: String, val quantity: Int, val price: Double)

data class ReceiptData(
    val billId: Long,
    val items: List<ReceiptItem>,
    val subtotal: Double,
    val tax: Double,
    val total: Double,
    val paymentMode: String,
    val date: String
)

data class StaffMember(val id: Long, val name: String, val role: String)
