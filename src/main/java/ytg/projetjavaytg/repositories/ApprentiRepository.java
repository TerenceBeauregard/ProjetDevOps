package ytg.projetjavaytg.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ytg.projetjavaytg.models.Apprenti;

import java.util.List;

public interface ApprentiRepository extends JpaRepository<Apprenti,Long> {

    // Récupère tous les apprentis dont l'entreprise a le nom exact (case-sensitive selon DB)
    @Query("select a from Apprenti a where a.entreprise.raisonSociale = :raisonSociale")
    List<Apprenti> findAllByRaisonSociale(@Param("raisonSociale") String raisonSociale);

    // Variante insensitive (fonctionne avec JPQL mais dépend du dialecte pour lower) :
    @Query("select a from Apprenti a where lower(a.entreprise.raisonSociale) = lower(:raisonSociale)")
    List<Apprenti> findAllByRaisonSocialeIgnoreCase(@Param("raisonSociale") String raisonSociale);
    List<Apprenti> findByArchiveFalse();

    @Modifying
    @Query("UPDATE Apprenti a SET a.niveau = :nouveauNiveau WHERE a.niveau = :ancienNiveau AND a.archive = false")
    int promouvoirApprentisByNiveau(@Param("ancienNiveau") String ancienNiveau, @Param("nouveauNiveau") String nouveauNiveau);

    @Modifying
    @Query("UPDATE Apprenti a SET a.archive = true WHERE a.niveau = 'I3' AND a.archive = false")
    int archiverApprentisI3();
}
