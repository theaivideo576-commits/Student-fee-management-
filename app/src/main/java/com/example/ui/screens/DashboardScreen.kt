package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CloudSync
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material.icons.filled.Payment
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.School
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.PaymentEntity
import com.example.data.model.ClassCollection
import com.example.data.model.DashboardStats
import com.example.data.model.StudentWithFeeStatus
import com.example.ui.components.StatCard
import com.example.ui.components.StatusChip
import com.example.ui.theme.AmberPending
import com.example.ui.theme.AmberPendingBg
import com.example.ui.theme.AmberPendingBorder
import com.example.ui.theme.EmeraldPaid
import com.example.ui.theme.EmeraldPaidBg
import com.example.ui.theme.NavyLight
import com.example.ui.theme.NavyPrimary
import com.example.ui.theme.RecentItemBg
import com.example.ui.theme.SlateBorder
import com.example.ui.theme.SoftBlueContainer
import com.example.ui.theme.SoftPurpleContainer

@Composable
fun DashboardScreen(
    stats: DashboardStats,
    studentList: List<StudentWithFeeStatus>,
    payments: List<PaymentEntity>,
    classCollections: List<ClassCollection>,
    currencySymbol: String,
    instituteName: String,
    onAddStudentClick: () -> Unit,
    onAddPaymentClick: () -> Unit,
    onStudentClick: (String) -> Unit,
    onSyncClick: () -> Unit
) {
    val pendingStudents = studentList.filter { it.status == "PENDING" }
    val recentPayments = payments.take(4)

    Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item { Spacer(modifier = Modifier.height(4.dp)) }

            // Header Row (Matching Professional Polish Theme Top Header)
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = if (instituteName.isBlank()) "Fee Manager" else instituteName,
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 22.sp,
                                letterSpacing = (-0.5).sp
                            ),
                            color = NavyPrimary
                        )
                        Text(
                            text = "FEE MANAGEMENT SYSTEM • फीस प्रबंधन प्रणाली",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 10.sp,
                                letterSpacing = 1.sp
                            ),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(
                            onClick = onSyncClick,
                            modifier = Modifier
                                .size(38.dp)
                                .clip(CircleShape)
                                .background(SoftBlueContainer)
                                .testTag("sync_button")
                        ) {
                            Icon(
                                Icons.Default.CloudSync,
                                contentDescription = "Sync",
                                tint = NavyPrimary,
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        // Avatar badge
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(SoftBlueContainer),
                            contentAlignment = Alignment.Center
                        ) {
                            val initials = instituteName.take(2).uppercase().ifBlank { "FM" }
                            Text(
                                text = initials,
                                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                                color = NavyPrimary
                            )
                        }
                    }
                }
            }

            // Quick Stats Grid (Matching 2-column Pastel Cards)
            item {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        StatCard(
                            title = "Total Students",
                            titleHi = "छात्र",
                            value = "${stats.totalStudents}",
                            icon = Icons.Default.Group,
                            containerColor = SoftBlueContainer,
                            contentColor = NavyPrimary,
                            modifier = Modifier.weight(1f).testTag("stat_total_students")
                        )

                        StatCard(
                            title = "Collection",
                            titleHi = "वसूली",
                            value = "$currencySymbol${stats.totalPaidAmount.toInt()}",
                            icon = Icons.Default.MonetizationOn,
                            containerColor = SoftPurpleContainer,
                            contentColor = Color(0xFF21005D),
                            modifier = Modifier.weight(1f).testTag("stat_total_paid")
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        StatCard(
                            title = "Today Collection",
                            titleHi = "आज",
                            value = "$currencySymbol${stats.todaysCollection.toInt()}",
                            icon = Icons.Default.Payment,
                            containerColor = EmeraldPaidBg,
                            contentColor = EmeraldPaid,
                            iconBgColor = Color.White.copy(alpha = 0.6f),
                            iconColor = EmeraldPaid,
                            modifier = Modifier.weight(1f).testTag("stat_today_collection")
                        )

                        StatCard(
                            title = "Total Pending",
                            titleHi = "बकाया",
                            value = "$currencySymbol${stats.totalPendingAmount.toInt()}",
                            icon = Icons.Default.MonetizationOn,
                            containerColor = AmberPendingBg,
                            contentColor = AmberPending,
                            iconBgColor = Color.White.copy(alpha = 0.6f),
                            iconColor = AmberPending,
                            modifier = Modifier.weight(1f).testTag("stat_total_pending")
                        )
                    }
                }
            }

            // Recent Payments & Main Status Container Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(28.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, SlateBorder),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Recent Payments / हाल के भुगतान",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }

                        if (recentPayments.isEmpty()) {
                            Text(
                                text = "No recent payments recorded. / कोई नया भुगतान नहीं।",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(vertical = 12.dp)
                            )
                        } else {
                            recentPayments.forEach { payment ->
                                val student = studentList.find { it.student.studentId == payment.studentId }
                                Surface(
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(16.dp),
                                    color = RecentItemBg,
                                    border = BorderStroke(1.dp, SoftBlueContainer)
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(12.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Box(
                                                modifier = Modifier
                                                    .size(38.dp)
                                                    .clip(RoundedCornerShape(10.dp))
                                                    .background(Color.White),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Icon(
                                                    Icons.Default.School,
                                                    contentDescription = null,
                                                    tint = NavyPrimary,
                                                    modifier = Modifier.size(20.dp)
                                                )
                                            }
                                            Spacer(modifier = Modifier.width(10.dp))
                                            Column {
                                                Text(
                                                    text = student?.student?.name ?: payment.studentId,
                                                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                                    color = MaterialTheme.colorScheme.onSurface
                                                )
                                                Text(
                                                    text = "Class ${student?.student?.className ?: ""} • ID: ${payment.studentId}",
                                                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                                )
                                            }
                                        }

                                        Column(horizontalAlignment = Alignment.End) {
                                            Text(
                                                text = "+ $currencySymbol${payment.amount.toInt()}",
                                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                                color = EmeraldPaid
                                            )
                                            Spacer(modifier = Modifier.height(2.dp))
                                            Surface(
                                                color = EmeraldPaidBg,
                                                shape = RoundedCornerShape(4.dp)
                                            ) {
                                                Text(
                                                    text = payment.mode.uppercase(),
                                                    color = EmeraldPaid,
                                                    style = MaterialTheme.typography.labelSmall.copy(
                                                        fontWeight = FontWeight.Bold,
                                                        fontSize = 9.sp
                                                    ),
                                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        // Pending Dues Banner Box (Matching Coral/Red Pending Box in HTML theme)
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 4.dp),
                            shape = RoundedCornerShape(20.dp),
                            color = AmberPendingBg,
                            border = BorderStroke(1.dp, AmberPendingBorder)
                        ) {
                            Column(
                                modifier = Modifier.padding(14.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "Pending Dues / बकाया शुल्क",
                                        style = MaterialTheme.typography.labelMedium.copy(
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 11.sp
                                        ),
                                        color = Color(0xFF410002)
                                    )
                                    Text(
                                        text = "$currencySymbol${stats.totalPendingAmount.toInt()}",
                                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                        color = AmberPending
                                    )
                                }

                                val pendingRatio = if (stats.totalFeeAmount > 0) {
                                    (stats.totalPendingAmount / stats.totalFeeAmount).toFloat().coerceIn(0f, 1f)
                                } else 0f

                                LinearProgressIndicator(
                                    progress = { pendingRatio },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(6.dp)
                                        .clip(CircleShape),
                                    color = AmberPending,
                                    trackColor = Color.White.copy(alpha = 0.6f)
                                )

                                Text(
                                    text = "${stats.pendingStudentsCount} students have pending fees balance.",
                                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                    color = Color(0xFF8C1D18)
                                )
                            }
                        }
                    }
                }
            }

            // Pending Students Shortcut List if any
            if (pendingStudents.isNotEmpty()) {
                item {
                    Text(
                        text = "Pending Fee Students / बकाया छात्र",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.padding(top = 6.dp)
                    )
                }

                items(pendingStudents.take(3)) { item ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onStudentClick(item.student.studentId) },
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = BorderStroke(1.dp, SlateBorder)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = item.student.name,
                                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                                )
                                Text(
                                    text = "Class ${item.student.className} • Batch ${item.student.batch}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    text = "Due: $currencySymbol${item.pendingFee.toInt()}",
                                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                                    color = AmberPending
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                StatusChip(status = "PENDING")
                            }
                        }
                    }
                }
            }

            item { Spacer(modifier = Modifier.height(90.dp)) }
        }

        // Floating Action Buttons (Matching Royal Blue `#005FB0` design)
        Column(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            FloatingActionButton(
                onClick = onAddStudentClick,
                containerColor = SoftBlueContainer,
                contentColor = NavyPrimary,
                modifier = Modifier.testTag("fab_add_student")
            ) {
                Icon(Icons.Default.PersonAdd, contentDescription = "Add Student")
            }

            FloatingActionButton(
                onClick = onAddPaymentClick,
                containerColor = NavyLight,
                contentColor = Color.White,
                modifier = Modifier.testTag("fab_add_payment")
            ) {
                Icon(Icons.Default.Add, contentDescription = "Record Fee Payment")
            }
        }
    }
}
