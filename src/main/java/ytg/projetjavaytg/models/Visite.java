package ytg.projetjavaytg.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.time.LocalDate;

@Getter @Setter
@Entity
@Table(name = "visite")
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class Visite {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "apprenti_id", nullable = false)
    private Apprenti apprenti;

    @Column(name = "date_visite") private LocalDate dateVisite;
    @Column(name = "format", length = 20) private String format;

    @Column(name = "commentaires", columnDefinition = "TEXT") private String commentaires;

    @Column(name = "date_creation", nullable = false, updatable = false)
    private Instant dateCreation;

    @PrePersist
    void prePersist() { this.dateCreation = Instant.now(); }
}
