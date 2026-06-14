package ytg.projetjavaytg.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import ytg.projetjavaytg.models.Apprenti;
import ytg.projetjavaytg.services.AnneeAcademiqueService;
import ytg.projetjavaytg.services.ApprentiService;
import ytg.projetjavaytg.utils.SecurityUtils;

import java.util.List;
import java.util.stream.Collectors;

@Controller
public class DashboardController {

    private final ApprentiService apprentiService;
    private final AnneeAcademiqueService anneeAcademiqueService;

    public DashboardController(ApprentiService apprentiService, AnneeAcademiqueService anneeAcademiqueService) {
        this.apprentiService = apprentiService;
        this.anneeAcademiqueService = anneeAcademiqueService;
    }

    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        String anneeAcademique = anneeAcademiqueService.getAnneeAcademiqueEnCours();
        List<Apprenti> apprentis = apprentiService.getApprentisNonArchives().stream()
                .filter(a -> a.getAnneeAcademique() != null && a.getAnneeAcademique().equals(anneeAcademique))
                .collect(Collectors.toList());

        model.addAttribute("username", SecurityUtils.getCurrentUserPrenom());
        model.addAttribute("apprentis", apprentis);
        model.addAttribute("anneeAcademique", anneeAcademique);
        return "dashboard";
    }
}