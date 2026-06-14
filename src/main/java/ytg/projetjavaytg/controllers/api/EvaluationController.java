package ytg.projetjavaytg.controllers.api;

import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ytg.projetjavaytg.dto.EvaluationDTO;
import ytg.projetjavaytg.models.Apprenti;
import ytg.projetjavaytg.models.Evaluation;
import ytg.projetjavaytg.services.ApprentiService;
import ytg.projetjavaytg.services.EvaluationService;
import ytg.projetjavaytg.exception.ResourceNotFoundException;

import java.util.List;

@Tag(name = "Evaluation")
@RestController
@RequestMapping("/api/evaluations")
public class EvaluationController {

    private final EvaluationService evaluationService;
    private final ApprentiService apprentiService;

    public EvaluationController(EvaluationService evaluationService, ApprentiService apprentiService) {
        this.evaluationService = evaluationService;
        this.apprentiService = apprentiService;
    }

    @GetMapping
    public ResponseEntity<List<Evaluation>> getAllEvaluations() {
        List<Evaluation> evaluations = evaluationService.getAllEvaluations();
        return ResponseEntity.ok(evaluations);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Evaluation> getEvaluationById(@PathVariable Long id) {
        return evaluationService.getEvaluationById(id)
                .map(ResponseEntity::ok)
                .orElseThrow(() -> new ResourceNotFoundException("L'Evaluation que vous voulez trouver n'existe pas"));
    }

    @PostMapping
    public ResponseEntity<Evaluation> createEvaluation(@RequestBody EvaluationDTO dto) {
        Apprenti apprenti = apprentiService.getApprentiById(dto.getApprentiId())
                .orElseThrow(() -> new ResourceNotFoundException("Apprenti non trouvé avec id " + dto.getApprentiId()));

        Evaluation evaluation = new Evaluation();
        evaluation.setApprenti(apprenti);
        evaluation.setMemoireTheme(dto.getMemoireTheme());
        evaluation.setMemoireNote(dto.getMemoireNote());
        evaluation.setMemoireCommentaires(dto.getMemoireCommentaires());
        evaluation.setSoutenanceDate(dto.getSoutenanceDate());
        evaluation.setSoutenanceNote(dto.getSoutenanceNote());
        evaluation.setSoutenanceCommentaires(dto.getSoutenanceCommentaires());
        evaluation.setRemarquesGenerales(dto.getRemarquesGenerales());

        Evaluation createdEvaluation = evaluationService.createEvaluation(evaluation);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdEvaluation);
    }
}
