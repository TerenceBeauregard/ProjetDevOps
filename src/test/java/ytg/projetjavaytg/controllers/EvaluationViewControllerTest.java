package ytg.projetjavaytg.controllers;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import ytg.projetjavaytg.models.Apprenti;
import ytg.projetjavaytg.models.Entreprise;
import ytg.projetjavaytg.models.Evaluation;
import ytg.projetjavaytg.models.Visite;
import ytg.projetjavaytg.repositories.VisiteRepository;
import ytg.projetjavaytg.security.CustomUserDetailsService;
import ytg.projetjavaytg.services.ApprentiService;
import ytg.projetjavaytg.services.EvaluationService;
import ytg.projetjavaytg.services.PdfGenerationService;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(EvaluationViewController.class)
class EvaluationViewControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private EvaluationService evaluationService;

    @MockitoBean
    private ApprentiService apprentiService;

    @MockitoBean
    private PdfGenerationService pdfGenerationService;

    @MockitoBean
    private VisiteRepository visiteRepository;

    @MockitoBean
    private CustomUserDetailsService customUserDetailsService;

    private Apprenti apprentiAvecEntreprise() {
        Apprenti apprenti = new Apprenti();
        apprenti.setId(1L);
        apprenti.setNom("Dupont");
        apprenti.setPrenom("Jean");
        apprenti.setProgramme("Informatique");
        apprenti.setMajeure("Dev Web");
        apprenti.setAnneeAcademique("2025-2026");
        apprenti.setNiveau("I1");

        Entreprise entreprise = new Entreprise();
        entreprise.setRaisonSociale("Acme");
        apprenti.setEntreprise(entreprise);
        return apprenti;
    }

    @Test
    @WithMockUser
    void showEvaluationForm_redirigeQuandApprentiNonTrouve() throws Exception {
        when(apprentiService.getApprentiById(99L)).thenReturn(Optional.empty());

        mockMvc.perform(get("/evaluation/apprenti/99"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/dashboard?error=apprenti_not_found"));
    }

    @Test
    @WithMockUser
    void showEvaluationForm_retourne200AvecEvaluationExistante() throws Exception {
        Apprenti apprenti = apprentiAvecEntreprise();
        Evaluation evaluation = new Evaluation();
        evaluation.setId(5L);
        evaluation.setMemoireTheme("Sujet");

        when(apprentiService.getApprentiById(1L)).thenReturn(Optional.of(apprenti));
        when(evaluationService.getEvaluationByApprentiId(1L)).thenReturn(Optional.of(evaluation));

        mockMvc.perform(get("/evaluation/apprenti/1"))
                .andExpect(status().isOk())
                .andExpect(view().name("evaluation/form"))
                .andExpect(model().attribute("apprenti", apprenti))
                .andExpect(model().attribute("evaluation", evaluation))
                .andExpect(model().attribute("isNew", false));
    }

    @Test
    @WithMockUser
    void showEvaluationForm_creeNouvelleEvaluationSansVisite() throws Exception {
        Apprenti apprenti = apprentiAvecEntreprise();

        when(apprentiService.getApprentiById(1L)).thenReturn(Optional.of(apprenti));
        when(evaluationService.getEvaluationByApprentiId(1L)).thenReturn(Optional.empty());
        when(visiteRepository.findByApprentiIdOrderByDateVisiteDesc(1L)).thenReturn(List.of());

        mockMvc.perform(get("/evaluation/apprenti/1"))
                .andExpect(status().isOk())
                .andExpect(view().name("evaluation/form"))
                .andExpect(model().attribute("isNew", true))
                .andExpect(model().attributeDoesNotExist("visiteInfo"));
    }

    @Test
    @WithMockUser
    void showEvaluationForm_preremplitDateDepuisDerniereVisite() throws Exception {
        Apprenti apprenti = apprentiAvecEntreprise();

        Visite visite = new Visite();
        visite.setDateVisite(LocalDate.of(2026, 6, 15));
        visite.setFormat("Visio");

        when(apprentiService.getApprentiById(1L)).thenReturn(Optional.of(apprenti));
        when(evaluationService.getEvaluationByApprentiId(1L)).thenReturn(Optional.empty());
        when(visiteRepository.findByApprentiIdOrderByDateVisiteDesc(1L)).thenReturn(List.of(visite));

        mockMvc.perform(get("/evaluation/apprenti/1"))
                .andExpect(status().isOk())
                .andExpect(view().name("evaluation/form"))
                .andExpect(model().attribute("isNew", true))
                .andExpect(model().attributeExists("visiteInfo"));
    }

    @Test
    @WithMockUser
    void viewEvaluation_redirigeQuandApprentiNonTrouve() throws Exception {
        when(apprentiService.getApprentiById(99L)).thenReturn(Optional.empty());

        mockMvc.perform(get("/evaluation/view/99"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/dashboard?error=apprenti_not_found"));
    }

    @Test
    @WithMockUser
    void viewEvaluation_redirigeQuandEvaluationNonTrouvee() throws Exception {
        Apprenti apprenti = apprentiAvecEntreprise();
        when(apprentiService.getApprentiById(1L)).thenReturn(Optional.of(apprenti));
        when(evaluationService.getEvaluationByApprentiId(1L)).thenReturn(Optional.empty());

        mockMvc.perform(get("/evaluation/view/1"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/apprentis/1?error=no_evaluation"));
    }

    @Test
    @WithMockUser
    void viewEvaluation_retourne200QuandTrouvee() throws Exception {
        Apprenti apprenti = apprentiAvecEntreprise();
        Evaluation evaluation = new Evaluation();
        evaluation.setId(5L);
        evaluation.setMemoireTheme("Sujet");
        evaluation.setSoutenanceDate(LocalDate.of(2026, 6, 15));
        evaluation.setMemoireNote(BigDecimal.valueOf(15));
        evaluation.setSoutenanceNote(BigDecimal.valueOf(16));

        when(apprentiService.getApprentiById(1L)).thenReturn(Optional.of(apprenti));
        when(evaluationService.getEvaluationByApprentiId(1L)).thenReturn(Optional.of(evaluation));

        mockMvc.perform(get("/evaluation/view/1"))
                .andExpect(status().isOk())
                .andExpect(view().name("evaluation/view"))
                .andExpect(model().attribute("apprenti", apprenti))
                .andExpect(model().attribute("evaluation", evaluation))
                .andExpect(model().attribute("readOnly", true));
    }

    @Test
    @WithMockUser
    void saveEvaluation_redirigeAvecErreurQuandApprentiNonTrouve() throws Exception {
        when(apprentiService.getApprentiById(99L)).thenReturn(Optional.empty());

        mockMvc.perform(post("/evaluation/save")
                        .with(csrf())
                        .param("apprentiId", "99")
                        .param("memoireTheme", "Sujet"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/dashboard"))
                .andExpect(flash().attribute("error", "Apprenti non trouvé"));
    }

    @Test
    @WithMockUser
    void saveEvaluation_creeEvaluationQuandPasIdFourni() throws Exception {
        Apprenti apprenti = apprentiAvecEntreprise();
        when(apprentiService.getApprentiById(1L)).thenReturn(Optional.of(apprenti));
        when(evaluationService.createEvaluation(any(Evaluation.class))).thenReturn(new Evaluation());

        mockMvc.perform(post("/evaluation/save")
                        .with(csrf())
                        .param("apprentiId", "1")
                        .param("memoireTheme", "Sujet"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/evaluation/apprenti/1"))
                .andExpect(flash().attribute("success", "Évaluation créée avec succès !"));

        verify(evaluationService).createEvaluation(any(Evaluation.class));
    }

    @Test
    @WithMockUser
    void saveEvaluation_metAJourEvaluationQuandIdFourni() throws Exception {
        Apprenti apprenti = apprentiAvecEntreprise();
        when(apprentiService.getApprentiById(1L)).thenReturn(Optional.of(apprenti));
        when(evaluationService.updateEvaluation(eq(5L), any(Evaluation.class))).thenReturn(new Evaluation());

        mockMvc.perform(post("/evaluation/save")
                        .with(csrf())
                        .param("apprentiId", "1")
                        .param("evaluationId", "5")
                        .param("memoireTheme", "Sujet"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/evaluation/apprenti/1"))
                .andExpect(flash().attribute("success", "Évaluation mise à jour avec succès !"));

        verify(evaluationService).updateEvaluation(eq(5L), any(Evaluation.class));
    }

    @Test
    @WithMockUser
    void saveEvaluation_redirigeAvecErreurQuandException() throws Exception {
        Apprenti apprenti = apprentiAvecEntreprise();
        when(apprentiService.getApprentiById(1L)).thenReturn(Optional.of(apprenti));
        when(evaluationService.createEvaluation(any(Evaluation.class))).thenThrow(new RuntimeException("Erreur BDD"));

        mockMvc.perform(post("/evaluation/save")
                        .with(csrf())
                        .param("apprentiId", "1")
                        .param("memoireTheme", "Sujet"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/evaluation/apprenti/1"))
                .andExpect(flash().attribute("error", "Erreur lors de l'enregistrement : Erreur BDD"));
    }

    @Test
    @WithMockUser
    void downloadEvaluationPdf_retourne404QuandApprentiOuEvaluationNonTrouve() throws Exception {
        when(apprentiService.getApprentiById(99L)).thenReturn(Optional.empty());
        when(evaluationService.getEvaluationByApprentiId(99L)).thenReturn(Optional.empty());

        mockMvc.perform(get("/evaluation/download/99"))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser
    void downloadEvaluationPdf_retourne200AvecPdf() throws Exception {
        Apprenti apprenti = apprentiAvecEntreprise();
        Evaluation evaluation = new Evaluation();
        evaluation.setId(5L);

        when(apprentiService.getApprentiById(1L)).thenReturn(Optional.of(apprenti));
        when(evaluationService.getEvaluationByApprentiId(1L)).thenReturn(Optional.of(evaluation));
        when(pdfGenerationService.generateEvaluationPdf(evaluation, apprenti)).thenReturn("%PDF-1.4".getBytes());

        mockMvc.perform(get("/evaluation/download/1"))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/pdf"))
                .andExpect(header().string("Content-Disposition", org.hamcrest.Matchers.containsString("Evaluation_Dupont_Jean.pdf")));
    }

    @Test
    @WithMockUser
    void downloadEvaluationPdf_retourne500QuandException() throws Exception {
        Apprenti apprenti = apprentiAvecEntreprise();
        Evaluation evaluation = new Evaluation();
        evaluation.setId(5L);

        when(apprentiService.getApprentiById(1L)).thenReturn(Optional.of(apprenti));
        when(evaluationService.getEvaluationByApprentiId(1L)).thenReturn(Optional.of(evaluation));
        when(pdfGenerationService.generateEvaluationPdf(evaluation, apprenti)).thenThrow(new RuntimeException("Erreur PDF"));

        mockMvc.perform(get("/evaluation/download/1"))
                .andExpect(status().isInternalServerError());
    }

    @Test
    void showEvaluationForm_retourne401SansAuthentification() throws Exception {
        mockMvc.perform(get("/evaluation/apprenti/1"))
                .andExpect(status().isUnauthorized());
    }
}
