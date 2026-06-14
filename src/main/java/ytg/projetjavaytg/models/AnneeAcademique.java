package ytg.projetjavaytg.models;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Getter @Setter
@Entity
@Table(name = "annee_academique")
public class AnneeAcademique {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @Column(name = "annee", nullable = false, unique = true, length = 20)
    private String annee;

    @Column(name = "active", nullable = false)
    private boolean active = false;

    @Column(name = "date_creation", nullable = false, updatable = false)
    private Instant dateCreation;

    @Column(name = "date_activation") private Instant dateActivation;

    @PrePersist
    void prePersist() { this.dateCreation = Instant.now(); }
}
