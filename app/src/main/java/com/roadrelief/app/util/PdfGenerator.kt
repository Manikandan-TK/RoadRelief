
package com.roadrelief.app.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import android.net.Uri
import android.os.Build
import android.text.Layout
import android.text.StaticLayout
import android.text.TextPaint
import androidx.core.graphics.scale
import androidx.core.net.toUri
import com.roadrelief.app.data.database.entity.CaseEntity
import com.roadrelief.app.data.database.entity.EvidenceEntity
import com.roadrelief.app.data.database.entity.UserEntity
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject
import kotlin.math.min

class PdfGenerationException(message: String, cause: Throwable? = null) :
    Exception(message, cause)

class PdfGenerator @Inject constructor(
    @ApplicationContext private val context: Context
) {

    // --- Document Layout Constants ---
    private val pageWidth = 595
    private val pageHeight = 842
    private val margin = 40f
    private val contentWidth = (pageWidth - 2 * margin)
    private val imageGridWidth = (contentWidth - margin) / 2

    // --- Typography Constants ---
    private val headingSize = 14f
    private val subheadingSize = 12f
    private val bodySize = 10f
    private val metaSize = 8f
    private val lineSpacing = 1.4f

    // --- Spacing Constants ---
    private val sectionSpacing = 24f
    private val paraSpacing = 12f
    private val keyValueSpacing = 6f

    // --- Paint Objects ---
    private val headingPaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
        textSize = headingSize
        typeface = android.graphics.Typeface.create(android.graphics.Typeface.DEFAULT, android.graphics.Typeface.BOLD)
        color = Color.BLACK
    }

    private val subheadingPaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
        textSize = subheadingSize
        typeface = android.graphics.Typeface.create(android.graphics.Typeface.DEFAULT, android.graphics.Typeface.BOLD)
        color = Color.DKGRAY
    }

    private val bodyPaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
        textSize = bodySize
        typeface = android.graphics.Typeface.create(android.graphics.Typeface.DEFAULT, android.graphics.Typeface.NORMAL)
        color = Color.DKGRAY
    }

    private val bodyBoldPaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
        textSize = bodySize
        typeface = android.graphics.Typeface.create(android.graphics.Typeface.DEFAULT, android.graphics.Typeface.BOLD)
        color = Color.BLACK
    }

    private val metaPaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
        textSize = metaSize
        typeface = android.graphics.Typeface.create(android.graphics.Typeface.DEFAULT, android.graphics.Typeface.ITALIC)
        color = Color.GRAY
    }

    private var currentPage: PdfDocument.Page? = null
    private var yPos = 0f
    private lateinit var document: PdfDocument

    // --- Main Generation Function ---
    fun generatePdfReport(
        user: UserEntity?,
        case: CaseEntity,
        evidence: List<EvidenceEntity>
    ): Uri {
        document = PdfDocument()
        startNewPage()

        yPos = drawHeader()
        yPos = drawRecipientInfo(case, yPos)
        yPos = drawSubject(case, yPos)
        yPos = drawSalutation(yPos)
        yPos = drawIntroduction(yPos)
        yPos = drawSectionTitle("Incident Details", yPos)
        yPos = drawIncidentDetails(case, yPos)
        yPos = drawSectionTitle("Description of Damages", yPos)
        yPos = drawClaimDetails(case, yPos)

        if (evidence.isNotEmpty()) {
            yPos = drawSectionTitle("Supporting Evidence", yPos)
            yPos = drawEvidenceGrid(evidence, yPos)
        }
        yPos = drawCompensationClaim(case, yPos)
        yPos = drawConclusion(yPos)
        yPos = drawSignatureSection(user, yPos)
        yPos = drawDisclaimer(yPos)

        // Finalize the last page
        drawPageNumber()
        document.finishPage(currentPage)


        // --- File Saving ---
        val cacheDir = context.cacheDir
        val roadReliefDir = File(cacheDir, "RoadRelief")
        if (!roadReliefDir.exists()) {
            roadReliefDir.mkdirs()
        }
        val fileName = "RoadRelief_Report_${case.id}_${System.currentTimeMillis()}.pdf"
        val file = File(roadReliefDir, fileName)

        try {
            document.writeTo(FileOutputStream(file))
            return file.toUri()
        } catch (e: IOException) {
            throw PdfGenerationException("Failed to save the PDF file.", e)
        } finally {
            document.close()
        }
    }


    private fun startNewPage() {
        currentPage?.let {
            drawPageNumber()
            document.finishPage(it)
        }
        val pageNumber = document.pages.size + 1
        val pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNumber).create()
        currentPage = document.startPage(pageInfo)
        yPos = margin
    }

    private fun checkPageHeight(neededHeight: Float) {
        if (yPos + neededHeight > pageHeight - margin) {
            startNewPage()
        }
    }


    private fun drawHeader(): Float {
        val dateText = "Date: ${formatDate(System.currentTimeMillis(), "dd MMMM yyyy")}"
        currentPage!!.canvas.drawText(
            dateText,
            pageWidth - margin - bodyPaint.measureText(dateText),
            margin,
            bodyPaint
        )
        return margin + paraSpacing
    }

    private fun drawRecipientInfo(case: CaseEntity, y: Float): Float {
        var yPos = y
        checkPageHeight(50f)
        currentPage!!.canvas.drawText("To,", margin, yPos, bodyBoldPaint)
        yPos += bodyPaint.textSize * lineSpacing
        currentPage!!.canvas.drawText("The Concerned Authority,", margin, yPos, bodyPaint)
        yPos += bodyPaint.textSize * lineSpacing
        currentPage!!.canvas.drawText(case.authority, margin, yPos, bodyPaint)
        return yPos + sectionSpacing
    }

    private fun drawSubject(case: CaseEntity, y: Float): Float {
        val subjectText =
            "Notice of Claim for Damages due to Poor Road Conditions (Case ID: RR-${case.id})"
        checkPageHeight(50f)
        currentPage!!.canvas.drawText("Subject:", margin, y, bodyBoldPaint)
        val subjectXOffset = margin + bodyBoldPaint.measureText("Subject:  ")
        val subjectYPos = drawWrappedText(
            subjectText,
            bodyPaint,
            y,
            x = subjectXOffset,
            width = (pageWidth - subjectXOffset - margin).toInt()
        )
        return subjectYPos + sectionSpacing
    }

    private fun drawSalutation(y: Float): Float {
        val salutation = "Dear Sir/Madam,"
        checkPageHeight(30f)
        currentPage!!.canvas.drawText(salutation, margin, y, bodyBoldPaint)
        return y + bodyPaint.textSize * lineSpacing + paraSpacing
    }

    private fun drawIntroduction(y: Float): Float {
        val intro =
            "I, the undersigned, am writing to formally notify you of damages sustained to my vehicle due to the hazardous condition of a road under your jurisdiction. This notice serves as a formal claim for compensation as per the details provided below."
        val layout = createStaticLayout(intro, bodyPaint)
        checkPageHeight(layout.height.toFloat() + paraSpacing)
        val yPos = drawWrappedText(intro, bodyPaint, y)
        return yPos + sectionSpacing
    }


    private fun drawSectionTitle(title: String, y: Float): Float {
        checkPageHeight(headingSize * 2)
        currentPage!!.canvas.drawText(title, margin, y + headingSize, headingPaint)
        currentPage!!.canvas.drawLine(
            margin,
            y + headingSize + 5,
            pageWidth - margin,
            y + headingSize + 5,
            headingPaint
        )
        return y + headingSize * lineSpacing + paraSpacing
    }

    private fun drawIncidentDetails(case: CaseEntity, y: Float): Float {
        var yPos = y
        val locationString = if (case.incidentLatitude != null && case.incidentLongitude != null) {
            "Lat: ${"%.5f".format(Locale.US, case.incidentLatitude)}, Lon: ${"%.5f".format(Locale.US, case.incidentLongitude)}"
        } else {
            "Not recorded"
        }

        yPos = drawKeyValue("Date of Incident", formatDate(case.incidentDate), yPos)
        yPos = drawKeyValue("Approx. Time of Incident", formatDate(case.incidentDate, "hh:mm a"), yPos)
        yPos = drawKeyValue("Location of Incident", locationString, yPos)

        return yPos + sectionSpacing
    }

    private fun drawClaimDetails(case: CaseEntity, y: Float): Float {
        var yPos = y
        yPos = drawKeyValue("Road Condition Description", case.description, yPos)
        yPos = drawKeyValue("Vehicle Damage Description", case.vehicleDamageDescription, yPos)
        return yPos + sectionSpacing
    }


    private fun drawEvidenceGrid(evidence: List<EvidenceEntity>, y: Float): Float {
        var currentY = y
        var xPos: Float
        evidence.forEachIndexed { index, item ->
            val bitmap = loadBitmap(item.photoUri)
            if (bitmap != null) {
                val scaledBitmap = scaleBitmapForGrid(bitmap)
                val neededHeight = scaledBitmap.height + metaSize * 4

                if (currentY + neededHeight > pageHeight - margin) {
                    startNewPage()
                    currentY = drawSectionTitle("Supporting Evidence (Continued)", this.yPos)
                }
                xPos = if (index % 2 != 0) {
                    margin + imageGridWidth + margin
                } else {
                    margin
                }
                drawEvidenceItem(scaledBitmap, item, xPos, currentY, index + 1)
                bitmap.recycle()
                scaledBitmap.recycle()
                if (index % 2 != 0 || index == evidence.lastIndex) {
                    currentY += neededHeight + paraSpacing
                }
            }
        }
        return currentY + sectionSpacing
    }

    private fun drawEvidenceItem(
        bitmap: Bitmap,
        item: EvidenceEntity,
        x: Float,
        y: Float,
        index: Int
    ) {
        val canvas = currentPage!!.canvas
        var yPos = y

        canvas.drawText("Evidence #${index}", x, yPos, subheadingPaint)
        yPos += subheadingPaint.textSize * lineSpacing

        canvas.drawBitmap(bitmap, x, yPos, null)
        yPos += bitmap.height + keyValueSpacing

        val metaText = "Timestamp: ${formatDate(item.timestamp, "dd/MM/yy HH:mm")}\n" +
                "Geotag: ${"%.5f".format(Locale.US, item.latitude)}, ${"%.5f".format(Locale.US, item.longitude)}"
        drawWrappedText(metaText, metaPaint, yPos, x, width = imageGridWidth.toInt())
    }


    private fun drawCompensationClaim(case: CaseEntity, y: Float): Float {
        var yPos = y
        yPos = drawSectionTitle("Compensation Claim", yPos)
        yPos = drawKeyValue(
            "Total Compensation Requested",
            "₹ ${String.format(Locale.US, "%,.2f", case.compensation)}",
            yPos
        )
        return yPos + sectionSpacing
    }

    private fun drawConclusion(y: Float): Float {
        val text =
            "I request that you investigate this matter and process this claim for compensation at the earliest. I have attached photographic evidence of the road condition and the resulting damages for your reference. Please contact me at your earliest convenience to discuss this matter further."
        val layout = createStaticLayout(text, bodyPaint)
        checkPageHeight(layout.height.toFloat() + sectionSpacing)
        val yPos = drawWrappedText(text, bodyPaint, y)
        return yPos + sectionSpacing
    }

    private fun drawSignatureSection(user: UserEntity?, y: Float): Float {
        var yPos = y
        checkPageHeight(80f)
        yPos = drawWrappedText("Sincerely,", bodyPaint, yPos)
        yPos += 60 // Space for signature
        yPos = drawWrappedText(user?.name ?: "N/A", bodyBoldPaint, yPos)
        yPos = drawWrappedText(user?.address ?: "N/A", bodyPaint, yPos)
        yPos = drawWrappedText("Vehicle No.: ${user?.vehicleNumber ?: "N/A"}", bodyPaint, yPos)
        return yPos + sectionSpacing
    }

    private fun drawDisclaimer(y: Float): Float {
        val text =
            "Disclaimer: This document was generated by a user of the RoadRelief application. The information contained herein is based on data provided by the user and has not been independently verified. The application and its developers are not responsible for the accuracy or validity of this claim."
        val layout = createStaticLayout(text, metaPaint)
        checkPageHeight(layout.height.toFloat())
        yPos = drawWrappedText(text, metaPaint, y)
        return yPos
    }


    // --- Core Drawing Utilities ---

    private fun drawKeyValue(key: String, value: String, y: Float): Float {
        val keyWidth = 160f
        val layout = createStaticLayout(
            value,
            bodyPaint,
            (contentWidth - keyWidth - keyValueSpacing).toInt()
        )
        checkPageHeight(layout.height.toFloat() + keyValueSpacing)

        currentPage!!.canvas.save()
        currentPage!!.canvas.translate(margin, y)

        val keyLayout = createStaticLayout(key, bodyBoldPaint, keyWidth.toInt())
        keyLayout.draw(currentPage!!.canvas)

        currentPage!!.canvas.translate(keyWidth + keyValueSpacing, 0f)
        layout.draw(currentPage!!.canvas)
        currentPage!!.canvas.restore()
        return y + layout.height + paraSpacing
    }

    private fun drawWrappedText(
        text: String,
        paint: TextPaint,
        y: Float,
        x: Float = margin,
        width: Int = (pageWidth - margin - x).toInt()
    ): Float {
        val layout = createStaticLayout(text, paint, width)
        checkPageHeight(layout.height.toFloat())
        currentPage!!.canvas.save()
        currentPage!!.canvas.translate(x, y)
        layout.draw(currentPage!!.canvas)
        currentPage!!.canvas.restore()
        return y + layout.height
    }


    private fun createStaticLayout(text: String, paint: TextPaint, width: Int = contentWidth.toInt()): StaticLayout {
        return StaticLayout.Builder.obtain(text, 0, text.length, paint, width)
                .setAlignment(Layout.Alignment.ALIGN_NORMAL)
                .setLineSpacing(0f, lineSpacing)
                .setIncludePad(false)
                .build()
    }

    private fun drawPageNumber() {
        currentPage?.let {
            val pageNum = it.info.pageNumber
            val text = "Page $pageNum"
            val x = pageWidth - margin - metaPaint.measureText(text)
            val y = pageHeight - margin / 2
            it.canvas.drawText(text, x, y, metaPaint)
        }
    }

    // --- Bitmap & Formatting Utilities ---

    private fun loadBitmap(uriString: String): Bitmap? {
        return try {
            val imageUri = uriString.toUri()
            context.contentResolver.openInputStream(imageUri)?.use { inputStream ->
                BitmapFactory.decodeStream(inputStream)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }


    private fun scaleBitmapForGrid(bitmap: Bitmap): Bitmap {
        val aspectRatio = bitmap.width.toFloat() / bitmap.height.toFloat()
        val scaledWidth = min(imageGridWidth, bitmap.width.toFloat())
        val scaledHeight = scaledWidth / aspectRatio
        return bitmap.scale(scaledWidth.toInt(), scaledHeight.toInt())
    }

    private fun formatDate(timestamp: Long, format: String = "dd MMMM yyyy"): String {
        return SimpleDateFormat(format, Locale.US).format(Date(timestamp))
    }
}
