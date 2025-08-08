import com.lowagie.text.*
import com.lowagie.text.List
import com.lowagie.text.pdf.*
import com.lowagie.text.pdf.draw.LineSeparator
import models.Report
import java.awt.Color
import java.io.FileOutputStream
import java.nio.file.Path
import java.nio.file.Paths
import java.time.LocalDate

fun main() {
    val mockReport = Report(
        reportId = 1,
        creatorId = 101,
        executionOrder = 12345,
        requestDate = LocalDate.of(2025, 7, 15),
        workStartDate = LocalDate.of(2025, 8, 5),
        workFinishDate = LocalDate.of(2025, 8, 8),
        pumpOwnerId = 201,
        operators = listOf("John Doe", "Jane Smith"),
        type = Report.OperationType.ASSEMBLY,
        depth = 120L,
        staticLevel = 30L,
        dynamicLevel = 45L,
        pumpShimming = 110L,
        speed = 50.5f,
        engine = "Moteur submersible Schneider Electric 200kW",
        pump = "Pompe submersible Grundfos SP 95-9 (95 m³/h)",
        elements = "• 120m de tuyau PVC PN16 DN150\n• Câble électrique submersible 4x35mm²\n• Clapet anti-retour DN150\n• Collier de serrage en inox",
        notes = "This is a sample note about the work performed and observations made during the operation. The pump was successfully installed and tested.",
        purchaseRequest = "DA-67890",
        quotation = "Q-112233",
        purchaseOrder = "BC-445566",
        invoice = "INV-778899",
        invoiceDate = LocalDate.of(2025, 8, 10)
    )

    val outputPath: Path = Paths.get("debug_v2.pdf")

    try {
        generateReport(report = mockReport, outputPath = outputPath.toString())
        println("Successfully generated PDF at: ${outputPath.toAbsolutePath()}")
    } catch (e: Exception) {
        println("Failed to generate PDF: ${e.message}")
        e.printStackTrace()
    }
}

fun generateReport(report: Report, outputPath: String) {
    FontFactory.registerDirectories()

    val document = Document(PageSize.A4)
    val writer = PdfWriter.getInstance(document, FileOutputStream(outputPath))

    writer.pageEvent = HeaderFooterPageEvent()

    document.open()

    // Add a modern, stylized title page
    addTitleAndHeaderInfo(document, report, creatorName = "Ahmed El Idrissi")
    addGeneralInfoSection(
        document,
        report,
        clientName = "Mohammed",
        farmName = "Ferme Al Amal",
        pumpName = "Pompe AIT MELLOUL"
    )
    addFinancialSection(document, report)
    addTechnicalSection(document, report)
    addNotesSection(document, report)

    document.close()
    println("PDF generated successfully at: $outputPath")
}

// Define custom fonts and colors
val robotoBold = FontFactory.getFont("Roboto-Bold", BaseFont.IDENTITY_H, true, 18f)
val robotoRegular = FontFactory.getFont("Roboto-Regular", BaseFont.IDENTITY_H, true, 12f)
val robotoLight = FontFactory.getFont("Roboto-Light", BaseFont.IDENTITY_H, true, 10f)

val accentColor = Color(0, 102, 204) // A nice, professional blue

private fun createLabeledCell(label: String, value: String): PdfPCell {
    val cell = PdfPCell().apply {
        border = Rectangle.NO_BORDER
        setPadding(4f) // Reduced padding for compactness
    }
    val phrase = Phrase().apply {
        add(Chunk("$label : ", robotoRegular.apply { color = Color.DARK_GRAY }))
        add(Chunk(value, robotoLight))
    }
    cell.addElement(phrase)
    return cell
}

// Header and Footer using PdfPageEventHelper
class HeaderFooterPageEvent : PdfPageEventHelper() {
    override fun onEndPage(writer: PdfWriter, document: Document) {
        val contentByte = writer.directContent

        val footerTable = PdfPTable(1).apply {
            widthPercentage = 100f // We'll let the table span the full width to keep it simple
            defaultCell.border = Rectangle.NO_BORDER
            defaultCell.setPadding(5f)
            defaultCell.verticalAlignment = Element.ALIGN_MIDDLE
            defaultCell.horizontalAlignment = Element.ALIGN_CENTER
        }

        /*
        val firstLineParagraph = Paragraph(
            "355, Av. Al Mouquaouama, Bloc B, 80150, AIT MELLOUL | Tél: 05.28.24.65.64 - Fax: 05.28.24.65.50 - Email: magrinov@gmail.com",
            robotoLight.apply { size = 8f; color = Color.BLACK }
        ).apply {
            setAlignment(Element.ALIGN_CENTER)
            setSpacingAfter(2f)
        }
         */

        val firstLineParagraph = Paragraph(
            "355, Av. Al Mouquaouama, Bloc B, 80150, AIT MELLOUL | Tél: 05.28.24.65.64 - Fax: 05.28.24.65.50",
            robotoLight.apply { size = 9f; color = Color.BLACK }
        ).apply {
            setAlignment(Element.ALIGN_CENTER)
            setSpacingAfter(2f)
        }

        /*
        val secondLineParagraph = Paragraph(
            "C.N.S.S: 8907670 - R.C: 8089 Inzegane - I.F: 40435556 - Patente: 49810052 - ICE: 001446940000049",
            robotoLight.apply { size = 8f; color = Color.BLACK }
        ).apply {
            setAlignment(Element.ALIGN_CENTER)
        }
         */

        val footerCell = PdfPCell().apply {
            border = Rectangle.NO_BORDER
            horizontalAlignment = Element.ALIGN_CENTER
            verticalAlignment = Element.ALIGN_MIDDLE
            addElement(firstLineParagraph)
            //addElement(secondLineParagraph)
        }

        footerTable.addCell(footerCell)

        footerTable.setTotalWidth(document.right() - document.left())

        val footerYPosition = document.bottomMargin() + 10f

        footerTable.writeSelectedRows(
            0, -1,
            document.leftMargin(),
            footerYPosition,
            contentByte
        )
    }
}

// Title Page
private fun addTitleAndHeaderInfo(document: Document, report: Report, creatorName: String) {
    val titleTable = PdfPTable(2).apply {
        widthPercentage = 100f
        setSpacingAfter(10f)
        defaultCell.border = Rectangle.NO_BORDER
    }

    val titleCell = PdfPCell().apply {
        border = Rectangle.NO_BORDER
        addElement(Paragraph("RAPPORT D'INTERVENTION", robotoBold.apply { size = 16f; setColor(accentColor) }))
        addElement(Paragraph("Rapport N° : ${report.reportId}", robotoRegular.apply { size = 10f }))
    }
    titleTable.addCell(titleCell)

    val detailsCell = PdfPCell().apply {
        border = Rectangle.NO_BORDER
        horizontalAlignment = Element.ALIGN_RIGHT
        addElement(
            Paragraph("Date : ${LocalDate.now()}", robotoRegular.apply { size = 10f })
                .apply { setAlignment(Element.ALIGN_RIGHT) }
        )
        addElement(
            Paragraph("Créé par : $creatorName", robotoRegular.apply { size = 10f })
                .apply { setAlignment(Element.ALIGN_RIGHT) }
        )
        addElement(
            Paragraph(
                "Période des travaux : ${report.workStartDate} au ${report.workFinishDate}",
                robotoRegular.apply { size = 10f })
                .apply { setAlignment(Element.ALIGN_RIGHT) }
        )
    }
    titleTable.addCell(detailsCell)

    document.add(titleTable)
    document.add(LineSeparator())
}

private fun addGeneralInfoSection(
    document: Document,
    report: Report,
    clientName: String,
    farmName: String,
    pumpName: String
) {
    document.add(Paragraph("Informations Générales", robotoBold.apply {
        size = 12f
        setColor(accentColor)
    }).apply { setSpacingBefore(10f); setSpacingAfter(5f) })

    val table = PdfPTable(2).apply {
        widthPercentage = 100f
        setSpacingBefore(5f)
    }
    table.addCell(createLabeledCell("Client", clientName))
    table.addCell(createLabeledCell("Installation", farmName))
    table.addCell(createLabeledCell("Forage", pumpName))
    table.addCell(createLabeledCell("Ordre d'exécution", report.executionOrder.toString()))
    table.addCell(createLabeledCell("Type d'opération", report.type.toString()))

    val operatorsList = List(false, 5f)
    report.operators.forEach { operator ->
        operatorsList.add(ListItem("- $operator", robotoLight))
    }
    val operatorsCell = PdfPCell().apply {
        border = Rectangle.NO_BORDER
        setPadding(4f)
        addElement(Phrase("Opérateurs : ", robotoRegular.apply { setColor(Color.DARK_GRAY) }))
        addElement(operatorsList)
    }
    table.addCell(operatorsCell)
    table.addCell(createLabeledCell("Date de la demande", report.requestDate.toString()))

    document.add(table)
}

// Financial Details Section
private fun addFinancialSection(document: Document, report: Report) {
    document.add(Paragraph("Détails Financiers", robotoBold.apply {
        size = 12f
        setColor(accentColor)
    }).apply { setSpacingBefore(10f); setSpacingAfter(5f) })

    val table = PdfPTable(2).apply {
        widthPercentage = 100f
        setSpacingBefore(5f)
    }
    table.addCell(createLabeledCell("Demande d'achat", report.purchaseRequest))
    table.addCell(createLabeledCell("Bon de commande", report.purchaseOrder))
    table.addCell(createLabeledCell("Devis", report.quotation))
    table.addCell(createLabeledCell("Facture", "${report.invoice} (Date: ${report.invoiceDate})"))

    document.add(table)
}

// Technical Details Section
private fun addTechnicalSection(document: Document, report: Report) {
    document.add(Paragraph("Détails Techniques", robotoBold.apply {
        size = 12f
        setColor(accentColor)
    }).apply { setSpacingBefore(10f); setSpacingAfter(5f) })

    val table = PdfPTable(2).apply {
        widthPercentage = 100f
        setSpacingBefore(5f)
    }

    // Add smaller details first
    table.addCell(createLabeledCell("Débit", "${report.speed} m3/min"))
    table.addCell(createLabeledCell("Profondeur", "${report.depth} m"))
    table.addCell(createLabeledCell("Niveau statique", "${report.staticLevel} m"))
    table.addCell(createLabeledCell("Niveau dynamique", "${report.dynamicLevel} m"))
    table.addCell(createLabeledCell("Calage de la pompe", "${report.pumpShimming} m"))

    // Conditionally add motor and pump details
    if (report.engine != null) {
        table.addCell(createLabeledCell("Moteur", report.engine))
    }

    if (report.pump != null) {
        // Use a single cell with colspan = 2 for the last item to make it a full row
        val pumpCell = PdfPCell(createLabeledCell("Pompe", report.pump)).apply {
            colspan = 2
            border = Rectangle.NO_BORDER
            setPadding(4f)
        }
        table.addCell(pumpCell)
    }

    document.add(table)

    document.add(Paragraph("Éléments utilisés", robotoRegular.apply {
        setColor(accentColor)
    }).apply {
        setSpacingBefore(10f)
    })
    val elementsList = List(false, 5f)

    val elementsToDisplay = report.elements ?: "N/A"

    if (elementsToDisplay == "N/A") {
        elementsList.add(ListItem("N/A", robotoLight))
    } else {
        elementsToDisplay.split("\n").forEach { line ->
            if (line.isNotBlank()) {
                elementsList.add(ListItem(line.trim().removePrefix("•").trim(), robotoLight))
            }
        }
    }
    document.add(elementsList)
}

// Notes Section
private fun addNotesSection(document: Document, report: Report) {
    document.add(Paragraph("Notes et Observations", robotoBold.apply {
        size = 12f
        color = accentColor
    }).apply { setSpacingBefore(10f); setSpacingAfter(5f) })

    val notesParagraph = Paragraph(report.notes, robotoLight.apply {
        color = Color.BLACK
    }).apply {
        setSpacingBefore(0f)
    }
    document.add(notesParagraph)
}
