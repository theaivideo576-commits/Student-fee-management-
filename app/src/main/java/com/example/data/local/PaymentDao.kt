package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface PaymentDao {
    @Query("SELECT * FROM payments ORDER BY timestamp DESC")
    fun getAllPayments(): Flow<List<PaymentEntity>>

    @Query("SELECT * FROM payments WHERE studentId = :studentId ORDER BY timestamp DESC")
    fun getPaymentsByStudentId(studentId: String): Flow<List<PaymentEntity>>

    @Query("SELECT * FROM payments WHERE studentId = :studentId ORDER BY timestamp DESC")
    suspend fun getPaymentsByStudentIdSync(studentId: String): List<PaymentEntity>

    @Query("SELECT SUM(amount) FROM payments WHERE studentId = :studentId")
    fun getTotalPaidForStudent(studentId: String): Flow<Double?>

    @Query("SELECT SUM(amount) FROM payments WHERE studentId = :studentId")
    suspend fun getTotalPaidForStudentSync(studentId: String): Double?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPayment(payment: PaymentEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPayments(payments: List<PaymentEntity>)

    @Query("DELETE FROM payments WHERE paymentId = :paymentId")
    suspend fun deletePaymentById(paymentId: String)

    @Query("DELETE FROM payments WHERE studentId = :studentId")
    suspend fun deletePaymentsByStudentId(studentId: String)

    @Query("DELETE FROM payments")
    suspend fun clearAllPayments()
}
