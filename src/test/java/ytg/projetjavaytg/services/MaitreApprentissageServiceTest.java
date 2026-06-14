package ytg.projetjavaytg.services;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ytg.projetjavaytg.models.MaitreApprentissage;
import ytg.projetjavaytg.repositories.MaitreApprentissageRepository;
import ytg.projetjavaytg.exception.ResourceNotFoundException;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MaitreApprentissageServiceTest {

    @Mock
    private MaitreApprentissageRepository maitreApprentissageRepository;

    @InjectMocks
    private MaitreApprentissageService maitreApprentissageService;

    @Test
    void getAllMaitresApprentissage_retourneListeQuandNonVide() {
        MaitreApprentissage maitre = new MaitreApprentissage();
        maitre.setId(1L);
        maitre.setNom("Dupont");
        when(maitreApprentissageRepository.findAll()).thenReturn(List.of(maitre));

        List<MaitreApprentissage> result = maitreApprentissageService.getAllMaitresApprentissage();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getNom()).isEqualTo("Dupont");
    }

    @Test
    void getAllMaitresApprentissage_leveExceptionQuandListeVide() {
        when(maitreApprentissageRepository.findAll()).thenReturn(Collections.emptyList());

        assertThatThrownBy(() -> maitreApprentissageService.getAllMaitresApprentissage())
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Aucun maitre d'apprentissage en base");
    }

    @Test
    void getMaitreApprentissageById_retourneQuandTrouve() {
        MaitreApprentissage maitre = new MaitreApprentissage();
        maitre.setId(1L);
        when(maitreApprentissageRepository.findById(1L)).thenReturn(Optional.of(maitre));

        Optional<MaitreApprentissage> result = maitreApprentissageService.getMaitreApprentissageById(1L);

        assertThat(result).isPresent();
        assertThat(result.get().getId()).isEqualTo(1L);
    }

    @Test
    void getMaitreApprentissageById_leveExceptionSiNonTrouve() {
        when(maitreApprentissageRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> maitreApprentissageService.getMaitreApprentissageById(99L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("99");
    }

    @Test
    void createMaitreApprentissage_sauvegardeEtRetourne() {
        MaitreApprentissage maitre = new MaitreApprentissage();
        maitre.setNom("Martin");
        when(maitreApprentissageRepository.save(maitre)).thenReturn(maitre);

        MaitreApprentissage result = maitreApprentissageService.createMaitreApprentissage(maitre);

        assertThat(result.getNom()).isEqualTo("Martin");
        verify(maitreApprentissageRepository).save(maitre);
    }

    @Test
    void deleteMaitreApprentissage_supprimeSiExiste() {
        MaitreApprentissage maitre = new MaitreApprentissage();
        maitre.setId(1L);
        when(maitreApprentissageRepository.findById(1L)).thenReturn(Optional.of(maitre));

        maitreApprentissageService.deleteMaitreApprentissage(1L);

        verify(maitreApprentissageRepository).deleteById(1L);
    }

    @Test
    void deleteMaitreApprentissage_leveExceptionSiNonTrouve() {
        when(maitreApprentissageRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> maitreApprentissageService.deleteMaitreApprentissage(99L))
                .isInstanceOf(ResourceNotFoundException.class);

        verify(maitreApprentissageRepository, never()).deleteById(any());
    }

    @Test
    void findAllMaitresApprentissage_retourneListeQuandNonVide() {
        MaitreApprentissage maitre = new MaitreApprentissage();
        maitre.setId(1L);
        when(maitreApprentissageRepository.findAllByEntrepriseRaisonSociale("Acme")).thenReturn(List.of(maitre));

        List<MaitreApprentissage> result = maitreApprentissageService.findAllMaitresApprentissage("Acme");

        assertThat(result).hasSize(1);
    }

    @Test
    void findAllMaitresApprentissage_leveExceptionQuandVide() {
        when(maitreApprentissageRepository.findAllByEntrepriseRaisonSociale("Inconnue"))
                .thenReturn(Collections.emptyList());

        assertThatThrownBy(() -> maitreApprentissageService.findAllMaitresApprentissage("Inconnue"))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Inconnue");
    }
}
