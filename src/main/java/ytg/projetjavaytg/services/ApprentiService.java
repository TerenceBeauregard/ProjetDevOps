package ytg.projetjavaytg.services;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ytg.projetjavaytg.models.Apprenti;
import ytg.projetjavaytg.repositories.ApprentiRepository;
import ytg.projetjavaytg.repositories.EvaluationRepository;
import ytg.projetjavaytg.repositories.VisiteRepository;
import ytg.projetjavaytg.exception.ResourceNotFoundException;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Service
public class ApprentiService {

    private final ApprentiRepository apprentiRepository;
    private final AnneeAcademiqueService anneeAcademiqueService;
    private final EvaluationRepository evaluationRepository;
    private final VisiteRepository visiteRepository;

    public ApprentiService(ApprentiRepository apprentiRepository,
                          AnneeAcademiqueService anneeAcademiqueService,
                          EvaluationRepository evaluationRepository,
                          VisiteRepository visiteRepository) {
        this.apprentiRepository = apprentiRepository;
        this.anneeAcademiqueService = anneeAcademiqueService;
        this.evaluationRepository = evaluationRepository;
        this.visiteRepository = visiteRepository;
    }

    public List<Apprenti> getAllApprentis() {
        List<Apprenti> apprentis = apprentiRepository.findAll();
        if (apprentis.isEmpty()){
            throw new ResourceNotFoundException("Aucun apprenti existant en base");
        }
        return apprentis;
    }

    public Optional<Apprenti> getApprentiById(Long id) {
        Optional<Apprenti> apprenti = apprentiRepository.findById(id);
        if (apprenti.isPresent()){
            return apprenti;
        }
        throw new ResourceNotFoundException("Aucun apprenti trouvé avec l'id " + id);
    }

    @Transactional
    public Apprenti createApprenti(Apprenti apprenti) {
        apprenti.setDateCreation(Instant.now());
        apprenti.setDateModification(Instant.now()); // Est-ce qu'on laisse la date de modification a null pour une creation ?
        if (apprenti.getArchive() == null) {
            apprenti.setArchive(false);
        }
        if (apprenti.getNiveau() == null) {
            apprenti.setNiveau("I1"); // valeur par defaut en base
        }
        if (apprenti.getAnneeAcademique() == null || apprenti.getAnneeAcademique().isEmpty()) {
            String anneeEnCours = anneeAcademiqueService.getAnneeAcademiqueEnCours();
            apprenti.setAnneeAcademique(anneeEnCours);
        }
        return apprentiRepository.save(apprenti);
    }

    @Transactional
    public void deleteApprenti(Long id) {
        // Vérifier que l'apprenti existe
        apprentiRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("L'apprenti que vous voulez supprimer n'existe pas"));
        // Supprimer d'abord les entités dépendantes dans la même transaction
        evaluationRepository.deleteByApprentiId(id);
        visiteRepository.deleteByApprentiId(id);
        // Puis supprimer l'apprenti
        apprentiRepository.deleteById(id);
    }

    public List<Apprenti> getAllByRaisonSociale(String raisonSociale) {
        List<Apprenti> apprentis = apprentiRepository.findAllByRaisonSocialeIgnoreCase(raisonSociale);
        if (apprentis.isEmpty()){
            throw new ResourceNotFoundException("Aucun apprenti trouvé dans l'entreprise " + raisonSociale);
        }
        return apprentis;
    }

    public List<Apprenti> getApprentisNonArchives() {
        return apprentiRepository.findByArchiveFalse();
    }

    @Transactional
    public void creerNouvelleAnneeAcademique(String nouvelleAnnee) {
        anneeAcademiqueService.creerEtActiverAnnee(nouvelleAnnee);
        apprentiRepository.archiverApprentisI3();
        apprentiRepository.promouvoirApprentisByNiveau("I2", "I3");
        apprentiRepository.promouvoirApprentisByNiveau("I1", "I2");
        List<Apprenti> apprentisActifs = apprentiRepository.findByArchiveFalse();
        for (Apprenti apprenti : apprentisActifs) {
            apprenti.setAnneeAcademique(nouvelleAnnee);
            apprenti.setDateModification(Instant.now());
            apprentiRepository.save(apprenti);
        }
    }

    @Transactional
    public Apprenti updateApprenti(Long id, Apprenti apprentiDetails) {
        Optional<Apprenti> apprentiOpt = apprentiRepository.findById(id);
        if (apprentiOpt.isPresent()) {
            Apprenti apprenti = apprentiOpt.get();
            apprenti.setNom(apprentiDetails.getNom());
            apprenti.setPrenom(apprentiDetails.getPrenom());
            apprenti.setEmail(apprentiDetails.getEmail());
            apprenti.setTelephone(apprentiDetails.getTelephone());
            apprenti.setProgramme(apprentiDetails.getProgramme());
            apprenti.setAnneeAcademique(apprentiDetails.getAnneeAcademique());
            apprenti.setMajeure(apprentiDetails.getMajeure());
            apprenti.setNiveau(apprentiDetails.getNiveau());
            apprenti.setEntreprise(apprentiDetails.getEntreprise());
            apprenti.setMaitreApprentissage(apprentiDetails.getMaitreApprentissage());
            apprenti.setMissionMotsCles(apprentiDetails.getMissionMotsCles());
            apprenti.setMissionMetierCible(apprentiDetails.getMissionMetierCible());
            apprenti.setMissionCommentaires(apprentiDetails.getMissionCommentaires());
            apprenti.setFeedbackTuteur(apprentiDetails.getFeedbackTuteur());
            apprenti.setRemarquesGenerales(apprentiDetails.getRemarquesGenerales());
            apprenti.setDateModification(Instant.now());
            return apprentiRepository.save(apprenti);
        }
        return null;
    }
}
