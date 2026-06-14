package ytg.projetjavaytg.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import ytg.projetjavaytg.models.Apprenti;
import ytg.projetjavaytg.services.ApprentiService;
import ytg.projetjavaytg.services.EntrepriseService;
import ytg.projetjavaytg.services.AnneeAcademiqueService;
import ytg.projetjavaytg.utils.SecurityUtils;

import java.util.List;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/recherche")
public class RechercheViewController {

    private final ApprentiService apprentiService;
    private final EntrepriseService entrepriseService;
    private final AnneeAcademiqueService anneeAcademiqueService;

    public RechercheViewController(ApprentiService apprentiService,
                                   EntrepriseService entrepriseService,
                                   AnneeAcademiqueService anneeAcademiqueService) {
        this.apprentiService = apprentiService;
        this.entrepriseService = entrepriseService;
        this.anneeAcademiqueService = anneeAcademiqueService;
    }

    @GetMapping
    public String afficherPageRecherche(Model model,
                                        @RequestParam(value = "nom", required = false) String nom,
                                        @RequestParam(value = "prenom", required = false) String prenom,
                                        @RequestParam(value = "niveau", required = false) String niveau,
                                        @RequestParam(value = "anneeAcademique", required = false) String anneeAcademique,
                                        @RequestParam(value = "entreprise", required = false) String entreprise,
                                        @RequestParam(value = "statut", required = false) String statut,
                                        @RequestParam(value = "mission_mots_cles", required = false) String motCle) {
        model.addAttribute("username", SecurityUtils.getCurrentUserPrenom());

        List<Apprenti> tousLesApprentis = apprentiService.getAllApprentis();

        // Application des filtres
        List<Apprenti> apprentisFiltres = tousLesApprentis.stream()
                .filter(apprenti -> {
                    // Filtre par nom
                    if (nom != null && !nom.trim().isEmpty()) {
                        if (!apprenti.getNom().toLowerCase().contains(nom.toLowerCase().trim())) {
                            return false;
                        }
                    }

                    // Filtre par prénom
                    if (prenom != null && !prenom.trim().isEmpty()) {
                        if (!apprenti.getPrenom().toLowerCase().contains(prenom.toLowerCase().trim())) {
                            return false;
                        }
                    }

                    // Filtre par niveau
                    if (niveau != null && !niveau.trim().isEmpty() && !niveau.equals("tous")) {
                        if (!niveau.equals(apprenti.getNiveau())) {
                            return false;
                        }
                    }

                    // Filtre par année académique
                    if (anneeAcademique != null && !anneeAcademique.trim().isEmpty() && !anneeAcademique.equals("toutes")) {
                        if (!anneeAcademique.equals(apprenti.getAnneeAcademique())) {
                            return false;
                        }
                    }

                    // Filtre par entreprise
                    if (entreprise != null && !entreprise.trim().isEmpty()) {
                        if (apprenti.getEntreprise() == null ||
                                !apprenti.getEntreprise().getRaisonSociale().toLowerCase().contains(entreprise.toLowerCase().trim())) {
                            return false;
                        }
                    }

                    // Filtre par statut
                    if (statut != null && !statut.trim().isEmpty() && !statut.equals("tous")) {
                        boolean estArchive = apprenti.getArchive() != null && apprenti.getArchive();
                        if (statut.equals("actif") && estArchive) {
                            return false;
                        }
                        if (statut.equals("archive") && !estArchive) {
                            return false;
                        }
                    }

                    // Filtre par mots-clés de mission
                    if (motCle != null && !motCle.trim().isEmpty()) {
                        if (apprenti.getMissionMotsCles() == null ||
                                !apprenti.getMissionMotsCles().toLowerCase().contains(motCle.toLowerCase().trim())) {
                            return false;
                        }
                    }

                    return true;
                })
                .collect(Collectors.toList());

        model.addAttribute("apprentis", apprentisFiltres);
        model.addAttribute("totalApprentis", apprentisFiltres.size());
        model.addAttribute("totalApprentisGlobal", tousLesApprentis.size());

        // Données pour les filtres
        model.addAttribute("entreprises", entrepriseService.getAllEntreprises());
        model.addAttribute("annees", anneeAcademiqueService.getAllAnnees());

        // Valeurs actuelles des filtres pour maintenir l'état
        model.addAttribute("filtreNom", nom != null ? nom : "");
        model.addAttribute("filtrePrenom", prenom != null ? prenom : "");
        model.addAttribute("filtreNiveau", niveau != null ? niveau : "tous");
        model.addAttribute("filtreAnneeAcademique", anneeAcademique != null ? anneeAcademique : "toutes");
        model.addAttribute("filtreEntreprise", entreprise != null ? entreprise : "");
        model.addAttribute("filtreStatut", statut != null ? statut : "tous");
        model.addAttribute("filtreMotCle", motCle != null ? motCle : "");

        return "recherche/liste";
    }
}
