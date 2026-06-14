package ytg.projetjavaytg.services;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ytg.projetjavaytg.models.Entreprise;
import ytg.projetjavaytg.repositories.EntrepriseRepository;
import ytg.projetjavaytg.exception.ResourceNotFoundException;

import java.util.List;
import java.util.Optional;

@Service
public class EntrepriseService {

    private final EntrepriseRepository entrepriseRepository;

    public EntrepriseService(EntrepriseRepository entrepriseRepository) {
        this.entrepriseRepository = entrepriseRepository;
    }

    public List<Entreprise> getAllEntreprises() {
        List<Entreprise> entreprises = entrepriseRepository.findAll();
        if (entreprises.isEmpty()){
            throw new ResourceNotFoundException("Aucune entreprise en base");
        }
        return entreprises;
    }

    public Optional<Entreprise> getEntrepriseById(Long id) {
        Optional<Entreprise> entreprise = entrepriseRepository.findById(id);
        if (entreprise.isPresent()){
            return entreprise;
        }
        throw new ResourceNotFoundException("Aucune entreprise trouvée avec l'id " + id);
    }

    @Transactional
    public Entreprise createEntreprise(Entreprise entreprise) {
        return entrepriseRepository.save(entreprise);
    }

    @Transactional
    public void deleteEntreprise(Long id) {
        entrepriseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("L'entreprise que vous voulez supprimer n'existe pas" ));
        entrepriseRepository.deleteById(id);
    }
}
