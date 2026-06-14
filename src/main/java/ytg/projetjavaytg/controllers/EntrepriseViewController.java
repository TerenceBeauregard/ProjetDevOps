package ytg.projetjavaytg.controllers;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import ytg.projetjavaytg.dto.EntrepriseFormDTO;
import ytg.projetjavaytg.models.Entreprise;
import ytg.projetjavaytg.services.EntrepriseService;
import ytg.projetjavaytg.utils.SecurityUtils;

@Controller
@RequestMapping("/entreprises")
public class EntrepriseViewController {

    public static final String ERROR = "error";
    private final EntrepriseService entrepriseService;

    public EntrepriseViewController(EntrepriseService entrepriseService) {
        this.entrepriseService = entrepriseService;
    }

    @GetMapping
    public String list(Model model) {
        model.addAttribute("username", SecurityUtils.getCurrentUserPrenom());
        model.addAttribute("entreprises", entrepriseService.getAllEntreprises());
        return "entreprise/list";
    }

    @GetMapping("/create")
    public String createForm(Model model) {
        model.addAttribute("username", SecurityUtils.getCurrentUserPrenom());
        return "entreprise/create";
    }

    @PostMapping("/create")
    public String createEntreprise(@ModelAttribute EntrepriseFormDTO form, RedirectAttributes redirectAttributes) {
        try {
            Entreprise entreprise = new Entreprise();
            entreprise.setRaisonSociale(form.getRaisonSociale());
            entreprise.setAdresse(form.getAdresse());
            entreprise.setInformationsAcces(form.getInformationsAcces());
            entrepriseService.createEntreprise(entreprise);
            redirectAttributes.addFlashAttribute("success", "L'entreprise a été créée avec succès !");
            return "redirect:/entreprises";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute(ERROR, "Erreur lors de la création : " + e.getMessage());
            return "redirect:/entreprises/create";
        }
    }

    @PostMapping("/{id}/delete")
    public String deleteEntreprise(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            entrepriseService.getEntrepriseById(id).ifPresent(entreprise -> entrepriseService.deleteEntreprise(id));
            redirectAttributes.addFlashAttribute("success", "L'entreprise a été supprimée avec succès !");
        } catch (DataIntegrityViolationException e) {
            redirectAttributes.addFlashAttribute(ERROR, "Impossible de supprimer cette entreprise. Elle est associée à un ou plusieurs apprentis. Veuillez d'abord modifier ou supprimer ces apprentis.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute(ERROR, "Erreur lors de la suppression : " + e.getMessage());
        }
        return "redirect:/entreprises";
    }
}

