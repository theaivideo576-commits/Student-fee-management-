package com.example.ui.screens

import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Payment
import androidx.compose.material.icons.filled.PendingActions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.model.StudentWithFeeStatus
import com.example.ui.components.FilterDropdown
import com.example.ui.components.StatusChip
import com.example.ui.theme.AmberPending
import com.example.ui.theme.AmberPendingBg
import com.example.ui.theme.EmeraldPaid
import com.example.ui.theme.NavyPrimary

@Composable
fun PendingFeesScreen(
    students: List<StudentWithFeeStatus>,
    classes: List<String>,
    selectedClass: String,
    onClassSelected: (String) -> Unit,
    batches: List<String>,
    selectedBatch: String,
    onBatchSelected: (String) -> Unit,
    currencySymbol: String,
    onCollectFeeClick: (String) -> Unit,
    onStudentClick: (String) -> Unit
) {
    val pendingList = students.filter { item ->
        item.status == "PENDING" &&
                (selectedClass == "All" || item.student.className.equals(selectedClass, ignoreCase = true)) &&
                (selectedBatch == "All" || item.student.batch.equals(selectedBatch, ignoreCase = true))
    }

    val totalPendingAmount = pendingList.sumOf { it.pendingFee }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {
        Spacer(modifier = Modifier.height(8.dp))

        // Banner Card
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            color = AmberPendingBg,
            border = androidx.compose.foundation.BorderStroke(1.dp, AmberPending.copy(alpha = 0.3f))
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.PendingActions,
                    contentDescription = null,
                    tint = AmberPending,
                    modifier = Modifier.size(36.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = "Pending Fees Summary / बकाया सूची",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                        color = AmberPending
                    )
                    Text(
                        text = "${pendingList.size} Students | Total: $currencySymbol${totalPendingAmount.toInt()}",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Filter Dropdowns
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            FilterDropdown(
                label = "Class: $selectedClass",
                options = classes,
                selectedOption = selectedClass,
                onOptionSelected = onClassSelected,
                modifier = Modifier.weight(1f)
            )

            FilterDropdown(
                label = "Batch: $selectedBatch",
                options = batches,
                selectedOption = selectedBatch,
                onOptionSelected = onBatchSelected,
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        if (pendingList.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.PendingActions,
                        contentDescription = null,
                        modifier = Modifier.size(64.dp),
                        tint = EmeraldPaid.copy(alpha = 0.5f)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "No pending fees in selected filter! / कोई बकाया फीस नहीं है",
                        style = MaterialTheme.typography.bodyMedium,
                        color = EmeraldPaid,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(pendingList, key = { it.student.studentId }) { item ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onStudentClick(item.student.studentId) },
                        shape = RoundedCornerShape(24.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = androidx.compose.foundation.BorderStroke(1.dp, com.example.ui.theme.SlateBorder),
                        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.Top
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = item.student.name,
                                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                                    )
                                    Text(
                                        text = "ID: ${item.student.studentId} | Father: ${item.student.fatherName}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Text(
                                        text = "Class: ${item.student.className} (${item.student.batch})",
                                        style = MaterialTheme.typography.bodySmall,
                                        fontWeight = FontWeight.Medium
                                    )
                                }

                                StatusChip(status = "PENDING")
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            Surface(
                                modifier = Modifier.fillMaxWidth(),
                                color = com.example.ui.theme.RecentItemBg,
                                shape = RoundedCornerShape(14.dp),
                                border = androidx.compose.foundation.BorderStroke(1.dp, com.example.ui.theme.SoftBlueContainer)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(12.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text("Total Fee", style = MaterialTheme.typography.labelSmall)
                                        Text("$currencySymbol${item.student.totalFee.toInt()}", style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold))
                                    }
                                    Column {
                                        Text("Paid So Far", style = MaterialTheme.typography.labelSmall)
                                        Text("$currencySymbol${item.totalPaid.toInt()}", style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold, color = EmeraldPaid))
                                    }
                                    Column {
                                        Text("Pending Balance", style = MaterialTheme.typography.labelSmall)
                                        Text("$currencySymbol${item.pendingFee.toInt()}", style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold, color = AmberPending))
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            Button(
                                onClick = { onCollectFeeClick(item.student.studentId) },
                                modifier = Modifier.fillMaxWidth().height(48.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = com.example.ui.theme.NavyLight),
                                shape = RoundedCornerShape(14.dp)
                            ) {
                                Icon(Icons.Default.Payment, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Collect Fee / जमा करें ($currencySymbol${item.pendingFee.toInt()})", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }

                item { Spacer(modifier = Modifier.height(40.dp)) }
            }
        }
    }
}
