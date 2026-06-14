package ytg.projetjavaytg.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import ytg.projetjavaytg.models.Evaluation;

import java.util.Optional;

public interface EvaluationRepository extends JpaRepository<Evaluation,Long> {
    Optional<Evaluation> findByApprentiId(Long apprentiId);
    void deleteByApprentiId(Long apprentiId);
}
