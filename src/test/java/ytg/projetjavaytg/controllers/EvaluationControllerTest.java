package ytg.projetjavaytg.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import ytg.projetjavaytg.controllers.api.EvaluationController;
import ytg.projetjavaytg.dto.EvaluationDTO;
import ytg.projetjavaytg.models.Apprenti;
import ytg.projetjavaytg.models.Evaluation;
import ytg.projetjavaytg.security.CustomUserDetailsService;
import ytg.projetjavaytg.services.ApprentiService;
import ytg.projetjavaytg.services.EvaluationService;
import ytg.projetjavaytg.exception.ResourceNotFoundException;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(EvaluationController.class)
class EvaluationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private EvaluationService evaluationService;

    @MockBean
    private ApprentiService apprentiService;

    @MockBean
    private CustomUserDetailsService customUserDetailsService;

    @Test
    @WithMockUser
    void getAllEvaluations_retourne200AvecListe() throws Exception {
        Evaluation evaluation = new Evaluation();
        evaluation.setId(1L);
        evaluation.setMemoireTheme("Sujet de mémoire");
        when(evaluationService.getAllEvaluations()).thenReturn(List.of(evaluation));

        mockMvc.perform(get("/api/evaluations"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$[0].memoireTheme").value("Sujet de mémoire"));
    }

    @Test
    @WithMockUser
    void getAllEvaluations_retourne404QuandAucuneEvaluation() throws Exception {
        when(evaluationService.getAllEvaluations())
                .thenThrow(new ResourceNotFoundException("Aucune evaluation en base"));

        mockMvc.perform(get("/api/evaluations"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Aucune evaluation en base"));
    }

    @Test
    @WithMockUser
    void getEvaluationById_retourne200QuandTrouvee() throws Exception {
        Evaluation evaluation = new Evaluation();
        evaluation.setId(1L);
        evaluation.setMemoireTheme("Sujet");
        when(evaluationService.getEvaluationById(1L)).thenReturn(Optional.of(evaluation));

        mockMvc.perform(get("/api/evaluations/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.memoireTheme").value("Sujet"));
    }

    @Test
    @WithMockUser
    void getEvaluationById_retourne404QuandNonTrouvee() throws Exception {
        when(evaluationService.getEvaluationById(99L)).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/evaluations/99"))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser
    void createEvaluation_retourne201AvecEvaluationCreee() throws Exception {
        Apprenti apprenti = new Apprenti();
        apprenti.setId(1L);
        when(apprentiService.getApprentiById(1L)).thenReturn(Optional.of(apprenti));

        Evaluation evaluation = new Evaluation();
        evaluation.setId(1L);
        evaluation.setMemoireTheme("Sujet");
        when(evaluationService.createEvaluation(any(Evaluation.class))).thenReturn(evaluation);

        EvaluationDTO dto = new EvaluationDTO();
        dto.setApprentiId(1L);
        dto.setMemoireTheme("Sujet");
        dto.setMemoireNote(BigDecimal.valueOf(15));

        mockMvc.perform(post("/api/evaluations")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.memoireTheme").value("Sujet"));
    }

    @Test
    @WithMockUser
    void createEvaluation_retourne404SiApprentiNonTrouve() throws Exception {
        when(apprentiService.getApprentiById(99L)).thenReturn(Optional.empty());

        EvaluationDTO dto = new EvaluationDTO();
        dto.setApprentiId(99L);
        dto.setMemoireTheme("Sujet");

        mockMvc.perform(post("/api/evaluations")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isNotFound());
    }

    @Test
    void getAllEvaluations_retourne401SansAuthentification() throws Exception {
        mockMvc.perform(get("/api/evaluations"))
                .andExpect(status().isUnauthorized());
    }
}
