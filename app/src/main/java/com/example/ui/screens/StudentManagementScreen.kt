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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Payment
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.local.StudentEntity
import com.example.data.model.StudentWithFeeStatus
import com.example.ui.components.ConfirmDeleteDialog
import com.example.ui.components.SearchBarWithFilters
import com.example.ui.components.StatusChip
import com.example.ui.theme.AmberPending
import com.example.ui.theme.EmeraldPaid
import com.example.ui.theme.NavyPrimary

@Composable
fun StudentManagementScreen(
    students: List<StudentWithFeeStatus>,
    query: String,
    onQueryChange: (String) -> Unit,
    classes: List<String>,
    selectedClass: String,
    onClassSelected: (String) -> Unit,
    batches: List<String>,
    selectedBatch: String,
    onBatchSelected: (String) -> Unit,
    selectedStatus: String,
    onStatusSelected: (String) -> Unit,
    currencySymbol: String,
    onAddStudentClick: () -> Unit,
    onEditStudentClick: (StudentEntity) -> Unit,
    onDeleteStudentClick: (String) -> Unit,
    onStudentDetailClick: (String) -> Unit,
    onCollectFeeClick: (String) -> Unit
) {
    var studentToDelete by remember { mutableStateOf<StudentWithFeeStatus?>(null) }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp)
        ) {
            Spacer(modifier = Modifier.height(8.dp))

            // Search Bar & Filter Controls
            SearchBarWithFilters(
                query = query,
                onQueryChange = onQueryChange,
                classes = classes,
                selectedClass = selectedClass,
                onClassSelected = onClassSelected,
                batches = batches,
                selectedBatch = selectedBatch,
                onBatchSelected = onBatchSelected,
                selectedStatus = selectedStatus,
                onStatusSelected = onStatusSelected
            )

            Text(
                text = "Total Students Found: ${students.size}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(vertical = 4.dp)
            )

            // Student Cards List
            if (students.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = null,
                            modifier = Modifier.size(64.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "No students found / कोई विद्यार्थी नहीं मिला",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(students, key = { it.student.studentId }) { item ->
                        StudentCardItem(
                            item = item,
                            currencySymbol = currencySymbol,
                            onDetailClick = { onStudentDetailClick(item.student.studentId) },
                            onEditClick = { onEditStudentClick(item.student) },
                            onDeleteClick = { studentToDelete = item },
                            onCollectFeeClick = { onCollectFeeClick(item.student.studentId) }
                        )
                    }

                    item { Spacer(modifier = Modifier.height(80.dp)) }
                }
            }
        }

        // Floating Action Button
        FloatingActionButton(
            onClick = onAddStudentClick,
            containerColor = NavyPrimary,
            contentColor = Color.White,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(20.dp)
                .testTag("fab_add_student")
        ) {
            Icon(Icons.Default.Add, contentDescription = "Add Student")
        }

        // Delete Confirmation Dialog
        studentToDelete?.let { target ->
            ConfirmDeleteDialog(
                title = "Delete Student Record?",
                message = "Are you sure you want to delete ${target.student.name} (${target.student.studentId})? All payment records for this student will also be permanently deleted.",
                onConfirm = { onDeleteStudentClick(target.student.studentId) },
                onDismiss = { studentToDelete = null }
            )
        }
    }
}

@Composable
fun StudentCardItem(
    item: StudentWithFeeStatus,
    currencySymbol: String,
    onDetailClick: () -> Unit,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit,
    onCollectFeeClick: () -> Unit
) {
    val s = item.student

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onDetailClick() },
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = androidx.compose.foundation.BorderStroke(1.dp, com.example.ui.theme.SlateBorder),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header Row: ID, Name, Status Badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = s.name,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "ID: ${s.studentId} | Father: ${s.fatherName.ifBlank { "N/A" }}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                StatusChip(status = item.status)
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Class, Batch, Mobile Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Class: ${s.className} (${s.batch})",
                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium)
                )
                if (s.mobile.isNotBlank()) {
                    Text(
                        text = "📞 ${s.mobile}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Fee Stats Grid inside Card
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
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Total Fee", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text("$currencySymbol${s.totalFee.toInt()}", style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold))
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Paid", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text("$currencySymbol${item.totalPaid.toInt()}", style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold), color = EmeraldPaid)
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Pending", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text("$currencySymbol${item.pendingFee.toInt()}", style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold), color = if (item.pendingFee > 0) AmberPending else EmeraldPaid)
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Action Buttons Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (item.pendingFee > 0) {
                    OutlinedButton(
                        onClick = onCollectFeeClick,
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(Icons.Default.Payment, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Pay Fee / फीस लें", style = MaterialTheme.typography.labelMedium)
                    }
                } else {
                    Text(
                        text = "Fully Paid / पूर्ण भुगतान",
                        style = MaterialTheme.typography.labelMedium.copy(color = EmeraldPaid, fontWeight = FontWeight.Bold)
                    )
                }

                Row {
                    IconButton(onClick = onEditClick) {
                        Icon(Icons.Default.Edit, contentDescription = "Edit", tint = MaterialTheme.colorScheme.primary)
                    }
                    IconButton(onClick = onDeleteClick) {
                        Icon(Icons.Default.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error)
                    }
                }
            }
        }
    }
}
