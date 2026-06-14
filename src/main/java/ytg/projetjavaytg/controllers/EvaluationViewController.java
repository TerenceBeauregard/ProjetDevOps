package ytg.projetjavaytg.controllers;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import ytg.projetjavaytg.models.Apprenti;
import ytg.projetjavaytg.models.Evaluation;
import ytg.projetjavaytg.models.Visite;
import ytg.projetjavaytg.repositories.VisiteRepository;
import ytg.projetjavaytg.services.ApprentiService;
import ytg.projetjavaytg.services.EvaluationService;
import ytg.projetjavaytg.services.PdfGenerationService;
import ytg.projetjavaytg.utils.SecurityUtils;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/evaluation")
public class EvaluationViewController {

    private final EvaluationService evaluationService;
    private final ApprentiService apprentiService;
    private final PdfGenerationService pdfGenerationService;
    private final VisiteRepository visiteRepository;

    public EvaluationViewController(EvaluationService evaluationService,
                                   ApprentiService apprentiService,
                                   PdfGenerationService pdfGenerationService,
                                   VisiteRepository visiteRepository) {
        this.evaluationService = evaluationService;
        this.apprentiService = apprentiService;
        this.pdfGenerationService = pdfGenerationService;
        this.visiteRepository = visiteRepository;
    }

    @GetMapping("/apprenti/{apprentiId}")
    public String showEvaluationForm(@PathVariable Long apprentiId, Model model) {
        Optional<Apprenti> apprentiOpt = apprentiService.getApprentiById(apprentiId);

        if (apprentiOpt.isEmpty()) {
            return "redirect:/dashboard?error=apprenti_not_found";
        }

        Apprenti apprenti = apprentiOpt.get();
        Optional<Evaluation> evaluationOpt = evaluationService.getEvaluationByApprentiId(apprentiId);

        Evaluation evaluation;
        if (evaluationOpt.isPresent()) {
            evaluation = evaluationOpt.get();
        } else {
            // Créer une nouvelle évaluation vide
            evaluation = new Evaluation();
            evaluation.setApprenti(apprenti);

            // Récupérer la dernière visite programmée pour cet apprenti
            List<Visite> visites = visiteRepository.findByApprentiIdOrderByDateVisiteDesc(apprentiId);
            if (!visites.isEmpty()) {
                Visite derniereVisite = visites.get(0);
                evaluation.setSoutenanceDate(derniereVisite.getDateVisite());
                model.addAttribute("visiteInfo", "Date pré-remplie depuis la visite du " +
                    derniereVisite.getDateVisite() + " (Format: " + derniereVisite.getFormat() + ")");
            }
        }

        model.addAttribute("username", SecurityUtils.getCurrentUserPrenom());
        model.addAttribute("apprenti", apprenti);
        model.addAttribute("evaluation", evaluation);
        model.addAttribute("isNew", evaluationOpt.isEmpty());

        return "evaluation/form";
    }

    @GetMapping("/view/{apprentiId}")
    public String viewEvaluation(@PathVariable Long apprentiId, Model model) {
        Optional<Apprenti> apprentiOpt = apprentiService.getApprentiById(apprentiId);

        if (apprentiOpt.isEmpty()) {
            return "redirect:/dashboard?error=apprenti_not_found";
        }

        Apprenti apprenti = apprentiOpt.get();
        Optional<Evaluation> evaluationOpt = evaluationService.getEvaluationByApprentiId(apprentiId);

        if (evaluationOpt.isEmpty()) {
            return "redirect:/apprentis/" + apprentiId + "?error=no_evaluation";
        }

        Evaluation evaluation = evaluationOpt.get();

        model.addAttribute("username", SecurityUtils.getCurrentUserPrenom());
        model.addAttribute("apprenti", apprenti);
        model.addAttribute("evaluation", evaluation);
        model.addAttribute("readOnly", true);

        return "evaluation/view";
    }

    @PostMapping("/save")
    public String saveEvaluation(@ModelAttribute Evaluation evaluation,
                                @RequestParam Long apprentiId,
                                @RequestParam(required = false) Long evaluationId,
                                RedirectAttributes redirectAttributes) {
        try {
            Optional<Apprenti> apprentiOpt = apprentiService.getApprentiById(apprentiId);

            if (apprentiOpt.isEmpty()) {
                redirectAttributes.addFlashAttribute("error", "Apprenti non trouvé");
                return "redirect:/dashboard";
            }

            Apprenti apprenti = apprentiOpt.get();
            evaluation.setApprenti(apprenti);

            if (evaluationId != null && evaluationId > 0) {
                // Mise à jour
                evaluation.setId(evaluationId);
                evaluationService.updateEvaluation(evaluationId, evaluation);
                redirectAttributes.addFlashAttribute("success", "Évaluation mise à jour avec succès !");
            } else {
                // Création
                evaluation.setDateCreation(Instant.now());
                evaluation.setDateModification(Instant.now());
                evaluationService.createEvaluation(evaluation);
                redirectAttributes.addFlashAttribute("success", "Évaluation créée avec succès !");
            }

            return "redirect:/evaluation/apprenti/" + apprentiId;

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Erreur lors de l'enregistrement : " + e.getMessage());
            return "redirect:/evaluation/apprenti/" + apprentiId;
        }
    }

    @GetMapping("/download/{apprentiId}")
    public ResponseEntity<byte[]> downloadEvaluationPdf(@PathVariable Long apprentiId) {
        try {
            Optional<Apprenti> apprentiOpt = apprentiService.getApprentiById(apprentiId);
            Optional<Evaluation> evaluationOpt = evaluationService.getEvaluationByApprentiId(apprentiId);

            if (apprentiOpt.isEmpty() || evaluationOpt.isEmpty()) {
                return ResponseEntity.notFound().build();
            }

            Apprenti apprenti = apprentiOpt.get();
            Evaluation evaluation = evaluationOpt.get();

            byte[] pdfBytes = pdfGenerationService.generateEvaluationPdf(evaluation, apprenti);

            String filename = "Evaluation_" + apprenti.getNom() + "_" + apprenti.getPrenom() + ".pdf";

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            headers.setContentDispositionFormData("attachment", filename);
            headers.setCacheControl("must-revalidate, post-check=0, pre-check=0");

            return new ResponseEntity<>(pdfBytes, headers, HttpStatus.OK);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}

