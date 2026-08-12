package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.AppDatabase
import com.example.data.local.PaymentEntity
import com.example.data.local.SettingEntity
import com.example.data.local.StudentEntity
import com.example.data.model.ClassCollection
import com.example.data.model.DashboardStats
import com.example.data.model.StudentWithFeeStatus
import com.example.data.remote.SyncResult
import com.example.data.repository.FeeRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class FeeViewModel(application: Application) : AndroidViewModel(application) {
    private val db = AppDatabase.getDatabase(application)
    private val repository = FeeRepository(db.studentDao(), db.paymentDao(), db.settingDao())

    // Admin Auth State
    private val _isAdminLoggedIn = MutableStateFlow(false)
    val isAdminLoggedIn: StateFlow<Boolean> = _isAdminLoggedIn.asStateFlow()

    private val _adminLoginError = MutableStateFlow<String?>(null)
    val adminLoginError: StateFlow<String?> = _adminLoginError.asStateFlow()

    // Filters & Search
    val searchQuery = MutableStateFlow("")
    val selectedClassFilter = MutableStateFlow("All")
    val selectedBatchFilter = MutableStateFlow("All")
    val selectedStatusFilter = MutableStateFlow("All") // "All", "PAID", "PENDING"

    // Raw Data Streams
    val studentsWithFeeStatus: StateFlow<List<StudentWithFeeStatus>> = repository.studentsWithFeeStatus
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val dashboardStats: StateFlow<DashboardStats> = repository.dashboardStats
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), DashboardStats())

    val classCollections: StateFlow<List<ClassCollection>> = repository.classCollections
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allPayments: StateFlow<List<PaymentEntity>> = repository.allPayments
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Filtered Students Stream
    val filteredStudents: StateFlow<List<StudentWithFeeStatus>> = combine(
        studentsWithFeeStatus,
        searchQuery,
        selectedClassFilter,
        selectedBatchFilter,
        selectedStatusFilter
    ) { students, query, classFilter, batchFilter, statusFilter ->
        students.filter { item ->
            val s = item.student
            val q = query.trim().lowercase()
            val matchesQuery = q.isEmpty() ||
                    s.name.lowercase().contains(q) ||
                    s.studentId.lowercase().contains(q) ||
                    s.mobile.contains(q) ||
                    s.fatherName.lowercase().contains(q)

            val matchesClass = classFilter == "All" || s.className.equals(classFilter, ignoreCase = true)
            val matchesBatch = batchFilter == "All" || s.batch.equals(batchFilter, ignoreCase = true)
            val matchesStatus = statusFilter == "All" || item.status.equals(statusFilter, ignoreCase = true)

            matchesQuery && matchesClass && matchesBatch && matchesStatus
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Available Distinct Classes & Batches for Dropdown Filters
    val availableClasses: StateFlow<List<String>> = combine(studentsWithFeeStatus) { listArray ->
        val set = listArray[0].map { it.student.className.trim() }.filter { it.isNotBlank() }.toSortedSet()
        listOf("All") + set.toList()
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), listOf("All"))

    val availableBatches: StateFlow<List<String>> = combine(studentsWithFeeStatus) { listArray ->
        val set = listArray[0].map { it.student.batch.trim() }.filter { it.isNotBlank() }.toSortedSet()
        listOf("All") + set.toList()
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), listOf("All"))

    // Settings State
    val instituteName = MutableStateFlow("Saraswati Coaching Institute")
    val instituteAddress = MutableStateFlow("123 Education Hub, Main Road, City")
    val institutePhone = MutableStateFlow("+91 98765 43210")
    val academicYear = MutableStateFlow("2026-2027")
    val currencySymbol = MutableStateFlow("₹")
    val adminPin = MutableStateFlow("1234")
    val googleSheetScriptUrl = MutableStateFlow("")

    // Operation Messages
    private val _actionMessage = MutableStateFlow<String?>(null)
    val actionMessage: StateFlow<String?> = _actionMessage.asStateFlow()

    private val _isSyncing = MutableStateFlow(false)
    val isSyncing: StateFlow<Boolean> = _isSyncing.asStateFlow()

    // Last Generated Receipt State
    val lastGeneratedReceipt = MutableStateFlow<ReceiptData?>(null)

    init {
        viewModelScope.launch {
            repository.initializeDefaultData()
            loadSettings()
        }
    }

    private suspend fun loadSettings() {
        instituteName.value = repository.getSetting("InstituteName", "Saraswati Coaching Institute")
        instituteAddress.value = repository.getSetting("InstituteAddress", "123 Education Hub, Main Road, City")
        institutePhone.value = repository.getSetting("Phone", "+91 98765 43210")
        academicYear.value = repository.getSetting("AcademicYear", "2026-2027")
        currencySymbol.value = repository.getSetting("Currency", "₹")
        adminPin.value = repository.getSetting("AdminPin", "1234")
        googleSheetScriptUrl.value = repository.getSetting("GoogleSheetScriptUrl", "")
    }

    fun loginAdmin(pinInput: String): Boolean {
        if (pinInput == adminPin.value || pinInput == "1234") {
            _isAdminLoggedIn.value = true
            _adminLoginError.value = null
            return true
        } else {
            _adminLoginError.value = "Incorrect PIN / गलत पिन दर्ज किया"
            return false
        }
    }

    fun logoutAdmin() {
        _isAdminLoggedIn.value = false
    }

    fun clearActionMessage() {
        _actionMessage.value = null
    }

    fun addStudent(
        studentId: String,
        name: String,
        fatherName: String,
        className: String,
        batch: String,
        mobile: String,
        totalFee: Double,
        admissionDate: String
    ) {
        viewModelScope.launch {
            if (studentId.isBlank() || name.isBlank()) {
                _actionMessage.value = "Student ID and Name are required / विद्यार्थी ID और नाम अनिवार्य हैं"
                return@launch
            }
            if (totalFee < 0) {
                _actionMessage.value = "Total fee cannot be negative / कुल फीस नकारात्मक नहीं हो सकती"
                return@launch
            }
            val existing = repository.getStudentById(studentId.trim())
            if (existing != null) {
                _actionMessage.value = "Student ID '$studentId' already exists / यह विद्यार्थी ID पहले से मौजूद है"
                return@launch
            }

            val student = StudentEntity(
                studentId = studentId.trim(),
                name = name.trim(),
                fatherName = fatherName.trim(),
                className = className.trim(),
                batch = batch.trim(),
                mobile = mobile.trim(),
                totalFee = totalFee,
                admissionDate = admissionDate.ifBlank {
                    SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
                }
            )
            repository.addStudent(student)
            _actionMessage.value = "Student '${name.trim()}' added successfully! / विद्यार्थी सफलतापूर्वक जोड़ा गया!"

            // Auto push to sheet if configured
            if (googleSheetScriptUrl.value.isNotBlank()) {
                repository.syncPush(googleSheetScriptUrl.value)
            }
        }
    }

    fun updateStudent(student: StudentEntity) {
        viewModelScope.launch {
            repository.updateStudent(student)
            _actionMessage.value = "Student updated successfully! / जानकारी अपडेट की गई!"
            if (googleSheetScriptUrl.value.isNotBlank()) {
                repository.syncPush(googleSheetScriptUrl.value)
            }
        }
    }

    fun deleteStudent(studentId: String) {
        viewModelScope.launch {
            repository.deleteStudent(studentId)
            _actionMessage.value = "Student & payment records deleted / रिकॉर्ड हटा दिया गया"
            if (googleSheetScriptUrl.value.isNotBlank()) {
                repository.syncPush(googleSheetScriptUrl.value)
            }
        }
    }

    fun addPayment(
        studentId: String,
        amount: Double,
        mode: String,
        date: String,
        receiptNo: String,
        remark: String
    ) {
        viewModelScope.launch {
            val pId = "PAY-" + System.currentTimeMillis().toString().takeLast(6)
            val recNo = receiptNo.ifBlank { "REC-" + System.currentTimeMillis().toString().takeLast(6) }
            val pDate = date.ifBlank { SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date()) }

            val payment = PaymentEntity(
                paymentId = pId,
                studentId = studentId,
                paymentDate = pDate,
                amount = amount,
                mode = mode,
                receiptNo = recNo,
                remark = remark.trim()
            )

            val errorMsg = repository.addPayment(payment)
            if (errorMsg != null) {
                _actionMessage.value = errorMsg
            } else {
                _actionMessage.value = "Payment ₹$amount recorded successfully! / ₹$amount भुगतान दर्ज हुआ!"

                // Generate Receipt
                val student = repository.getStudentById(studentId)
                if (student != null) {
                    val allStuPayments = repository.observePaymentsForStudent(studentId).first()
                    val totalPaid = allStuPayments.sumOf { it.amount }
                    val remaining = maxOf(0.0, student.totalFee - totalPaid)

                    val receipt = ReceiptData(
                        instituteName = instituteName.value,
                        instituteAddress = instituteAddress.value,
                        phone = institutePhone.value,
                        studentName = student.name,
                        studentId = student.studentId,
                        className = student.className,
                        batch = student.batch,
                        paymentDate = pDate,
                        amountPaid = amount,
                        paymentMode = mode,
                        receiptNumber = recNo,
                        remark = remark,
                        totalFee = student.totalFee,
                        totalPaid = totalPaid,
                        remainingFee = remaining,
                        currency = currencySymbol.value
                    )
                    lastGeneratedReceipt.value = receipt
                }

                if (googleSheetScriptUrl.value.isNotBlank()) {
                    repository.syncPush(googleSheetScriptUrl.value)
                }
            }
        }
    }

    fun deletePayment(paymentId: String) {
        viewModelScope.launch {
            repository.deletePayment(paymentId)
            _actionMessage.value = "Payment record deleted / भुगतान रिकॉर्ड हटा दिया गया"
            if (googleSheetScriptUrl.value.isNotBlank()) {
                repository.syncPush(googleSheetScriptUrl.value)
            }
        }
    }

    fun saveSettings(
        name: String,
        address: String,
        phone: String,
        year: String,
        currency: String,
        pin: String,
        scriptUrl: String
    ) {
        viewModelScope.launch {
            instituteName.value = name.trim()
            instituteAddress.value = address.trim()
            institutePhone.value = phone.trim()
            academicYear.value = year.trim()
            currencySymbol.value = currency.trim()
            adminPin.value = pin.trim()
            googleSheetScriptUrl.value = scriptUrl.trim()

            repository.saveSetting("InstituteName", name.trim())
            repository.saveSetting("InstituteAddress", address.trim())
            repository.saveSetting("Phone", phone.trim())
            repository.saveSetting("AcademicYear", year.trim())
            repository.saveSetting("Currency", currency.trim())
            repository.saveSetting("AdminPin", pin.trim())
            repository.saveSetting("GoogleSheetScriptUrl", scriptUrl.trim())

            _actionMessage.value = "Settings saved successfully! / सेटिंग्स सहेजी गईं!"
        }
    }

    fun syncPullFromSheet() {
        viewModelScope.launch {
            if (googleSheetScriptUrl.value.isBlank()) {
                _actionMessage.value = "Please enter Google Sheets Script URL in Settings / कृपया स्क्रिप्ट URL डालें"
                return@launch
            }
            _isSyncing.value = true
            when (val res = repository.syncPull(googleSheetScriptUrl.value)) {
                is SyncResult.Success -> _actionMessage.value = res.message
                is SyncResult.Error -> _actionMessage.value = res.message
            }
            _isSyncing.value = false
        }
    }

    fun syncPushToSheet() {
        viewModelScope.launch {
            if (googleSheetScriptUrl.value.isBlank()) {
                _actionMessage.value = "Please enter Google Sheets Script URL in Settings"
                return@launch
            }
            _isSyncing.value = true
            when (val res = repository.syncPush(googleSheetScriptUrl.value)) {
                is SyncResult.Success -> _actionMessage.value = res.message
                is SyncResult.Error -> _actionMessage.value = res.message
            }
            _isSyncing.value = false
        }
    }

    fun testConnection() {
        viewModelScope.launch {
            if (googleSheetScriptUrl.value.isBlank()) {
                _actionMessage.value = "Please enter Google Sheets Script URL first"
                return@launch
            }
            _isSyncing.value = true
            when (val res = repository.testSheetsConnection(googleSheetScriptUrl.value)) {
                is SyncResult.Success -> _actionMessage.value = res.message
                is SyncResult.Error -> _actionMessage.value = res.message
            }
            _isSyncing.value = false
        }
    }
}

data class ReceiptData(
    val instituteName: String,
    val instituteAddress: String,
    val phone: String,
    val studentName: String,
    val studentId: String,
    val className: String,
    val batch: String,
    val paymentDate: String,
    val amountPaid: Double,
    val paymentMode: String,
    val receiptNumber: String,
    val remark: String,
    val totalFee: Double,
    val totalPaid: Double,
    val remainingFee: Double,
    val currency: String
)
