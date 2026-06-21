package ytg.projetjavaytg.controllers;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import ytg.projetjavaytg.models.Apprenti;
import ytg.projetjavaytg.security.CustomUserDetailsService;
import ytg.projetjavaytg.services.AnneeAcademiqueService;
import ytg.projetjavaytg.services.ApprentiService;

import java.util.List;

import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AnneeAcademiqueViewController.class)
class AnneeAcademiqueViewControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ApprentiService apprentiService;

    @MockitoBean
    private AnneeAcademiqueService anneeAcademiqueService;

    @MockitoBean
    private CustomUserDetailsService customUserDetailsService;

    @Test
    @WithMockUser
    void afficherPage_retourne200AvecStatistiquesParNiveau() throws Exception {
        when(anneeAcademiqueService.getAnneeAcademiqueEnCours()).thenReturn("2025-2026");

        Apprenti i1 = new Apprenti();
        i1.setId(1L);
        i1.setNiveau("I1");
        Apprenti i2 = new Apprenti();
        i2.setId(2L);
        i2.setNiveau("I2");
        Apprenti i3a = new Apprenti();
        i3a.setId(3L);
        i3a.setNiveau("I3");
        Apprenti i3b = new Apprenti();
        i3b.setId(4L);
        i3b.setNiveau("I3");
        Apprenti sansNiveau = new Apprenti();
        sansNiveau.setId(5L);
        sansNiveau.setNiveau(null);

        when(apprentiService.getApprentisNonArchives()).thenReturn(List.of(i1, i2, i3a, i3b, sansNiveau));

        mockMvc.perform(get("/annee-academique"))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/yearmanagement"))
                .andExpect(model().attribute("anneeActuelle", "2025-2026"))
                .andExpect(model().attribute("nbI1", 1L))
                .andExpect(model().attribute("nbI2", 1L))
                .andExpect(model().attribute("nbI3", 2L))
                .andExpect(model().attribute("totalApprentis", 5));
    }

    @Test
    @WithMockUser
    void afficherPage_gereExceptionEtRetourneValeursParDefaut() throws Exception {
        when(anneeAcademiqueService.getAnneeAcademiqueEnCours()).thenThrow(new RuntimeException("Erreur BDD"));

        mockMvc.perform(get("/annee-academique"))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/yearmanagement"))
                .andExpect(model().attribute("error", "Erreur lors du chargement de la page : Erreur BDD"))
                .andExpect(model().attribute("nbI1", 0L))
                .andExpect(model().attribute("nbI2", 0L))
                .andExpect(model().attribute("nbI3", 0L))
                .andExpect(model().attribute("totalApprentis", 0L))
                .andExpect(model().attribute("anneeActuelle", ""));
    }

    @Test
    @WithMockUser
    void creerAnneeSuivante_redirigeAvecSuccesQuandOk() throws Exception {
        when(anneeAcademiqueService.calculerAnneeSuivante()).thenReturn("2026-2027");
        doNothing().when(apprentiService).creerNouvelleAnneeAcademique("2026-2027");

        mockMvc.perform(post("/annee-academique/creer-suivante").with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/annee-academique"))
                .andExpect(flash().attribute("success",
                        "Année académique 2026-2027 créée avec succès ! Les apprentis ont été promus et les I3 archivés."));

        verify(apprentiService).creerNouvelleAnneeAcademique("2026-2027");
    }

    @Test
    @WithMockUser
    void creerAnneeSuivante_redirigeAvecErreurQuandException() throws Exception {
        when(anneeAcademiqueService.calculerAnneeSuivante()).thenThrow(new RuntimeException("Erreur calcul"));

        mockMvc.perform(post("/annee-academique/creer-suivante").with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/annee-academique"))
                .andExpect(flash().attribute("error", "Erreur lors de la création de l'année suivante : Erreur calcul"));
    }

    @Test
    void afficherPage_retourne401SansAuthentification() throws Exception {
        mockMvc.perform(get("/annee-academique"))
                .andExpect(status().isUnauthorized());
    }
}
