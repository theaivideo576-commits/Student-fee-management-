package com.example.data.remote

import com.example.data.local.PaymentEntity
import com.example.data.local.StudentEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

sealed class SyncResult {
    data class Success(val message: String, val pulledStudents: List<StudentEntity> = emptyList(), val pulledPayments: List<PaymentEntity> = emptyList()) : SyncResult()
    data class Error(val message: String) : SyncResult()
}

class GoogleSheetsSyncManager {
    private val client = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .followRedirects(true)
        .followSslRedirects(true)
        .build()

    suspend fun testConnection(scriptUrl: String): SyncResult = withContext(Dispatchers.IO) {
        if (scriptUrl.isBlank()) {
            return@withContext SyncResult.Error("Google Sheets Script URL empty / स्क्रिप्ट URL खाली है")
        }
        try {
            val url = if (scriptUrl.contains("?")) "$scriptUrl&action=ping" else "$scriptUrl?action=ping"
            val request = Request.Builder().url(url).build()
            client.newCall(request).execute().use { response ->
                if (response.isSuccessful) {
                    val body = response.body?.string() ?: ""
                    if (body.contains("pong") || response.code == 200) {
                        SyncResult.Success("Google Sheet Connection Successful / कनेक्शन सफल रहा!")
                    } else {
                        SyncResult.Success("Connected! Response: ${body.take(50)}")
                    }
                } else {
                    SyncResult.Error("Server error code: ${response.code}")
                }
            }
        } catch (e: Exception) {
            SyncResult.Error("Connection failed: ${e.localizedMessage ?: e.message}")
        }
    }

    suspend fun pullFromSheet(scriptUrl: String): SyncResult = withContext(Dispatchers.IO) {
        if (scriptUrl.isBlank()) {
            return@withContext SyncResult.Error("Please set Google Sheets Web App URL in Settings / कृपया सेटिंग्स में URL डालें")
        }
        try {
            val url = if (scriptUrl.contains("?")) "$scriptUrl&action=pullAll" else "$scriptUrl?action=pullAll"
            val request = Request.Builder().url(url).build()
            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    return@withContext SyncResult.Error("HTTP Error ${response.code}")
                }
                val bodyStr = response.body?.string() ?: ""
                val json = JSONObject(bodyStr)
                if (json.optString("status") == "error") {
                    return@withContext SyncResult.Error(json.optString("message", "Sheet Error"))
                }

                val studentsArr = json.optJSONArray("students") ?: JSONArray()
                val paymentsArr = json.optJSONArray("payments") ?: JSONArray()

                val studentList = mutableListOf<StudentEntity>()
                for (i in 0 until studentsArr.length()) {
                    val obj = studentsArr.getJSONObject(i)
                    studentList.add(
                        StudentEntity(
                            studentId = obj.optString("StudentID", obj.optString("studentId", "")),
                            name = obj.optString("Name", obj.optString("name", "")),
                            fatherName = obj.optString("FatherName", obj.optString("fatherName", "")),
                            className = obj.optString("Class", obj.optString("className", "")),
                            batch = obj.optString("Batch", obj.optString("batch", "")),
                            mobile = obj.optString("Mobile", obj.optString("mobile", "")),
                            totalFee = obj.optDouble("TotalFee", obj.optDouble("totalFee", 0.0)),
                            admissionDate = obj.optString("AdmissionDate", obj.optString("admissionDate", ""))
                        )
                    )
                }

                val paymentList = mutableListOf<PaymentEntity>()
                for (i in 0 until paymentsArr.length()) {
                    val obj = paymentsArr.getJSONObject(i)
                    paymentList.add(
                        PaymentEntity(
                            paymentId = obj.optString("PaymentID", obj.optString("paymentId", "")),
                            studentId = obj.optString("StudentID", obj.optString("studentId", "")),
                            paymentDate = obj.optString("PaymentDate", obj.optString("paymentDate", "")),
                            amount = obj.optDouble("Amount", obj.optDouble("amount", 0.0)),
                            mode = obj.optString("Mode", obj.optString("mode", "Cash")),
                            receiptNo = obj.optString("ReceiptNo", obj.optString("receiptNo", "")),
                            remark = obj.optString("Remark", obj.optString("remark", ""))
                        )
                    )
                }

                SyncResult.Success(
                    message = "Pulled ${studentList.size} students & ${paymentList.size} payments from Google Sheet!",
                    pulledStudents = studentList,
                    pulledPayments = paymentList
                )
            }
        } catch (e: Exception) {
            SyncResult.Error("Sync pull failed: ${e.localizedMessage ?: e.message}")
        }
    }

    suspend fun pushToSheet(
        scriptUrl: String,
        students: List<StudentEntity>,
        payments: List<PaymentEntity>
    ): SyncResult = withContext(Dispatchers.IO) {
        if (scriptUrl.isBlank()) {
            return@withContext SyncResult.Error("Please set Google Sheets Web App URL in Settings")
        }
        try {
            val rootObj = JSONObject()
            rootObj.put("action", "pushAll")

            val studentsArr = JSONArray()
            students.forEach { s ->
                val sObj = JSONObject()
                sObj.put("StudentID", s.studentId)
                sObj.put("Name", s.name)
                sObj.put("FatherName", s.fatherName)
                sObj.put("Class", s.className)
                sObj.put("Batch", s.batch)
                sObj.put("Mobile", s.mobile)
                sObj.put("TotalFee", s.totalFee)
                sObj.put("AdmissionDate", s.admissionDate)
                studentsArr.put(sObj)
            }
            rootObj.put("students", studentsArr)

            val paymentsArr = JSONArray()
            payments.forEach { p ->
                val pObj = JSONObject()
                pObj.put("PaymentID", p.paymentId)
                pObj.put("StudentID", p.studentId)
                pObj.put("PaymentDate", p.paymentDate)
                pObj.put("Amount", p.amount)
                pObj.put("Mode", p.mode)
                pObj.put("ReceiptNo", p.receiptNo)
                pObj.put("Remark", p.remark)
                paymentsArr.put(pObj)
            }
            rootObj.put("payments", paymentsArr)

            val mediaType = "application/json; charset=utf-8".toMediaType()
            val requestBody = rootObj.toString().toRequestBody(mediaType)
            val request = Request.Builder()
                .url(scriptUrl)
                .post(requestBody)
                .build()

            client.newCall(request).execute().use { response ->
                if (response.isSuccessful) {
                    SyncResult.Success("Successfully pushed ${students.size} students & ${payments.size} payments to Google Sheet!")
                } else {
                    SyncResult.Error("Push HTTP Error ${response.code}")
                }
            }
        } catch (e: Exception) {
            SyncResult.Error("Push failed: ${e.localizedMessage ?: e.message}")
        }
    }
}
