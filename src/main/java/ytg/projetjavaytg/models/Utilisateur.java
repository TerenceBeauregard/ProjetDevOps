package ytg.projetjavaytg.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Getter @Setter
@Entity
@Table(name = "utilisateur")
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class Utilisateur {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @Column(name = "username", nullable = false, length = 100) private String username;
    @Column(name = "password", nullable = false) private String password;
    @Column(name = "prenom", nullable = false, length = 100) private String prenom;
    @Column(name = "nom", nullable = false, length = 100) private String nom;
    @Column(name = "email", nullable = false, length = 150) private String email;

    @Column(name = "role", length = 50, nullable = false)
    private String role = "ROLE_TUTEUR";

    @Column(name = "enabled", nullable = false)
    private Boolean enabled = true;

    @Column(name = "date_creation", nullable = false, updatable = false)
    private Instant dateCreation;

    @Column(name = "date_derniere_connexion") private Instant dateDerniereConnexion;

    @PrePersist
    void prePersist() {
        this.dateCreation = Instant.now();
    }
}
