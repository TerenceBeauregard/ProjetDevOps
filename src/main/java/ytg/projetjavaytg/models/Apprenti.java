package ytg.projetjavaytg.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Getter
@Setter
@Entity
@Table(name = "apprenti")
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class Apprenti {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @Column(name = "nom", nullable = false, length = 100)
    private String nom;

    @Column(name = "prenom", nullable = false, length = 100)
    private String prenom;

    @Column(name = "email", nullable = false, length = 150)
    private String email;

    @Column(name = "telephone", length = 20)
    private String telephone;

    @Column(name = "programme", length = 100)
    private String programme;

    @Column(name = "annee_academique", nullable = false, length = 20)
    private String anneeAcademique;

    @Column(name = "majeure", length = 150)
    private String majeure;

    @Column(name = "niveau", length = 10, nullable = false)
    private String niveau = "I1";

    @Column(name = "archive", nullable = false)
    private Boolean archive = false;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "entreprise_id", nullable = false)
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private Entreprise entreprise;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "maitre_apprentissage_id", nullable = false)
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private MaitreApprentissage maitreApprentissage;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tuteur_enseignant_id", nullable = false)
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private Utilisateur tuteurEnseignant;

    @Column(name = "mission_mots_cles", columnDefinition = "TEXT")
    private String missionMotsCles;

    @Column(name = "mission_metier_cible", length = 200)
    private String missionMetierCible;

    @Column(name = "mission_commentaires", columnDefinition = "TEXT")
    private String missionCommentaires;

    @Column(name = "feedback_tuteur", columnDefinition = "TEXT")
    private String feedbackTuteur;

    @Column(name = "remarques_generales",columnDefinition = "TEXT")
    private String remarquesGenerales;

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
