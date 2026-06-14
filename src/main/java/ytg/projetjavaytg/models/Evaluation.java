package ytg.projetjavaytg.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

@Getter
@Setter
@Entity
@Table(name = "evaluation",
        uniqueConstraints = @UniqueConstraint(name = "uk_evaluation_apprenti", columnNames = "apprenti_id"))
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class Evaluation {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "apprenti_id", nullable = false)
    private Apprenti apprenti;

    @Column(name = "memoire_theme") private String memoireTheme;
    @Column(name = "memoire_note", precision = 4, scale = 2) private BigDecimal memoireNote;
    @Column(name = "memoire_commentaires", columnDefinition = "TEXT") private String memoireCommentaires;

    @Column(name = "soutenance_date") private LocalDate soutenanceDate;
    @Column(name = "soutenance_note", precision = 4, scale = 2) private BigDecimal soutenanceNote;
    @Column(name = "soutenance_commentaires", columnDefinition = "TEXT") private String soutenanceCommentaires;

    @Column(name = "remarques_generales", columnDefinition = "TEXT") private String remarquesGenerales;

    @Column(name = "date_creation", nullable = false, updatable = false)
    private Instant dateCreation;

    @Column(name = "date_modification", nullable = false)
    private Instant dateModification;

    @PrePersist
    void prePersist() {
        Instant now = Instant.now();
        this.dateCreation = now;
        this.dateModification = now;
    }

    @PreUpdate
    void preUpdate() {
        this.dateModification = Instant.now();
    }
}
