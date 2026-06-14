package ytg.projetjavaytg.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
public class ApprentiDTO {
    private String nom;
    private String prenom;
    private String email;
    private String telephone;
    private String programme;
    private String anneeAcademique;
    private String majeure;
    private String niveau;
    private String missionMotsCles;
    private String missionMetierCible;
    private String missionCommentaires;
    private String feedbackTuteur;
    private String remarquesGenerales;
    private Long entrepriseId;
    private Long maitreApprentissageId;
    private Long tuteurEnseignantId;
}
