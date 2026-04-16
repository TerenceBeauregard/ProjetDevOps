package ytg.projetjavaytg.services;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ytg.projetjavaytg.Models.Visite;
import ytg.projetjavaytg.Repositories.VisiteRepository;
import ytg.projetjavaytg.Services.VisiteService;
import ytg.projetjavaytg.exception.ResourceNotFoundException;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class VisiteServiceTest {

    @Mock
    private VisiteRepository visiteRepository;

    @InjectMocks
    private VisiteService visiteService;

    @Test
    void getAllVisites_retourneListeQuandNonVide() {
        Visite v = new Visite();
        v.setId(1L);
        v.setDateVisite(LocalDate.now());
        when(visiteRepository.findAll()).thenReturn(List.of(v));

        List<Visite> result = visiteService.getAllVisites();

        assertThat(result).hasSize(1);
    }

    @Test
    void getAllVisites_leveRuntimeExceptionQuandListeVide() {
        when(visiteRepository.findAll()).thenReturn(Collections.emptyList());

        assertThatThrownBy(() -> visiteService.getAllVisites())
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Aucune visite en base");
    }

    @Test
    void getVisiteById_retourneVisiteQuandTrouvee() {
        Visite v = new Visite();
        v.setId(1L);
        when(visiteRepository.findById(1L)).thenReturn(Optional.of(v));

        Optional<Visite> result = visiteService.getVisiteById(1L);

        assertThat(result).isPresent();
        assertThat(result.get().getId()).isEqualTo(1L);
    }

    @Test
    void getVisiteById_leveExceptionSiNonTrouvee() {
        when(visiteRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> visiteService.getVisiteById(99L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("99");
    }

    @Test
    void createVisite_definidatecreationEtSauvegarde() {
        Visite v = new Visite();
        v.setDateVisite(LocalDate.of(2025, 3, 15));
        v.setFormat("présentiel");
        when(visiteRepository.save(any(Visite.class))).thenAnswer(inv -> inv.getArgument(0));

        Visite result = visiteService.createVisite(v);

        assertThat(result.getDateCreation()).isNotNull();
        assertThat(result.getFormat()).isEqualTo("présentiel");
        verify(visiteRepository).save(v);
    }
}
