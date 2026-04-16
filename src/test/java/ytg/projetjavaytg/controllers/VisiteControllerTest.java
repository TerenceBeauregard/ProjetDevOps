package ytg.projetjavaytg.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import ytg.projetjavaytg.Controllers.api.VisiteController;
import ytg.projetjavaytg.DTO.CreateVisiteDTO;
import ytg.projetjavaytg.Models.Apprenti;
import ytg.projetjavaytg.Models.Visite;
import ytg.projetjavaytg.Security.CustomUserDetailsService;
import ytg.projetjavaytg.Services.ApprentiService;
import ytg.projetjavaytg.Services.VisiteService;
import ytg.projetjavaytg.exception.ResourceNotFoundException;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(VisiteController.class)
class VisiteControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private VisiteService visiteService;

    @MockBean
    private ApprentiService apprentiService;

    @MockBean
    private CustomUserDetailsService customUserDetailsService;

    @Test
    @WithMockUser
    void getAllVisites_retourne200AvecListe() throws Exception {
        Visite v = new Visite();
        v.setId(1L);
        v.setDateVisite(LocalDate.of(2025, 3, 10));
        v.setFormat("présentiel");
        when(visiteService.getAllVisites()).thenReturn(List.of(v));

        mockMvc.perform(get("/api/visites"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$[0].format").value("présentiel"));
    }

    @Test
    @WithMockUser
    void getAllVisites_retourne500QuandAucuneVisite() throws Exception {
        when(visiteService.getAllVisites())
                .thenThrow(new RuntimeException("Aucune visite en base"));

        mockMvc.perform(get("/api/visites"))
                .andExpect(status().isInternalServerError());
    }

    @Test
    @WithMockUser
    void getVisiteById_retourne200QuandTrouvee() throws Exception {
        Visite v = new Visite();
        v.setId(1L);
        v.setCommentaires("RAS");
        when(visiteService.getVisiteById(1L)).thenReturn(Optional.of(v));

        mockMvc.perform(get("/api/visites/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.commentaires").value("RAS"));
    }

    @Test
    @WithMockUser
    void getVisiteById_retourne404QuandNonTrouvee() throws Exception {
        when(visiteService.getVisiteById(99L))
                .thenThrow(new ResourceNotFoundException("Aucune visite trouvée avec l'id 99"));

        mockMvc.perform(get("/api/visites/99"))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser
    void createVisite_retourne201AvecVisiteCree() throws Exception {
        Visite v = new Visite();
        v.setId(1L);
        v.setFormat("hybride");
        when(visiteService.createVisite(any(Visite.class))).thenReturn(v);

        mockMvc.perform(post("/api/visites")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(v)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.format").value("hybride"));
    }

    @Test
    @WithMockUser
    void createVisiteSimple_retourne201QuandDonneesValides() throws Exception {
        Apprenti apprenti = new Apprenti();
        apprenti.setId(1L);
        apprenti.setNom("Dupont");

        Visite visiteCreee = new Visite();
        visiteCreee.setId(10L);
        visiteCreee.setFormat("présentiel");

        when(apprentiService.getApprentiById(1L)).thenReturn(Optional.of(apprenti));
        when(visiteService.createVisite(any(Visite.class))).thenReturn(visiteCreee);

        CreateVisiteDTO dto = new CreateVisiteDTO();
        dto.setApprentiId(1L);
        dto.setDateVisite("2025-04-01");
        dto.setFormat("présentiel");
        dto.setCommentaires("Visite de suivi");

        mockMvc.perform(post("/api/visites/simple")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated());
    }

    @Test
    @WithMockUser
    void createVisiteSimple_retourne400SiDateMalFormatee() throws Exception {
        CreateVisiteDTO dto = new CreateVisiteDTO();
        dto.setApprentiId(1L);
        dto.setDateVisite("01/04/2025"); // format incorrect, attendu yyyy-MM-dd

        mockMvc.perform(post("/api/visites/simple")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser
    void createVisiteSimple_retourne400SiFormatInvalide() throws Exception {
        Apprenti apprenti = new Apprenti();
        apprenti.setId(1L);
        when(apprentiService.getApprentiById(1L)).thenReturn(Optional.of(apprenti));

        CreateVisiteDTO dto = new CreateVisiteDTO();
        dto.setApprentiId(1L);
        dto.setDateVisite("2025-04-01");
        dto.setFormat("visioconférence"); // format non reconnu

        mockMvc.perform(post("/api/visites/simple")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser
    void createVisiteSimple_retourne404SiApprentiInexistant() throws Exception {
        when(apprentiService.getApprentiById(99L))
                .thenThrow(new ResourceNotFoundException("Aucun apprenti trouvé avec l'id 99"));

        CreateVisiteDTO dto = new CreateVisiteDTO();
        dto.setApprentiId(99L);
        dto.setDateVisite("2025-04-01");

        mockMvc.perform(post("/api/visites/simple")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isNotFound());
    }

    @Test
    void getAllVisites_retourne302SansAuthentification() throws Exception {
        mockMvc.perform(get("/api/visites"))
                .andExpect(status().is3xxRedirection());
    }
}
