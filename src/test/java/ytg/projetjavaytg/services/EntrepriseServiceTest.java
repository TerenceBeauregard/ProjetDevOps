package ytg.projetjavaytg.services;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ytg.projetjavaytg.models.Entreprise;
import ytg.projetjavaytg.repositories.EntrepriseRepository;
import ytg.projetjavaytg.exception.ResourceNotFoundException;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EntrepriseServiceTest {

    @Mock
    private EntrepriseRepository entrepriseRepository;

    @InjectMocks
    private EntrepriseService entrepriseService;

    @Test
    void getAllEntreprises_retourneListeQuandNonVide() {
        Entreprise e = new Entreprise();
        e.setId(1L);
        e.setRaisonSociale("Acme");
        when(entrepriseRepository.findAll()).thenReturn(List.of(e));

        List<Entreprise> result = entrepriseService.getAllEntreprises();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getRaisonSociale()).isEqualTo("Acme");
    }

    @Test
    void getAllEntreprises_leveExceptionQuandListeVide() {
        when(entrepriseRepository.findAll()).thenReturn(Collections.emptyList());

        assertThatThrownBy(() -> entrepriseService.getAllEntreprises())
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Aucune entreprise en base");
    }

    @Test
    void getEntrepriseById_retourneEntrepriseQuandTrouvee() {
        Entreprise e = new Entreprise();
        e.setId(1L);
        e.setRaisonSociale("Acme");
        when(entrepriseRepository.findById(1L)).thenReturn(Optional.of(e));

        Optional<Entreprise> result = entrepriseService.getEntrepriseById(1L);

        assertThat(result).isPresent();
        assertThat(result.get().getRaisonSociale()).isEqualTo("Acme");
    }

    @Test
    void getEntrepriseById_leveExceptionSiNonTrouvee() {
        when(entrepriseRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> entrepriseService.getEntrepriseById(99L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("99");
    }

    @Test
    void createEntreprise_sauvegardeEtRetourneEntreprise() {
        Entreprise e = new Entreprise();
        e.setRaisonSociale("Test Corp");
        when(entrepriseRepository.save(e)).thenReturn(e);

        Entreprise result = entrepriseService.createEntreprise(e);

        assertThat(result.getRaisonSociale()).isEqualTo("Test Corp");
        verify(entrepriseRepository).save(e);
    }

    @Test
    void deleteEntreprise_supprimeSiEntrepriseExiste() {
        Entreprise e = new Entreprise();
        e.setId(1L);
        when(entrepriseRepository.findById(1L)).thenReturn(Optional.of(e));

        entrepriseService.deleteEntreprise(1L);

        verify(entrepriseRepository).deleteById(1L);
    }

    @Test
    void deleteEntreprise_leveExceptionSiNonTrouvee() {
        when(entrepriseRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> entrepriseService.deleteEntreprise(99L))
                .isInstanceOf(ResourceNotFoundException.class);

        verify(entrepriseRepository, never()).deleteById(any());
    }
}
