package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.compose.ui.text.font.FontWeight
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.data.local.StudentEntity
import com.example.ui.components.ReceiptDialog
import com.example.ui.navigation.NavRoute
import com.example.ui.navigation.bottomNavItems
import com.example.ui.screens.AddEditStudentDialog
import com.example.ui.screens.AddPaymentDialog
import com.example.ui.screens.AdminLoginScreen
import com.example.ui.screens.DashboardScreen
import com.example.ui.screens.PaymentManagementScreen
import com.example.ui.screens.PendingFeesScreen
import com.example.ui.screens.ReportsScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.screens.StudentDetailScreen
import com.example.ui.screens.StudentManagementScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.NavyPrimary
import com.example.ui.viewmodel.FeeViewModel
import com.example.ui.viewmodel.ReceiptData

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                StudentFeeManagementApp()
            }
        }
    }
}

@Composable
fun StudentFeeManagementApp(viewModel: FeeViewModel = viewModel()) {
    val isLoggedIn by viewModel.isAdminLoggedIn.collectAsStateWithLifecycle()
    val loginError by viewModel.adminLoginError.collectAsStateWithLifecycle()

    val instituteName by viewModel.instituteName.collectAsStateWithLifecycle()
    val instituteAddress by viewModel.instituteAddress.collectAsStateWithLifecycle()
    val institutePhone by viewModel.institutePhone.collectAsStateWithLifecycle()
    val academicYear by viewModel.academicYear.collectAsStateWithLifecycle()
    val currencySymbol by viewModel.currencySymbol.collectAsStateWithLifecycle()
    val adminPin by viewModel.adminPin.collectAsStateWithLifecycle()
    val scriptUrl by viewModel.googleSheetScriptUrl.collectAsStateWithLifecycle()

    val stats by viewModel.dashboardStats.collectAsStateWithLifecycle()
    val studentsWithFeeStatus by viewModel.studentsWithFeeStatus.collectAsStateWithLifecycle()
    val filteredStudents by viewModel.filteredStudents.collectAsStateWithLifecycle()
    val allPayments by viewModel.allPayments.collectAsStateWithLifecycle()
    val classCollections by viewModel.classCollections.collectAsStateWithLifecycle()

    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val availableClasses by viewModel.availableClasses.collectAsStateWithLifecycle()
    val availableBatches by viewModel.availableBatches.collectAsStateWithLifecycle()
    val selectedClass by viewModel.selectedClassFilter.collectAsStateWithLifecycle()
    val selectedBatch by viewModel.selectedBatchFilter.collectAsStateWithLifecycle()
    val selectedStatus by viewModel.selectedStatusFilter.collectAsStateWithLifecycle()

    val actionMessage by viewModel.actionMessage.collectAsStateWithLifecycle()
    val isSyncing by viewModel.isSyncing.collectAsStateWithLifecycle()
    val lastReceipt by viewModel.lastGeneratedReceipt.collectAsStateWithLifecycle()

    val snackbarHostState = remember { SnackbarHostState() }
    val navController = rememberNavController()

    // Dialog Control States
    var showAddStudentDialog by remember { mutableStateOf(false) }
    var studentToEdit by remember { mutableStateOf<StudentEntity?>(null) }

    var showAddPaymentDialog by remember { mutableStateOf(false) }
    var preSelectedStudentIdForPayment by remember { mutableStateOf<String?>(null) }

    var activeReceiptDialog by remember { mutableStateOf<ReceiptData?>(null) }

    // Show Action Messages in Snackbar
    LaunchedEffect(actionMessage) {
        actionMessage?.let { msg ->
            snackbarHostState.showSnackbar(msg)
            viewModel.clearActionMessage()
        }
    }

    // Auto open receipt dialog when a payment is saved
    LaunchedEffect(lastReceipt) {
        lastReceipt?.let { receipt ->
            activeReceiptDialog = receipt
            viewModel.lastGeneratedReceipt.value = null
        }
    }

    if (!isLoggedIn) {
        AdminLoginScreen(
            instituteName = instituteName,
            loginError = loginError,
            onLoginSubmit = { pin -> viewModel.loginAdmin(pin) }
        )
        return
    }

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route ?: NavRoute.Dashboard.route

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        bottomBar = {
            if (bottomNavItems.any { it.route == currentRoute }) {
                NavigationBar(
                    modifier = Modifier
                        .windowInsetsPadding(WindowInsets.navigationBars)
                        .testTag("bottom_navigation_bar"),
                    containerColor = MaterialTheme.colorScheme.surface,
                    tonalElevation = 0.dp
                ) {
                    bottomNavItems.forEach { item ->
                        val selected = currentRoute == item.route
                        NavigationBarItem(
                            selected = selected,
                            onClick = {
                                if (currentRoute != item.route) {
                                    navController.navigate(item.route) {
                                        popUpTo(NavRoute.Dashboard.route) { saveState = true }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                }
                            },
                            icon = {
                                Icon(
                                    imageVector = item.icon,
                                    contentDescription = item.titleEn
                                )
                            },
                            label = {
                                Text(
                                    text = item.titleEn,
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium
                                    )
                                )
                            },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = NavyPrimary,
                                selectedTextColor = NavyPrimary,
                                unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                indicatorColor = com.example.ui.theme.SoftBlueContainer
                            )
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            NavHost(
                navController = navController,
                startDestination = NavRoute.Dashboard.route
            ) {
                composable(NavRoute.Dashboard.route) {
                    DashboardScreen(
                        stats = stats,
                        studentList = studentsWithFeeStatus,
                        payments = allPayments,
                        classCollections = classCollections,
                        currencySymbol = currencySymbol,
                        instituteName = instituteName,
                        onAddStudentClick = {
                            studentToEdit = null
                            showAddStudentDialog = true
                        },
                        onAddPaymentClick = {
                            preSelectedStudentIdForPayment = null
                            showAddPaymentDialog = true
                        },
                        onStudentClick = { studentId ->
                            navController.navigate("student_detail/$studentId")
                        },
                        onSyncClick = { viewModel.syncPullFromSheet() }
                    )
                }

                composable(NavRoute.Students.route) {
                    StudentManagementScreen(
                        students = filteredStudents,
                        query = searchQuery,
                        onQueryChange = { viewModel.searchQuery.value = it },
                        classes = availableClasses,
                        selectedClass = selectedClass,
                        onClassSelected = { viewModel.selectedClassFilter.value = it },
                        batches = availableBatches,
                        selectedBatch = selectedBatch,
                        onBatchSelected = { viewModel.selectedBatchFilter.value = it },
                        selectedStatus = selectedStatus,
                        onStatusSelected = { viewModel.selectedStatusFilter.value = it },
                        currencySymbol = currencySymbol,
                        onAddStudentClick = {
                            studentToEdit = null
                            showAddStudentDialog = true
                        },
                        onEditStudentClick = { entity ->
                            studentToEdit = entity
                            showAddStudentDialog = true
                        },
                        onDeleteStudentClick = { id -> viewModel.deleteStudent(id) },
                        onStudentDetailClick = { id -> navController.navigate("student_detail/$id") },
                        onCollectFeeClick = { id ->
                            preSelectedStudentIdForPayment = id
                            showAddPaymentDialog = true
                        }
                    )
                }

                composable(NavRoute.Payments.route) {
                    PaymentManagementScreen(
                        payments = allPayments,
                        students = studentsWithFeeStatus,
                        currencySymbol = currencySymbol,
                        instituteName = instituteName,
                        instituteAddress = instituteAddress,
                        institutePhone = institutePhone,
                        onAddPaymentClick = {
                            preSelectedStudentIdForPayment = null
                            showAddPaymentDialog = true
                        },
                        onDeletePaymentClick = { id -> viewModel.deletePayment(id) },
                        onShowReceipt = { receipt -> activeReceiptDialog = receipt }
                    )
                }

                composable(NavRoute.PendingFees.route) {
                    PendingFeesScreen(
                        students = studentsWithFeeStatus,
                        classes = availableClasses,
                        selectedClass = selectedClass,
                        onClassSelected = { viewModel.selectedClassFilter.value = it },
                        batches = availableBatches,
                        selectedBatch = selectedBatch,
                        onBatchSelected = { viewModel.selectedBatchFilter.value = it },
                        currencySymbol = currencySymbol,
                        onCollectFeeClick = { id ->
                            preSelectedStudentIdForPayment = id
                            showAddPaymentDialog = true
                        },
                        onStudentClick = { id -> navController.navigate("student_detail/$id") }
                    )
                }

                composable(NavRoute.Reports.route) {
                    ReportsScreen(
                        stats = stats,
                        classCollections = classCollections,
                        currencySymbol = currencySymbol
                    )
                }

                composable(NavRoute.Settings.route) {
                    SettingsScreen(
                        currentName = instituteName,
                        currentAddress = instituteAddress,
                        currentPhone = institutePhone,
                        currentYear = academicYear,
                        currentCurrency = currencySymbol,
                        currentAdminPin = adminPin,
                        currentScriptUrl = scriptUrl,
                        isSyncing = isSyncing,
                        onSaveSettings = { name, address, phone, year, currency, pin, script ->
                            viewModel.saveSettings(name, address, phone, year, currency, pin, script)
                        },
                        onTestConnection = { viewModel.testConnection() },
                        onSyncPush = { viewModel.syncPushToSheet() },
                        onSyncPull = { viewModel.syncPullFromSheet() },
                        onLogout = { viewModel.logoutAdmin() }
                    )
                }

                composable("student_detail/{studentId}") { backStackEntry ->
                    val studentId = backStackEntry.arguments?.getString("studentId") ?: ""
                    val studentStatusItem = studentsWithFeeStatus.find { it.student.studentId == studentId }
                    val studentPayments = allPayments.filter { it.studentId == studentId }

                    StudentDetailScreen(
                        studentStatusItem = studentStatusItem,
                        payments = studentPayments,
                        currencySymbol = currencySymbol,
                        instituteName = instituteName,
                        instituteAddress = instituteAddress,
                        institutePhone = institutePhone,
                        onBackClick = { navController.popBackStack() },
                        onEditClick = { entity ->
                            studentToEdit = entity
                            showAddStudentDialog = true
                        },
                        onAddPaymentClick = { id ->
                            preSelectedStudentIdForPayment = id
                            showAddPaymentDialog = true
                        },
                        onDeletePaymentClick = { pId -> viewModel.deletePayment(pId) },
                        onShowReceipt = { receipt -> activeReceiptDialog = receipt }
                    )
                }
            }
        }
    }

    // Add/Edit Student Dialog
    if (showAddStudentDialog) {
        AddEditStudentDialog(
            studentToEdit = studentToEdit,
            onDismiss = {
                showAddStudentDialog = false
                studentToEdit = null
            },
            onSave = { sId, sName, sFather, sClass, sBatch, sMobile, sFee, sDate ->
                if (studentToEdit != null) {
                    viewModel.updateStudent(
                        StudentEntity(
                            studentId = sId,
                            name = sName,
                            fatherName = sFather,
                            className = sClass,
                            batch = sBatch,
                            mobile = sMobile,
                            totalFee = sFee,
                            admissionDate = sDate
                        )
                    )
                } else {
                    viewModel.addStudent(sId, sName, sFather, sClass, sBatch, sMobile, sFee, sDate)
                }
            }
        )
    }

    // Add Payment Dialog
    if (showAddPaymentDialog) {
        AddPaymentDialog(
            students = studentsWithFeeStatus,
            preSelectedStudentId = preSelectedStudentIdForPayment,
            currencySymbol = currencySymbol,
            onDismiss = {
                showAddPaymentDialog = false
                preSelectedStudentIdForPayment = null
            },
            onSavePayment = { sId, amt, mode, date, recNo, remark ->
                viewModel.addPayment(sId, amt, mode, date, recNo, remark)
            }
        )
    }

    // Receipt Modal / Dialog
    activeReceiptDialog?.let { receipt ->
        ReceiptDialog(
            receipt = receipt,
            onDismiss = { activeReceiptDialog = null }
        )
    }
}
