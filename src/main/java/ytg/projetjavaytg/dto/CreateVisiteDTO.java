package ytg.projetjavaytg.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CreateVisiteDTO {
    private Long apprentiId;
    private String dateVisite; // yyyy-MM-dd
    private String format;
    private String commentaires;
}

