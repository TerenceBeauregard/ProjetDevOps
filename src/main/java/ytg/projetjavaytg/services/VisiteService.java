package ytg.projetjavaytg.services;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ytg.projetjavaytg.models.Visite;
import ytg.projetjavaytg.repositories.VisiteRepository;
import ytg.projetjavaytg.exception.ResourceNotFoundException;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Service
public class VisiteService {

    private final VisiteRepository visiteRepository;

    public VisiteService(VisiteRepository visiteRepository) {
        this.visiteRepository = visiteRepository;
    }

    public List<Visite> getAllVisites() {
        List<Visite>  visites = visiteRepository.findAll();
        if (visites.isEmpty()){
            throw new RuntimeException("Aucune visite en base");
        }
        return visites;
    }

    public Optional<Visite> getVisiteById(Long id) {
        Optional<Visite> visite = visiteRepository.findById(id);
        if (visite.isPresent()){
            return visite;
        }
        throw new ResourceNotFoundException("Aucune visite trouver avec l'id " + id);
    }

    @Transactional
    public Visite createVisite(Visite visite) {
        visite.setDateCreation(Instant.now());
        return visiteRepository.save(visite);
    }
}
