package ytg.projetjavaytg.controllers;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import ytg.projetjavaytg.models.Apprenti;
import ytg.projetjavaytg.security.CustomUserDetailsService;
import ytg.projetjavaytg.services.AnneeAcademiqueService;
import ytg.projetjavaytg.services.ApprentiService;

import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(DashboardController.class)
class DashboardControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ApprentiService apprentiService;

    @MockBean
    private AnneeAcademiqueService anneeAcademiqueService;

    @MockBean
    private CustomUserDetailsService customUserDetailsService;

    @Test
    @WithMockUser
    void dashboard_retourne200AvecApprentisDeLAnneeEnCours() throws Exception {
        when(anneeAcademiqueService.getAnneeAcademiqueEnCours()).thenReturn("2025-2026");

        Apprenti apprentiAnneeCourante = new Apprenti();
        apprentiAnneeCourante.setId(1L);
        apprentiAnneeCourante.setNom("Dupont");
        apprentiAnneeCourante.setAnneeAcademique("2025-2026");

        Apprenti apprentiAutreAnnee = new Apprenti();
        apprentiAutreAnnee.setId(2L);
        apprentiAutreAnnee.setNom("Martin");
        apprentiAutreAnnee.setAnneeAcademique("2024-2025");

        Apprenti apprentiSansAnnee = new Apprenti();
        apprentiSansAnnee.setId(3L);
        apprentiSansAnnee.setNom("Durand");
        apprentiSansAnnee.setAnneeAcademique(null);

        when(apprentiService.getApprentisNonArchives())
                .thenReturn(List.of(apprentiAnneeCourante, apprentiAutreAnnee, apprentiSansAnnee));

        mockMvc.perform(get("/dashboard"))
                .andExpect(status().isOk())
                .andExpect(view().name("dashboard"))
                .andExpect(model().attribute("anneeAcademique", "2025-2026"))
                .andExpect(model().attribute("apprentis", List.of(apprentiAnneeCourante)));
    }

    @Test
    void dashboard_retourne401SansAuthentification() throws Exception {
        mockMvc.perform(get("/dashboard"))
                .andExpect(status().isUnauthorized());
    }
}
