package ytg.projetjavaytg.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
public class EvaluationDTO {
    private Long apprentiId;
    private String memoireTheme;
    private BigDecimal memoireNote;
    private String memoireCommentaires;
    private LocalDate soutenanceDate;
    private BigDecimal soutenanceNote;
    private String soutenanceCommentaires;
    private String remarquesGenerales;
}
