package ytg.projetjavaytg.services;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ytg.projetjavaytg.Models.Apprenti;
import ytg.projetjavaytg.Repositories.ApprentiRepository;
import ytg.projetjavaytg.Repositories.EvaluationRepository;
import ytg.projetjavaytg.Repositories.VisiteRepository;
import ytg.projetjavaytg.Services.AnneeAcademiqueService;
import ytg.projetjavaytg.Services.ApprentiService;
import ytg.projetjavaytg.exception.ResourceNotFoundException;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ApprentiServiceTest {

    @Mock
    private ApprentiRepository apprentiRepository;

    @Mock
    private AnneeAcademiqueService anneeAcademiqueService;

    @Mock
    private EvaluationRepository evaluationRepository;

    @Mock
    private VisiteRepository visiteRepository;

    @InjectMocks
    private ApprentiService apprentiService;

    @Test
    void getAllApprentis_retourneListeQuandNonVide() {
        Apprenti apprenti = new Apprenti();
        apprenti.setId(1L);
        apprenti.setNom("Dupont");
        when(apprentiRepository.findAll()).thenReturn(List.of(apprenti));

        List<Apprenti> result = apprentiService.getAllApprentis();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getNom()).isEqualTo("Dupont");
    }

    @Test
    void getAllApprentis_leveExceptionQuandListeVide() {
        when(apprentiRepository.findAll()).thenReturn(Collections.emptyList());

        assertThatThrownBy(() -> apprentiService.getAllApprentis())
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Aucun apprenti existant en base");
    }

    @Test
    void getApprentiById_retourneApprentiQuandTrouve() {
        Apprenti apprenti = new Apprenti();
        apprenti.setId(1L);
        when(apprentiRepository.findById(1L)).thenReturn(Optional.of(apprenti));

        Optional<Apprenti> result = apprentiService.getApprentiById(1L);

        assertThat(result).isPresent();
        assertThat(result.get().getId()).isEqualTo(1L);
    }

    @Test
    void getApprentiById_leveExceptionSiNonTrouve() {
        when(apprentiRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> apprentiService.getApprentiById(99L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("99");
    }

    @Test
    void createApprenti_sauvegardeEtRetourneApprenti() {
        Apprenti apprenti = new Apprenti();
        apprenti.setNom("Martin");
        apprenti.setNiveau("I1");
        apprenti.setAnneeAcademique("2024-2025");
        apprenti.setArchive(false);
        when(apprentiRepository.save(any(Apprenti.class))).thenReturn(apprenti);

        Apprenti result = apprentiService.createApprenti(apprenti);

        assertThat(result.getNom()).isEqualTo("Martin");
        verify(apprentiRepository).save(apprenti);
    }

    @Test
    void createApprenti_initialiseAnneeAcademiqueParDefautSiAbsente() {
        Apprenti apprenti = new Apprenti();
        apprenti.setNiveau("I1");
        // anneeAcademique null => doit appeler anneeAcademiqueService
        when(anneeAcademiqueService.getAnneeAcademiqueEnCours()).thenReturn("2025-2026");
        when(apprentiRepository.save(any(Apprenti.class))).thenAnswer(inv -> inv.getArgument(0));

        Apprenti result = apprentiService.createApprenti(apprenti);

        assertThat(result.getAnneeAcademique()).isEqualTo("2025-2026");
        verify(anneeAcademiqueService).getAnneeAcademiqueEnCours();
    }

    @Test
    void createApprenti_initialiseNiveauParDefautSiNull() {
        Apprenti apprenti = new Apprenti();
        apprenti.setNiveau(null);
        apprenti.setAnneeAcademique("2025-2026");
        when(apprentiRepository.save(any(Apprenti.class))).thenAnswer(inv -> inv.getArgument(0));

        Apprenti result = apprentiService.createApprenti(apprenti);

        assertThat(result.getNiveau()).isEqualTo("I1");
    }

    @Test
    void deleteApprenti_supprimeDependancesPuisApprenti() {
        Apprenti apprenti = new Apprenti();
        apprenti.setId(1L);
        when(apprentiRepository.findById(1L)).thenReturn(Optional.of(apprenti));

        apprentiService.deleteApprenti(1L);

        verify(evaluationRepository).deleteByApprentiId(1L);
        verify(visiteRepository).deleteByApprentiId(1L);
        verify(apprentiRepository).deleteById(1L);
    }

    @Test
    void deleteApprenti_leveExceptionSiApprentiNonTrouve() {
        when(apprentiRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> apprentiService.deleteApprenti(99L))
                .isInstanceOf(ResourceNotFoundException.class);

        verify(apprentiRepository, never()).deleteById(any());
    }

    @Test
    void getAllByRaisonSociale_retourneListeQuandEntrepriseExiste() {
        Apprenti apprenti = new Apprenti();
        when(apprentiRepository.findAllByRaisonSocialeIgnoreCase("Acme")).thenReturn(List.of(apprenti));

        List<Apprenti> result = apprentiService.getAllByRaisonSociale("Acme");

        assertThat(result).hasSize(1);
    }

    @Test
    void getAllByRaisonSociale_leveExceptionSiAucunApprenti() {
        when(apprentiRepository.findAllByRaisonSocialeIgnoreCase("Inconnue"))
                .thenReturn(Collections.emptyList());

        assertThatThrownBy(() -> apprentiService.getAllByRaisonSociale("Inconnue"))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Inconnue");
    }

    @Test
    void getApprentisNonArchives_retourneListe() {
        Apprenti apprenti = new Apprenti();
        apprenti.setArchive(false);
        when(apprentiRepository.findByArchiveFalse()).thenReturn(List.of(apprenti));

        List<Apprenti> result = apprentiService.getApprentisNonArchives();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getArchive()).isFalse();
    }
}
