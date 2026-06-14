package ytg.projetjavaytg.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import ytg.projetjavaytg.models.Utilisateur;
import java.util.Optional;

public interface UtilisateurRepository extends JpaRepository<Utilisateur, Long> {
    Optional<Utilisateur> findByUsername(String username);
}

