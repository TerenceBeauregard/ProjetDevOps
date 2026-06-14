package ytg.projetjavaytg.services;

/*
 * Service d'inscription désactivé
 * La fonctionnalité d'inscription a été retirée du projet
 */

/*
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import ytg.projetjavaytg.DTO.RegisterForm;
import ytg.projetjavaytg.Models.Utilisateur;
import ytg.projetjavaytg.Repositories.UtilisateurRepository;

import java.time.Instant;

@Service
public class RegisterService {
    private final UtilisateurRepository utilisateurRepository;
    private final PasswordEncoder passwordEncoder;

    public RegisterService(UtilisateurRepository utilisateurRepository, PasswordEncoder passwordEncoder) {
        this.utilisateurRepository = utilisateurRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public void register(RegisterForm form) {
        if (utilisateurRepository.findByUsername(form.getUsername()).isPresent()) {
            throw new IllegalArgumentException("Nom d'utilisateur déjà utilisé.");
        }
        if (utilisateurRepository.findByEmail(form.getEmail()).isPresent()) {
            throw new IllegalArgumentException("Email déjà utilisé.");
        }
        if (form.getPassword() == null || form.getPassword().length() < 6) {
            throw new IllegalArgumentException("Le mot de passe doit contenir au moins 6 caractères.");
        }
        Utilisateur utilisateur = new Utilisateur();
        utilisateur.setUsername(form.getUsername());
        utilisateur.setPassword(passwordEncoder.encode(form.getPassword()));
        utilisateur.setPrenom(form.getPrenom());
        utilisateur.setNom(form.getNom());
        utilisateur.setEmail(form.getEmail());
        utilisateur.setRole("ROLE_TUTEUR");
        utilisateur.setEnabled(true);
        utilisateur.setDateCreation(Instant.now());
        utilisateurRepository.save(utilisateur);
    }
}

*/