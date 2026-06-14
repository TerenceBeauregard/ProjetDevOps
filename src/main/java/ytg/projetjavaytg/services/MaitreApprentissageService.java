package ytg.projetjavaytg.services;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ytg.projetjavaytg.models.MaitreApprentissage;
import ytg.projetjavaytg.repositories.MaitreApprentissageRepository;
import ytg.projetjavaytg.exception.ResourceNotFoundException;

import java.util.List;
import java.util.Optional;

@Service
public class MaitreApprentissageService {

    private final MaitreApprentissageRepository maitreApprentissageRepository;

    public MaitreApprentissageService(MaitreApprentissageRepository maitreApprentissageRepository) {
        this.maitreApprentissageRepository = maitreApprentissageRepository;
    }

    public List<MaitreApprentissage> getAllMaitresApprentissage() {
        List<MaitreApprentissage> maitreApprentissages = maitreApprentissageRepository.findAll();
        if (maitreApprentissages.isEmpty()){
            throw new RuntimeException("Aucun maitre d'apprentissage en base");
        }
        return maitreApprentissages;
    }

    public Optional<MaitreApprentissage> getMaitreApprentissageById(Long id) {
        Optional<MaitreApprentissage> maitreApprentissage = maitreApprentissageRepository.findById(id);
        if (maitreApprentissage.isPresent()){
            return maitreApprentissage;
        }
        throw new ResourceNotFoundException("Aucun maitre d'apprentissage trouver avec l'id " + id);
    }

    @Transactional
    public MaitreApprentissage createMaitreApprentissage(MaitreApprentissage maitreApprentissage) {
        return maitreApprentissageRepository.save(maitreApprentissage);
    }

    @Transactional
    public void deleteMaitreApprentissage(Long id) {
        maitreApprentissageRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Le maitre d'apprentissage que vous voulez supprimer n'existe pas"));
        maitreApprentissageRepository.deleteById(id);
    }

    public List<MaitreApprentissage> findAllMaitresApprentissage(String raisonSociale) {
        List<MaitreApprentissage> maitres = maitreApprentissageRepository.findAllByEntrepriseRaisonSociale(raisonSociale);
        if (maitres.isEmpty()){
            throw new ResourceNotFoundException("Aucun maitre d'apprentissage trouvé pour la raison sociale " + raisonSociale);
        }
        return maitres;
    }
}
