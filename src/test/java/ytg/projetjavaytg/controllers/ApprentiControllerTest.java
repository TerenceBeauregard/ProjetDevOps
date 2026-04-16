package ytg.projetjavaytg.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import ytg.projetjavaytg.Controllers.api.ApprentiController;
import ytg.projetjavaytg.Models.Apprenti;
import ytg.projetjavaytg.Security.CustomUserDetailsService;
import ytg.projetjavaytg.Services.ApprentiService;
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

@WebMvcTest(ApprentiController.class)
class ApprentiControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ApprentiService apprentiService;

    // Requis pour que Spring Security puisse construire l'AuthenticationManager
    @MockBean
    private CustomUserDetailsService customUserDetailsService;

    @Test
    @WithMockUser
    void getAllApprentis_retourne200AvecListe() throws Exception {
        Apprenti a = new Apprenti();
        a.setId(1L);
        a.setNom("Dupont");
        a.setPrenom("Jean");
        when(apprentiService.getAllApprentis()).thenReturn(List.of(a));

        mockMvc.perform(get("/api/apprentis"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$[0].nom").value("Dupont"))
                .andExpect(jsonPath("$[0].prenom").value("Jean"));
    }

    @Test
    @WithMockUser
    void getAllApprentis_retourne404QuandAucunApprenti() throws Exception {
        when(apprentiService.getAllApprentis())
                .thenThrow(new ResourceNotFoundException("Aucun apprenti existant en base"));

        mockMvc.perform(get("/api/apprentis"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Aucun apprenti existant en base"));
    }

    @Test
    @WithMockUser
    void getApprentiById_retourne200QuandTrouve() throws Exception {
        Apprenti a = new Apprenti();
        a.setId(1L);
        a.setNom("Dupont");
        when(apprentiService.getApprentiById(1L)).thenReturn(Optional.of(a));

        mockMvc.perform(get("/api/apprentis/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nom").value("Dupont"));
    }

    @Test
    @WithMockUser
    void getApprentiById_retourne404QuandNonTrouve() throws Exception {
        when(apprentiService.getApprentiById(99L))
                .thenThrow(new ResourceNotFoundException("Apprenti non trouvé avec id 99"));

        mockMvc.perform(get("/api/apprentis/99"))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser
    void createApprenti_retourne201AvecApprentiCree() throws Exception {
        Apprenti a = new Apprenti();
        a.setId(1L);
        a.setNom("Martin");
        a.setPrenom("Sophie");
        when(apprentiService.createApprenti(any(Apprenti.class))).thenReturn(a);

        mockMvc.perform(post("/api/apprentis")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(a)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.nom").value("Martin"))
                .andExpect(jsonPath("$.prenom").value("Sophie"));
    }

    @Test
    @WithMockUser
    void deleteApprenti_retourne204QuandSupprime() throws Exception {
        doNothing().when(apprentiService).deleteApprenti(1L);

        mockMvc.perform(delete("/api/apprentis/1").with(csrf()))
                .andExpect(status().isNoContent());
    }

    @Test
    @WithMockUser
    void deleteApprenti_retourne404SiApprentiInexistant() throws Exception {
        doThrow(new ResourceNotFoundException("L'apprenti que vous voulez supprimer n'existe pas"))
                .when(apprentiService).deleteApprenti(99L);

        mockMvc.perform(delete("/api/apprentis/99").with(csrf()))
                .andExpect(status().isNotFound());
    }

    @Test
    void getAllApprentis_retourne302SansAuthentification() throws Exception {
        mockMvc.perform(get("/api/apprentis"))
                .andExpect(status().is3xxRedirection());
    }
}
