package ytg.projetjavaytg.services;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ytg.projetjavaytg.models.Utilisateur;
import ytg.projetjavaytg.repositories.UtilisateurRepository;
import ytg.projetjavaytg.exception.ResourceNotFoundException;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Service
public class UtilisateurService {

    private final UtilisateurRepository utilisateurRepository;

    public UtilisateurService(UtilisateurRepository utilisateurRepository) {
        this.utilisateurRepository = utilisateurRepository;
    }

    public List<Utilisateur> getAllUtilisateurs() {
        List<Utilisateur> utilisateurs = utilisateurRepository.findAll();
        if (utilisateurs.isEmpty()){
            throw new ResourceNotFoundException("Aucun utilisateur en base");
        }
        return utilisateurs;
    }

    public Optional<Utilisateur> getUtilisateurById(Long id) {
        Optional<Utilisateur> utilisateur = utilisateurRepository.findById(id);
        if (utilisateur.isPresent()){
            return utilisateur;
        }
        throw new ResourceNotFoundException("Aucun utilisateur trouvé avec l'id " + id);
    }

    @Transactional
    public Utilisateur createUtilisateur(Utilisateur utilisateur) {
        utilisateur.setDateCreation(Instant.now());
        if (utilisateur.getEnabled() == null) {
            utilisateur.setEnabled(true);
        }
        if (utilisateur.getRole() == null) {
            utilisateur.setRole("ROLE_TUTEUR");
        }
        return utilisateurRepository.save(utilisateur);
    }
}
