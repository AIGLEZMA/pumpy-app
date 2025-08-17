import com.lowagie.text.*
import com.lowagie.text.List
import com.lowagie.text.pdf.*
import com.lowagie.text.pdf.draw.LineSeparator
import models.Company
import models.Report
import java.awt.Color
import java.awt.image.BufferedImage
import java.io.ByteArrayOutputStream
import java.io.FileOutputStream
import java.io.InputStream
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import javax.imageio.ImageIO

val frenchDateFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")

fun generateReport(
    report: Report,
    clientUsername: String,
    creatorName: String,
    farmName: String,
    pumpName: String,
    company: Company,
    outputPath: String
) {
    FontFactory.registerDirectories()

    val document = Document(PageSize.A4)
    val writer = PdfWriter.getInstance(document, FileOutputStream(outputPath))

    writer.pageEvent = HeaderFooterPageEvent()

    document.open()

    addTitleAndHeaderInfo(document, report = report, creatorName = creatorName, company = company)
    addGeneralInfoSection(
        document, report, clientName = clientUsername, farmName = farmName, pumpName = pumpName
    )
    addFinancialSection(document, report)
    addTechnicalSection(document, report)
    addNotesSection(document, report)

    document.close()
    Logger.debug("PDF generated successfully at: $outputPath")
}

val robotoBold: Font = FontFactory.getFont("Roboto-Bold", BaseFont.IDENTITY_H, true, 18f)
val robotoRegular: Font = FontFactory.getFont("Roboto-Regular", BaseFont.IDENTITY_H, true, 12f)
val robotoLight: Font = FontFactory.getFont("Roboto-Light", BaseFont.IDENTITY_H, true, 10f)

val accentColor = Color(0, 102, 204)

private fun createLabeledCell(label: String, value: String): PdfPCell {
    val cell = PdfPCell().apply {
        border = Rectangle.NO_BORDER
        setPadding(4f)
    }
    val phrase = Phrase().apply {
        add(
            Chunk(
                "$label : ",
                robotoRegular.apply {
                    size = 10f
                    color = Color.DARK_GRAY
                }
            ))
        add(
            Chunk(
                value,
                robotoLight.apply {
                    size = 11f
                    color = Color.BLACK
                }
            ))
    }
    cell.addElement(phrase)
    return cell
}

class HeaderFooterPageEvent : PdfPageEventHelper() {
    override fun onEndPage(writer: PdfWriter, document: Document) {
        val contentByte = writer.directContent

        val footerTable = PdfPTable(1).apply {
            widthPercentage = 100f
            defaultCell.border = Rectangle.NO_BORDER
            defaultCell.setPadding(5f)
            defaultCell.verticalAlignment = Element.ALIGN_MIDDLE
            defaultCell.horizontalAlignment = Element.ALIGN_CENTER
        }

        val firstLineParagraph = Paragraph(
            "355, Av. Al Mouquaouama, Bloc B, 80150, AIT MELLOUL | Tél: 05.28.24.65.64 - Fax: 05.28.24.65.50",
            robotoLight.apply { size = 9f; color = Color.BLACK }).apply {
            setAlignment(Element.ALIGN_CENTER)
            setSpacingAfter(2f)
        }

        val footerCell = PdfPCell().apply {
            border = Rectangle.NO_BORDER
            horizontalAlignment = Element.ALIGN_CENTER
            verticalAlignment = Element.ALIGN_MIDDLE
            addElement(firstLineParagraph)
        }

        footerTable.addCell(footerCell)

        footerTable.setTotalWidth(document.right() - document.left())

        val footerYPosition = document.bottomMargin() + 10f

        footerTable.writeSelectedRows(
            0, -1, document.leftMargin(), footerYPosition, contentByte
        )
    }
}


private fun addTitleAndHeaderInfo(document: Document, report: Report, creatorName: String, company: Company) {
    val headerTable = PdfPTable(3).apply {
        widthPercentage = 100f
        setWidths(floatArrayOf(1f, 2f, 1f))
        defaultCell.border = Rectangle.NO_BORDER
    }

    val name = if (company == Company.LOTRAX) "lotrax_logo_cropped.jpeg" else "magrinov_logo_cropped.png"
    val logoStream =
        Thread.currentThread().contextClassLoader.getResourceAsStream(name)
    if (logoStream != null) {
        val logoImage = Image.getInstance(logoStream.readBytes())
        logoImage.scaleToFit(80f, 80f)
        val logoCell = PdfPCell(logoImage).apply {
            border = Rectangle.NO_BORDER
            horizontalAlignment = Element.ALIGN_LEFT
            verticalAlignment = Element.ALIGN_MIDDLE
        }
        headerTable.addCell(logoCell)
    } else {
        headerTable.addCell(PdfPCell().apply { border = Rectangle.NO_BORDER })
    }

    val titleFont = robotoBold.apply {
        size = 16f
        color = accentColor
    }
    val reportIdFont = robotoRegular.apply {
        size = 10f
    }

    val titleParagraph = Paragraph("RAPPORT D'INTERVENTION\n", titleFont).apply {
        alignment = Element.ALIGN_CENTER
    }
    val reportIdParagraph = Paragraph("Rapport N° : ${report.reportId}", reportIdFont).apply {
        alignment = Element.ALIGN_CENTER
    }

    val titleCell = PdfPCell().apply {
        border = Rectangle.NO_BORDER
        horizontalAlignment = Element.ALIGN_CENTER
        verticalAlignment = Element.ALIGN_MIDDLE
        addElement(titleParagraph)
        addElement(reportIdParagraph)
    }
    headerTable.addCell(titleCell)

    val detailsCell = PdfPCell().apply {
        border = Rectangle.NO_BORDER
        horizontalAlignment = Element.ALIGN_RIGHT
        verticalAlignment = Element.ALIGN_MIDDLE
        addElement(
            Paragraph(
                "Date : ${LocalDate.now().format(frenchDateFormatter)}",
                robotoRegular.apply { size = 8f }).apply { setAlignment(Element.ALIGN_RIGHT) })
        addElement(
            Paragraph(
                "Créé par : $creatorName",
                robotoRegular.apply { size = 8f }).apply { setAlignment(Element.ALIGN_RIGHT) })
        addElement(
            Paragraph(
                "Travaux : ${report.workStartDate.format(frenchDateFormatter)} - ${
                    report.workFinishDate.format(
                        frenchDateFormatter
                    )
                }", robotoRegular.apply { size = 8f }).apply { setAlignment(Element.ALIGN_RIGHT) })
    }
    headerTable.addCell(detailsCell)

    document.add(headerTable)
    val spacer = Paragraph("").apply { spacingBefore = 5f }
    document.add(spacer)
    document.add(LineSeparator())
}

private fun addGeneralInfoSection(
    document: Document, report: Report, clientName: String, farmName: String, pumpName: String
) {
    document.add(Paragraph("Informations Générales", robotoBold.apply {
        size = 12f
        color = accentColor
    }).apply { setSpacingBefore(10f); setSpacingAfter(5f) })

    val table = PdfPTable(2).apply {
        widthPercentage = 100f
        setSpacingBefore(5f)
    }
    table.addCell(createLabeledCell("Client", clientName))
    table.addCell(createLabeledCell("Bon d'exécution", report.executionOrder.toString()))
    table.addCell(createLabeledCell("Installation", farmName))
    table.addCell(createLabeledCell("Forage", pumpName))
    table.addCell(createLabeledCell("Type d'opération", report.type.beautiful))

    val operatorsList = List(false, 5f)
    report.operators.forEach { operator ->
        operatorsList.add(ListItem("- $operator", robotoLight))
    }
    val operatorsCell = PdfPCell().apply {
        border = Rectangle.NO_BORDER
        setPadding(4f)
        addElement(Phrase("Opérateurs : ", robotoRegular.apply { color = Color.DARK_GRAY }))
        addElement(operatorsList)
    }
    table.addCell(operatorsCell)
    table.addCell(createLabeledCell("Date de la demande", report.requestDate.format(frenchDateFormatter)))

    document.add(table)
}

private fun addFinancialSection(document: Document, report: Report) {
    document.add(Paragraph("Détails Financiers", robotoBold.apply {
        size = 12f
        color = accentColor
    }).apply { setSpacingBefore(10f); setSpacingAfter(5f) })

    val table = PdfPTable(2).apply {
        widthPercentage = 100f
        setSpacingBefore(5f)
    }
    table.addCell(createLabeledCell("Demande d'achat", report.purchaseRequest))
    table.addCell(createLabeledCell("Bon de commande", report.purchaseOrder))
    table.addCell(createLabeledCell("Devis", report.quotation))
    table.addCell(
        createLabeledCell(
            "Facture", "${report.invoice} (Date: ${report.invoiceDate?.format(frenchDateFormatter) ?: "N/A"})"
        )
    )

    document.add(table)
}

private fun addTechnicalSection(document: Document, report: Report) {
    document.add(Paragraph("Détails Techniques", robotoBold.apply {
        size = 12f
        color = accentColor
    }).apply { setSpacingBefore(10f); setSpacingAfter(5f) })

    val table = PdfPTable(2).apply {
        widthPercentage = 100f
        setSpacingBefore(5f)
    }

    table.addCell(createLabeledCell("Débit", "${report.speed} m³/h"))
    table.addCell(createLabeledCell("Profondeur", "${report.depth} m"))
    table.addCell(createLabeledCell("Niveau statique", "${report.staticLevel} m"))
    table.addCell(createLabeledCell("Niveau dynamique", "${report.dynamicLevel} m"))
    table.addCell(createLabeledCell("Calage de la pompe", "${report.pumpShimming} m"))

    if (report.engine != null) {
        table.addCell(createLabeledCell("Moteur", report.engine))
    }

    if (report.pump != null) {
        val pumpCell = PdfPCell(createLabeledCell("Pompe", report.pump)).apply {
            colspan = 2
            border = Rectangle.NO_BORDER
            setPadding(4f)
        }
        table.addCell(pumpCell)
    }

    document.add(table)

    document.add(Paragraph("Éléments utilisés", robotoRegular.apply {
        color = accentColor
    }).apply {
        setSpacingBefore(10f)
    })
    val elementsList = List(false, 5f)

    report.elements.forEach { element ->
        if (element.isNotBlank()) {
            elementsList.add(ListItem(element, robotoLight))
        }
    }

    document.add(elementsList)
}

private fun addNotesSection(document: Document, report: Report) {
    // Section title
    document.add(
        Paragraph("Notes et Observations", robotoBold.apply {
            size = 12f
            color = accentColor
        }).apply { setSpacingBefore(10f); setSpacingAfter(5f) }
    )

    // 2/3 notes, 1/3 image
    val table = PdfPTable(2).apply {
        widthPercentage = 100f
        setSpacingBefore(5f)
        setWidths(floatArrayOf(2f, 1f)) // 2/3 : 1/3
        defaultCell.border = Rectangle.NO_BORDER
    }

    // LEFT (2/3): Notes text
    val notesParagraph = Paragraph(report.notes, robotoLight.apply {
        color = Color.BLACK
        size = 10f
    })
    val notesCell = PdfPCell().apply {
        border = Rectangle.NO_BORDER
        setPadding(4f)
        addElement(notesParagraph)
    }
    table.addCell(notesCell)

    // RIGHT (1/3): schema image from resources
    val imageCell = PdfPCell().apply {
        border = Rectangle.NO_BORDER
        horizontalAlignment = Element.ALIGN_CENTER
        verticalAlignment = Element.ALIGN_MIDDLE
        setPadding(4f)
        minimumHeight = 120f // gives the cell a little height even if the image is small
    }

    try {
        val img = getAnnotatedSchema()
        img.alignment = Element.ALIGN_CENTER

        val tableInnerWidth = document.right() - document.left()
        val maxImgWidth = (tableInnerWidth * (1f / 3f)) - 12f

        if (img.width > maxImgWidth) {
            val scale = maxImgWidth / img.width
            img.scaleAbsolute(img.width * scale, img.height * scale)
        }

        imageCell.addElement(img)
    } catch (e: Exception) {
        imageCell.addElement(
            Paragraph("Erreur de chargement du schéma", robotoLight.apply {
                color = Color.RED
                size = 9f
            }).apply { alignment = Element.ALIGN_CENTER }
        )
    }

    table.addCell(imageCell)

    document.add(table)
}

fun getAnnotatedSchema(): Image {
    val originalStream: InputStream = Thread.currentThread().contextClassLoader
        .getResourceAsStream("schema.png")
        ?: throw IllegalArgumentException("Image 'schema.png' not found in resources")

    val originalImage: BufferedImage = ImageIO.read(originalStream)

    val annotatedImage = BufferedImage(
        originalImage.width, originalImage.height, BufferedImage.TYPE_INT_ARGB
    )
    val g2d = annotatedImage.createGraphics()

    g2d.drawImage(originalImage, 0, 0, null)

    g2d.color = Color.BLACK
    g2d.font = java.awt.Font("Arial", Font.NORMAL, 36)
    g2d.drawString("10", 480, 400) // X=475
    g2d.drawString("20", 580, 675) // X=575; Y=650
    g2d.drawString("30", 680, 1190) // X=; Y=1150
    g2d.drawString("40", 775, 1310)

    if (false) {
        for (y in 0 until annotatedImage.height step 25) {
            g2d.color = Color.LIGHT_GRAY
            g2d.font = java.awt.Font("Arial", Font.NORMAL, 8)
            g2d.drawLine(0, y, annotatedImage.width, y)
            g2d.drawString("Y=$y", 5, y - 5)
        }

        for (x in 0 until annotatedImage.width step 25) {
            g2d.color = Color.LIGHT_GRAY
            g2d.font = java.awt.Font("Arial", Font.NORMAL, 8)
            g2d.drawLine(x, 0, x, annotatedImage.height)
            g2d.drawString("X=$x", x + 2, 10)
        }
    }

    g2d.dispose()

    val baos = ByteArrayOutputStream()
    ImageIO.write(annotatedImage, "png", baos)
    val imageBytes = baos.toByteArray()
    return Image.getInstance(imageBytes)
}
