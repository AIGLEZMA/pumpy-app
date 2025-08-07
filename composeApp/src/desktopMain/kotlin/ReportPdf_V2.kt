import com.lowagie.text.*
import com.lowagie.text.pdf.PdfPCell
import com.lowagie.text.pdf.PdfPTable
import com.lowagie.text.pdf.PdfWriter
import models.Report
import java.awt.Color
import java.io.FileOutputStream
import java.nio.file.Path
import java.time.format.DateTimeFormatter

private val FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy")
private val BORDER_COLOR = Color.decode("#000000") // Black for borders

fun generateAndSaveWithOpenPDF(
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
    headerTable.setSpacingBefore(50f)

    // Column 1: Logo
    val logoCell = PdfPCell().apply {
        // Here you would load your logo image
        // val logoPath = javaClass.getResource("/logo.png")
        // if (logoPath != null) {
        //     val logoImage = Image.getInstance(logoPath)
        //     logoImage.scaleToFit(50f, 50f)
        //     this.addElement(logoImage)
        // }
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
    infoTable.addCell(createBorderedCell("Bon d'exécution N°: ${report.executionOrder}"))
    infoTable.addCell(createBorderedCell("Date de demande: ${report.requestDate.format(FORMATTER)}"))
    infoTable.addCell(createBorderedCell("Débit des travaux: ${report.workFinishDate.format(FORMATTER)}"))
    infoTable.addCell(createBorderedCell("Client: $clientUsername"))
    infoTable.addCell(createBorderedCell("Demandeur: $creatorName"))

    val infoCell = PdfPCell(infoTable).apply {
        border = Rectangle.BOX
        borderColor = BORDER_COLOR
    }
    headerTable.addCell(infoCell)

    document.add(headerTable)
}

private fun addContent(document: Document, report: Report) {
    val contentTable = PdfPTable(2) // 2 columns: tables and image
    contentTable.widthPercentage = 100f
    contentTable.setSpacingBefore(20f)
    contentTable.setTotalWidth(floatArrayOf(60f, 40f))

    // Column 1: Technical Tables
    val technicalTablesCell = PdfPCell().apply {
        border = Rectangle.NO_BORDER

        val designationTable = PdfPTable(4).apply {
            widthPercentage = 100f
            setSpacingBefore(10f)
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

        val depthTable = PdfPTable(2).apply {
            widthPercentage = 100f
            setSpacingBefore(10f)
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
    contentTable.addCell(technicalTablesCell)

    // Column 2: Image
    val imageCell = PdfPCell().apply {
        horizontalAlignment = Element.ALIGN_CENTER
        verticalAlignment = Element.ALIGN_MIDDLE
        border = Rectangle.BOX
        borderColor = BORDER_COLOR
        // Load and add the image
        // The image path needs to be resolved correctly.
        // val svgDiagramPath = javaClass.getResource("/diagram.svg")
        // if (svgDiagramPath != null) {
        //     // OpenPDF can't directly handle SVG, you'd need to rasterize it first
        //     val image = Image.getInstance(svgDiagramPath)
        //     image.scaleToFit(200f, 200f)
        //     this.addElement(image)
        // }
    }
    contentTable.addCell(imageCell)

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

// Custom cell for consistency
private fun createBorderedCell(text: Any): PdfPCell {
    val textAsString = text.toString() // Safely converts any object to its string representation
    return PdfPCell(Phrase(textAsString, Font(Font.HELVETICA, 10f, Font.NORMAL))).apply {
        horizontalAlignment = Element.ALIGN_CENTER
        verticalAlignment = Element.ALIGN_MIDDLE
        setPadding(5f)
        border = Rectangle.BOX
        borderColor = BORDER_COLOR
    }
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