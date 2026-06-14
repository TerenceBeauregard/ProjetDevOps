package ytg.projetjavaytg.dto;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
public class EvaluationFormDTO {
    private String memoireTheme;
    private BigDecimal memoireNote;
    private String memoireCommentaires;
    private LocalDate soutenanceDate;
    private BigDecimal soutenanceNote;
    private String soutenanceCommentaires;
    private String remarquesGenerales;
}
