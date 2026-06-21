package ytg.projetjavaytg.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import ytg.projetjavaytg.controllers.api.EntrepriseController;
import ytg.projetjavaytg.models.Entreprise;
import ytg.projetjavaytg.security.CustomUserDetailsService;
import ytg.projetjavaytg.services.EntrepriseService;
import ytg.projetjavaytg.exception.ResourceNotFoundException;

import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(EntrepriseController.class)
class EntrepriseControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private EntrepriseService entrepriseService;

    @MockitoBean
    private CustomUserDetailsService customUserDetailsService;

    @Test
    @WithMockUser
    void getAllEntreprises_retourne200AvecListe() throws Exception {
        Entreprise e = new Entreprise();
        e.setId(1L);
        e.setRaisonSociale("Acme Corp");
        when(entrepriseService.getAllEntreprises()).thenReturn(List.of(e));

        mockMvc.perform(get("/api/entreprises"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$[0].raisonSociale").value("Acme Corp"));
    }

    @Test
    @WithMockUser
    void getAllEntreprises_retourne404QuandAucuneEntreprise() throws Exception {
        when(entrepriseService.getAllEntreprises())
                .thenThrow(new ResourceNotFoundException("Aucune entreprise en base"));

        mockMvc.perform(get("/api/entreprises"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Aucune entreprise en base"));
    }

    @Test
    @WithMockUser
    void getEntrepriseById_retourne200QuandTrouvee() throws Exception {
        Entreprise e = new Entreprise();
        e.setId(1L);
        e.setRaisonSociale("Tech Solutions");
        e.setAdresse("12 rue de la Paix");
        when(entrepriseService.getEntrepriseById(1L)).thenReturn(Optional.of(e));

        mockMvc.perform(get("/api/entreprises/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.raisonSociale").value("Tech Solutions"))
                .andExpect(jsonPath("$.adresse").value("12 rue de la Paix"));
    }

    @Test
    @WithMockUser
    void getEntrepriseById_retourne404QuandNonTrouvee() throws Exception {
        when(entrepriseService.getEntrepriseById(99L))
                .thenThrow(new ResourceNotFoundException("Entreprise non trouvé avec id 99"));

        mockMvc.perform(get("/api/entreprises/99"))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser
    void createEntreprise_retourne201AvecEntrepriseCree() throws Exception {
        Entreprise e = new Entreprise();
        e.setId(1L);
        e.setRaisonSociale("Nouvelle Société");
        when(entrepriseService.createEntreprise(any(Entreprise.class))).thenReturn(e);

        mockMvc.perform(post("/api/entreprises")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(e)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.raisonSociale").value("Nouvelle Société"));
    }

    @Test
    @WithMockUser
    void deleteEntreprise_retourne204QuandSupprimee() throws Exception {
        doNothing().when(entrepriseService).deleteEntreprise(1L);

        mockMvc.perform(delete("/api/entreprises/1").with(csrf()))
                .andExpect(status().isNoContent());
    }

    @Test
    @WithMockUser
    void deleteEntreprise_retourne404SiEntrepriseInexistante() throws Exception {
        doThrow(new ResourceNotFoundException("L'entreprise que vous voulez supprimer n'existe pas"))
                .when(entrepriseService).deleteEntreprise(99L);

        mockMvc.perform(delete("/api/entreprises/99").with(csrf()))
                .andExpect(status().isNotFound());
    }

    @Test
    void getAllEntreprises_retourne401SansAuthentification() throws Exception {
        mockMvc.perform(get("/api/entreprises"))
                .andExpect(status().isUnauthorized());
    }
}
