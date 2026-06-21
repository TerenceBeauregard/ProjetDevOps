package ytg.projetjavaytg.controllers;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import ytg.projetjavaytg.models.Entreprise;
import ytg.projetjavaytg.security.CustomUserDetailsService;
import ytg.projetjavaytg.services.EntrepriseService;

import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(EntrepriseViewController.class)
class EntrepriseViewControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private EntrepriseService entrepriseService;

    @MockitoBean
    private CustomUserDetailsService customUserDetailsService;

    @Test
    @WithMockUser
    void list_retourne200AvecListeEntreprises() throws Exception {
        Entreprise entreprise = new Entreprise();
        entreprise.setId(1L);
        entreprise.setRaisonSociale("Acme");
        when(entrepriseService.getAllEntreprises()).thenReturn(List.of(entreprise));

        mockMvc.perform(get("/entreprises"))
                .andExpect(status().isOk())
                .andExpect(view().name("entreprise/list"))
                .andExpect(model().attribute("entreprises", List.of(entreprise)));
    }

    @Test
    @WithMockUser
    void createForm_retourne200() throws Exception {
        mockMvc.perform(get("/entreprises/create"))
                .andExpect(status().isOk())
                .andExpect(view().name("entreprise/create"));
    }

    @Test
    @WithMockUser
    void createEntreprise_redirigeVersListeQuandSucces() throws Exception {
        when(entrepriseService.createEntreprise(any(Entreprise.class))).thenReturn(new Entreprise());

        mockMvc.perform(post("/entreprises/create")
                        .with(csrf())
                        .param("raisonSociale", "Acme")
                        .param("adresse", "1 rue de Paris")
                        .param("informationsAcces", "Badge requis"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/entreprises"))
                .andExpect(flash().attribute("success", "L'entreprise a été créée avec succès !"));
    }

    @Test
    @WithMockUser
    void createEntreprise_redirigeVersFormulaireQuandErreur() throws Exception {
        when(entrepriseService.createEntreprise(any(Entreprise.class)))
                .thenThrow(new RuntimeException("Erreur BDD"));

        mockMvc.perform(post("/entreprises/create")
                        .with(csrf())
                        .param("raisonSociale", "Acme"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/entreprises/create"))
                .andExpect(flash().attribute("error", "Erreur lors de la création : Erreur BDD"));
    }

    @Test
    @WithMockUser
    void deleteEntreprise_redirigeAvecSuccesQuandTrouvee() throws Exception {
        Entreprise entreprise = new Entreprise();
        entreprise.setId(1L);
        when(entrepriseService.getEntrepriseById(1L)).thenReturn(Optional.of(entreprise));
        doNothing().when(entrepriseService).deleteEntreprise(1L);

        mockMvc.perform(post("/entreprises/1/delete").with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/entreprises"))
                .andExpect(flash().attribute("success", "L'entreprise a été supprimée avec succès !"));

        verify(entrepriseService).deleteEntreprise(1L);
    }

    @Test
    @WithMockUser
    void deleteEntreprise_redirigeAvecSuccesQuandNonTrouvee() throws Exception {
        when(entrepriseService.getEntrepriseById(99L)).thenReturn(Optional.empty());

        mockMvc.perform(post("/entreprises/99/delete").with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/entreprises"))
                .andExpect(flash().attribute("success", "L'entreprise a été supprimée avec succès !"));

        verify(entrepriseService, never()).deleteEntreprise(99L);
    }

    @Test
    @WithMockUser
    void deleteEntreprise_gereDataIntegrityViolationException() throws Exception {
        Entreprise entreprise = new Entreprise();
        entreprise.setId(1L);
        when(entrepriseService.getEntrepriseById(1L)).thenReturn(Optional.of(entreprise));
        doThrow(new DataIntegrityViolationException("contrainte")).when(entrepriseService).deleteEntreprise(1L);

        mockMvc.perform(post("/entreprises/1/delete").with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/entreprises"))
                .andExpect(flash().attribute("error",
                        "Impossible de supprimer cette entreprise. Elle est associée à un ou plusieurs apprentis. Veuillez d'abord modifier ou supprimer ces apprentis."));
    }

    @Test
    void list_retourne401SansAuthentification() throws Exception {
        mockMvc.perform(get("/entreprises"))
                .andExpect(status().isUnauthorized());
    }
}
