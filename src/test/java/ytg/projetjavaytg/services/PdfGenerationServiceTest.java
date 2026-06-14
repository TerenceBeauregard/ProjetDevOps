package ytg.projetjavaytg.services;

import org.junit.jupiter.api.Test;
import ytg.projetjavaytg.models.Apprenti;
import ytg.projetjavaytg.models.Entreprise;
import ytg.projetjavaytg.models.Evaluation;
import ytg.projetjavaytg.models.MaitreApprentissage;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

class PdfGenerationServiceTest {

    private final PdfGenerationService pdfGenerationService = new PdfGenerationService();

    @Test
    void generateEvaluationPdf_genereUnPdfNonVideAvecToutesLesInformations() {
        Entreprise entreprise = new Entreprise();
        entreprise.setRaisonSociale("Acme Corp");
        entreprise.setAdresse("1 rue de Paris");
        entreprise.setInformationsAcces("Badge requis");

        MaitreApprentissage maitre = new MaitreApprentissage();
        maitre.setNom("Durand");
        maitre.setPrenom("Paul");
        maitre.setPoste("Manager");
        maitre.setEmail("paul.durand@acme.com");
        maitre.setTelephone("0102030405");

        Apprenti apprenti = new Apprenti();
        apprenti.setNom("Dupont");
        apprenti.setPrenom("Jean");
        apprenti.setEmail("jean.dupont@mail.com");
        apprenti.setTelephone("0607080910");
        apprenti.setProgramme("Master Info");
        apprenti.setAnneeAcademique("2024-2025");
        apprenti.setMajeure("Génie Logiciel");
        apprenti.setNiveau("I2");
        apprenti.setEntreprise(entreprise);
        apprenti.setMaitreApprentissage(maitre);
        apprenti.setMissionMotsCles("Java, Spring");
        apprenti.setMissionMetierCible("Développeur");
        apprenti.setMissionCommentaires("Bonne mission");
        apprenti.setFeedbackTuteur("Très bon apprenti");
        apprenti.setRemarquesGenerales("RAS");

        Evaluation evaluation = new Evaluation();
        evaluation.setMemoireTheme("Sujet de mémoire");
        evaluation.setMemoireNote(BigDecimal.valueOf(15));
        evaluation.setMemoireCommentaires("Bon mémoire");
        evaluation.setSoutenanceDate(LocalDate.of(2025, 6, 15));
        evaluation.setSoutenanceNote(BigDecimal.valueOf(16));
        evaluation.setSoutenanceCommentaires("Bonne soutenance");
        evaluation.setRemarquesGenerales("Excellent");

        byte[] pdf = pdfGenerationService.generateEvaluationPdf(evaluation, apprenti);

        assertThat(pdf).isNotEmpty();
        assertThat(new String(pdf, 0, 5, java.nio.charset.StandardCharsets.US_ASCII)).isEqualTo("%PDF-");
    }

    @Test
    void generateEvaluationPdf_genereUnPdfNonVideAvecChampsManquants() {
        Apprenti apprenti = new Apprenti();
        apprenti.setNom("Martin");
        apprenti.setPrenom("Sophie");

        Evaluation evaluation = new Evaluation();

        byte[] pdf = pdfGenerationService.generateEvaluationPdf(evaluation, apprenti);

        assertThat(pdf).isNotEmpty();
        assertThat(new String(pdf, 0, 5, java.nio.charset.StandardCharsets.US_ASCII)).isEqualTo("%PDF-");
    }
}
