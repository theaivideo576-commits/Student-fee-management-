package com.example.data.repository

import com.example.data.local.PaymentDao
import com.example.data.local.PaymentEntity
import com.example.data.local.SettingDao
import com.example.data.local.SettingEntity
import com.example.data.local.StudentDao
import com.example.data.local.StudentEntity
import com.example.data.model.ClassCollection
import com.example.data.model.DashboardStats
import com.example.data.model.StudentWithFeeStatus
import com.example.data.remote.GoogleSheetsSyncManager
import com.example.data.remote.SyncResult
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class FeeRepository(
    private val studentDao: StudentDao,
    private val paymentDao: PaymentDao,
    private val settingDao: SettingDao,
    private val syncManager: GoogleSheetsSyncManager = GoogleSheetsSyncManager()
) {
    val allStudents: Flow<List<StudentEntity>> = studentDao.getAllStudents()
    val allPayments: Flow<List<PaymentEntity>> = paymentDao.getAllPayments()

    // Combined Reactive Flow of Students with Computed Fee Status
    val studentsWithFeeStatus: Flow<List<StudentWithFeeStatus>> = combine(
        studentDao.getAllStudents(),
        paymentDao.getAllPayments()
    ) { students, payments ->
        val paymentGroup = payments.groupBy { it.studentId }
        students.map { student ->
            val studentPayments = paymentGroup[student.studentId] ?: emptyList()
            val totalPaid = studentPayments.sumOf { it.amount }
            val pending = maxOf(0.0, student.totalFee - totalPaid)
            val status = if (pending <= 0.0) "PAID" else "PENDING"
            val lastDate = studentPayments.maxOfOrNull { it.paymentDate }
            StudentWithFeeStatus(
                student = student,
                totalPaid = totalPaid,
                pendingFee = pending,
                status = status,
                lastPaymentDate = lastDate
            )
        }
    }

    // Dashboard Statistics Flow
    val dashboardStats: Flow<DashboardStats> = studentsWithFeeStatus.combine(paymentDao.getAllPayments()) { studentList, payments ->
        val todayStr = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        val currentMonthStr = SimpleDateFormat("yyyy-MM", Locale.getDefault()).format(Date())

        val totalStudents = studentList.size
        val totalFee = studentList.sumOf { it.student.totalFee }
        val totalPaid = studentList.sumOf { it.totalPaid }
        val totalPending = studentList.sumOf { it.pendingFee }
        val pendingCount = studentList.count { it.status == "PENDING" }
        val paidCount = studentList.count { it.status == "PAID" }

        val todaysColl = payments
            .filter { it.paymentDate == todayStr }
            .sumOf { it.amount }

        val thisMonthColl = payments
            .filter { it.paymentDate.startsWith(currentMonthStr) }
            .sumOf { it.amount }

        DashboardStats(
            totalStudents = totalStudents,
            totalFeeAmount = totalFee,
            totalPaidAmount = totalPaid,
            totalPendingAmount = totalPending,
            todaysCollection = todaysColl,
            thisMonthsCollection = thisMonthColl,
            pendingStudentsCount = pendingCount,
            paidStudentsCount = paidCount
        )
    }

    val classCollections: Flow<List<ClassCollection>> = studentsWithFeeStatus.combine(studentDao.getAllStudents()) { studentList, _ ->
        val groupedByClass = studentList.groupBy { it.student.className.ifBlank { "Unassigned" } }
        groupedByClass.map { (clsName, list) ->
            val clsTotalFee = list.sumOf { it.student.totalFee }
            val clsPaid = list.sumOf { it.totalPaid }
            val clsPending = list.sumOf { it.pendingFee }
            ClassCollection(
                className = clsName,
                totalFee = clsTotalFee,
                paidAmount = clsPaid,
                pendingAmount = clsPending,
                studentCount = list.size
            )
        }.sortedBy { it.className }
    }

    // Student CRUD
    suspend fun addStudent(student: StudentEntity) {
        studentDao.insertStudent(student)
    }

    suspend fun updateStudent(student: StudentEntity) {
        studentDao.updateStudent(student)
    }

    suspend fun deleteStudent(studentId: String) {
        studentDao.deleteStudentById(studentId)
        paymentDao.deletePaymentsByStudentId(studentId)
    }

    suspend fun getStudentById(studentId: String): StudentEntity? {
        return studentDao.getStudentById(studentId)
    }

    fun observePaymentsForStudent(studentId: String): Flow<List<PaymentEntity>> {
        return paymentDao.getPaymentsByStudentId(studentId)
    }

    // Payment CRUD
    suspend fun addPayment(payment: PaymentEntity): String? {
        // Validation: Student must exist
        val student = studentDao.getStudentById(payment.studentId)
            ?: return "Student ID not found / विद्यार्थी आईडी नहीं मिली"

        if (payment.amount <= 0) {
            return "Payment amount must be greater than 0 / राशि 0 से अधिक होनी चाहिए"
        }

        val existingPayments = paymentDao.getPaymentsByStudentIdSync(payment.studentId)
        val currentPaid = existingPayments.sumOf { it.amount }
        val currentPending = maxOf(0.0, student.totalFee - currentPaid)

        if (payment.amount > currentPending + 0.01) {
            return "Payment (₹${payment.amount}) cannot exceed pending fee (₹${currentPending}) / भुगतान बकाया राशि से अधिक नहीं हो सकता"
        }

        paymentDao.insertPayment(payment)
        return null // Success
    }

    suspend fun deletePayment(paymentId: String) {
        paymentDao.deletePaymentById(paymentId)
    }

    // Settings Management
    suspend fun getSetting(key: String, defaultValue: String): String {
        return settingDao.getSetting(key) ?: defaultValue
    }

    fun observeSetting(key: String, defaultValue: String): Flow<String> {
        return combine(settingDao.observeSetting(key)) { valueArray ->
            valueArray[0] ?: defaultValue
        }
    }

    suspend fun saveSetting(key: String, value: String) {
        settingDao.setSetting(SettingEntity(key, value))
    }

    // Sync with Google Sheets
    suspend fun syncPull(scriptUrl: String): SyncResult {
        val result = syncManager.pullFromSheet(scriptUrl)
        if (result is SyncResult.Success) {
            if (result.pulledStudents.isNotEmpty()) {
                studentDao.insertStudents(result.pulledStudents)
            }
            if (result.pulledPayments.isNotEmpty()) {
                paymentDao.insertPayments(result.pulledPayments)
            }
        }
        return result
    }

    suspend fun syncPush(scriptUrl: String): SyncResult {
        val students = studentDao.getAllStudents().first()
        val payments = paymentDao.getAllPayments().first()
        return syncManager.pushToSheet(scriptUrl, students, payments)
    }

    suspend fun testSheetsConnection(scriptUrl: String): SyncResult {
        return syncManager.testConnection(scriptUrl)
    }

    suspend fun initializeDefaultData() {
        // Seed default settings if missing
        if (settingDao.getSetting("InstituteName") == null) {
            settingDao.setSetting(SettingEntity("InstituteName", "Saraswati Coaching Institute"))
            settingDao.setSetting(SettingEntity("InstituteAddress", "123 Education Hub, Main Road, City"))
            settingDao.setSetting(SettingEntity("Phone", "+91 98765 43210"))
            settingDao.setSetting(SettingEntity("AcademicYear", "2026-2027"))
            settingDao.setSetting(SettingEntity("Currency", "₹"))
            settingDao.setSetting(SettingEntity("AdminPin", "1234"))
            settingDao.setSetting(SettingEntity("GoogleSheetScriptUrl", ""))
        }

        // Seed initial demo data if database is empty
        val existingStudents = studentDao.getAllStudents().first()
        if (existingStudents.isEmpty()) {
            val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
            val demoStudents = listOf(
                StudentEntity("STU-101", "Aarav Sharma", "Rajesh Sharma", "Class 10", "Morning A", "9876543210", 15000.0, today),
                StudentEntity("STU-102", "Ananya Verma", "Suresh Verma", "12th Science", "Evening B", "9812345678", 20000.0, today),
                StudentEntity("STU-103", "Rohan Gupta", "Pankaj Gupta", "NEET Target", "Morning A", "9988776655", 25000.0, today),
                StudentEntity("STU-104", "Kavya Patel", "Mahesh Patel", "Class 10", "Evening B", "9765432109", 15000.0, today)
            )
            studentDao.insertStudents(demoStudents)

            val demoPayments = listOf(
                PaymentEntity("PAY-1001", "STU-101", today, 5000.0, "UPI", "REC-2026-001", "1st Installment"),
                PaymentEntity("PAY-1002", "STU-102", today, 20000.0, "Cash", "REC-2026-002", "Full Payment"),
                PaymentEntity("PAY-1003", "STU-103", today, 10000.0, "Bank Transfer", "REC-2026-003", "Partial Payment")
            )
            paymentDao.insertPayments(demoPayments)
        }
    }
}
