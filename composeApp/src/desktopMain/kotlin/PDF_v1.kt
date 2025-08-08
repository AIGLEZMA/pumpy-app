import com.lowagie.text.*
import com.lowagie.text.pdf.PdfPCell
import com.lowagie.text.pdf.PdfPTable
import com.lowagie.text.pdf.PdfWriter
import models.Report
import java.awt.Color
import java.io.FileOutputStream
import java.nio.file.Path
import java.nio.file.Paths
import java.time.LocalDate
import java.time.format.DateTimeFormatter

private val FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy")
private val BORDER_COLOR = Color.decode("#000000") // Black for borders

fun main() {
    // 1. Create a mock Report object with sample data
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

    // 2. Define the output path for the generated PDF
    // This will create a file named "debug_report.pdf" in the project directory
    val outputPath: Path = Paths.get("debug_report.pdf")

    // 3. Call the main generation function with the mock data
    try {
        generateAndSaveWithOpenPDFV2(
            report = mockReport,
            clientUsername = "Magrinov",
            creatorName = "Admin",
            farmName = "Les Fermes de l'Atlas",
            pumpName = "Pompe Submersible 300",
            outputPath = outputPath
        )
        println("Successfully generated PDF at: ${outputPath.toAbsolutePath()}")
    } catch (e: Exception) {
        println("Failed to generate PDF: ${e.message}")
        e.printStackTrace()
    }
}

fun generateAndSaveWithOpenPDFV2(
    report: Report,
    clientUsername: String,
    creatorName: String,
    farmName: String,
    pumpName: String,
    outputPath: Path
) {
    // 1. Setup Document
    val document = Document(PageSize.A4, 30f, 30f, 30f, 30f) // A4 page with margins
    PdfWriter.getInstance(document, FileOutputStream(outputPath.toFile()))
    document.open()

    // 2. Add Header
    addHeader(document, report, clientUsername, creatorName)

    // 3. Add Content
    addContent(document, report)

    // 4. Add Observations & Intervenants
    addObservationsAndOperators(document, report)

    // 5. Add Executive Summary Table
    addExecutiveSummary(document, report)

    // 6. Add Footer
    addFooter(document)

    document.close()
}

// =================================================================================================
// HELPER FUNCTIONS FOR SECTIONS
// =================================================================================================

private fun addHeader(document: Document, report: Report, clientUsername: String, creatorName: String) {
    val headerTable = PdfPTable(3) // 3 columns: Logo, Title, Info
    headerTable.widthPercentage = 100f
    headerTable.setSpacingBefore(10f) // Reduced spacing from 50f to 10f

    // Column 1: Logo
    val logoCell = PdfPCell().apply {
        // Here you would load your logo image
        // ... (your image loading code)
        this.border = Rectangle.NO_BORDER
    }
    headerTable.addCell(logoCell)

    // Column 2: Title
    val titleCell = PdfPCell(Phrase("RAPPORT", Font(Font.HELVETICA, 20f, Font.BOLD))).apply {
        horizontalAlignment = Element.ALIGN_CENTER
        verticalAlignment = Element.ALIGN_MIDDLE
        border = Rectangle.NO_BORDER
    }
    headerTable.addCell(titleCell)

    // Column 3: Info
    val infoTable = PdfPTable(1).apply {
        widthPercentage = 100f
    }
    // Make 'Bon d'exécution N°' and its value bold
    val executionOrderPhrase = Phrase().apply {
        add(Chunk("Bon d'exécution N° : ", Font(Font.HELVETICA, 10f, Font.NORMAL)))
        add(Chunk(report.executionOrder.toString(), Font(Font.HELVETICA, 10f, Font.BOLD)))
    }
    infoTable.addCell(createBorderedCell(executionOrderPhrase))

    // Make 'Client' and its value bold
    val clientPhrase = Phrase().apply {
        add(Chunk("Client: ", Font(Font.HELVETICA, 10f, Font.NORMAL)))
        add(Chunk(clientUsername, Font(Font.HELVETICA, 10f, Font.BOLD)))
    }
    infoTable.addCell(createBorderedCell(clientPhrase))

    infoTable.addCell(createBorderedCell("Date de demande: ${report.requestDate.format(FORMATTER)}"))
    infoTable.addCell(createBorderedCell("Début des travaux: ${report.workStartDate.format(FORMATTER)}"))
    infoTable.addCell(createBorderedCell("Fin des travaux: ${report.workFinishDate.format(FORMATTER)}"))
    infoTable.addCell(createBorderedCell("Demandeur: $creatorName"))

    val infoCell = PdfPCell(infoTable).apply {
        border = Rectangle.BOX
        borderColor = BORDER_COLOR
        verticalAlignment = Element.ALIGN_MIDDLE
        horizontalAlignment = Element.ALIGN_CENTER
    }
    headerTable.addCell(infoCell)

    document.add(headerTable)
}

private fun createBorderedCell(content: Any): PdfPCell {
    return when (content) {
        is Phrase -> PdfPCell(content)
        is String -> PdfPCell(Phrase(content, Font(Font.HELVETICA, 10f, Font.NORMAL)))
        else -> PdfPCell(Phrase(content.toString(), Font(Font.HELVETICA, 10f, Font.NORMAL)))
    }.apply {
        horizontalAlignment = Element.ALIGN_CENTER
        verticalAlignment = Element.ALIGN_MIDDLE
        setPadding(5f)
        border = Rectangle.BOX
        borderColor = BORDER_COLOR
    }
}

private fun addContent(document: Document, report: Report) {
    // A single-column table to hold all the content
    val contentTable = PdfPTable(1)
    contentTable.widthPercentage = 100f
    contentTable.setSpacingBefore(20f)
    contentTable.setTotalWidth(floatArrayOf(100f))

    // Cell to hold the technical details tables
    val technicalTablesCell = PdfPCell().apply {
        border = Rectangle.NO_BORDER

        // Designation Table with adjusted column widths
        val designationTable = PdfPTable(4).apply {
            widthPercentage = 100f
            setSpacingBefore(10f)
            // Adjusted column widths: Designation is smaller, others are larger
            setTotalWidth(floatArrayOf(10f, 30f, 30f, 30f))
        }
        designationTable.addCell(createBorderedHeaderCell("Désignation"))
        designationTable.addCell(createBorderedHeaderCell("Moteur"))
        designationTable.addCell(createBorderedHeaderCell("Pompe"))
        designationTable.addCell(createBorderedHeaderCell("Eléments"))

        designationTable.addCell(createBorderedCell("Montage"))
        designationTable.addCell(createBorderedCell(report.engine ?: "Aucun"))
        designationTable.addCell(createBorderedCell(report.pump ?: "Aucune"))
        designationTable.addCell(createBorderedCell(report.elements ?: "Aucuns"))

        designationTable.addCell(createBorderedCell("Démontage"))
        designationTable.addCell(createBorderedCell(""))
        designationTable.addCell(createBorderedCell(""))
        designationTable.addCell(createBorderedCell(""))

        this.addElement(designationTable)

        // New layout: a table with two columns, one for depth data and one for operators
        val technicalAndOperatorsTable = PdfPTable(2).apply {
            widthPercentage = 100f
            setSpacingBefore(10f)
        }

        // Column 1: Depth and other technical measurements
        val technicalDataCell = PdfPCell().apply {
            border = Rectangle.NO_BORDER
            setPadding(0f)

            val depthTable = PdfPTable(2).apply {
                widthPercentage = 100f
            }
            depthTable.addCell(createBorderedCell("Profondeur (m)"))
            depthTable.addCell(createBorderedCell(report.depth ?: ""))
            depthTable.addCell(createBorderedCell("Niveau Statique (m)"))
            depthTable.addCell(createBorderedCell(report.staticLevel ?: ""))
            depthTable.addCell(createBorderedCell("Niveau Dynamique (m)"))
            depthTable.addCell(createBorderedCell(report.dynamicLevel ?: ""))
            depthTable.addCell(createBorderedCell("Calage de Pompe (m)"))
            depthTable.addCell(createBorderedCell(report.pumpShimming ?: ""))
            depthTable.addCell(createBorderedCell("Débit (m3/h)"))
            depthTable.addCell(createBorderedCell(report.speed ?: ""))

            this.addElement(depthTable)
        }
        technicalAndOperatorsTable.addCell(technicalDataCell)

        // Column 2: Operators list with padding
        val operatorsCell = PdfPCell().apply {
            border = Rectangle.BOX
            borderColor = BORDER_COLOR
            setPadding(10f)
            // Ensure the text is at the top of the cell
            verticalAlignment = Element.ALIGN_TOP
            // Left align for better readability
            horizontalAlignment = Element.ALIGN_LEFT
            addElement(Paragraph("Intervenants", Font(Font.HELVETICA, 12f, Font.BOLD)))
            addElement(Paragraph(report.operators.joinToString(" - "), Font(Font.HELVETICA, 10f, Font.NORMAL)))
        }
        technicalAndOperatorsTable.addCell(operatorsCell)

        this.addElement(technicalAndOperatorsTable)
    }

    contentTable.addCell(technicalTablesCell)
    document.add(contentTable)
}

private fun addObservationsAndOperators(document: Document, report: Report) {
    val twoColumnsTable = PdfPTable(2) // 2 columns for observations and operators
    twoColumnsTable.widthPercentage = 100f
    twoColumnsTable.setSpacingBefore(20f)

    // Observations
    val observationsCell = PdfPCell().apply {
        border = Rectangle.BOX
        borderColor = BORDER_COLOR
        setPadding(10f)
        addElement(Paragraph("Travaux effectués et Observations:", Font(Font.HELVETICA, 12f, Font.BOLD)))
        addElement(Paragraph(report.notes, Font(Font.HELVETICA, 10f, Font.NORMAL)))
    }
    twoColumnsTable.addCell(observationsCell)

    // Intervenants
    val intervenantsCell = PdfPCell().apply {
        border = Rectangle.BOX
        borderColor = BORDER_COLOR
        setPadding(10f)
        addElement(Paragraph("Intervenants:", Font(Font.HELVETICA, 12f, Font.BOLD)))
        addElement(Paragraph(report.operators.joinToString(" - "), Font(Font.HELVETICA, 10f, Font.NORMAL)))
    }
    twoColumnsTable.addCell(intervenantsCell)

    document.add(twoColumnsTable)
}

private fun addExecutiveSummary(document: Document, report: Report) {
    val summaryTable = PdfPTable(2)
    summaryTable.widthPercentage = 100f
    summaryTable.setSpacingBefore(20f)
    summaryTable.addCell(createBorderedCell("Date fin des travaux"))
    summaryTable.addCell(createBorderedCell(report.workFinishDate.format(FORMATTER)))
    summaryTable.addCell(createBorderedCell("N°D.A"))
    summaryTable.addCell(createBorderedCell(report.purchaseRequest))
    summaryTable.addCell(createBorderedCell("N°Devis"))
    summaryTable.addCell(createBorderedCell(report.quotation))
    summaryTable.addCell(createBorderedCell("N°B.C"))
    summaryTable.addCell(createBorderedCell(report.purchaseOrder))
    summaryTable.addCell(createBorderedCell("N°Facture et la date"))
    summaryTable.addCell(createBorderedCell("${report.invoice} le ${report.invoiceDate?.format(FORMATTER) ?: "Inconnu"}"))

    document.add(summaryTable)
}

private fun addFooter(document: Document) {
    val footerTable = PdfPTable(1)
    footerTable.widthPercentage = 100f
    footerTable.setSpacingBefore(20f)

    val footerCell = createBorderedCell(
        "355 .Av. AL Mouquaouama .Bloc B- 80150 Ait Melloul - Tél: 05.28.24.65.64 -Fax: 05.28.24.65.50 -Email: magrinov@gmail.com C.N.S.S: 8907670 -R.C: 8089 Inzegane -I.F: 40435556 -Patente: 49810052 -ICE: 001446940000049"
    ).apply {
        border = Rectangle.TOP
        borderColor = BORDER_COLOR
        horizontalAlignment = Element.ALIGN_CENTER
        paddingTop = 10f
    }
    footerTable.addCell(footerCell)

    document.add(footerTable)
}

private fun createBorderedHeaderCell(text: String): PdfPCell {
    return PdfPCell(Phrase(text, Font(Font.HELVETICA, 10f, Font.BOLD))).apply {
        horizontalAlignment = Element.ALIGN_CENTER
        verticalAlignment = Element.ALIGN_MIDDLE
        setPadding(5f)
        border = Rectangle.BOX
        borderColor = BORDER_COLOR
        backgroundColor = Color.decode("#F0F0F0") // Light gray header background
    }
}