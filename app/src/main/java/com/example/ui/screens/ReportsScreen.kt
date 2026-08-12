package com.example.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.model.ClassCollection
import com.example.data.model.DashboardStats
import com.example.ui.theme.AmberPending
import com.example.ui.theme.AmberPendingBg
import com.example.ui.theme.EmeraldPaid
import com.example.ui.theme.EmeraldPaidBg
import com.example.ui.theme.NavyPrimary

@Composable
fun ReportsScreen(
    stats: DashboardStats,
    classCollections: List<ClassCollection>,
    currencySymbol: String
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item { Spacer(modifier = Modifier.height(8.dp)) }

        item {
            Text(
                text = "Reports & Analytics / संस्थान रिपोर्ट",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = NavyPrimary
            )
        }

        // Summary Card
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
                        text = "Overall Financial Summary",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    ReportStatRow("Total Enrolled Students:", "${stats.totalStudents}")
                    ReportStatRow("Total Expected Fee:", "$currencySymbol${stats.totalFeeAmount.toInt()}")
                    ReportStatRow("Total Collected Fee:", "$currencySymbol${stats.totalPaidAmount.toInt()}", color = EmeraldPaid, isBold = true)
                    ReportStatRow("Total Pending Fee:", "$currencySymbol${stats.totalPendingAmount.toInt()}", color = AmberPending, isBold = true)
                    ReportStatRow("Today's Collection:", "$currencySymbol${stats.todaysCollection.toInt()}", color = EmeraldPaid)
                    ReportStatRow("This Month's Collection:", "$currencySymbol${stats.thisMonthsCollection.toInt()}", color = EmeraldPaid)
                }
            }
        }

        // Class-wise Breakdown Header
        item {
            Text(
                text = "Class-wise Fee Recovery / कक्षा अनुसार स्थिति",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
            )
        }

        if (classCollections.isEmpty()) {
            item {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surface
                ) {
                    Text(
                        text = "No class records available.",
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(16.dp)
                    )
                }
            }
        } else {
            items(classCollections) { cls ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = androidx.compose.foundation.BorderStroke(1.dp, com.example.ui.theme.SlateBorder),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Class: ${cls.className}",
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                            )
                            Text(
                                text = "${cls.studentCount} Students",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        val progress = if (cls.totalFee > 0) (cls.paidAmount / cls.totalFee).toFloat() else 0f
                        val percent = (progress * 100).toInt()

                        LinearProgressIndicator(
                            progress = { progress },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp)
                                .clip(RoundedCornerShape(4.dp)),
                            color = EmeraldPaid,
                            trackColor = AmberPendingBg
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(text = "Total: $currencySymbol${cls.totalFee.toInt()}", style = MaterialTheme.typography.bodySmall)
                            Text(text = "Paid: $currencySymbol${cls.paidAmount.toInt()} ($percent%)", style = MaterialTheme.typography.bodySmall, color = EmeraldPaid, fontWeight = FontWeight.Bold)
                            Text(text = "Pending: $currencySymbol${cls.pendingAmount.toInt()}", style = MaterialTheme.typography.bodySmall, color = AmberPending, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        item { Spacer(modifier = Modifier.height(40.dp)) }
    }
}

@Composable
fun ReportStatRow(label: String, value: String, color: androidx.compose.ui.graphics.Color = MaterialTheme.colorScheme.onSurface, isBold: Boolean = false) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(
            text = value,
            style = if (isBold) MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold) else MaterialTheme.typography.bodyMedium,
            color = color
        )
    }
}
