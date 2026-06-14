package ytg.projetjavaytg.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
@Entity
@Table(name = "maitre_apprentissage")
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class MaitreApprentissage {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false) private Long id;

    @Column(name = "nom", nullable = false, length = 100) private String nom;
    @Column(name = "prenom", nullable = false, length = 100) private String prenom;
    @Column(name = "poste", length = 150) private String poste;
    @Column(name = "email", length = 150) private String email;
    @Column(name = "telephone", length = 20) private String telephone;

    @Column(name = "remarques", columnDefinition = "TEXT") private String remarques;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "entreprise_id", nullable = false)
    private Entreprise entreprise;
}
