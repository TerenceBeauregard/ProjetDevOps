package ytg.projetjavaytg.controllers;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import ytg.projetjavaytg.models.Apprenti;
import ytg.projetjavaytg.models.Visite;
import ytg.projetjavaytg.security.CustomUserDetailsService;
import ytg.projetjavaytg.services.ApprentiService;
import ytg.projetjavaytg.services.VisiteService;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(VisiteViewController.class)
class VisiteViewControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private VisiteService visiteService;

    @MockitoBean
    private ApprentiService apprentiService;

    @MockitoBean
    private CustomUserDetailsService customUserDetailsService;

    @Test
    @WithMockUser
    void monthlyView_retourne200AvecCalendrierDuMoisCourant() throws Exception {
        Visite visite = new Visite();
        visite.setDateVisite(LocalDate.now());
        visite.setFormat("Visio");
        when(visiteService.getAllVisites()).thenReturn(List.of(visite));
        when(apprentiService.getAllApprentis()).thenReturn(List.of());

        mockMvc.perform(get("/visites"))
                .andExpect(status().isOk())
                .andExpect(view().name("visite/monthly"))
                .andExpect(model().attribute("month", 0))
                .andExpect(model().attributeExists("calendar"))
                .andExpect(model().attributeExists("currentMonthLabel"));
    }

    @Test
    @WithMockUser
    void monthlyView_avecOffsetRetourneMoisDecale() throws Exception {
        when(visiteService.getAllVisites()).thenReturn(List.of());
        when(apprentiService.getAllApprentis()).thenReturn(List.of());

        mockMvc.perform(get("/visites").param("month", "1"))
                .andExpect(status().isOk())
                .andExpect(view().name("visite/monthly"))
                .andExpect(model().attribute("month", 1));
    }

    @Test
    @WithMockUser
    void createVisite_redirigeAvecSuccesQuandApprentiTrouve() throws Exception {
        Apprenti apprenti = new Apprenti();
        apprenti.setId(1L);
        when(apprentiService.getApprentiById(1L)).thenReturn(Optional.of(apprenti));
        when(visiteService.createVisite(any(Visite.class))).thenReturn(new Visite());

        mockMvc.perform(post("/visites/create")
                        .with(csrf())
                        .param("apprentiId", "1")
                        .param("dateVisite", "2026-06-20")
                        .param("format", "Présentiel")
                        .param("commentaires", "RAS"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/visites"))
                .andExpect(flash().attribute("success", "Visite créée"));

        verify(visiteService).createVisite(any(Visite.class));
    }

    @Test
    @WithMockUser
    void createVisite_redirigeAvecMoisQuandFourni() throws Exception {
        Apprenti apprenti = new Apprenti();
        apprenti.setId(1L);
        when(apprentiService.getApprentiById(1L)).thenReturn(Optional.of(apprenti));
        when(visiteService.createVisite(any(Visite.class))).thenReturn(new Visite());

        mockMvc.perform(post("/visites/create")
                        .with(csrf())
                        .param("apprentiId", "1")
                        .param("dateVisite", "2026-06-20")
                        .param("month", "2"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/visites?month=2"));
    }

    @Test
    @WithMockUser
    void createVisite_redirigeAvecErreurQuandApprentiIntrouvable() throws Exception {
        when(apprentiService.getApprentiById(99L)).thenReturn(Optional.empty());

        mockMvc.perform(post("/visites/create")
                        .with(csrf())
                        .param("apprentiId", "99")
                        .param("dateVisite", "2026-06-20"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/visites"))
                .andExpect(flash().attribute("error", "Apprenti introuvable"));

        verify(visiteService, never()).createVisite(any(Visite.class));
    }

    @Test
    @WithMockUser
    void createVisite_redirigeAvecErreurQuandExceptionPendantLaCreation() throws Exception {
        Apprenti apprenti = new Apprenti();
        apprenti.setId(1L);
        when(apprentiService.getApprentiById(1L)).thenReturn(Optional.of(apprenti));
        when(visiteService.createVisite(any(Visite.class))).thenThrow(new RuntimeException("Erreur BDD"));

        mockMvc.perform(post("/visites/create")
                        .with(csrf())
                        .param("apprentiId", "1")
                        .param("dateVisite", "2026-06-20"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/visites"))
                .andExpect(flash().attribute("error", "Erreur lors de la création : Erreur BDD"));
    }

    @Test
    void monthlyView_retourne401SansAuthentification() throws Exception {
        mockMvc.perform(get("/visites"))
                .andExpect(status().isUnauthorized());
    }
}
