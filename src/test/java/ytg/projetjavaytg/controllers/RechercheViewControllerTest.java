package ytg.projetjavaytg.controllers;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import ytg.projetjavaytg.models.Apprenti;
import ytg.projetjavaytg.models.Entreprise;
import ytg.projetjavaytg.security.CustomUserDetailsService;
import ytg.projetjavaytg.services.AnneeAcademiqueService;
import ytg.projetjavaytg.services.ApprentiService;
import ytg.projetjavaytg.services.EntrepriseService;

import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(RechercheViewController.class)
class RechercheViewControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ApprentiService apprentiService;

    @MockBean
    private EntrepriseService entrepriseService;

    @MockBean
    private AnneeAcademiqueService anneeAcademiqueService;

    @MockBean
    private CustomUserDetailsService customUserDetailsService;

    private Apprenti apprenti(String nom, String prenom, String niveau, String anneeAcademique,
                               String raisonSociale, boolean archive, String missionMotsCles) {
        Apprenti a = new Apprenti();
        a.setNom(nom);
        a.setPrenom(prenom);
        a.setNiveau(niveau);
        a.setAnneeAcademique(anneeAcademique);
        a.setArchive(archive);
        a.setMissionMotsCles(missionMotsCles);
        if (raisonSociale != null) {
            Entreprise entreprise = new Entreprise();
            entreprise.setRaisonSociale(raisonSociale);
            a.setEntreprise(entreprise);
        }
        return a;
    }

    @Test
    @WithMockUser
    void afficherPage_sansFiltresRetourneTousLesApprentis() throws Exception {
        Apprenti a1 = apprenti("Dupont", "Jean", "I1", "2025-2026", "Acme", false, "Java");
        Apprenti a2 = apprenti("Martin", "Sophie", "I2", "2024-2025", "OtherCorp", true, "Python");
        when(apprentiService.getAllApprentis()).thenReturn(List.of(a1, a2));
        when(entrepriseService.getAllEntreprises()).thenReturn(List.of());
        when(anneeAcademiqueService.getAllAnnees()).thenReturn(List.of());

        mockMvc.perform(get("/recherche"))
                .andExpect(status().isOk())
                .andExpect(view().name("recherche/liste"))
                .andExpect(model().attribute("totalApprentis", 2))
                .andExpect(model().attribute("totalApprentisGlobal", 2))
                .andExpect(model().attribute("filtreNom", ""))
                .andExpect(model().attribute("filtrePrenom", ""))
                .andExpect(model().attribute("filtreNiveau", "tous"))
                .andExpect(model().attribute("filtreAnneeAcademique", "toutes"))
                .andExpect(model().attribute("filtreEntreprise", ""))
                .andExpect(model().attribute("filtreStatut", "tous"))
                .andExpect(model().attribute("filtreMotCle", ""));
    }

    @Test
    @WithMockUser
    void afficherPage_filtreParNomEtPrenom() throws Exception {
        Apprenti a1 = apprenti("Dupont", "Jean", "I1", "2025-2026", "Acme", false, "Java");
        Apprenti a2 = apprenti("Martin", "Sophie", "I2", "2024-2025", "OtherCorp", false, "Python");
        when(apprentiService.getAllApprentis()).thenReturn(List.of(a1, a2));
        when(entrepriseService.getAllEntreprises()).thenReturn(List.of());
        when(anneeAcademiqueService.getAllAnnees()).thenReturn(List.of());

        mockMvc.perform(get("/recherche").param("nom", "dup").param("prenom", "jean"))
                .andExpect(status().isOk())
                .andExpect(model().attribute("totalApprentis", 1))
                .andExpect(model().attribute("filtreNom", "dup"))
                .andExpect(model().attribute("filtrePrenom", "jean"));
    }

    @Test
    @WithMockUser
    void afficherPage_filtreParNiveauAnneeEntrepriseStatutEtMotCle() throws Exception {
        Apprenti i1Actif = apprenti("Dupont", "Jean", "I1", "2025-2026", "Acme", false, "Java Spring");
        Apprenti i2Archive = apprenti("Martin", "Sophie", "I2", "2025-2026", "Acme", true, "Python");
        Apprenti autreAnnee = apprenti("Petit", "Emma", "I1", "2024-2025", "Acme", false, "Java");
        when(apprentiService.getAllApprentis()).thenReturn(List.of(i1Actif, i2Archive, autreAnnee));
        when(entrepriseService.getAllEntreprises()).thenReturn(List.of());
        when(anneeAcademiqueService.getAllAnnees()).thenReturn(List.of());

        mockMvc.perform(get("/recherche")
                        .param("niveau", "I1")
                        .param("anneeAcademique", "2025-2026")
                        .param("entreprise", "acme")
                        .param("statut", "actif")
                        .param("mission_mots_cles", "spring"))
                .andExpect(status().isOk())
                .andExpect(model().attribute("totalApprentis", 1))
                .andExpect(model().attribute("filtreNiveau", "I1"))
                .andExpect(model().attribute("filtreAnneeAcademique", "2025-2026"))
                .andExpect(model().attribute("filtreEntreprise", "acme"))
                .andExpect(model().attribute("filtreStatut", "actif"))
                .andExpect(model().attribute("filtreMotCle", "spring"));
    }

    @Test
    @WithMockUser
    void afficherPage_filtreStatutArchiveEtSansEntreprise() throws Exception {
        Apprenti archive = apprenti("Martin", "Sophie", "I2", "2025-2026", null, true, "Python");
        Apprenti actif = apprenti("Dupont", "Jean", "I1", "2025-2026", null, false, "Java");
        when(apprentiService.getAllApprentis()).thenReturn(List.of(archive, actif));
        when(entrepriseService.getAllEntreprises()).thenReturn(List.of());
        when(anneeAcademiqueService.getAllAnnees()).thenReturn(List.of());

        mockMvc.perform(get("/recherche")
                        .param("statut", "archive")
                        .param("entreprise", "acme"))
                .andExpect(status().isOk())
                .andExpect(model().attribute("totalApprentis", 0));
    }

    @Test
    void afficherPage_retourne401SansAuthentification() throws Exception {
        mockMvc.perform(get("/recherche"))
                .andExpect(status().isUnauthorized());
    }
}
