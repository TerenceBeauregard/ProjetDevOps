package ytg.projetjavaytg.controllers.api;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ytg.projetjavaytg.dto.CreateVisiteDTO;
import ytg.projetjavaytg.dto.VisiteDTO;
import ytg.projetjavaytg.models.Apprenti;
import ytg.projetjavaytg.models.Visite;
import ytg.projetjavaytg.services.VisiteService;
import ytg.projetjavaytg.services.ApprentiService;

import java.time.LocalDate;
import java.util.List;

import io.swagger.v3.oas.annotations.tags.Tag;
import ytg.projetjavaytg.exception.ResourceNotFoundException;

@Tag(name = "Visite")
@RestController
@RequestMapping("/api/visites")
public class VisiteController {

    private final VisiteService visiteService;
    private final ApprentiService apprentiService;

    public VisiteController(VisiteService visiteService, ApprentiService apprentiService) {
        this.visiteService = visiteService;
        this.apprentiService = apprentiService;
    }

    @GetMapping
    public ResponseEntity<List<Visite>> getAllVisites() {
        List<Visite> visites = visiteService.getAllVisites();
        return ResponseEntity.ok(visites);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Visite> getVisiteById(@PathVariable Long id) {
        return visiteService.getVisiteById(id)
                .map(ResponseEntity::ok)
                .orElseThrow(() -> new ResourceNotFoundException("Aucune viste trouvé avec l'id " + id ));
    }

    @PostMapping
    public ResponseEntity<Visite> createVisite(@RequestBody VisiteDTO dto) {
        Apprenti apprenti = apprentiService.getApprentiById(dto.getApprentiId())
                .orElseThrow(() -> new ResourceNotFoundException("Aucun apprenti trouvé avec l'id " + dto.getApprentiId()));

        Visite visite = new Visite();
        visite.setApprenti(apprenti);
        visite.setDateVisite(dto.getDateVisite());
        visite.setFormat(dto.getFormat());
        visite.setCommentaires(dto.getCommentaires());

        Visite createdVisite = visiteService.createVisite(visite);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdVisite);
    }

    @PostMapping("/simple")
    public ResponseEntity<?> createVisiteSimple(@RequestBody CreateVisiteDTO dto) {
        if (dto == null || dto.getApprentiId() == null || dto.getDateVisite() == null) {
            return ResponseEntity.badRequest().body("apprentiId and dateVisite required");
        }

        // parse date first (must be yyyy-MM-dd)
        final LocalDate parsedDate;
        try {
            parsedDate = LocalDate.parse(dto.getDateVisite());
        } catch (Exception ex) {
            return ResponseEntity.badRequest().body("dateVisite must be in format yyyy-MM-dd");
        }

        // Validate and normalize format (final so can be used in lambda)
        final String savedFormat;
        if (dto.getFormat() == null || dto.getFormat().trim().isEmpty()) {
            savedFormat = null;
        } else {
            String f = dto.getFormat().trim().toLowerCase();
            if (f.equals("présentiel") || f.equals("presentiel")) {
                savedFormat = "présentiel";
            } else if (f.equals("hybride")) {
                savedFormat = "hybride";
            } else if (f.equals("distance") || f.equals("à distance") || f.equals("a distance") || f.equals("adistance")) {
                savedFormat = "distance";
            } else {
                return ResponseEntity.badRequest().body("Le format doit être : présentiel, hybride, distance");
            }
        }

        return apprentiService.getApprentiById(dto.getApprentiId())
                .map(apprenti -> {
                    try {
                        Visite v = new Visite();
                        v.setApprenti(apprenti);
                        v.setDateVisite(parsedDate);
                        v.setFormat(savedFormat);
                        v.setCommentaires(dto.getCommentaires());
                        Visite created = visiteService.createVisite(v);
                        return ResponseEntity.status(HttpStatus.CREATED).body(created);
                    } catch (Exception e) {
                        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
                    }
                })
                .orElseThrow(() -> new ResourceNotFoundException("Aucun apprenti trouvé avec l'id " + dto.getApprentiId()));
    }

}
