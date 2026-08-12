package com.example.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
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
import com.example.data.local.StudentEntity
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun AddEditStudentDialog(
    studentToEdit: StudentEntity? = null,
    onDismiss: () -> Unit,
    onSave: (
        studentId: String,
        name: String,
        fatherName: String,
        className: String,
        batch: String,
        mobile: String,
        totalFee: Double,
        admissionDate: String
    ) -> Unit
) {
    val isEdit = studentToEdit != null
    val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
    val autoId = "STU-" + System.currentTimeMillis().toString().takeLast(4)

    var studentId by remember { mutableStateOf(studentToEdit?.studentId ?: autoId) }
    var name by remember { mutableStateOf(studentToEdit?.name ?: "") }
    var fatherName by remember { mutableStateOf(studentToEdit?.fatherName ?: "") }
    var className by remember { mutableStateOf(studentToEdit?.className ?: "Class 10") }
    var batch by remember { mutableStateOf(studentToEdit?.batch ?: "Morning A") }
    var mobile by remember { mutableStateOf(studentToEdit?.mobile ?: "") }
    var totalFeeStr by remember { mutableStateOf(studentToEdit?.totalFee?.toString() ?: "15000") }
    var admissionDate by remember { mutableStateOf(studentToEdit?.admissionDate ?: today) }

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
                    text = if (isEdit) "Edit Student / विद्यार्थी संपादित करें" else "Add New Student / नया विद्यार्थी जोड़ें",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = com.example.ui.theme.NavyPrimary
                )

                Spacer(modifier = Modifier.height(16.dp))

                if (errorMessage != null) {
                    Text(
                        text = errorMessage!!,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                }

                // Student ID
                OutlinedTextField(
                    value = studentId,
                    onValueChange = { studentId = it },
                    label = { Text("Student ID (विद्यार्थी ID)") },
                    enabled = !isEdit,
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth().testTag("student_id_input"),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Name
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Student Name (विद्यार्थी का नाम)*") },
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth().testTag("student_name_input"),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Father Name
                OutlinedTextField(
                    value = fatherName,
                    onValueChange = { fatherName = it },
                    label = { Text("Father's Name (पिता का नाम)") },
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth().testTag("father_name_input"),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Class & Batch
                OutlinedTextField(
                    value = className,
                    onValueChange = { className = it },
                    label = { Text("Class (कक्षा, e.g. 10th, 12th)*") },
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth().testTag("class_input"),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = batch,
                    onValueChange = { batch = it },
                    label = { Text("Batch (बैच, e.g. Morning A, Evening B)") },
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth().testTag("batch_input"),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Mobile
                OutlinedTextField(
                    value = mobile,
                    onValueChange = { mobile = it },
                    label = { Text("Mobile Number (मोबाइल नंबर)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth().testTag("mobile_input"),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Total Fee
                OutlinedTextField(
                    value = totalFeeStr,
                    onValueChange = { totalFeeStr = it },
                    label = { Text("Total Fee (कुल फीस ₹)*") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth().testTag("total_fee_input"),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Admission Date
                OutlinedTextField(
                    value = admissionDate,
                    onValueChange = { admissionDate = it },
                    label = { Text("Admission Date (प्रवेश तिथि yyyy-MM-dd)") },
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth().testTag("admission_date_input"),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(20.dp))

                // Action Buttons
                Button(
                    onClick = {
                        if (studentId.isBlank() || name.isBlank() || className.isBlank()) {
                            errorMessage = "Please fill mandatory fields / आवश्यक फ़ील्ड भरें"
                            return@Button
                        }
                        val feeVal = totalFeeStr.toDoubleOrNull()
                        if (feeVal == null || feeVal < 0) {
                            errorMessage = "Please enter valid Total Fee / सही फीस दर्ज करें"
                            return@Button
                        }

                        onSave(
                            studentId,
                            name,
                            fatherName,
                            className,
                            batch,
                            mobile,
                            feeVal,
                            admissionDate
                        )
                        onDismiss()
                    },
                    modifier = Modifier.fillMaxWidth().height(48.dp).testTag("save_student_button"),
                    shape = RoundedCornerShape(14.dp),
                    colors = androidx.compose.material3.ButtonDefaults.buttonColors(containerColor = com.example.ui.theme.NavyLight)
                ) {
                    Text(if (isEdit) "Update Student / अपडेट करें" else "Save Student / सहेजें", fontWeight = FontWeight.Bold)
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
