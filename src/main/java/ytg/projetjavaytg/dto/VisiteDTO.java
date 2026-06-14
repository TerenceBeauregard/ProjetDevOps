package ytg.projetjavaytg.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
public class VisiteDTO {
    private Long apprentiId;
    private LocalDate dateVisite;
    private String format;
    private String commentaires;
}
