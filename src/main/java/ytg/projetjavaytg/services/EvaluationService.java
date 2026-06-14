package ytg.projetjavaytg.services;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ytg.projetjavaytg.models.Evaluation;
import ytg.projetjavaytg.repositories.EvaluationRepository;
import ytg.projetjavaytg.exception.ResourceNotFoundException;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Service
public class EvaluationService {

    private final EvaluationRepository evaluationRepository;

    public EvaluationService(EvaluationRepository evaluationRepository) {
        this.evaluationRepository = evaluationRepository;
    }

    public List<Evaluation> getAllEvaluations() {
        List<Evaluation> evaluations = evaluationRepository.findAll();
        if (evaluations.isEmpty()){
            throw new ResourceNotFoundException("Aucune evaluation en base");
        }
        return evaluations;
    }

    public Optional<Evaluation> getEvaluationById(Long id) {
        Optional<Evaluation> evaluation = evaluationRepository.findById(id);
        if (evaluation.isPresent()){
            return evaluation;
        }
        throw new ResourceNotFoundException("Aucune evaluation trouver avec id " + id);
    }

    public Optional<Evaluation> getEvaluationByApprentiId(Long apprentiId) {
        return evaluationRepository.findByApprentiId(apprentiId);
    }

    @Transactional
    public Evaluation createEvaluation(Evaluation evaluation) {
        evaluation.setDateCreation(Instant.now());
        evaluation.setDateModification(Instant.now());
        return evaluationRepository.save(evaluation);
    }

    @Transactional
    public Evaluation updateEvaluation(Long id, Evaluation evaluation) {
        Evaluation existingEvaluation = evaluationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Évaluation non trouvée"));

        existingEvaluation.setMemoireTheme(evaluation.getMemoireTheme());
        existingEvaluation.setMemoireNote(evaluation.getMemoireNote());
        existingEvaluation.setMemoireCommentaires(evaluation.getMemoireCommentaires());
        existingEvaluation.setSoutenanceDate(evaluation.getSoutenanceDate());
        existingEvaluation.setSoutenanceNote(evaluation.getSoutenanceNote());
        existingEvaluation.setSoutenanceCommentaires(evaluation.getSoutenanceCommentaires());
        existingEvaluation.setRemarquesGenerales(evaluation.getRemarquesGenerales());
        existingEvaluation.setDateModification(Instant.now());

        return evaluationRepository.save(existingEvaluation);
    }
}
