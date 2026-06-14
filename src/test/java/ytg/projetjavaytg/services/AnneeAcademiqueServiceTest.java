package ytg.projetjavaytg.services;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ytg.projetjavaytg.models.AnneeAcademique;
import ytg.projetjavaytg.repositories.AnneeAcademiqueRepository;

import java.time.Year;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AnneeAcademiqueServiceTest {

    @Mock
    private AnneeAcademiqueRepository anneeAcademiqueRepository;

    @Mock
    private AnneeAcademiqueService self;

    @InjectMocks
    private AnneeAcademiqueService anneeAcademiqueService;

    @Test
    void getAnneeAcademiqueEnCours_retourneAnneeActiveSiPresente() {
        AnneeAcademique annee = new AnneeAcademique();
        annee.setAnnee("2024-2025");
        annee.setActive(true);
        when(anneeAcademiqueRepository.findByActiveTrue()).thenReturn(Optional.of(annee));

        String result = anneeAcademiqueService.getAnneeAcademiqueEnCours();

        assertThat(result).isEqualTo("2024-2025");
        verify(self, never()).creerEtActiverAnnee(any());
    }

    @Test
    void getAnneeAcademiqueEnCours_creeNouvelleAnneeSiAucuneActive() {
        when(anneeAcademiqueRepository.findByActiveTrue()).thenReturn(Optional.empty());

        String result = anneeAcademiqueService.getAnneeAcademiqueEnCours();

        int currentYear = Year.now().getValue();
        String expected = currentYear + "-" + (currentYear + 1);
        assertThat(result).isEqualTo(expected);
        verify(self).creerEtActiverAnnee(expected);
    }

    @Test
    void creerEtActiverAnnee_desactiveAncienneAnneeEtCreeNouvelle() {
        AnneeAcademique ancienne = new AnneeAcademique();
        ancienne.setAnnee("2023-2024");
        ancienne.setActive(true);
        when(anneeAcademiqueRepository.findByActiveTrue()).thenReturn(Optional.of(ancienne));
        when(anneeAcademiqueRepository.save(any(AnneeAcademique.class))).thenAnswer(inv -> inv.getArgument(0));

        anneeAcademiqueService.creerEtActiverAnnee("2024-2025");

        assertThat(ancienne.isActive()).isFalse();
        verify(anneeAcademiqueRepository, times(2)).save(any(AnneeAcademique.class));
    }

    @Test
    void creerEtActiverAnnee_sansAncienneAnneeActive() {
        when(anneeAcademiqueRepository.findByActiveTrue()).thenReturn(Optional.empty());
        when(anneeAcademiqueRepository.save(any(AnneeAcademique.class))).thenAnswer(inv -> inv.getArgument(0));

        anneeAcademiqueService.creerEtActiverAnnee("2024-2025");

        verify(anneeAcademiqueRepository, times(1)).save(any(AnneeAcademique.class));
    }

    @Test
    void calculerAnneeSuivante_calculeCorrectementAvecFormatValide() {
        AnneeAcademique annee = new AnneeAcademique();
        annee.setAnnee("2024-2025");
        annee.setActive(true);
        when(anneeAcademiqueRepository.findByActiveTrue()).thenReturn(Optional.of(annee));

        String result = anneeAcademiqueService.calculerAnneeSuivante();

        assertThat(result).isEqualTo("2025-2026");
    }

    @Test
    void calculerAnneeSuivante_retourneAnneeCouranteSiFormatSansTiret() {
        AnneeAcademique annee = new AnneeAcademique();
        annee.setAnnee("2024");
        annee.setActive(true);
        when(anneeAcademiqueRepository.findByActiveTrue()).thenReturn(Optional.of(annee));

        String result = anneeAcademiqueService.calculerAnneeSuivante();

        int currentYear = Year.now().getValue();
        assertThat(result).isEqualTo(currentYear + "-" + (currentYear + 1));
    }

    @Test
    void calculerAnneeSuivante_retourneAnneeCouranteSiPartiesNonNumeriques() {
        AnneeAcademique annee = new AnneeAcademique();
        annee.setAnnee("abcd-efgh");
        annee.setActive(true);
        when(anneeAcademiqueRepository.findByActiveTrue()).thenReturn(Optional.of(annee));

        String result = anneeAcademiqueService.calculerAnneeSuivante();

        int currentYear = Year.now().getValue();
        assertThat(result).isEqualTo(currentYear + "-" + (currentYear + 1));
    }

    @Test
    void getAllAnnees_retourneListeTrieeParOrdreDecroissant() {
        AnneeAcademique a1 = new AnneeAcademique();
        a1.setAnnee("2023-2024");
        AnneeAcademique a2 = new AnneeAcademique();
        a2.setAnnee("2025-2026");
        AnneeAcademique a3 = new AnneeAcademique();
        a3.setAnnee("2024-2025");
        when(anneeAcademiqueRepository.findAll()).thenReturn(List.of(a1, a2, a3));

        List<String> result = anneeAcademiqueService.getAllAnnees();

        assertThat(result).containsExactly("2025-2026", "2024-2025", "2023-2024");
    }
}
