package ytg.projetjavaytg.services;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import ytg.projetjavaytg.models.Apprenti;
import ytg.projetjavaytg.models.Entreprise;
import ytg.projetjavaytg.models.MaitreApprentissage;
import ytg.projetjavaytg.models.Utilisateur;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ExcelImportServiceTest {

    @Mock
    private ApprentiService apprentiService;

    @Mock
    private EntrepriseService entrepriseService;

    @Mock
    private MaitreApprentissageService maitreApprentissageService;

    @Mock
    private UtilisateurService utilisateurService;

    @Mock
    private AnneeAcademiqueService anneeAcademiqueService;

    @InjectMocks
    private ExcelImportService excelImportService;

    private static final String[] HEADERS = {
            "nom", "prenom", "email", "telephone", "programme", "majeure", "niveau",
            "annee_academique", "mission_mots_cles", "mission_metier_cible",
            "mission_commentaires", "remarques_generales",
            "entreprise", "maitre_apprentissage", "tuteur_enseignant"
    };

    private Entreprise entreprise(String raisonSociale) {
        Entreprise e = new Entreprise();
        e.setRaisonSociale(raisonSociale);
        return e;
    }

    private MaitreApprentissage maitre(String nom, String prenom) {
        MaitreApprentissage m = new MaitreApprentissage();
        m.setNom(nom);
        m.setPrenom(prenom);
        return m;
    }

    private Utilisateur utilisateur(String nom, String prenom) {
        Utilisateur u = new Utilisateur();
        u.setNom(nom);
        u.setPrenom(prenom);
        return u;
    }

    private MockMultipartFile toMultipartFile(byte[] content) {
        return new MockMultipartFile("file", "apprentis.xlsx",
                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", content);
    }

    private byte[] buildWorkbook(String[] headers, List<Object[]> rows) throws IOException {
        try (XSSFWorkbook workbook = new XSSFWorkbook();
             ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Sheet sheet = workbook.createSheet("Apprentis");

            if (headers != null) {
                Row headerRow = sheet.createRow(0);
                for (int i = 0; i < headers.length; i++) {
                    headerRow.createCell(i).setCellValue(headers[i]);
                }
            }

            for (int r = 0; r < rows.size(); r++) {
                Object[] values = rows.get(r);
                Row row = sheet.createRow(r + 1);
                for (int c = 0; c < values.length; c++) {
                    Object value = values[c];
                    if (value == null) {
                        continue;
                    }
                    Cell cell = row.createCell(c);
                    if (value instanceof String s) {
                        cell.setCellValue(s);
                    } else if (value instanceof Double d) {
                        cell.setCellValue(d);
                    } else if (value instanceof Boolean b) {
                        cell.setCellValue(b);
                    } else if (value instanceof Date date) {
                        CellStyle dateStyle = workbook.createCellStyle();
                        dateStyle.setDataFormat(workbook.createDataFormat().getFormat("m/d/yy"));
                        cell.setCellValue(date);
                        cell.setCellStyle(dateStyle);
                    } else if (value instanceof Formula f) {
                        cell.setCellFormula(f.expression());
                    }
                }
            }

            workbook.write(out);
            return out.toByteArray();
        }
    }

    private record Formula(String expression) {
    }

    private Object[] ligneValide() {
        return new Object[]{
                "Dupont", "Jean", "jean.dupont@mail.com", "0601020304", "Info", "GL", "I2",
                "2024-2025", "Java", "Dev", "Bonne mission", "RAS",
                "Acme", "Paul Durand", "Marie Curie"
        };
    }

    @Test
    void importApprentisFromExcel_importeApprentiAvecSucces() throws IOException {
        byte[] content = buildWorkbook(HEADERS, List.<Object[]>of(ligneValide()));

        when(apprentiService.getAllApprentis()).thenReturn(Collections.emptyList());
        when(entrepriseService.getAllEntreprises()).thenReturn(List.of(entreprise("Acme")));
        when(maitreApprentissageService.getAllMaitresApprentissage()).thenReturn(List.of(maitre("Durand", "Paul")));
        when(utilisateurService.getAllUtilisateurs()).thenReturn(List.of(utilisateur("Curie", "Marie")));
        when(apprentiService.createApprenti(any(Apprenti.class))).thenAnswer(inv -> inv.getArgument(0));

        ExcelImportService.ImportResult result = excelImportService.importApprentisFromExcel(toMultipartFile(content));

        assertThat(result.hasErrors()).isFalse();
        assertThat(result.getTotalRows()).isEqualTo(1);
        assertThat(result.getSuccessfulImports()).isEqualTo(1);

        ArgumentCaptor<Apprenti> captor = ArgumentCaptor.forClass(Apprenti.class);
        verify(apprentiService).createApprenti(captor.capture());
        Apprenti created = captor.getValue();
        assertThat(created.getNom()).isEqualTo("Dupont");
        assertThat(created.getEmail()).isEqualTo("jean.dupont@mail.com");
        assertThat(created.getNiveau()).isEqualTo("I2");
        assertThat(created.getAnneeAcademique()).isEqualTo("2024-2025");
        assertThat(created.getEntreprise().getRaisonSociale()).isEqualTo("Acme");
        assertThat(created.getMaitreApprentissage().getNom()).isEqualTo("Durand");
        assertThat(created.getTuteurEnseignant().getNom()).isEqualTo("Curie");
        assertThat(created.getArchive()).isFalse();
    }

    @Test
    void importApprentisFromExcel_leveExceptionSiPasDeLigneEntete() throws IOException {
        byte[] content = buildWorkbook(null, Collections.emptyList());

        org.junit.jupiter.api.Assertions.assertThrows(IllegalArgumentException.class,
                () -> excelImportService.importApprentisFromExcel(toMultipartFile(content)));
    }

    @Test
    void importApprentisFromExcel_retourneErreursSiColonnesObligatoiresManquantes() throws IOException {
        String[] headersIncomplets = {"nom", "prenom", "email"};
        byte[] content = buildWorkbook(headersIncomplets, Collections.emptyList());

        ExcelImportService.ImportResult result = excelImportService.importApprentisFromExcel(toMultipartFile(content));

        assertThat(result.hasErrors()).isTrue();
        assertThat(result.getErrors()).anyMatch(e -> e.contains("entreprise"));
        assertThat(result.getErrors()).anyMatch(e -> e.contains("maitre_apprentissage"));
        assertThat(result.getErrors()).anyMatch(e -> e.contains("tuteur_enseignant"));
        assertThat(result.getTotalRows()).isZero();
        assertThat(result.getSuccessfulImports()).isZero();
    }

    @Test
    void importApprentisFromExcel_ajouteErreurSiEmailExisteDeja() throws IOException {
        byte[] content = buildWorkbook(HEADERS, List.<Object[]>of(ligneValide()));

        Apprenti existant = new Apprenti();
        existant.setEmail("jean.dupont@mail.com");
        when(apprentiService.getAllApprentis()).thenReturn(List.of(existant));

        ExcelImportService.ImportResult result = excelImportService.importApprentisFromExcel(toMultipartFile(content));

        assertThat(result.hasErrors()).isTrue();
        assertThat(result.getErrors().get(0)).contains("existe déjà");
        assertThat(result.getTotalRows()).isEqualTo(1);
        assertThat(result.getSuccessfulImports()).isZero();
        verify(apprentiService, never()).createApprenti(any());
    }

    @Test
    void importApprentisFromExcel_ajouteErreurSiEntrepriseNonTrouvee() throws IOException {
        byte[] content = buildWorkbook(HEADERS, List.<Object[]>of(ligneValide()));

        when(apprentiService.getAllApprentis()).thenReturn(Collections.emptyList());
        when(entrepriseService.getAllEntreprises()).thenReturn(Collections.emptyList());

        ExcelImportService.ImportResult result = excelImportService.importApprentisFromExcel(toMultipartFile(content));

        assertThat(result.hasErrors()).isTrue();
        assertThat(result.getErrors().get(0)).contains("Entreprise non trouvée");
        assertThat(result.getSuccessfulImports()).isZero();
        verify(apprentiService, never()).createApprenti(any());
    }

    @Test
    void importApprentisFromExcel_ajouteErreurSiMaitreNonTrouve() throws IOException {
        byte[] content = buildWorkbook(HEADERS, List.<Object[]>of(ligneValide()));

        when(apprentiService.getAllApprentis()).thenReturn(Collections.emptyList());
        when(entrepriseService.getAllEntreprises()).thenReturn(List.of(entreprise("Acme")));
        when(maitreApprentissageService.getAllMaitresApprentissage()).thenReturn(Collections.emptyList());

        ExcelImportService.ImportResult result = excelImportService.importApprentisFromExcel(toMultipartFile(content));

        assertThat(result.hasErrors()).isTrue();
        assertThat(result.getErrors().get(0)).contains("Maître d'apprentissage non trouvé");
        verify(apprentiService, never()).createApprenti(any());
    }

    @Test
    void importApprentisFromExcel_ajouteErreurSiTuteurNonTrouve() throws IOException {
        byte[] content = buildWorkbook(HEADERS, List.<Object[]>of(ligneValide()));

        when(apprentiService.getAllApprentis()).thenReturn(Collections.emptyList());
        when(entrepriseService.getAllEntreprises()).thenReturn(List.of(entreprise("Acme")));
        when(maitreApprentissageService.getAllMaitresApprentissage()).thenReturn(List.of(maitre("Durand", "Paul")));
        when(utilisateurService.getAllUtilisateurs()).thenReturn(Collections.emptyList());

        ExcelImportService.ImportResult result = excelImportService.importApprentisFromExcel(toMultipartFile(content));

        assertThat(result.hasErrors()).isTrue();
        assertThat(result.getErrors().get(0)).contains("Tuteur enseignant non trouvé");
        verify(apprentiService, never()).createApprenti(any());
    }

    @Test
    void importApprentisFromExcel_utiliseValeursParDefautPourNiveauEtAnneeQuandCellulesVides() throws IOException {
        Object[] ligne = ligneValide();
        ligne[6] = null;  // niveau
        ligne[7] = null;  // annee_academique
        byte[] content = buildWorkbook(HEADERS, List.<Object[]>of(ligne));

        when(apprentiService.getAllApprentis()).thenReturn(Collections.emptyList());
        when(entrepriseService.getAllEntreprises()).thenReturn(List.of(entreprise("Acme")));
        when(maitreApprentissageService.getAllMaitresApprentissage()).thenReturn(List.of(maitre("Durand", "Paul")));
        when(utilisateurService.getAllUtilisateurs()).thenReturn(List.of(utilisateur("Curie", "Marie")));
        when(anneeAcademiqueService.getAnneeAcademiqueEnCours()).thenReturn("2025-2026");
        when(apprentiService.createApprenti(any(Apprenti.class))).thenAnswer(inv -> inv.getArgument(0));

        ExcelImportService.ImportResult result = excelImportService.importApprentisFromExcel(toMultipartFile(content));

        assertThat(result.hasErrors()).isFalse();
        assertThat(result.getSuccessfulImports()).isEqualTo(1);

        ArgumentCaptor<Apprenti> captor = ArgumentCaptor.forClass(Apprenti.class);
        verify(apprentiService).createApprenti(captor.capture());
        assertThat(captor.getValue().getNiveau()).isEqualTo("I1");
        assertThat(captor.getValue().getAnneeAcademique()).isEqualTo("2025-2026");
        verify(anneeAcademiqueService).getAnneeAcademiqueEnCours();
    }

    @Test
    void importApprentisFromExcel_geresLesTypesDeCelluleNumeriqueBooleenneEtFormule() throws IOException {
        Object[] ligne = ligneValide();
        ligne[3] = 601020304.0; // telephone en numérique
        ligne[10] = Boolean.TRUE; // mission_commentaires en booléen
        ligne[11] = new Formula("1+1"); // remarques_generales en formule
        byte[] content = buildWorkbook(HEADERS, List.<Object[]>of(ligne));

        when(apprentiService.getAllApprentis()).thenReturn(Collections.emptyList());
        when(entrepriseService.getAllEntreprises()).thenReturn(List.of(entreprise("Acme")));
        when(maitreApprentissageService.getAllMaitresApprentissage()).thenReturn(List.of(maitre("Durand", "Paul")));
        when(utilisateurService.getAllUtilisateurs()).thenReturn(List.of(utilisateur("Curie", "Marie")));
        when(apprentiService.createApprenti(any(Apprenti.class))).thenAnswer(inv -> inv.getArgument(0));

        ExcelImportService.ImportResult result = excelImportService.importApprentisFromExcel(toMultipartFile(content));

        assertThat(result.hasErrors()).isFalse();
        assertThat(result.getSuccessfulImports()).isEqualTo(1);

        ArgumentCaptor<Apprenti> captor = ArgumentCaptor.forClass(Apprenti.class);
        verify(apprentiService).createApprenti(captor.capture());
        assertThat(captor.getValue().getTelephone()).isEqualTo("601020304");
        assertThat(captor.getValue().getMissionCommentaires()).isEqualTo("true");
        assertThat(captor.getValue().getRemarquesGenerales()).isEqualTo("1+1");
    }
}
