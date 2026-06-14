package ytg.projetjavaytg.services;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ytg.projetjavaytg.models.Evaluation;
import ytg.projetjavaytg.repositories.EvaluationRepository;
import ytg.projetjavaytg.exception.ResourceNotFoundException;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EvaluationServiceTest {

    @Mock
    private EvaluationRepository evaluationRepository;

    @InjectMocks
    private EvaluationService evaluationService;

    @Test
    void getAllEvaluations_retourneListeQuandNonVide() {
        Evaluation evaluation = new Evaluation();
        evaluation.setId(1L);
        evaluation.setMemoireTheme("Sujet");
        when(evaluationRepository.findAll()).thenReturn(List.of(evaluation));

        List<Evaluation> result = evaluationService.getAllEvaluations();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getMemoireTheme()).isEqualTo("Sujet");
    }

    @Test
    void getAllEvaluations_leveExceptionQuandListeVide() {
        when(evaluationRepository.findAll()).thenReturn(Collections.emptyList());

        assertThatThrownBy(() -> evaluationService.getAllEvaluations())
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Aucune evaluation en base");
    }

    @Test
    void getEvaluationById_retourneEvaluationQuandTrouvee() {
        Evaluation evaluation = new Evaluation();
        evaluation.setId(1L);
        when(evaluationRepository.findById(1L)).thenReturn(Optional.of(evaluation));

        Optional<Evaluation> result = evaluationService.getEvaluationById(1L);

        assertThat(result).isPresent();
        assertThat(result.get().getId()).isEqualTo(1L);
    }

    @Test
    void getEvaluationById_leveExceptionSiNonTrouvee() {
        when(evaluationRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> evaluationService.getEvaluationById(99L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("99");
    }

    @Test
    void getEvaluationByApprentiId_retourneOptionalPresentQuandTrouvee() {
        Evaluation evaluation = new Evaluation();
        evaluation.setId(1L);
        when(evaluationRepository.findByApprentiId(10L)).thenReturn(Optional.of(evaluation));

        Optional<Evaluation> result = evaluationService.getEvaluationByApprentiId(10L);

        assertThat(result).isPresent();
    }

    @Test
    void getEvaluationByApprentiId_retourneOptionalVideQuandNonTrouvee() {
        when(evaluationRepository.findByApprentiId(99L)).thenReturn(Optional.empty());

        Optional<Evaluation> result = evaluationService.getEvaluationByApprentiId(99L);

        assertThat(result).isEmpty();
    }

    @Test
    void createEvaluation_sauvegardeEtRetourneEvaluation() {
        Evaluation evaluation = new Evaluation();
        evaluation.setMemoireTheme("Sujet");
        when(evaluationRepository.save(any(Evaluation.class))).thenAnswer(inv -> inv.getArgument(0));

        Evaluation result = evaluationService.createEvaluation(evaluation);

        assertThat(result.getMemoireTheme()).isEqualTo("Sujet");
        assertThat(result.getDateCreation()).isNotNull();
        assertThat(result.getDateModification()).isNotNull();
        verify(evaluationRepository).save(evaluation);
    }

    @Test
    void updateEvaluation_metAJourEtRetourneEvaluation() {
        Evaluation existante = new Evaluation();
        existante.setId(1L);
        existante.setMemoireTheme("Ancien sujet");
        when(evaluationRepository.findById(1L)).thenReturn(Optional.of(existante));
        when(evaluationRepository.save(any(Evaluation.class))).thenAnswer(inv -> inv.getArgument(0));

        Evaluation nouvellesDonnees = new Evaluation();
        nouvellesDonnees.setMemoireTheme("Nouveau sujet");
        nouvellesDonnees.setMemoireNote(BigDecimal.valueOf(15));
        nouvellesDonnees.setMemoireCommentaires("Bon travail");
        nouvellesDonnees.setSoutenanceNote(BigDecimal.valueOf(16));
        nouvellesDonnees.setSoutenanceCommentaires("Bonne soutenance");
        nouvellesDonnees.setRemarquesGenerales("RAS");

        Evaluation result = evaluationService.updateEvaluation(1L, nouvellesDonnees);

        assertThat(result.getMemoireTheme()).isEqualTo("Nouveau sujet");
        assertThat(result.getMemoireNote()).isEqualTo(BigDecimal.valueOf(15));
        assertThat(result.getSoutenanceNote()).isEqualTo(BigDecimal.valueOf(16));
        assertThat(result.getRemarquesGenerales()).isEqualTo("RAS");
        verify(evaluationRepository).save(existante);
    }

    @Test
    void updateEvaluation_leveExceptionSiNonTrouvee() {
        when(evaluationRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> evaluationService.updateEvaluation(99L, new Evaluation()))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Évaluation non trouvée");
    }
}
