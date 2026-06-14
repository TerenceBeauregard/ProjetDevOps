package ytg.projetjavaytg.utils;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

public class SecurityUtils {

    private SecurityUtils() {
    }

    /**
     * Récupère le nom d'utilisateur actuellement connecté
     * @return le nom d'utilisateur ou "Utilisateur" si aucun utilisateur connecté
     */
    public static String getCurrentUsername() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication != null ? authentication.getName() : "Utilisateur";
    }

    /**
     * Récupère le prénom de l'utilisateur actuellement connecté
     * Cette méthode est destinée à être utilisée avec un UtilisateurService
     * @return le nom d'utilisateur (pour compatibilité avec les templates existants)
     */
    public static String getCurrentUserPrenom() {
        return getCurrentUsername();
    }
}

