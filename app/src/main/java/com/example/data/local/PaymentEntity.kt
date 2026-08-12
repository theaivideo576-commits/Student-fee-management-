package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "payments")
data class PaymentEntity(
    @PrimaryKey
    val paymentId: String,
    val studentId: String,
    val paymentDate: String,
    val amount: Double,
    val mode: String, // Cash, UPI, Bank Transfer, Other
    val receiptNo: String,
    val remark: String,
    val timestamp: Long = System.currentTimeMillis()
)
