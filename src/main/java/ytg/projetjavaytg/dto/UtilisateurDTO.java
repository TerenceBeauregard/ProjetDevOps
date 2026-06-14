package ytg.projetjavaytg.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
public class UtilisateurDTO {
    private String username;
    private String password;
    private String prenom;
    private String nom;
    private String email;
    private String role;
    private Boolean enabled;
}
