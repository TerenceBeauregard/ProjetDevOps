package ytg.projetjavaytg.services;

import com.itextpdf.kernel.colors.ColorConstants;
import com.itextpdf.kernel.colors.DeviceRgb;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;
import org.springframework.stereotype.Service;
import ytg.projetjavaytg.models.Apprenti;
import ytg.projetjavaytg.models.Evaluation;

import java.io.ByteArrayOutputStream;
import java.time.format.DateTimeFormatter;

@Service
public class PdfGenerationService {

    private static final DeviceRgb HEADER_COLOR = new DeviceRgb(70, 130, 180);
    private static final DeviceRgb SECTION_COLOR = new DeviceRgb(211, 211, 211);

    public byte[] generateEvaluationPdf(Evaluation evaluation, Apprenti apprenti) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        try {
            PdfWriter writer = new PdfWriter(baos);
            PdfDocument pdf = new PdfDocument(writer);
            Document document = new Document(pdf);

            // Titre du bulletin
            Paragraph title = new Paragraph("BULLETIN D'ÉVALUATION")
                    .setFontSize(18)
                    .setBold()
                    .setTextAlignment(TextAlignment.CENTER)
                    .setMarginBottom(20);
            document.add(title);

            // Informations de l'apprenti
            document.add(createSectionTitle("INFORMATIONS APPRENTI"));
            document.add(createApprentiTable(apprenti));

            // Informations entreprise
            document.add(createSectionTitle("ENTREPRISE ET MAÎTRE D'APPRENTISSAGE"));
            document.add(createEntrepriseTable(apprenti));

            // Mission
            document.add(createSectionTitle("MISSION"));
            document.add(createMissionTable(apprenti));

            // Évaluation académique
            document.add(createSectionTitle("ÉVALUATION ACADÉMIQUE"));
            document.add(createEvaluationTable(evaluation));

            // Feedback du tuteur
            document.add(createSectionTitle("FEEDBACK DU TUTEUR-ENSEIGNANT"));
            document.add(createFeedbackTable(apprenti, evaluation));

            // Date de génération
            Paragraph dateGen = new Paragraph("Bulletin généré le : " +
                    java.time.LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")))
                    .setFontSize(10)
                    .setTextAlignment(TextAlignment.RIGHT)
                    .setMarginTop(20);
            document.add(dateGen);

            document.close();

        } catch (Exception e) {
            throw new RuntimeException("Erreur lors de la génération du PDF", e);
        }

        return baos.toByteArray();
    }

    private Paragraph createSectionTitle(String title) {
        return new Paragraph(title)
                .setFontSize(14)
                .setBold()
                .setBackgroundColor(SECTION_COLOR)
                .setPadding(5)
                .setMarginTop(15)
                .setMarginBottom(10);
    }

    private Table createApprentiTable(Apprenti apprenti) {
        Table table = new Table(UnitValue.createPercentArray(new float[]{30, 70}))
                .setWidth(UnitValue.createPercentValue(100));

        addTableRow(table, "Nom", apprenti.getNom());
        addTableRow(table, "Prénom", apprenti.getPrenom());
        addTableRow(table, "Email", apprenti.getEmail());
        addTableRow(table, "Téléphone", apprenti.getTelephone());
        addTableRow(table, "Programme", apprenti.getProgramme());
        addTableRow(table, "Année académique", apprenti.getAnneeAcademique());
        addTableRow(table, "Majeure", apprenti.getMajeure());
        addTableRow(table, "Niveau", apprenti.getNiveau());

        return table;
    }

    private Table createEntrepriseTable(Apprenti apprenti) {
        Table table = new Table(UnitValue.createPercentArray(new float[]{30, 70}))
                .setWidth(UnitValue.createPercentValue(100));

        if (apprenti.getEntreprise() != null) {
            addTableRow(table, "Raison sociale", apprenti.getEntreprise().getRaisonSociale());
            addTableRow(table, "Adresse", apprenti.getEntreprise().getAdresse());
            addTableRow(table, "Informations d'accès", apprenti.getEntreprise().getInformationsAcces());
        }

        if (apprenti.getMaitreApprentissage() != null) {
            addTableRow(table, "Maître d'apprentissage",
                    apprenti.getMaitreApprentissage().getPrenom() + " " +
                    apprenti.getMaitreApprentissage().getNom());
            addTableRow(table, "Poste", apprenti.getMaitreApprentissage().getPoste());
            addTableRow(table, "Email", apprenti.getMaitreApprentissage().getEmail());
            addTableRow(table, "Téléphone", apprenti.getMaitreApprentissage().getTelephone());
        }

        return table;
    }

    private Table createMissionTable(Apprenti apprenti) {
        Table table = new Table(UnitValue.createPercentArray(new float[]{30, 70}))
                .setWidth(UnitValue.createPercentValue(100));

        addTableRow(table, "Mots-clés", apprenti.getMissionMotsCles());
        addTableRow(table, "Métier cible", apprenti.getMissionMetierCible());
        addTableRow(table, "Commentaires", apprenti.getMissionCommentaires());

        return table;
    }

    private Table createEvaluationTable(Evaluation evaluation) {
        Table table = new Table(UnitValue.createPercentArray(new float[]{30, 70}))
                .setWidth(UnitValue.createPercentValue(100));

        // Mémoire / Rapport
        addHeaderRow(table, "MÉMOIRE / RAPPORT");
        addTableRow(table, "Thème/Sujet", evaluation.getMemoireTheme());
        addTableRow(table, "Note finale",
                evaluation.getMemoireNote() != null ? evaluation.getMemoireNote().toString() + "/20" : "Non noté");
        addTableRow(table, "Commentaires", evaluation.getMemoireCommentaires());

        // Soutenance
        addHeaderRow(table, "SOUTENANCE");
        addTableRow(table, "Date",
                evaluation.getSoutenanceDate() != null ?
                evaluation.getSoutenanceDate().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")) : "Non programmée");
        addTableRow(table, "Note finale",
                evaluation.getSoutenanceNote() != null ? evaluation.getSoutenanceNote().toString() + "/20" : "Non noté");
        addTableRow(table, "Commentaires", evaluation.getSoutenanceCommentaires());

        return table;
    }

    private Table createFeedbackTable(Apprenti apprenti, Evaluation evaluation) {
        Table table = new Table(UnitValue.createPercentArray(new float[]{30, 70}))
                .setWidth(UnitValue.createPercentValue(100));

        addTableRow(table, "Feedback du tuteur", apprenti.getFeedbackTuteur());
        addTableRow(table, "Remarques générales (Évaluation)", evaluation.getRemarquesGenerales());
        addTableRow(table, "Remarques générales (Apprenti)", apprenti.getRemarquesGenerales());

        return table;
    }

    private void addHeaderRow(Table table, String content) {
        Cell cell = new Cell(1, 2)
                .add(new Paragraph(content).setBold())
                .setBackgroundColor(HEADER_COLOR)
                .setFontColor(ColorConstants.WHITE)
                .setTextAlignment(TextAlignment.CENTER);
        table.addCell(cell);
    }

    private void addTableRow(Table table, String label, String value) {
        Cell labelCell = new Cell()
                .add(new Paragraph(label).setBold())
                .setBackgroundColor(new DeviceRgb(245, 245, 245));
        Cell valueCell = new Cell()
                .add(new Paragraph(value != null ? value : "-"));

        table.addCell(labelCell);
        table.addCell(valueCell);
    }
}

