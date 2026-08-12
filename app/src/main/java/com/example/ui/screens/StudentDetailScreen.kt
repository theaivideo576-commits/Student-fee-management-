package com.example.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Print
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Sms
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.local.PaymentEntity
import com.example.data.local.StudentEntity
import com.example.data.model.StudentWithFeeStatus
import com.example.ui.components.ConfirmDeleteDialog
import com.example.ui.components.StatusChip
import com.example.ui.theme.AmberPending
import com.example.ui.theme.EmeraldPaid
import com.example.ui.theme.EmeraldPaidBg
import com.example.ui.theme.NavyPrimary
import com.example.ui.viewmodel.ReceiptData

@Composable
fun StudentDetailScreen(
    studentStatusItem: StudentWithFeeStatus?,
    payments: List<PaymentEntity>,
    currencySymbol: String,
    instituteName: String,
    instituteAddress: String,
    institutePhone: String,
    onBackClick: () -> Unit,
    onEditClick: (StudentEntity) -> Unit,
    onAddPaymentClick: (String) -> Unit,
    onDeletePaymentClick: (String) -> Unit,
    onShowReceipt: (ReceiptData) -> Unit
) {
    val context = LocalContext.current
    var paymentToDelete by remember { mutableStateOf<PaymentEntity?>(null) }

    if (studentStatusItem == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("Student Record Not Found / रिकॉर्ड नहीं मिला")
                Spacer(modifier = Modifier.height(12.dp))
                Button(onClick = onBackClick) { Text("Go Back / वापस जाएं") }
            }
        }
        return
    }

    val s = studentStatusItem.student

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item { Spacer(modifier = Modifier.height(8.dp)) }

        // Top Navigation Bar
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBackClick) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Student Profile / विद्यार्थी विवरण",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    modifier = Modifier.weight(1f)
                )
                IconButton(onClick = { onEditClick(s) }) {
                    Icon(Icons.Default.Edit, contentDescription = "Edit Student", tint = NavyPrimary)
                }
            }
        }

        // Student Overview Profile Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = androidx.compose.foundation.BorderStroke(1.dp, com.example.ui.theme.SlateBorder),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(52.dp)
                                    .clip(CircleShape)
                                    .background(com.example.ui.theme.SoftBlueContainer),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.Person, contentDescription = null, tint = NavyPrimary, modifier = Modifier.size(32.dp))
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = s.name,
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                                )
                                Text(
                                    text = "ID: ${s.studentId}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        StatusChip(status = studentStatusItem.status)
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Fields Table
                    DetailFieldRow("Father's Name (पिता का नाम)", s.fatherName.ifBlank { "N/A" })
                    DetailFieldRow("Class (कक्षा)", s.className)
                    DetailFieldRow("Batch (बैच)", s.batch.ifBlank { "N/A" })
                    DetailFieldRow("Mobile (मोबाइल)", s.mobile.ifBlank { "N/A" })
                    DetailFieldRow("Admission Date (प्रवेश तिथि)", s.admissionDate)

                    Spacer(modifier = Modifier.height(16.dp))

                    // Call & SMS Action Buttons
                    if (s.mobile.isNotBlank()) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            OutlinedButton(
                                onClick = {
                                    val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:${s.mobile}"))
                                    context.startActivity(intent)
                                },
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(Icons.Default.Call, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Call / कॉल करें")
                            }

                            OutlinedButton(
                                onClick = {
                                    val intent = Intent(Intent.ACTION_SENDTO, Uri.parse("smsto:${s.mobile}")).apply {
                                        putExtra("sms_body", "Dear ${s.name}, your pending fee for ${s.className} is $currencySymbol${studentStatusItem.pendingFee.toInt()}. Please clear it soon. - $instituteName")
                                    }
                                    context.startActivity(intent)
                                },
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(Icons.Default.Sms, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("SMS Reminder")
                            }
                        }
                    }
                }
            }
        }

        // Fee Balance Summary Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = androidx.compose.foundation.BorderStroke(1.dp, com.example.ui.theme.SlateBorder),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(
                        text = "Fee Balance Summary / फीस विवरण",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = NavyPrimary
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text("Total Fee (कुल फीस)", style = MaterialTheme.typography.bodySmall)
                            Text("$currencySymbol${s.totalFee.toInt()}", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                        }
                        Column {
                            Text("Paid (कुल जमा)", style = MaterialTheme.typography.bodySmall)
                            Text("$currencySymbol${studentStatusItem.totalPaid.toInt()}", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = EmeraldPaid))
                        }
                        Column {
                            Text("Pending (बकाया)", style = MaterialTheme.typography.bodySmall)
                            Text("$currencySymbol${studentStatusItem.pendingFee.toInt()}", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = if (studentStatusItem.pendingFee > 0) AmberPending else EmeraldPaid))
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = { onAddPaymentClick(s.studentId) },
                        modifier = Modifier.fillMaxWidth().height(48.dp).testTag("add_payment_for_student_button"),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = com.example.ui.theme.NavyLight)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Record Payment / भुगतान जमा करें", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // Payment History Header
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Payment History (${payments.size}) / भुगतान इतिहास",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )
            }
        }

        // Payment Transactions List
        if (payments.isEmpty()) {
            item {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surface
                ) {
                    Box(modifier = Modifier.padding(24.dp), contentAlignment = Alignment.Center) {
                        Text("No payment transactions recorded yet / कोई भुगतान रिकॉर्ड नहीं", style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }
        } else {
            items(payments) { p ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(EmeraldPaidBg),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.ReceiptLong, contentDescription = null, tint = EmeraldPaid, modifier = Modifier.size(20.dp))
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = "$currencySymbol${p.amount.toInt()} (${p.mode})",
                                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                    color = EmeraldPaid
                                )
                                Text(
                                    text = "Date: ${p.paymentDate} | Receipt: ${p.receiptNo}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                if (p.remark.isNotBlank()) {
                                    Text(
                                        text = "Remark: ${p.remark}",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }

                        Row {
                            IconButton(onClick = {
                                val receipt = ReceiptData(
                                    instituteName = instituteName,
                                    instituteAddress = instituteAddress,
                                    phone = institutePhone,
                                    studentName = s.name,
                                    studentId = s.studentId,
                                    className = s.className,
                                    batch = s.batch,
                                    paymentDate = p.paymentDate,
                                    amountPaid = p.amount,
                                    paymentMode = p.mode,
                                    receiptNumber = p.receiptNo,
                                    remark = p.remark,
                                    totalFee = s.totalFee,
                                    totalPaid = studentStatusItem.totalPaid,
                                    remainingFee = studentStatusItem.pendingFee,
                                    currency = currencySymbol
                                )
                                onShowReceipt(receipt)
                            }) {
                                Icon(Icons.Default.Print, contentDescription = "Receipt", tint = NavyPrimary)
                            }

                            IconButton(onClick = { paymentToDelete = p }) {
                                Icon(Icons.Default.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error)
                            }
                        }
                    }
                }
            }
        }

        item { Spacer(modifier = Modifier.height(40.dp)) }
    }

    // Delete Payment Confirmation Dialog
    paymentToDelete?.let { target ->
        ConfirmDeleteDialog(
            title = "Delete Payment Record?",
            message = "Are you sure you want to delete payment of $currencySymbol${target.amount} (Receipt: ${target.receiptNo})?",
            onConfirm = { onDeletePaymentClick(target.paymentId) },
            onDismiss = { paymentToDelete = null }
        )
    }
}

@Composable
fun DetailFieldRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(text = value, style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium), color = MaterialTheme.colorScheme.onSurface)
    }
}
