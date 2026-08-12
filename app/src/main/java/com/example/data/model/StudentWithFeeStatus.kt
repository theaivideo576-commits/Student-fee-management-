package com.example.data.model

import com.example.data.local.PaymentEntity
import com.example.data.local.StudentEntity

data class StudentWithFeeStatus(
    val student: StudentEntity,
    val totalPaid: Double,
    val pendingFee: Double,
    val status: String, // "PAID" or "PENDING"
    val lastPaymentDate: String? = null
)

data class DashboardStats(
    val totalStudents: Int = 0,
    val totalFeeAmount: Double = 0.0,
    val totalPaidAmount: Double = 0.0,
    val totalPendingAmount: Double = 0.0,
    val todaysCollection: Double = 0.0,
    val thisMonthsCollection: Double = 0.0,
    val pendingStudentsCount: Int = 0,
    val paidStudentsCount: Int = 0
)

data class ClassCollection(
    val className: String,
    val totalFee: Double,
    val paidAmount: Double,
    val pendingAmount: Double,
    val studentCount: Int
)
