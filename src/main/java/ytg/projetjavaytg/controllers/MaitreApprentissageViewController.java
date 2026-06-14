package ytg.projetjavaytg.controllers;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import ytg.projetjavaytg.models.MaitreApprentissage;
import ytg.projetjavaytg.services.EntrepriseService;
import ytg.projetjavaytg.services.MaitreApprentissageService;
import ytg.projetjavaytg.utils.SecurityUtils;

@Controller
@RequestMapping("/maitres")
public class MaitreApprentissageViewController {

    private final MaitreApprentissageService maitreApprentissageService;
    private final EntrepriseService entrepriseService;

    public MaitreApprentissageViewController(MaitreApprentissageService maitreApprentissageService,
                                            EntrepriseService entrepriseService) {
        this.maitreApprentissageService = maitreApprentissageService;
        this.entrepriseService = entrepriseService;
    }

    @GetMapping
    public String list(Model model) {
        model.addAttribute("username", SecurityUtils.getCurrentUserPrenom());
        model.addAttribute("maitres", maitreApprentissageService.getAllMaitresApprentissage());
        return "maitreapprentissage/list";
    }

    @GetMapping("/create")
    public String createForm(Model model) {
        model.addAttribute("username", SecurityUtils.getCurrentUserPrenom());
        model.addAttribute("entreprises", entrepriseService.getAllEntreprises());
        return "maitreapprentissage/create";
    }

    @PostMapping("/create")
    public String createMaitre(@ModelAttribute MaitreApprentissage maitre,
                              @RequestParam Long entrepriseId,
                              RedirectAttributes redirectAttributes) {
        try {
            entrepriseService.getEntrepriseById(entrepriseId).ifPresent(maitre::setEntreprise);
            maitreApprentissageService.createMaitreApprentissage(maitre);
            redirectAttributes.addFlashAttribute("success", "Le maître d'apprentissage a été créé avec succès !");
            return "redirect:/maitres";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Erreur lors de la création : " + e.getMessage());
            return "redirect:/maitres/create";
        }
    }

    @PostMapping("/{id}/delete")
    public String deleteMaitre(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            maitreApprentissageService.getMaitreApprentissageById(id).ifPresent(maitre -> maitreApprentissageService.deleteMaitreApprentissage(id));
            redirectAttributes.addFlashAttribute("success", "Le maître d'apprentissage a été supprimé avec succès !");
        } catch (DataIntegrityViolationException e) {
            redirectAttributes.addFlashAttribute("error", "Impossible de supprimer ce maître d'apprentissage. Il est associé à un ou plusieurs apprentis. Veuillez d'abord modifier ou supprimer ces apprentis.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Erreur lors de la suppression : " + e.getMessage());
        }
        return "redirect:/maitres";
    }
}

