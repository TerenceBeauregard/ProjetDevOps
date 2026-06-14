package ytg.projetjavaytg.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
public class MaitreApprentissageDTO {
    private String nom;
    private String prenom;
    private String poste;
    private String email;
    private String telephone;
    private String remarques;
    private Long entrepriseId;
}
