package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.EmeraldPaid
import com.example.ui.theme.NavyPrimary

@Composable
fun SettingsScreen(
    currentName: String,
    currentAddress: String,
    currentPhone: String,
    currentYear: String,
    currentCurrency: String,
    currentAdminPin: String,
    currentScriptUrl: String,
    isSyncing: Boolean,
    onSaveSettings: (
        name: String,
        address: String,
        phone: String,
        year: String,
        currency: String,
        pin: String,
        scriptUrl: String
    ) -> Unit,
    onTestConnection: () -> Unit,
    onSyncPush: () -> Unit,
    onSyncPull: () -> Unit,
    onLogout: () -> Unit
) {
    var name by remember { mutableStateOf(currentName) }
    var address by remember { mutableStateOf(currentAddress) }
    var phone by remember { mutableStateOf(currentPhone) }
    var year by remember { mutableStateOf(currentYear) }
    var currency by remember { mutableStateOf(currentCurrency) }
    var pin by remember { mutableStateOf(currentAdminPin) }
    var scriptUrl by remember { mutableStateOf(currentScriptUrl) }

    var showGuide by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item { Spacer(modifier = Modifier.height(8.dp)) }

        item {
            Text(
                text = "Settings / सेटिंग्स",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = NavyPrimary
            )
        }

        // Institute Profile Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = androidx.compose.foundation.BorderStroke(1.dp, com.example.ui.theme.SlateBorder),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.School, contentDescription = null, tint = NavyPrimary)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Institute Profile / संस्थान विवरण",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("Institute Name (संस्थान का नाम)") },
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier.fillMaxWidth().testTag("setting_institute_name"),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = address,
                        onValueChange = { address = it },
                        label = { Text("Address (पता)") },
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier.fillMaxWidth().testTag("setting_address"),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = phone,
                        onValueChange = { phone = it },
                        label = { Text("Phone Number (फ़ोन नंबर)") },
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier.fillMaxWidth().testTag("setting_phone"),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = year,
                            onValueChange = { year = it },
                            label = { Text("Academic Year") },
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier.weight(1f).testTag("setting_academic_year"),
                            singleLine = true
                        )

                        OutlinedTextField(
                            value = currency,
                            onValueChange = { currency = it },
                            label = { Text("Currency") },
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier.weight(1f).testTag("setting_currency"),
                            singleLine = true
                        )
                    }
                }
            }
        }

        // Admin Security PIN Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = androidx.compose.foundation.BorderStroke(1.dp, com.example.ui.theme.SlateBorder),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Lock, contentDescription = null, tint = NavyPrimary)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Admin Security PIN / एडमिन पिन बदलें",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = pin,
                        onValueChange = { pin = it },
                        label = { Text("Admin PIN") },
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier.fillMaxWidth().testTag("setting_admin_pin"),
                        singleLine = true
                    )
                }
            }
        }

        // Google Sheets Integration Panel
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
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Sync, contentDescription = null, tint = NavyPrimary)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Google Sheets Database / गूगल शीट सिंक",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                            )
                        }

                        if (isSyncing) {
                            CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = scriptUrl,
                        onValueChange = { scriptUrl = it },
                        label = { Text("Google Apps Script Web App URL") },
                        placeholder = { Text("https://script.google.com/macros/s/.../exec") },
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier.fillMaxWidth().testTag("setting_script_url"),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Sync Action Buttons
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = onTestConnection,
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(10.dp),
                            enabled = !isSyncing
                        ) {
                            Text("Test / जांचें", style = MaterialTheme.typography.labelSmall)
                        }

                        Button(
                            onClick = onSyncPush,
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = EmeraldPaid),
                            enabled = !isSyncing
                        ) {
                            Icon(Icons.Default.CloudUpload, contentDescription = null, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Push Data", style = MaterialTheme.typography.labelSmall)
                        }

                        Button(
                            onClick = onSyncPull,
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = com.example.ui.theme.NavyLight),
                            enabled = !isSyncing
                        ) {
                            Icon(Icons.Default.CloudDownload, contentDescription = null, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Pull Data", style = MaterialTheme.typography.labelSmall)
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Toggle Setup Instructions
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { showGuide = !showGuide }
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.HelpOutline, contentDescription = null, tint = NavyPrimary, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (showGuide) "Hide Google Sheets Setup Guide" else "How to setup Google Sheets? (गाइड देखें)",
                            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                            color = NavyPrimary,
                            modifier = Modifier.weight(1f)
                        )
                        Icon(if (showGuide) Icons.Default.ExpandLess else Icons.Default.ExpandMore, contentDescription = null)
                    }

                    AnimatedVisibility(visible = showGuide) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 8.dp)
                                .background(MaterialTheme.colorScheme.surfaceVariant, shape = RoundedCornerShape(10.dp))
                                .padding(12.dp)
                        ) {
                            Text("Google Sheets Setup Steps:", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodySmall)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("1. Create a Google Sheet with 3 Tabs:\n   - 'Students' (StudentID, Name, FatherName, Class, Batch, Mobile, TotalFee, AdmissionDate)\n   - 'Payments' (PaymentID, StudentID, PaymentDate, Amount, Mode, ReceiptNo, Remark)\n   - 'Settings' (Setting, Value)", style = MaterialTheme.typography.labelSmall)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("2. Click Extensions -> Apps Script.", style = MaterialTheme.typography.labelSmall)
                            Text("3. Paste the Apps Script handler code (provided in the final output summary) and click Deploy -> New Deployment.", style = MaterialTheme.typography.labelSmall)
                            Text("4. Set 'Who has access' to 'Anyone'.", style = MaterialTheme.typography.labelSmall)
                            Text("5. Copy Web App URL into the field above and click 'Test'.", style = MaterialTheme.typography.labelSmall)
                        }
                    }
                }
            }
        }

        // Save Button
        item {
            Button(
                onClick = {
                    onSaveSettings(name, address, phone, year, currency, pin, scriptUrl)
                },
                modifier = Modifier.fillMaxWidth().height(52.dp).testTag("save_settings_button"),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = com.example.ui.theme.NavyLight)
            ) {
                Icon(Icons.Default.Save, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Save All Settings / सहेजें", fontWeight = FontWeight.Bold)
            }
        }

        // Logout Admin
        item {
            OutlinedButton(
                onClick = onLogout,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error)
            ) {
                Icon(Icons.Default.Logout, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Lock Dashboard / लॉगआउट करें")
            }
        }

        item { Spacer(modifier = Modifier.height(40.dp)) }
    }
}
