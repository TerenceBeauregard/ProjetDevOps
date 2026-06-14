package ytg.projetjavaytg.controllers.api;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ytg.projetjavaytg.dto.UtilisateurDTO;
import ytg.projetjavaytg.models.Utilisateur;
import ytg.projetjavaytg.services.UtilisateurService;

import java.util.List;

import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "Utilisateur")
@RestController
@RequestMapping("/api/utilisateurs")
public class UtilisateurController {

    private final UtilisateurService utilisateurService;

    public UtilisateurController(UtilisateurService utilisateurService) {
        this.utilisateurService = utilisateurService;
    }

    @GetMapping
    public ResponseEntity<List<Utilisateur>> getAllUtilisateurs() {
        List<Utilisateur> utilisateurs = utilisateurService.getAllUtilisateurs();
        return ResponseEntity.ok(utilisateurs);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Utilisateur> getUtilisateurById(@PathVariable Long id) {
        return utilisateurService.getUtilisateurById(id)
                .map(ResponseEntity::ok)
                .orElseThrow(() -> new RuntimeException("Aucun utilisateur avec l'id " + id + " n'existe"));
    }

    @PostMapping
    public ResponseEntity<Utilisateur> createUtilisateur(@RequestBody UtilisateurDTO dto) {
        Utilisateur utilisateur = new Utilisateur();
        utilisateur.setUsername(dto.getUsername());
        utilisateur.setPassword(dto.getPassword());
        utilisateur.setPrenom(dto.getPrenom());
        utilisateur.setNom(dto.getNom());
        utilisateur.setEmail(dto.getEmail());
        utilisateur.setRole(dto.getRole());
        utilisateur.setEnabled(dto.getEnabled());
        Utilisateur createdUtilisateur = utilisateurService.createUtilisateur(utilisateur);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdUtilisateur);
    }
}
