package ytg.projetjavaytg.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import ytg.projetjavaytg.controllers.api.MaitreApprentissageController;
import ytg.projetjavaytg.dto.MaitreApprentissageDTO;
import ytg.projetjavaytg.models.Entreprise;
import ytg.projetjavaytg.models.MaitreApprentissage;
import ytg.projetjavaytg.security.CustomUserDetailsService;
import ytg.projetjavaytg.services.EntrepriseService;
import ytg.projetjavaytg.services.MaitreApprentissageService;
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

@WebMvcTest(MaitreApprentissageController.class)
class MaitreApprentissageControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private MaitreApprentissageService maitreApprentissageService;

    @MockBean
    private EntrepriseService entrepriseService;

    @MockBean
    private CustomUserDetailsService customUserDetailsService;

    @Test
    @WithMockUser
    void getAllMaitresApprentissage_retourne200AvecListe() throws Exception {
        MaitreApprentissage maitre = new MaitreApprentissage();
        maitre.setId(1L);
        maitre.setNom("Durand");
        when(maitreApprentissageService.getAllMaitresApprentissage()).thenReturn(List.of(maitre));

        mockMvc.perform(get("/api/maitreapprentissages"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$[0].nom").value("Durand"));
    }

    @Test
    @WithMockUser
    void getAllMaitresApprentissage_retourne500QuandAucunMaitre() throws Exception {
        when(maitreApprentissageService.getAllMaitresApprentissage())
                .thenThrow(new RuntimeException("Aucun maitre d'apprentissage en base"));

        mockMvc.perform(get("/api/maitreapprentissages"))
                .andExpect(status().isInternalServerError());
    }

    @Test
    @WithMockUser
    void getMaitreApprentissageById_retourne200QuandTrouve() throws Exception {
        MaitreApprentissage maitre = new MaitreApprentissage();
        maitre.setId(1L);
        maitre.setNom("Durand");
        when(maitreApprentissageService.getMaitreApprentissageById(1L)).thenReturn(Optional.of(maitre));

        mockMvc.perform(get("/api/maitreapprentissages/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nom").value("Durand"));
    }

    @Test
    @WithMockUser
    void getMaitreApprentissageById_retourne404QuandNonTrouve() throws Exception {
        when(maitreApprentissageService.getMaitreApprentissageById(99L))
                .thenThrow(new ResourceNotFoundException("Aucun apprenti trouvé avec l'id 99"));

        mockMvc.perform(get("/api/maitreapprentissages/99"))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser
    void createMaitreApprentissage_retourne201AvecMaitreCree() throws Exception {
        Entreprise entreprise = new Entreprise();
        entreprise.setId(1L);
        entreprise.setRaisonSociale("Acme");
        when(entrepriseService.getEntrepriseById(1L)).thenReturn(Optional.of(entreprise));

        MaitreApprentissage maitre = new MaitreApprentissage();
        maitre.setId(1L);
        maitre.setNom("Durand");
        maitre.setPrenom("Paul");
        when(maitreApprentissageService.createMaitreApprentissage(any(MaitreApprentissage.class))).thenReturn(maitre);

        MaitreApprentissageDTO dto = new MaitreApprentissageDTO();
        dto.setNom("Durand");
        dto.setPrenom("Paul");
        dto.setEntrepriseId(1L);

        mockMvc.perform(post("/api/maitreapprentissages")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.nom").value("Durand"));
    }

    @Test
    @WithMockUser
    void createMaitreApprentissage_retourne404SiEntrepriseNonTrouvee() throws Exception {
        when(entrepriseService.getEntrepriseById(99L)).thenReturn(Optional.empty());

        MaitreApprentissageDTO dto = new MaitreApprentissageDTO();
        dto.setNom("Durand");
        dto.setPrenom("Paul");
        dto.setEntrepriseId(99L);

        mockMvc.perform(post("/api/maitreapprentissages")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser
    void deleteMaitreApprentissage_retourne204QuandSupprime() throws Exception {
        doNothing().when(maitreApprentissageService).deleteMaitreApprentissage(1L);

        mockMvc.perform(delete("/api/maitreapprentissages/1").with(csrf()))
                .andExpect(status().isNoContent());
    }

    @Test
    @WithMockUser
    void deleteMaitreApprentissage_retourne404SiNonTrouve() throws Exception {
        doThrow(new ResourceNotFoundException("Le maitre d'apprentissage que vous voulez supprimer n'existe pas"))
                .when(maitreApprentissageService).deleteMaitreApprentissage(99L);

        mockMvc.perform(delete("/api/maitreapprentissages/99").with(csrf()))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser
    void getByRaisonSociale_retourne200AvecListe() throws Exception {
        MaitreApprentissage maitre = new MaitreApprentissage();
        maitre.setId(1L);
        maitre.setNom("Durand");
        when(maitreApprentissageService.findAllMaitresApprentissage("Acme")).thenReturn(List.of(maitre));

        mockMvc.perform(get("/api/maitreapprentissages/by-raison-sociale/Acme"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].nom").value("Durand"));
    }

    @Test
    @WithMockUser
    void getByRaisonSociale_retourne404QuandAucunMaitre() throws Exception {
        when(maitreApprentissageService.findAllMaitresApprentissage("Inconnue"))
                .thenThrow(new ResourceNotFoundException("Aucun maitre d'apprentissage trouvé pour la raison sociale Inconnue"));

        mockMvc.perform(get("/api/maitreapprentissages/by-raison-sociale/Inconnue"))
                .andExpect(status().isNotFound());
    }

    @Test
    void getAllMaitresApprentissage_retourne401SansAuthentification() throws Exception {
        mockMvc.perform(get("/api/maitreapprentissages"))
                .andExpect(status().isUnauthorized());
    }
}
