package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "students")
data class StudentEntity(
    @PrimaryKey
    val studentId: String,
    val name: String,
    val fatherName: String,
    val className: String,
    val batch: String,
    val mobile: String,
    val totalFee: Double,
    val admissionDate: String,
    val createdAt: Long = System.currentTimeMillis()
)
