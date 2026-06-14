package ytg.projetjavaytg.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import ytg.projetjavaytg.models.Entreprise;

public interface EntrepriseRepository extends JpaRepository<Entreprise,Long> {
}
