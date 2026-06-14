package ytg.projetjavaytg.controllers.api;

import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ytg.projetjavaytg.models.MaitreApprentissage;
import ytg.projetjavaytg.services.MaitreApprentissageService;
import ytg.projetjavaytg.exception.ResourceNotFoundException;

import java.util.List;

@Tag(name = "MaitreApprentissage")
@RestController
@RequestMapping("/api/maitreapprentissages")
public class MaitreApprentissageController {

    private final MaitreApprentissageService maitreApprentissageService;

    public MaitreApprentissageController(MaitreApprentissageService maitreApprentissageService) {
        this.maitreApprentissageService = maitreApprentissageService;
    }

    @GetMapping
    public ResponseEntity<List<MaitreApprentissage>> getAllMaitresApprentissage() {
        List<MaitreApprentissage> maitres = maitreApprentissageService.getAllMaitresApprentissage();
        return ResponseEntity.ok(maitres);
    }

    @GetMapping("/{id}")
    public ResponseEntity<MaitreApprentissage> getMaitreApprentissageById(@PathVariable Long id) {
        return maitreApprentissageService.getMaitreApprentissageById(id)
                .map(ResponseEntity::ok)
                .orElseThrow(() -> new ResourceNotFoundException("Aucun apprenti trouvé avec l'id " + id ));
    }

    @PostMapping
    public ResponseEntity<MaitreApprentissage> createMaitreApprentissage(@RequestBody MaitreApprentissage maitreApprentissage) {
        MaitreApprentissage createdMaitre = maitreApprentissageService.createMaitreApprentissage(maitreApprentissage);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdMaitre);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteMaitreApprentissage(@PathVariable Long id) {
        maitreApprentissageService.deleteMaitreApprentissage(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("by-raison-sociale/{raisonSociale}")
    public ResponseEntity<List<MaitreApprentissage>> getByRaisonSociale(@PathVariable String raisonSociale) {
        List<MaitreApprentissage> maitres = maitreApprentissageService.findAllMaitresApprentissage(raisonSociale);
        return ResponseEntity.ok(maitres);
    }
}
