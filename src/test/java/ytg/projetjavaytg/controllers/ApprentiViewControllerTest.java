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
import ytg.projetjavaytg.models.MaitreApprentissage;
import ytg.projetjavaytg.models.Utilisateur;
import ytg.projetjavaytg.security.CustomUserDetailsService;
import ytg.projetjavaytg.services.*;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ApprentiViewController.class)
class ApprentiViewControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ApprentiService apprentiService;

    @MockitoBean
    private EntrepriseService entrepriseService;

    @MockitoBean
    private MaitreApprentissageService maitreApprentissageService;

    @MockitoBean
    private UtilisateurService utilisateurService;

    @MockitoBean
    private AnneeAcademiqueService anneeAcademiqueService;

    @MockitoBean
    private EvaluationService evaluationService;

    @MockitoBean
    private CustomUserDetailsService customUserDetailsService;

    private Apprenti apprentiComplet() {
        Apprenti apprenti = new Apprenti();
        apprenti.setId(1L);
        apprenti.setNom("Dupont");
        apprenti.setPrenom("Jean");
        apprenti.setEmail("jean.dupont@mail.com");
        apprenti.setAnneeAcademique("2025-2026");
        apprenti.setNiveau("I1");
        apprenti.setDateCreation(Instant.now());
        apprenti.setDateModification(Instant.now());

        Entreprise entreprise = new Entreprise();
        entreprise.setId(1L);
        entreprise.setRaisonSociale("Acme");
        apprenti.setEntreprise(entreprise);

        MaitreApprentissage maitre = new MaitreApprentissage();
        maitre.setId(2L);
        maitre.setNom("Durand");
        maitre.setPrenom("Paul");
        maitre.setEntreprise(entreprise);
        apprenti.setMaitreApprentissage(maitre);

        Utilisateur tuteur = new Utilisateur();
        tuteur.setId(3L);
        tuteur.setNom("Curie");
        tuteur.setPrenom("Marie");
        apprenti.setTuteurEnseignant(tuteur);

        return apprenti;
    }

    @Test
    @WithMockUser
    void createApprentiForm_retourne200AvecListesNecessaires() throws Exception {
        when(anneeAcademiqueService.getAnneeAcademiqueEnCours()).thenReturn("2025-2026");
        when(entrepriseService.getAllEntreprises()).thenReturn(List.of());
        when(maitreApprentissageService.getAllMaitresApprentissage()).thenReturn(List.of());
        when(utilisateurService.getAllUtilisateurs()).thenReturn(List.of());
        when(anneeAcademiqueService.getAllAnnees()).thenReturn(List.of("2025-2026"));

        mockMvc.perform(get("/apprentis/create"))
                .andExpect(status().isOk())
                .andExpect(view().name("apprentice/createapprentice"))
                .andExpect(model().attribute("anneeAcademique", "2025-2026"))
                .andExpect(model().attributeExists("entreprises", "maitres", "tuteurs", "annees"));
    }

    @Test
    @WithMockUser
    void createApprenti_redirigeVersDashboardEtResoutRelations() throws Exception {
        Entreprise entreprise = new Entreprise();
        entreprise.setId(1L);
        when(entrepriseService.getEntrepriseById(1L)).thenReturn(Optional.of(entreprise));

        MaitreApprentissage maitre = new MaitreApprentissage();
        maitre.setId(2L);
        when(maitreApprentissageService.getMaitreApprentissageById(2L)).thenReturn(Optional.of(maitre));

        Utilisateur tuteur = new Utilisateur();
        tuteur.setId(3L);
        when(utilisateurService.getUtilisateurById(3L)).thenReturn(Optional.of(tuteur));

        when(apprentiService.createApprenti(any(Apprenti.class))).thenReturn(new Apprenti());

        mockMvc.perform(post("/apprentis/create")
                        .with(csrf())
                        .param("nom", "Martin")
                        .param("prenom", "Sophie")
                        .param("email", "sophie.martin@mail.com")
                        .param("anneeAcademique", "2025-2026")
                        .param("niveau", "I1")
                        .param("entrepriseId", "1")
                        .param("maitreApprentissageId", "2")
                        .param("tuteurEnseignantId", "3"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/dashboard"))
                .andExpect(flash().attribute("success", "L'apprenti a été créé avec succès !"));

        verify(entrepriseService).getEntrepriseById(1L);
        verify(maitreApprentissageService).getMaitreApprentissageById(2L);
        verify(utilisateurService).getUtilisateurById(3L);
        verify(apprentiService).createApprenti(any(Apprenti.class));
    }

    @Test
    @WithMockUser
    void createApprenti_redirigeVersFormulaireQuandErreur() throws Exception {
        when(entrepriseService.getEntrepriseById(1L)).thenReturn(Optional.empty());
        when(maitreApprentissageService.getMaitreApprentissageById(2L)).thenReturn(Optional.empty());
        when(utilisateurService.getUtilisateurById(3L)).thenReturn(Optional.empty());
        when(apprentiService.createApprenti(any(Apprenti.class))).thenThrow(new RuntimeException("Erreur BDD"));

        mockMvc.perform(post("/apprentis/create")
                        .with(csrf())
                        .param("nom", "Martin")
                        .param("prenom", "Sophie")
                        .param("entrepriseId", "1")
                        .param("maitreApprentissageId", "2")
                        .param("tuteurEnseignantId", "3"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/apprentis/create"))
                .andExpect(flash().attribute("error", "Erreur lors de la création de l'apprenti : Erreur BDD"));
    }

    @Test
    @WithMockUser
    void apprentiDetails_retourne200QuandTrouveAvecEvaluation() throws Exception {
        Apprenti apprenti = apprentiComplet();
        when(apprentiService.getApprentiById(1L)).thenReturn(Optional.of(apprenti));
        when(evaluationService.getEvaluationByApprentiId(1L)).thenReturn(Optional.of(new Evaluation()));

        mockMvc.perform(get("/apprentis/1"))
                .andExpect(status().isOk())
                .andExpect(view().name("apprentice/details"))
                .andExpect(model().attribute("apprenti", apprenti))
                .andExpect(model().attribute("hasEvaluation", true));
    }

    @Test
    @WithMockUser
    void apprentiDetails_retourne200QuandTrouveSansEvaluation() throws Exception {
        Apprenti apprenti = apprentiComplet();
        when(apprentiService.getApprentiById(1L)).thenReturn(Optional.of(apprenti));
        when(evaluationService.getEvaluationByApprentiId(1L)).thenReturn(Optional.empty());

        mockMvc.perform(get("/apprentis/1"))
                .andExpect(status().isOk())
                .andExpect(view().name("apprentice/details"))
                .andExpect(model().attribute("hasEvaluation", false));
    }

    @Test
    @WithMockUser
    void apprentiDetails_redirigeVersDashboardQuandNonTrouve() throws Exception {
        when(apprentiService.getApprentiById(99L)).thenReturn(Optional.empty());

        mockMvc.perform(get("/apprentis/99"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/dashboard"));
    }

    @Test
    @WithMockUser
    void editApprentiForm_retourne200QuandTrouve() throws Exception {
        Apprenti apprenti = apprentiComplet();
        when(apprentiService.getApprentiById(1L)).thenReturn(Optional.of(apprenti));
        when(entrepriseService.getAllEntreprises()).thenReturn(List.of(apprenti.getEntreprise()));
        when(maitreApprentissageService.getAllMaitresApprentissage()).thenReturn(List.of(apprenti.getMaitreApprentissage()));
        when(utilisateurService.getAllUtilisateurs()).thenReturn(List.of(apprenti.getTuteurEnseignant()));
        when(anneeAcademiqueService.getAllAnnees()).thenReturn(List.of("2025-2026"));

        mockMvc.perform(get("/apprentis/1/edit"))
                .andExpect(status().isOk())
                .andExpect(view().name("apprentice/edit"))
                .andExpect(model().attribute("apprenti", apprenti))
                .andExpect(model().attributeExists("entreprises", "maitres", "tuteurs", "annees"));
    }

    @Test
    @WithMockUser
    void editApprentiForm_redirigeVersDashboardQuandNonTrouve() throws Exception {
        when(apprentiService.getApprentiById(99L)).thenReturn(Optional.empty());

        mockMvc.perform(get("/apprentis/99/edit"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/dashboard"));
    }

    @Test
    @WithMockUser
    void editApprenti_redirigeVersDashboardQuandSucces() throws Exception {
        when(entrepriseService.getEntrepriseById(1L)).thenReturn(Optional.of(new Entreprise()));
        when(maitreApprentissageService.getMaitreApprentissageById(2L)).thenReturn(Optional.of(new MaitreApprentissage()));
        when(utilisateurService.getUtilisateurById(3L)).thenReturn(Optional.of(new Utilisateur()));
        when(apprentiService.updateApprenti(eq(1L), any(Apprenti.class))).thenReturn(new Apprenti());

        mockMvc.perform(post("/apprentis/1/edit")
                        .with(csrf())
                        .param("nom", "Dupont")
                        .param("prenom", "Jean")
                        .param("entrepriseId", "1")
                        .param("maitreApprentissageId", "2")
                        .param("tuteurEnseignantId", "3"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/dashboard"))
                .andExpect(flash().attribute("success", "L'apprenti a été modifié avec succès !"));
    }

    @Test
    @WithMockUser
    void editApprenti_redirigeVersDashboardAvecErreurQuandApprentiNonTrouve() throws Exception {
        when(entrepriseService.getEntrepriseById(1L)).thenReturn(Optional.of(new Entreprise()));
        when(maitreApprentissageService.getMaitreApprentissageById(2L)).thenReturn(Optional.of(new MaitreApprentissage()));
        when(utilisateurService.getUtilisateurById(3L)).thenReturn(Optional.of(new Utilisateur()));
        when(apprentiService.updateApprenti(eq(99L), any(Apprenti.class))).thenReturn(null);

        mockMvc.perform(post("/apprentis/99/edit")
                        .with(csrf())
                        .param("nom", "Dupont")
                        .param("prenom", "Jean")
                        .param("entrepriseId", "1")
                        .param("maitreApprentissageId", "2")
                        .param("tuteurEnseignantId", "3"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/dashboard"))
                .andExpect(flash().attribute("error", "Apprenti non trouvé"));
    }

    @Test
    @WithMockUser
    void editApprenti_redirigeVersFormulaireEditionQuandException() throws Exception {
        when(entrepriseService.getEntrepriseById(1L)).thenReturn(Optional.of(new Entreprise()));
        when(maitreApprentissageService.getMaitreApprentissageById(2L)).thenReturn(Optional.of(new MaitreApprentissage()));
        when(utilisateurService.getUtilisateurById(3L)).thenReturn(Optional.of(new Utilisateur()));
        when(apprentiService.updateApprenti(eq(1L), any(Apprenti.class))).thenThrow(new RuntimeException("Erreur BDD"));

        mockMvc.perform(post("/apprentis/1/edit")
                        .with(csrf())
                        .param("nom", "Dupont")
                        .param("prenom", "Jean")
                        .param("entrepriseId", "1")
                        .param("maitreApprentissageId", "2")
                        .param("tuteurEnseignantId", "3"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/apprentis/1/edit"))
                .andExpect(flash().attribute("error", "Erreur lors de la modification : Erreur BDD"));
    }

    @Test
    @WithMockUser
    void deleteApprenti_redirigeAvecSuccesQuandSupprime() throws Exception {
        doNothing().when(apprentiService).deleteApprenti(1L);

        mockMvc.perform(delete("/apprentis/1").with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/dashboard"))
                .andExpect(flash().attribute("success", "L'apprenti a été supprimé avec succès !"));
    }

    @Test
    @WithMockUser
    void deleteApprenti_redirigeAvecErreurQuandException() throws Exception {
        doThrow(new RuntimeException("Erreur BDD")).when(apprentiService).deleteApprenti(99L);

        mockMvc.perform(delete("/apprentis/99").with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/dashboard"))
                .andExpect(flash().attribute("error", "Erreur lors de la suppression : Erreur BDD"));
    }

    @Test
    void createApprentiForm_retourne401SansAuthentification() throws Exception {
        mockMvc.perform(get("/apprentis/create"))
                .andExpect(status().isUnauthorized());
    }
}
