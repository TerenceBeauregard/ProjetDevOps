package ytg.projetjavaytg.controllers;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import ytg.projetjavaytg.models.Entreprise;
import ytg.projetjavaytg.models.MaitreApprentissage;
import ytg.projetjavaytg.security.CustomUserDetailsService;
import ytg.projetjavaytg.services.EntrepriseService;
import ytg.projetjavaytg.services.MaitreApprentissageService;

import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(MaitreApprentissageViewController.class)
class MaitreApprentissageViewControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private MaitreApprentissageService maitreApprentissageService;

    @MockitoBean
    private EntrepriseService entrepriseService;

    @MockitoBean
    private CustomUserDetailsService customUserDetailsService;

    @Test
    @WithMockUser
    void list_retourne200AvecListeMaitres() throws Exception {
        Entreprise entreprise = new Entreprise();
        entreprise.setId(1L);
        entreprise.setRaisonSociale("Acme");

        MaitreApprentissage maitre = new MaitreApprentissage();
        maitre.setId(1L);
        maitre.setNom("Durand");
        maitre.setEntreprise(entreprise);
        when(maitreApprentissageService.getAllMaitresApprentissage()).thenReturn(List.of(maitre));

        mockMvc.perform(get("/maitres"))
                .andExpect(status().isOk())
                .andExpect(view().name("maitreapprentissage/list"))
                .andExpect(model().attribute("maitres", List.of(maitre)));
    }

    @Test
    @WithMockUser
    void createForm_retourne200AvecListeEntreprises() throws Exception {
        Entreprise entreprise = new Entreprise();
        entreprise.setId(1L);
        when(entrepriseService.getAllEntreprises()).thenReturn(List.of(entreprise));

        mockMvc.perform(get("/maitres/create"))
                .andExpect(status().isOk())
                .andExpect(view().name("maitreapprentissage/create"))
                .andExpect(model().attribute("entreprises", List.of(entreprise)));
    }

    @Test
    @WithMockUser
    void createMaitre_redirigeVersListeQuandSucces() throws Exception {
        Entreprise entreprise = new Entreprise();
        entreprise.setId(1L);
        when(entrepriseService.getEntrepriseById(1L)).thenReturn(Optional.of(entreprise));
        when(maitreApprentissageService.createMaitreApprentissage(any(MaitreApprentissage.class)))
                .thenReturn(new MaitreApprentissage());

        mockMvc.perform(post("/maitres/create")
                        .with(csrf())
                        .param("nom", "Durand")
                        .param("prenom", "Paul")
                        .param("entrepriseId", "1"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/maitres"))
                .andExpect(flash().attribute("success", "Le maître d'apprentissage a été créé avec succès !"));
    }

    @Test
    @WithMockUser
    void createMaitre_redirigeVersFormulaireQuandErreur() throws Exception {
        when(maitreApprentissageService.createMaitreApprentissage(any(MaitreApprentissage.class)))
                .thenThrow(new RuntimeException("Erreur BDD"));

        mockMvc.perform(post("/maitres/create")
                        .with(csrf())
                        .param("nom", "Durand")
                        .param("prenom", "Paul")
                        .param("entrepriseId", "1"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/maitres/create"))
                .andExpect(flash().attribute("error", "Erreur lors de la création : Erreur BDD"));
    }

    @Test
    @WithMockUser
    void deleteMaitre_redirigeAvecSuccesQuandTrouve() throws Exception {
        MaitreApprentissage maitre = new MaitreApprentissage();
        maitre.setId(1L);
        when(maitreApprentissageService.getMaitreApprentissageById(1L)).thenReturn(Optional.of(maitre));
        doNothing().when(maitreApprentissageService).deleteMaitreApprentissage(1L);

        mockMvc.perform(post("/maitres/1/delete").with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/maitres"))
                .andExpect(flash().attribute("success", "Le maître d'apprentissage a été supprimé avec succès !"));

        verify(maitreApprentissageService).deleteMaitreApprentissage(1L);
    }

    @Test
    @WithMockUser
    void deleteMaitre_gereDataIntegrityViolationException() throws Exception {
        MaitreApprentissage maitre = new MaitreApprentissage();
        maitre.setId(1L);
        when(maitreApprentissageService.getMaitreApprentissageById(1L)).thenReturn(Optional.of(maitre));
        doThrow(new DataIntegrityViolationException("contrainte")).when(maitreApprentissageService).deleteMaitreApprentissage(1L);

        mockMvc.perform(post("/maitres/1/delete").with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/maitres"))
                .andExpect(flash().attribute("error",
                        "Impossible de supprimer ce maître d'apprentissage. Il est associé à un ou plusieurs apprentis. Veuillez d'abord modifier ou supprimer ces apprentis."));
    }

    @Test
    void list_retourne401SansAuthentification() throws Exception {
        mockMvc.perform(get("/maitres"))
                .andExpect(status().isUnauthorized());
    }
}
