package com.roadrelief.app.util

import android.content.Context
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import android.net.Uri
import android.os.Environment
import com.roadrelief.app.data.database.entity.CaseEntity
import com.roadrelief.app.data.database.entity.EvidenceEntity
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject

class PdfGenerator @Inject constructor(
    @ApplicationContext private val context: Context
) {

    fun generatePdfReport(case: CaseEntity, evidence: List<EvidenceEntity>): Uri? {
        val document = PdfDocument()
        var pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create() // A4 size
        var page = document.startPage(pageInfo)
        var canvas: Canvas = page.canvas
        val paint = Paint()

        var y = 50f
        val x = 50f
        val lineHeight = 20f

        paint.textSize = 24f
        canvas.drawText("RoadRelief Incident Report", x, y, paint)
        y += 30f

        paint.textSize = 12f
        canvas.drawText("Case ID: ${case.id}", x, y, paint)
        y += lineHeight
        canvas.drawText("Incident Date: ${SimpleDateFormat("dd/MM/yyyy", Locale.US).format(Date(case.incidentDate))}", x, y, paint)
        y += lineHeight
        canvas.drawText("Authority: ${case.authority}", x, y, paint)
        y += lineHeight
        canvas.drawText("Description: ${case.description}", x, y, paint)
        y += lineHeight
        canvas.drawText("Compensation: ${case.compensation}", x, y, paint)
        y += lineHeight
        canvas.drawText("Status: ${case.status}", x, y, paint)
        y += 30f

        if (evidence.isNotEmpty()) {
            canvas.drawText("Evidence Photos:", x, y, paint)
            y += lineHeight

            evidence.forEach { evidenceItem ->
                try {
                    val imageUri = Uri.parse(evidenceItem.photoUri)
                    context.contentResolver.openInputStream(imageUri)?.use { inputStream ->
                        val bitmap = BitmapFactory.decodeStream(inputStream)

                        if (bitmap != null) {
                            val aspectRatio = bitmap.width.toFloat() / bitmap.height.toFloat()
                            val imgWidth = 200f
                            val imgHeight = imgWidth / aspectRatio

                            if (y + imgHeight > pageInfo.pageHeight - 50) { // Check if new page is needed
                                document.finishPage(page)
                                pageInfo = PdfDocument.PageInfo.Builder(595, 842, document.pages.size + 1).create()
                                page = document.startPage(pageInfo)
                                canvas = page.canvas
                                y = 50f // Reset y for new page
                            }

                            canvas.drawBitmap(bitmap, x, y, paint)
                            y += imgHeight + 10f
                            canvas.drawText("Lat: ${evidenceItem.latitude}, Lon: ${evidenceItem.longitude}", x, y, paint)
                            y += lineHeight
                            canvas.drawText("Timestamp: ${SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.US).format(Date(evidenceItem.timestamp))}", x, y, paint)
                            y += 20f
                        }
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                    // Handle error loading image
                }
            }
        }

        document.finishPage(page)

        val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
        val roadReliefDir = File(downloadsDir, "RoadRelief")
        if (!roadReliefDir.exists()) {
            roadReliefDir.mkdirs()
        }

        val fileName = "RoadRelief_Report_${SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())}.pdf"
        val file = File(roadReliefDir, fileName)

        return try {
            document.writeTo(FileOutputStream(file))
            Uri.fromFile(file)
        } catch (e: IOException) {
            e.printStackTrace()
            null
        } finally {
            document.close()
        }
    }
}