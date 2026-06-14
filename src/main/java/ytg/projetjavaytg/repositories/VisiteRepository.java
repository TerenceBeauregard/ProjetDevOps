package ytg.projetjavaytg.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import ytg.projetjavaytg.models.Visite;

import java.util.List;

public interface VisiteRepository extends JpaRepository<Visite,Long> {
    List<Visite> findByApprentiIdOrderByDateVisiteDesc(Long apprentiId);
    void deleteByApprentiId(Long apprentiId);
}
