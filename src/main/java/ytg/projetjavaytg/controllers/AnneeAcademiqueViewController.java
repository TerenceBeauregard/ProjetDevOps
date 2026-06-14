package ytg.projetjavaytg.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import ytg.projetjavaytg.models.Apprenti;
import ytg.projetjavaytg.services.AnneeAcademiqueService;
import ytg.projetjavaytg.services.ApprentiService;
import ytg.projetjavaytg.utils.SecurityUtils;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/annee-academique")
public class AnneeAcademiqueViewController {

    private final ApprentiService apprentiService;
    private final AnneeAcademiqueService anneeAcademiqueService;

    public AnneeAcademiqueViewController(ApprentiService apprentiService, AnneeAcademiqueService anneeAcademiqueService) {
        this.apprentiService = apprentiService;
        this.anneeAcademiqueService = anneeAcademiqueService;
    }

    @GetMapping
    public String afficherPageAnneeAcademique(Model model) {
        try {
            model.addAttribute("username", SecurityUtils.getCurrentUserPrenom());

            String anneeActuelle = anneeAcademiqueService.getAnneeAcademiqueEnCours();
            model.addAttribute("anneeActuelle", anneeActuelle);

            List<Apprenti> apprentisActifs = apprentiService.getApprentisNonArchives();
            Map<String, Long> statsParNiveau = apprentisActifs.stream()
                    .filter(a -> a.getNiveau() != null)
                    .collect(Collectors.groupingBy(Apprenti::getNiveau, Collectors.counting()));
            long nbI1 = statsParNiveau.getOrDefault("I1", 0L);
            long nbI2 = statsParNiveau.getOrDefault("I2", 0L);
            long nbI3 = statsParNiveau.getOrDefault("I3", 0L);
            model.addAttribute("nbI1", nbI1);
            model.addAttribute("nbI2", nbI2);
            model.addAttribute("nbI3", nbI3);
            model.addAttribute("totalApprentis", apprentisActifs.size());
            return "admin/yearmanagement";
        } catch (Exception e) {
            model.addAttribute("error", "Erreur lors du chargement de la page : " + e.getMessage());
            model.addAttribute("nbI1", 0L);
            model.addAttribute("nbI2", 0L);
            model.addAttribute("nbI3", 0L);
            model.addAttribute("totalApprentis", 0L);
            model.addAttribute("anneeActuelle", "");
            return "admin/yearmanagement";
        }
    }


    @PostMapping("/creer-suivante")
    public String creerAnneeSuivante(RedirectAttributes redirectAttributes) {
        try {
            String anneeSuivante = anneeAcademiqueService.calculerAnneeSuivante();
            apprentiService.creerNouvelleAnneeAcademique(anneeSuivante);
            redirectAttributes.addFlashAttribute("success",
                "Année académique " + anneeSuivante + " créée avec succès ! Les apprentis ont été promus et les I3 archivés.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error",
                "Erreur lors de la création de l'année suivante : " + e.getMessage());
        }
        return "redirect:/annee-academique";
    }
}

