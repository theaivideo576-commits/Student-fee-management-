package com.example.ui.screens

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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Print
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.local.PaymentEntity
import com.example.data.model.StudentWithFeeStatus
import com.example.ui.components.ConfirmDeleteDialog
import com.example.ui.theme.EmeraldPaid
import com.example.ui.theme.EmeraldPaidBg
import com.example.ui.theme.NavyPrimary
import com.example.ui.viewmodel.ReceiptData

@Composable
fun PaymentManagementScreen(
    payments: List<PaymentEntity>,
    students: List<StudentWithFeeStatus>,
    currencySymbol: String,
    instituteName: String,
    instituteAddress: String,
    institutePhone: String,
    onAddPaymentClick: () -> Unit,
    onDeletePaymentClick: (String) -> Unit,
    onShowReceipt: (ReceiptData) -> Unit
) {
    var filterQuery by remember { mutableStateOf("") }
    var paymentToDelete by remember { mutableStateOf<PaymentEntity?>(null) }

    val filteredPayments = payments.filter { p ->
        val q = filterQuery.trim().lowercase()
        if (q.isEmpty()) true
        else {
            val student = students.find { it.student.studentId == p.studentId }
            val studentName = student?.student?.name?.lowercase() ?: ""
            p.studentId.lowercase().contains(q) ||
                    p.receiptNo.lowercase().contains(q) ||
                    studentName.contains(q) ||
                    p.paymentDate.contains(q) ||
                    p.mode.lowercase().contains(q)
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp)
        ) {
            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Payment Transactions / भुगतान इतिहास",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = NavyPrimary
            )

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = filterQuery,
                onValueChange = { filterQuery = it },
                placeholder = { Text("Filter payments by Name, Receipt, ID, Mode... / खोजें") },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("payments_search_input"),
                shape = RoundedCornerShape(20.dp),
                colors = androidx.compose.material3.OutlinedTextFieldDefaults.colors(
                    unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                    focusedContainerColor = MaterialTheme.colorScheme.surface,
                    unfocusedBorderColor = com.example.ui.theme.SlateBorder,
                    focusedBorderColor = MaterialTheme.colorScheme.primary
                ),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "Total Payments Recorded: ${filteredPayments.size}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(8.dp))

            if (filteredPayments.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.Payments,
                            contentDescription = null,
                            modifier = Modifier.size(64.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "No payments found / कोई भुगतान रिकॉर्ड नहीं",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(filteredPayments, key = { it.paymentId }) { p ->
                        val studentItem = students.find { it.student.studentId == p.studentId }
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(20.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            border = androidx.compose.foundation.BorderStroke(1.dp, com.example.ui.theme.SlateBorder),
                            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
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
                                            .size(42.dp)
                                            .clip(CircleShape)
                                            .background(EmeraldPaidBg),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.ReceiptLong,
                                            contentDescription = null,
                                            tint = EmeraldPaid,
                                            modifier = Modifier.size(24.dp)
                                        )
                                    }

                                    Spacer(modifier = Modifier.width(12.dp))

                                    Column {
                                        Text(
                                            text = studentItem?.student?.name ?: p.studentId,
                                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                                        )
                                        Text(
                                            text = "Receipt: ${p.receiptNo} | Mode: ${p.mode}",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                        Text(
                                            text = "Date: ${p.paymentDate} | ID: ${p.studentId}",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }

                                Column(horizontalAlignment = Alignment.End) {
                                    Text(
                                        text = "+$currencySymbol${p.amount.toInt()}",
                                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                        color = EmeraldPaid
                                    )

                                    Row {
                                        IconButton(onClick = {
                                            studentItem?.let { item ->
                                                val receipt = ReceiptData(
                                                    instituteName = instituteName,
                                                    instituteAddress = instituteAddress,
                                                    phone = institutePhone,
                                                    studentName = item.student.name,
                                                    studentId = item.student.studentId,
                                                    className = item.student.className,
                                                    batch = item.student.batch,
                                                    paymentDate = p.paymentDate,
                                                    amountPaid = p.amount,
                                                    paymentMode = p.mode,
                                                    receiptNumber = p.receiptNo,
                                                    remark = p.remark,
                                                    totalFee = item.student.totalFee,
                                                    totalPaid = item.totalPaid,
                                                    remainingFee = item.pendingFee,
                                                    currency = currencySymbol
                                                )
                                                onShowReceipt(receipt)
                                            }
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

                    item { Spacer(modifier = Modifier.height(80.dp)) }
                }
            }
        }

        // Floating Action Button
        FloatingActionButton(
            onClick = onAddPaymentClick,
            containerColor = com.example.ui.theme.NavyLight,
            contentColor = Color.White,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(20.dp)
                .testTag("fab_add_payment")
        ) {
            Icon(Icons.Default.Add, contentDescription = "Record Fee Payment")
        }

        // Delete Confirmation Dialog
        paymentToDelete?.let { target ->
            ConfirmDeleteDialog(
                title = "Delete Payment Record?",
                message = "Are you sure you want to delete payment of $currencySymbol${target.amount} (Receipt: ${target.receiptNo})?",
                onConfirm = { onDeletePaymentClick(target.paymentId) },
                onDismiss = { paymentToDelete = null }
            )
        }
    }
}
