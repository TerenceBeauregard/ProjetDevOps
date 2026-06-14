package ytg.projetjavaytg.services;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ytg.projetjavaytg.models.Utilisateur;
import ytg.projetjavaytg.repositories.UtilisateurRepository;
import ytg.projetjavaytg.exception.ResourceNotFoundException;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UtilisateurServiceTest {

    @Mock
    private UtilisateurRepository utilisateurRepository;

    @InjectMocks
    private UtilisateurService utilisateurService;

    @Test
    void getAllUtilisateurs_retourneListeQuandNonVide() {
        Utilisateur utilisateur = new Utilisateur();
        utilisateur.setId(1L);
        utilisateur.setUsername("jdupont");
        when(utilisateurRepository.findAll()).thenReturn(List.of(utilisateur));

        List<Utilisateur> result = utilisateurService.getAllUtilisateurs();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getUsername()).isEqualTo("jdupont");
    }

    @Test
    void getAllUtilisateurs_leveExceptionQuandListeVide() {
        when(utilisateurRepository.findAll()).thenReturn(Collections.emptyList());

        assertThatThrownBy(() -> utilisateurService.getAllUtilisateurs())
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Aucun utilisateur en base");
    }

    @Test
    void getUtilisateurById_retourneQuandTrouve() {
        Utilisateur utilisateur = new Utilisateur();
        utilisateur.setId(1L);
        when(utilisateurRepository.findById(1L)).thenReturn(Optional.of(utilisateur));

        Optional<Utilisateur> result = utilisateurService.getUtilisateurById(1L);

        assertThat(result).isPresent();
        assertThat(result.get().getId()).isEqualTo(1L);
    }

    @Test
    void getUtilisateurById_leveExceptionSiNonTrouve() {
        when(utilisateurRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> utilisateurService.getUtilisateurById(99L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("99");
    }

    @Test
    void createUtilisateur_definitValeursParDefautSiAbsentes() {
        Utilisateur utilisateur = new Utilisateur();
        utilisateur.setUsername("jdupont");
        utilisateur.setEnabled(null);
        utilisateur.setRole(null);
        when(utilisateurRepository.save(any(Utilisateur.class))).thenAnswer(inv -> inv.getArgument(0));

        Utilisateur result = utilisateurService.createUtilisateur(utilisateur);

        assertThat(result.getEnabled()).isTrue();
        assertThat(result.getRole()).isEqualTo("ROLE_TUTEUR");
        assertThat(result.getDateCreation()).isNotNull();
        verify(utilisateurRepository).save(utilisateur);
    }

    @Test
    void createUtilisateur_conserveValeursExistantes() {
        Utilisateur utilisateur = new Utilisateur();
        utilisateur.setUsername("admin");
        utilisateur.setEnabled(false);
        utilisateur.setRole("ROLE_ADMIN");
        when(utilisateurRepository.save(any(Utilisateur.class))).thenAnswer(inv -> inv.getArgument(0));

        Utilisateur result = utilisateurService.createUtilisateur(utilisateur);

        assertThat(result.getEnabled()).isFalse();
        assertThat(result.getRole()).isEqualTo("ROLE_ADMIN");
    }
}
