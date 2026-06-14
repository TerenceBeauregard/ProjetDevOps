package ytg.projetjavaytg.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
public class EntrepriseFormDTO {
    private String raisonSociale;
    private String adresse;
    private String informationsAcces;
}
