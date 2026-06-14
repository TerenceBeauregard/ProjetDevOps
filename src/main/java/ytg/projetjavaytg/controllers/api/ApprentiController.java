package ytg.projetjavaytg.controllers.api;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ytg.projetjavaytg.dto.ApprentiDTO;
import ytg.projetjavaytg.models.Apprenti;
import ytg.projetjavaytg.services.ApprentiService;
import ytg.projetjavaytg.services.EntrepriseService;
import ytg.projetjavaytg.services.MaitreApprentissageService;
import ytg.projetjavaytg.services.UtilisateurService;

import java.util.List;

import io.swagger.v3.oas.annotations.tags.Tag;
import ytg.projetjavaytg.exception.ResourceNotFoundException;

@Tag(name = "Apprenti")
@RestController
@RequestMapping("/api/apprentis")
public class ApprentiController {

    private final ApprentiService apprentiService;
    private final EntrepriseService entrepriseService;
    private final MaitreApprentissageService maitreApprentissageService;
    private final UtilisateurService utilisateurService;

    public ApprentiController(ApprentiService apprentiService,
                             EntrepriseService entrepriseService,
                             MaitreApprentissageService maitreApprentissageService,
                             UtilisateurService utilisateurService) {
        this.apprentiService = apprentiService;
        this.entrepriseService = entrepriseService;
        this.maitreApprentissageService = maitreApprentissageService;
        this.utilisateurService = utilisateurService;
    }

    @GetMapping
    public ResponseEntity<List<Apprenti>> getAllApprentis() {
        List<Apprenti> apprentis = apprentiService.getAllApprentis();
        return ResponseEntity.ok(apprentis);
    }

    @GetMapping("/by-raison-sociale/{raisonSociale}")
    public ResponseEntity<List<Apprenti>> getByRaisonSociale(@PathVariable String raisonSociale) {
        List<Apprenti> apprentis = apprentiService.getAllByRaisonSociale(raisonSociale);
        return ResponseEntity.ok(apprentis);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Apprenti> getApprentiById(@PathVariable Long id) {
        return apprentiService.getApprentiById(id)
                .map(ResponseEntity::ok)
                .orElseThrow(() -> new ResourceNotFoundException("Apprenti non trouvé avec id " + id));
    }

    @PostMapping
    public ResponseEntity<Apprenti> createApprenti(@RequestBody ApprentiDTO dto) {
        Apprenti apprenti = new Apprenti();
        apprenti.setNom(dto.getNom());
        apprenti.setPrenom(dto.getPrenom());
        apprenti.setEmail(dto.getEmail());
        apprenti.setTelephone(dto.getTelephone());
        apprenti.setProgramme(dto.getProgramme());
        apprenti.setAnneeAcademique(dto.getAnneeAcademique());
        apprenti.setMajeure(dto.getMajeure());
        apprenti.setNiveau(dto.getNiveau());
        apprenti.setMissionMotsCles(dto.getMissionMotsCles());
        apprenti.setMissionMetierCible(dto.getMissionMetierCible());
        apprenti.setMissionCommentaires(dto.getMissionCommentaires());
        apprenti.setFeedbackTuteur(dto.getFeedbackTuteur());
        apprenti.setRemarquesGenerales(dto.getRemarquesGenerales());
        if (dto.getEntrepriseId() != null) {
            entrepriseService.getEntrepriseById(dto.getEntrepriseId()).ifPresent(apprenti::setEntreprise);
        }
        if (dto.getMaitreApprentissageId() != null) {
            maitreApprentissageService.getMaitreApprentissageById(dto.getMaitreApprentissageId())
                    .ifPresent(apprenti::setMaitreApprentissage);
        }
        if (dto.getTuteurEnseignantId() != null) {
            utilisateurService.getUtilisateurById(dto.getTuteurEnseignantId())
                    .ifPresent(apprenti::setTuteurEnseignant);
        }
        Apprenti createdApprenti = apprentiService.createApprenti(apprenti);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdApprenti);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteApprenti(@PathVariable Long id) {
        apprentiService.deleteApprenti(id);
        return ResponseEntity.noContent().build();
    }
}
