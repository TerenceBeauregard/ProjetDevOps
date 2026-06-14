package ytg.projetjavaytg.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import ytg.projetjavaytg.models.MaitreApprentissage;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface MaitreApprentissageRepository extends JpaRepository<MaitreApprentissage,Long> {
    // JPQL: récupère tous les maîtres d'apprentissage dont l'entreprise a la raison sociale fournie
    @Query("SELECT m FROM MaitreApprentissage m WHERE m.entreprise.raisonSociale = :raisonSociale")
    List<MaitreApprentissage> findAllByEntrepriseRaisonSociale(@Param("raisonSociale") String raisonSociale);
}
