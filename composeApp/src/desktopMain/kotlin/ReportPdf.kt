import com.itextpdf.html2pdf.HtmlConverter
import java.nio.file.Path
import java.time.format.DateTimeFormatter
import java.util.*
import kotlin.io.path.outputStream
import models.Report

object ReportPdf {

    private val FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy", Locale.FRENCH)

    fun generateAndSave(
            report: Report,
            clientUsername: String,
            creatorName: String,
            farmName: String,
            pumpName: String,
            outputPath: Path
    ) {
        // val logoPath = javaClass.getResource("/logo.png").toExternalForm()
        // println(logoPath)
        val svgDiagramPath = javaClass.getResource("/files/diagram.svg").toExternalForm()
        val htmlContent =
                """
            <!DOCTYPE html>
            <html lang="fr">
            <head>
                <meta charset="UTF-8">
                <meta name="viewport" content="width=device-width, initial-scale=1.0">
                <title>Rapport</title>
                <style>
                    @page {
                        size: A4;
                        margin: 0mm;
                    }

                    body {
                        font-family: Arial, sans-serif;
                        margin: 0;
                        padding: 0;
                    }

                    .container {
                        width: 100%;
                        margin: 0 auto;
                        padding: 10mm;
                        box-sizing: border-box;
                        border: 1px solid #000;
                    }

                    .header {
                        display: flex;
                        justify-content: space-between;
                        align-items: center;
                        margin-bottom: 20px;
                    }

                    .header .logo-title-info {
                        display: flex;
                        justify-content: space-between;
                        width: 100%;
                    }

                    .logo {
                        width: 30mm;
                        height: 30mm;
                    }

                    .title {
                        text-align: center;
                        font-size: 20px;
                        font-weight: bold;
                        width: 50mm;
                    }

                    .info {
                        font-size: 12px;
                        width: 70mm;
                        border: 1px solid #000;
                        padding: 10px;
                        box-sizing: border-box;
                    }

                    .content {
                        margin-top: 20px;
                        display: flex;
                        justify-content: space-between;
                    }

                    .tables-container {
                        width: 55%;
                    }

                    .image-container {
                        width: 40%;
                        display: flex;
                        justify-content: center;
                        align-items: center;
                    }

                    .image-container img {
                        max-width: 100%;
                        height: auto;
                    }

                    .table-section {
                        margin-bottom: 10mm;
                    }

                    .table-section table {
                        width: 100%;
                        border-collapse: collapse;
                        font-size: 10px;
                    }

                    .table-section th, .table-section td {
                        border: 1px solid #000;
                        padding: 5px;
                        text-align: center;
                    }

                    .two-columns {
                        display: flex;
                        justify-content: space-between;
                        margin-top: 20px;
                    }

                    .observations, .intervenants {
                        width: 48%;
                        border: 1px solid #000;
                        padding: 10px;
                        box-sizing: border-box;
                        font-size: 12px;
                        margin-bottom: 10mm;
                        margin-right: 10px;
                    }

                    .footer {
                        width: 100%;
                        text-align: center;
                        border-top: 1px solid #000;
                        padding-top: 10px;
                        font-size: 12px;
                    }
                </style>
            </head>
            <body>
            <div class="container">
                <div class="header">
                    <div class="logo-title-info">
                        <div class="logo">
                            <img src="logo.png" alt="Logo" style="width: 100%; height: 100%;">
                        </div>
                        <div class="title">
                            RAPPORT
                        </div>
                        <div class="info">
                            <p>Bon d'exécution N°: ${report.executionOrder}</p>
                            <p>Date de demande: ${report.requestDate.format(FORMATTER)}</p>
                            <p>Débit des travaux: ${report.workFinishDate.format(FORMATTER)}</p>
                            <p>Client: $clientUsername</p>
                            <p>Demandeur: $creatorName</p>
                        </div>
                    </div>
                </div>

                <div class="content">
                    <div class="tables-container">
                        <div class="table-section">
                            <table>
                                <tr>
                                    <th>Désignation</th>
                                    <th>Moteur</th>
                                    <th>Pompe</th>
                                    <th>Eléments</th>
                                </tr>
                                <tr>
                                    <td>Montage</td>
                                    <td>${report.engine ?: "Aucun"}</td>
                                    <td>${report.pump ?: "Aucune"}</td>
                                    <td>${report.elements ?: "Aucuns"}</td>
                                </tr>
                                <tr>
                                    <td>Démontage</td>
                                    <td></td>
                                    <td></td>
                                    <td></td>
                                </tr>
                            </table>
                        </div>

                        <div class="table-section">
                            <table>
                                <tr>
                                    <td>Profondeur (m)</td>
                                    <td>${report.depth ?: ""}</td>
                                </tr>
                                <tr>
                                    <td>Niveau Statique (m)</td>
                                    <td>${report.staticLevel ?: ""}</td>
                                </tr>
                                <tr>
                                    <td>Niveau Dynamique (m)</td>
                                    <td>${report.dynamicLevel ?: ""}</td>
                                </tr>
                                <tr>
                                    <td>Calage de Pompe (m)</td>
                                    <td>${report.pumpShimming ?: ""}</td>
                                </tr>
                                <tr>
                                    <td>Débit (m3/h)</td>
                                    <td>${report.speed ?: ""}</td>
                                </tr>
                            </table>
                        </div>
                    </div>

                    <div class="image-container">
                        <img src="$svgDiagramPath" alt="Observation Image">
                    </div>
                </div>

                <div class="two-columns">
                    <div class="observations">
                        <p>Travaux effectués et Observations:<br />${report.notes}</p>
                    </div>

                    <div class="intervenants">
                        <p>Intervenants:<br />${report.operators.joinToString(" - ")}</p>
                    </div>
                </div>

                <div class="table-section">
                    <table>
                        <tr>
                            <td>Date fin des travaux</td>
                            <td>${report.workFinishDate.format(FORMATTER)}</td>
                        </tr>
                        <tr>
                            <td>N°D.A</td>
                            <td>${report.purchaseRequest}</td>
                        </tr>
                        <tr>
                            <td>N°Devis</td>
                            <td>${report.quotation}</td>
                        </tr>
                        <tr>
                            <td>N°B.C</td>
                            <td>${report.purchaseOrder}</td>
                        </tr>
                        <tr>
                            <td>N°Facture et la date</td>
                            <td>${report.invoice} le ${report.invoiceDate?.format(FORMATTER) ?: "Inconnu"}</td>
                        </tr>
                    </table>
                </div>

                <div class="footer">
                    355 .Av. AL Mouquaouama .Bloc B- 80150 Ait Melloul - Tél: 05.28.24.65.64 -Fax: 05.28.24.65.50 -Email: magrinov@gmail.com C.N.S.S: 8907670 -R.C: 8089 Inzegane -I.F: 40435556 -Patente: 49810052 -ICE: 001446940000049
                </div>
            </div>
            </body>
            </html>
        """

        HtmlConverter.convertToPdf(htmlContent, outputPath.outputStream())
    }
}
