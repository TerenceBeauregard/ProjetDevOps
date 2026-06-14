package ytg.projetjavaytg.controllers.api;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ytg.projetjavaytg.models.Apprenti;
import ytg.projetjavaytg.services.ApprentiService;

import java.util.List;

import io.swagger.v3.oas.annotations.tags.Tag;
import ytg.projetjavaytg.exception.ResourceNotFoundException;

@Tag(name = "Apprenti")
@RestController
@RequestMapping("/api/apprentis")
public class ApprentiController {

    private final ApprentiService apprentiService;

    public ApprentiController(ApprentiService apprentiService) {
        this.apprentiService = apprentiService;
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
    public ResponseEntity<Apprenti> createApprenti(@RequestBody Apprenti apprenti) {
        Apprenti createdApprenti = apprentiService.createApprenti(apprenti);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdApprenti);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteApprenti(@PathVariable Long id) {
        apprentiService.deleteApprenti(id);
        return ResponseEntity.noContent().build();
    }
}
