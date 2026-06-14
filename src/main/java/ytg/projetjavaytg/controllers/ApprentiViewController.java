package ytg.projetjavaytg.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import ytg.projetjavaytg.dto.ApprentiFormDTO;
import ytg.projetjavaytg.models.Apprenti;
import ytg.projetjavaytg.services.*;
import ytg.projetjavaytg.utils.SecurityUtils;

@Controller
@RequestMapping("/apprentis")
public class ApprentiViewController {

    private static final String USERNAME = "username";
    private static final String SUCCESS = "success";
    private static final String ERROR = "error";
    private static final String REDIRECT_DASHBOARD = "redirect:/dashboard";

    private final ApprentiService apprentiService;
    private final EntrepriseService entrepriseService;
    private final MaitreApprentissageService maitreApprentissageService;
    private final UtilisateurService utilisateurService;
    private final AnneeAcademiqueService anneeAcademiqueService;
    private final EvaluationService evaluationService;

    public ApprentiViewController(ApprentiService apprentiService,
                                  EntrepriseService entrepriseService,
                                  MaitreApprentissageService maitreApprentissageService,
                                  UtilisateurService utilisateurService,
                                  AnneeAcademiqueService anneeAcademiqueService,
                                  EvaluationService evaluationService) {
        this.apprentiService = apprentiService;
        this.entrepriseService = entrepriseService;
        this.maitreApprentissageService = maitreApprentissageService;
        this.utilisateurService = utilisateurService;
        this.anneeAcademiqueService = anneeAcademiqueService;
        this.evaluationService = evaluationService;
    }

    @GetMapping("/create")
    public String createApprentiForm(Model model) {
        String anneeAcademique = anneeAcademiqueService.getAnneeAcademiqueEnCours();
        model.addAttribute(USERNAME, SecurityUtils.getCurrentUserPrenom());
        model.addAttribute("anneeAcademique", anneeAcademique);
        model.addAttribute("entreprises", entrepriseService.getAllEntreprises());
        model.addAttribute("maitres", maitreApprentissageService.getAllMaitresApprentissage());
        model.addAttribute("tuteurs", utilisateurService.getAllUtilisateurs());
        // Fournit la liste complète des années académiques pour le select
        model.addAttribute("annees", anneeAcademiqueService.getAllAnnees());
        return "apprentice/createapprentice";
    }

    @PostMapping("/create")
    public String createApprenti(@ModelAttribute ApprentiFormDTO form,
                                @RequestParam Long entrepriseId,
                                @RequestParam Long maitreApprentissageId,
                                @RequestParam Long tuteurEnseignantId,
                                RedirectAttributes redirectAttributes) {
        try {
            Apprenti apprenti = toApprenti(form);
            entrepriseService.getEntrepriseById(entrepriseId).ifPresent(apprenti::setEntreprise);
            maitreApprentissageService.getMaitreApprentissageById(maitreApprentissageId)
                    .ifPresent(apprenti::setMaitreApprentissage);
            utilisateurService.getUtilisateurById(tuteurEnseignantId)
                    .ifPresent(apprenti::setTuteurEnseignant);
            apprentiService.createApprenti(apprenti);
            redirectAttributes.addFlashAttribute(SUCCESS, "L'apprenti a été créé avec succès !");
            return REDIRECT_DASHBOARD;
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute(ERROR, "Erreur lors de la création de l'apprenti : " + e.getMessage());
            return "redirect:/apprentis/create";
        }
    }

    @GetMapping("/{id}")
    public String apprentiDetails(@PathVariable Long id, Model model) {
        return apprentiService.getApprentiById(id)
                .map(apprenti -> {
                    model.addAttribute(USERNAME, SecurityUtils.getCurrentUserPrenom());
                    model.addAttribute("apprenti", apprenti);
                    // Vérifier si une évaluation existe pour cet apprenti
                    boolean hasEvaluation = evaluationService.getEvaluationByApprentiId(id).isPresent();
                    model.addAttribute("hasEvaluation", hasEvaluation);
                    return "apprentice/details";
                })
                .orElse(REDIRECT_DASHBOARD);
    }

    @GetMapping("/{id}/edit")
    public String editApprentiForm(@PathVariable Long id, Model model) {
        return apprentiService.getApprentiById(id)
                .map(apprenti -> {
                    model.addAttribute(USERNAME, SecurityUtils.getCurrentUserPrenom());
                    model.addAttribute("apprenti", apprenti);
                    model.addAttribute("entreprises", entrepriseService.getAllEntreprises());
                    model.addAttribute("maitres", maitreApprentissageService.getAllMaitresApprentissage());
                    model.addAttribute("tuteurs", utilisateurService.getAllUtilisateurs());
                    // Fournit la liste complète des années académiques pour le select
                    model.addAttribute("annees", anneeAcademiqueService.getAllAnnees());
                    return "apprentice/edit";
                })
                .orElse(REDIRECT_DASHBOARD);
    }

    @PostMapping("/{id}/edit")
    public String editApprenti(@PathVariable Long id,
                               @ModelAttribute ApprentiFormDTO form,
                               @RequestParam Long entrepriseId,
                               @RequestParam Long maitreApprentissageId,
                               @RequestParam Long tuteurEnseignantId,
                               RedirectAttributes redirectAttributes) {
        try {
            Apprenti apprenti = toApprenti(form);
            entrepriseService.getEntrepriseById(entrepriseId).ifPresent(apprenti::setEntreprise);
            maitreApprentissageService.getMaitreApprentissageById(maitreApprentissageId)
                    .ifPresent(apprenti::setMaitreApprentissage);
            utilisateurService.getUtilisateurById(tuteurEnseignantId)
                    .ifPresent(apprenti::setTuteurEnseignant);
            Apprenti updated = apprentiService.updateApprenti(id, apprenti);
            if (updated != null) {
                redirectAttributes.addFlashAttribute(SUCCESS, "L'apprenti a été modifié avec succès !");
            } else {
                redirectAttributes.addFlashAttribute(ERROR, "Apprenti non trouvé");
            }
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute(ERROR, "Erreur lors de la modification : " + e.getMessage());
            return "redirect:/apprentis/" + id + "/edit";
        }
        return REDIRECT_DASHBOARD;
    }

    @DeleteMapping("/{id}")
    public String deleteApprenti(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            apprentiService.deleteApprenti(id);
            redirectAttributes.addFlashAttribute(SUCCESS, "L'apprenti a été supprimé avec succès !");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute(ERROR, "Erreur lors de la suppression : " + e.getMessage());
        }
        return REDIRECT_DASHBOARD;
    }

    private Apprenti toApprenti(ApprentiFormDTO form) {
        Apprenti apprenti = new Apprenti();
        apprenti.setNom(form.getNom());
        apprenti.setPrenom(form.getPrenom());
        apprenti.setEmail(form.getEmail());
        apprenti.setTelephone(form.getTelephone());
        apprenti.setProgramme(form.getProgramme());
        apprenti.setAnneeAcademique(form.getAnneeAcademique());
        apprenti.setMajeure(form.getMajeure());
        apprenti.setNiveau(form.getNiveau());
        apprenti.setMissionMotsCles(form.getMissionMotsCles());
        apprenti.setMissionMetierCible(form.getMissionMetierCible());
        apprenti.setMissionCommentaires(form.getMissionCommentaires());
        apprenti.setFeedbackTuteur(form.getFeedbackTuteur());
        apprenti.setRemarquesGenerales(form.getRemarquesGenerales());
        return apprenti;
    }
}
