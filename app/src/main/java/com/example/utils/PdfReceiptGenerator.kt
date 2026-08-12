package com.example.utils

import android.content.Context
import android.content.Intent
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import android.net.Uri
import androidx.core.content.FileProvider
import com.example.ui.viewmodel.ReceiptData
import java.io.File
import java.io.FileOutputStream

object PdfReceiptGenerator {

    fun generatePdfFile(context: Context, receipt: ReceiptData): File? {
        try {
            val pdfDocument = PdfDocument()
            val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create() // A4 page
            val page = pdfDocument.startPage(pageInfo)
            val canvas: Canvas = page.canvas

            val paint = Paint()
            val titlePaint = Paint()

            // Header Background
            paint.color = Color.parseColor("#1E3A8A") // Slate Navy
            canvas.drawRect(0f, 0f, 595f, 130f, paint)

            // Header Text
            titlePaint.color = Color.WHITE
            titlePaint.textSize = 24f
            titlePaint.isFakeBoldText = true
            titlePaint.textAlign = Paint.Align.CENTER
            canvas.drawText(receipt.instituteName, 595f / 2, 45f, titlePaint)

            titlePaint.textSize = 12f
            titlePaint.isFakeBoldText = false
            canvas.drawText(receipt.instituteAddress, 595f / 2, 70f, titlePaint)
            canvas.drawText("Phone: ${receipt.phone}", 595f / 2, 90f, titlePaint)

            // Receipt Title
            titlePaint.color = Color.parseColor("#1E3A8A")
            titlePaint.textSize = 18f
            titlePaint.isFakeBoldText = true
            canvas.drawText("FEE PAYMENT RECEIPT / फीस रसीद", 595f / 2, 165f, titlePaint)

            // Divider
            paint.color = Color.parseColor("#CBD5E1")
            paint.strokeWidth = 2f
            canvas.drawLine(40f, 180f, 555f, 180f, paint)

            // Receipt Details Table Grid
            var y = 210f
            val labelX = 50f
            val valueX = 220f
            val rightLabelX = 330f
            val rightValueX = 460f

            val bodyPaint = Paint().apply {
                textSize = 13f
                color = Color.parseColor("#1E293B")
            }
            val boldBodyPaint = Paint().apply {
                textSize = 13f
                color = Color.parseColor("#0F172A")
                typeface = Typeface.DEFAULT_BOLD
            }

            // Row 1: Receipt No & Date
            canvas.drawText("Receipt No:", labelX, y, bodyPaint)
            canvas.drawText(receipt.receiptNumber, valueX, y, boldBodyPaint)
            canvas.drawText("Date:", rightLabelX, y, bodyPaint)
            canvas.drawText(receipt.paymentDate, rightValueX, y, boldBodyPaint)

            y += 30f
            // Row 2: Student ID & Name
            canvas.drawText("Student ID:", labelX, y, bodyPaint)
            canvas.drawText(receipt.studentId, valueX, y, boldBodyPaint)
            canvas.drawText("Student Name:", rightLabelX, y, bodyPaint)
            canvas.drawText(receipt.studentName, rightValueX, y, boldBodyPaint)

            y += 30f
            // Row 3: Class & Batch
            canvas.drawText("Class:", labelX, y, bodyPaint)
            canvas.drawText(receipt.className, valueX, y, boldBodyPaint)
            canvas.drawText("Batch:", rightLabelX, y, bodyPaint)
            canvas.drawText(receipt.batch, rightValueX, y, boldBodyPaint)

            y += 30f
            // Row 4: Mode & Remark
            canvas.drawText("Payment Mode:", labelX, y, bodyPaint)
            canvas.drawText(receipt.paymentMode, valueX, y, boldBodyPaint)
            canvas.drawText("Remark:", rightLabelX, y, bodyPaint)
            canvas.drawText(receipt.remark.ifBlank { "N/A" }, rightValueX, y, bodyPaint)

            y += 40f
            // Highlight Box for Amounts
            paint.color = Color.parseColor("#F1F5F9")
            canvas.drawRoundRect(40f, y, 555f, y + 150f, 12f, 12f, paint)

            val boxY = y + 35f
            val greenPaint = Paint().apply {
                textSize = 16f
                color = Color.parseColor("#059669")
                typeface = Typeface.DEFAULT_BOLD
            }
            val redPaint = Paint().apply {
                textSize = 15f
                color = Color.parseColor("#DC2626")
                typeface = Typeface.DEFAULT_BOLD
            }

            canvas.drawText("Amount Paid (भुगतान राशि):", 60f, boxY, boldBodyPaint)
            canvas.drawText("${receipt.currency}${receipt.amountPaid}", 350f, boxY, greenPaint)

            canvas.drawText("Total Course Fee (कुल फीस):", 60f, boxY + 35f, bodyPaint)
            canvas.drawText("${receipt.currency}${receipt.totalFee}", 350f, boxY + 35f, boldBodyPaint)

            canvas.drawText("Total Paid Fee (कुल जमा):", 60f, boxY + 65f, bodyPaint)
            canvas.drawText("${receipt.currency}${receipt.totalPaid}", 350f, boxY + 65f, boldBodyPaint)

            canvas.drawText("Remaining Fee (बकाया राशि):", 60f, boxY + 95f, bodyPaint)
            canvas.drawText("${receipt.currency}${receipt.remainingFee}", 350f, boxY + 95f, if (receipt.remainingFee > 0) redPaint else greenPaint)

            // Footer
            val footerY = y + 230f
            paint.color = Color.parseColor("#CBD5E1")
            canvas.drawLine(40f, footerY - 20f, 555f, footerY - 20f, paint)

            val footerPaint = Paint().apply {
                textSize = 11f
                color = Color.parseColor("#64748B")
                textAlign = Paint.Align.CENTER
            }
            canvas.drawText("Thank you for your payment! This is a computer generated fee receipt.", 595f / 2, footerY, footerPaint)
            canvas.drawText("Authorized Signatory: ${receipt.instituteName}", 595f / 2, footerY + 20f, footerPaint)

            pdfDocument.finishPage(page)

            val file = File(context.cacheDir, "Receipt_${receipt.receiptNumber}.pdf")
            val outputStream = FileOutputStream(file)
            pdfDocument.writeTo(outputStream)
            pdfDocument.close()
            outputStream.close()
            return file
        } catch (e: Exception) {
            e.printStackTrace()
            return null
        }
    }

    fun shareReceiptText(context: Context, receipt: ReceiptData) {
        val text = """
            🏫 *${receipt.instituteName}*
            📍 ${receipt.instituteAddress} | 📞 ${receipt.phone}
            ------------------------------------
            🧾 *FEE RECEIPT / फीस रसीद*
            
            *Receipt No:* ${receipt.receiptNumber}
            *Date:* ${receipt.paymentDate}
            *Student ID:* ${receipt.studentId}
            *Student Name:* ${receipt.studentName}
            *Class:* ${receipt.className} (${receipt.batch})
            
            ------------------------------------
            💵 *AMOUNT PAID:* ${receipt.currency}${receipt.amountPaid}
            💳 *Mode:* ${receipt.paymentMode}
            ------------------------------------
            📊 *Total Fee:* ${receipt.currency}${receipt.totalFee}
            ✅ *Total Paid:* ${receipt.currency}${receipt.totalPaid}
            ⚠️ *Remaining Fee:* ${receipt.currency}${receipt.remainingFee}
            
            _Thank you for your payment!_
        """.trimIndent()

        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_SUBJECT, "Fee Receipt - ${receipt.studentName}")
            putExtra(Intent.EXTRA_TEXT, text)
        }
        context.startActivity(Intent.createChooser(intent, "Share Fee Receipt / रसीद शेयर करें"))
    }

    fun sharePdfFile(context: Context, receipt: ReceiptData) {
        val pdfFile = generatePdfFile(context, receipt) ?: run {
            shareReceiptText(context, receipt)
            return
        }
        try {
            val uri: Uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                pdfFile
            )
            val intent = Intent(Intent.ACTION_SEND).apply {
                type = "application/pdf"
                putExtra(Intent.EXTRA_STREAM, uri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            context.startActivity(Intent.createChooser(intent, "Share Receipt PDF"))
        } catch (e: Exception) {
            shareReceiptText(context, receipt)
        }
    }
}
