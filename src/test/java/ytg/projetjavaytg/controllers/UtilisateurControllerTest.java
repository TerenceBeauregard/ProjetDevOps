package ytg.projetjavaytg.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import ytg.projetjavaytg.controllers.api.UtilisateurController;
import ytg.projetjavaytg.dto.UtilisateurDTO;
import ytg.projetjavaytg.models.Utilisateur;
import ytg.projetjavaytg.security.CustomUserDetailsService;
import ytg.projetjavaytg.services.UtilisateurService;
import ytg.projetjavaytg.exception.ResourceNotFoundException;

import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(UtilisateurController.class)
class UtilisateurControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private UtilisateurService utilisateurService;

    @MockBean
    private CustomUserDetailsService customUserDetailsService;

    @Test
    @WithMockUser
    void getAllUtilisateurs_retourne200AvecListe() throws Exception {
        Utilisateur utilisateur = new Utilisateur();
        utilisateur.setId(1L);
        utilisateur.setUsername("jdupont");
        when(utilisateurService.getAllUtilisateurs()).thenReturn(List.of(utilisateur));

        mockMvc.perform(get("/api/utilisateurs"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$[0].username").value("jdupont"));
    }

    @Test
    @WithMockUser
    void getAllUtilisateurs_retourne404QuandAucunUtilisateur() throws Exception {
        when(utilisateurService.getAllUtilisateurs())
                .thenThrow(new ResourceNotFoundException("Aucun utilisateur en base"));

        mockMvc.perform(get("/api/utilisateurs"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Aucun utilisateur en base"));
    }

    @Test
    @WithMockUser
    void getUtilisateurById_retourne200QuandTrouve() throws Exception {
        Utilisateur utilisateur = new Utilisateur();
        utilisateur.setId(1L);
        utilisateur.setUsername("jdupont");
        when(utilisateurService.getUtilisateurById(1L)).thenReturn(Optional.of(utilisateur));

        mockMvc.perform(get("/api/utilisateurs/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("jdupont"));
    }

    @Test
    @WithMockUser
    void getUtilisateurById_retourne500QuandNonTrouve() throws Exception {
        when(utilisateurService.getUtilisateurById(99L))
                .thenThrow(new RuntimeException("Aucun utilisateur avec l'id 99 n'existe"));

        mockMvc.perform(get("/api/utilisateurs/99"))
                .andExpect(status().isInternalServerError());
    }

    @Test
    @WithMockUser
    void createUtilisateur_retourne201AvecUtilisateurCree() throws Exception {
        Utilisateur utilisateur = new Utilisateur();
        utilisateur.setId(1L);
        utilisateur.setUsername("jdupont");
        utilisateur.setRole("ROLE_TUTEUR");
        utilisateur.setEnabled(true);
        when(utilisateurService.createUtilisateur(any(Utilisateur.class))).thenReturn(utilisateur);

        UtilisateurDTO dto = new UtilisateurDTO();
        dto.setUsername("jdupont");
        dto.setPassword("secret");
        dto.setNom("Dupont");
        dto.setPrenom("Jean");
        dto.setEmail("jean.dupont@mail.com");

        mockMvc.perform(post("/api/utilisateurs")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.username").value("jdupont"));
    }

    @Test
    void getAllUtilisateurs_retourne401SansAuthentification() throws Exception {
        mockMvc.perform(get("/api/utilisateurs"))
                .andExpect(status().isUnauthorized());
    }
}
