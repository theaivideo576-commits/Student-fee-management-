package com.example.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.example.data.model.StudentWithFeeStatus
import com.example.ui.theme.AmberPending
import com.example.ui.theme.EmeraldPaid
import com.example.ui.theme.NavyPrimary
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun AddPaymentDialog(
    students: List<StudentWithFeeStatus>,
    preSelectedStudentId: String? = null,
    currencySymbol: String = "₹",
    onDismiss: () -> Unit,
    onSavePayment: (
        studentId: String,
        amount: Double,
        mode: String,
        date: String,
        receiptNo: String,
        remark: String
    ) -> Unit
) {
    var selectedStudent by remember {
        mutableStateOf(
            students.find { it.student.studentId == preSelectedStudentId } ?: students.firstOrNull()
        )
    }

    var dropdownExpanded by remember { mutableStateOf(false) }

    val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
    val autoReceipt = "REC-" + System.currentTimeMillis().toString().takeLast(6)

    var amountStr by remember { mutableStateOf("") }
    var selectedMode by remember { mutableStateOf("UPI") }
    var paymentDate by remember { mutableStateOf(today) }
    var receiptNo by remember { mutableStateOf(autoReceipt) }
    var remark by remember { mutableStateOf("Installment Fee") }

    var errorMessage by remember { mutableStateOf<String?>(null) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            shape = RoundedCornerShape(28.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = androidx.compose.foundation.BorderStroke(1.dp, com.example.ui.theme.SlateBorder),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Record Fee Payment / फीस भुगतान जोड़ें",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = NavyPrimary
                )

                Spacer(modifier = Modifier.height(16.dp))

                if (students.isEmpty()) {
                    Text(
                        text = "No students available. Please add a student first.",
                        color = MaterialTheme.colorScheme.error
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedButton(onClick = onDismiss, shape = RoundedCornerShape(14.dp)) {
                        Text("Close / बंद करें")
                    }
                    return@Column
                }

                if (errorMessage != null) {
                    Text(
                        text = errorMessage!!,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                }

                // Student Selector Dropdown
                Text(
                    text = "Select Student (विद्यार्थी चुनें):",
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.align(Alignment.Start)
                )
                Spacer(modifier = Modifier.height(4.dp))

                Box(modifier = Modifier.fillMaxWidth()) {
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { dropdownExpanded = true }
                            .testTag("student_picker_dropdown"),
                        shape = RoundedCornerShape(14.dp),
                        color = com.example.ui.theme.RecentItemBg,
                        border = androidx.compose.foundation.BorderStroke(1.dp, com.example.ui.theme.SlateBorder)
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = selectedStudent?.student?.name ?: "Select Student",
                                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                                )
                                Text(
                                    text = "ID: ${selectedStudent?.student?.studentId} | Class: ${selectedStudent?.student?.className}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Icon(Icons.Default.ArrowDropDown, contentDescription = null)
                        }
                    }

                    DropdownMenu(
                        expanded = dropdownExpanded,
                        onDismissRequest = { dropdownExpanded = false },
                        modifier = Modifier.fillMaxWidth(0.85f)
                    ) {
                        students.forEach { item ->
                            DropdownMenuItem(
                                text = {
                                    Column {
                                        Text("${item.student.name} (${item.student.studentId})", fontWeight = FontWeight.Bold)
                                        Text("Class: ${item.student.className} | Pending: $currencySymbol${item.pendingFee}", style = MaterialTheme.typography.bodySmall)
                                    }
                                },
                                onClick = {
                                    selectedStudent = item
                                    dropdownExpanded = false
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Student Fee Stats Summary Card
                selectedStudent?.let { stu ->
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        color = com.example.ui.theme.SoftBlueContainer.copy(alpha = 0.5f),
                        shape = RoundedCornerShape(14.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, com.example.ui.theme.SoftBlueContainer)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            FeeCalculationRow("Total Course Fee (कुल फीस):", "$currencySymbol${stu.student.totalFee}")
                            FeeCalculationRow("Already Paid (पहले जमा):", "$currencySymbol${stu.totalPaid}", color = EmeraldPaid)
                            FeeCalculationRow(
                                "Current Pending (वर्तमान बकाया):",
                                "$currencySymbol${stu.pendingFee}",
                                isBold = true,
                                color = if (stu.pendingFee > 0) AmberPending else EmeraldPaid
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Payment Amount Input
                OutlinedTextField(
                    value = amountStr,
                    onValueChange = { amountStr = it },
                    label = { Text("Payment Amount (भुगतान राशि $currencySymbol)*") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth().testTag("payment_amount_input"),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Payment Mode Selector
                Text(
                    text = "Payment Mode (भुगतान का प्रकार):",
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.align(Alignment.Start)
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val modes = listOf("UPI", "Cash", "Bank Transfer", "Other")
                    modes.forEach { mode ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .clickable { selectedMode = mode }
                                .padding(end = 8.dp)
                        ) {
                            RadioButton(
                                selected = (selectedMode == mode),
                                onClick = { selectedMode = mode }
                            )
                            Text(text = mode, style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Payment Date
                OutlinedTextField(
                    value = paymentDate,
                    onValueChange = { paymentDate = it },
                    label = { Text("Payment Date (भुगतान तिथि yyyy-MM-dd)") },
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth().testTag("payment_date_input"),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Receipt Number
                OutlinedTextField(
                    value = receiptNo,
                    onValueChange = { receiptNo = it },
                    label = { Text("Receipt Number (रसीद संख्या)") },
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth().testTag("receipt_no_input"),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Remark
                OutlinedTextField(
                    value = remark,
                    onValueChange = { remark = it },
                    label = { Text("Remark / Note (टिप्पणी, e.g. 1st Installment)") },
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth().testTag("remark_input"),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(20.dp))

                // Submit Button
                Button(
                    onClick = {
                        val stu = selectedStudent
                        if (stu == null) {
                            errorMessage = "Please select a student / विद्यार्थी चुनें"
                            return@Button
                        }
                        val amt = amountStr.toDoubleOrNull()
                        if (amt == null || amt <= 0) {
                            errorMessage = "Payment amount must be greater than 0 / राशि 0 से अधिक होनी चाहिए"
                            return@Button
                        }
                        if (amt > stu.pendingFee + 0.01) {
                            errorMessage = "Payment ($currencySymbol$amt) cannot exceed current pending fee ($currencySymbol${stu.pendingFee})"
                            return@Button
                        }

                        onSavePayment(
                            stu.student.studentId,
                            amt,
                            selectedMode,
                            paymentDate,
                            receiptNo,
                            remark
                        )
                        onDismiss()
                    },
                    modifier = Modifier.fillMaxWidth().height(48.dp).testTag("save_payment_button"),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = com.example.ui.theme.NavyLight)
                ) {
                    Text("Save & Generate Receipt / भुगतान सहेजें", fontWeight = FontWeight.Bold)
                }

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedButton(
                    onClick = onDismiss,
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Cancel / रद्द करें")
                }
            }
        }
    }
}

@Composable
fun FeeCalculationRow(label: String, value: String, isBold: Boolean = false, color: androidx.compose.ui.graphics.Color = MaterialTheme.colorScheme.onSurface) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        horizontalArrangement = androidx.compose.foundation.layout.Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = if (isBold) MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold) else MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurface
        )
        Text(
            text = value,
            style = if (isBold) MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold) else MaterialTheme.typography.bodySmall,
            color = color
        )
    }
}
