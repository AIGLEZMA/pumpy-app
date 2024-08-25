import com.itextpdf.html2pdf.HtmlConverter
import models.Report
import java.io.File

object ReportPdf {

    fun generateAndSave(
        report: Report,
        clientUsername: String,
        creatorName: String,
        farmName: String,
        pumpName: String,
        outputPath: String,
    ) {
        val htmlContent = """
        <html>
        <head><title>Report</title></head>
        <body>
            <h1>Report ID: ${report.reportId}</h1>
            <p>Date: ${report.requestDate}</p>
            <p>Pump: ${pumpName}</p>
            <p>Farm: ${farmName}</p>
            <p>Client: ${clientUsername}</p>
            <p>Created by: ${creatorName}</p>
            <!-- Add more report details here -->
        </body>
        </html>
    """

        val outputFile = File(outputPath)
        HtmlConverter.convertToPdf(htmlContent, outputFile.outputStream())
    }

}