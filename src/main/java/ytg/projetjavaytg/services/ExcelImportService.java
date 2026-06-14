package ytg.projetjavaytg.services;

import lombok.Getter;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import ytg.projetjavaytg.models.Apprenti;
import ytg.projetjavaytg.models.Entreprise;
import ytg.projetjavaytg.models.MaitreApprentissage;
import ytg.projetjavaytg.models.Utilisateur;

import java.io.IOException;
import java.io.InputStream;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ExcelImportService {

    private final ApprentiService apprentiService;
    private final EntrepriseService entrepriseService;
    private final MaitreApprentissageService maitreApprentissageService;
    private final UtilisateurService utilisateurService;
    private final AnneeAcademiqueService anneeAcademiqueService;

    public ExcelImportService(ApprentiService apprentiService,
                             EntrepriseService entrepriseService,
                             MaitreApprentissageService maitreApprentissageService,
                             UtilisateurService utilisateurService,
                             AnneeAcademiqueService anneeAcademiqueService) {
        this.apprentiService = apprentiService;
        this.entrepriseService = entrepriseService;
        this.maitreApprentissageService = maitreApprentissageService;
        this.utilisateurService = utilisateurService;
        this.anneeAcademiqueService = anneeAcademiqueService;
    }

    @Transactional
    public ImportResult importApprentisFromExcel(MultipartFile file) throws IOException {
        List<String> errors = new ArrayList<>();
        List<Apprenti> createdApprentis = new ArrayList<>();
        int totalRows = 0;
        int successfulImports = 0;

        try (InputStream inputStream = file.getInputStream();
             Workbook workbook = new XSSFWorkbook(inputStream)) {

            Sheet sheet = workbook.getSheetAt(0);
            Row headerRow = sheet.getRow(0);

            if (headerRow == null) {
                throw new IllegalArgumentException("Le fichier Excel doit contenir une ligne d'en-têtes");
            }

            Map<String, Integer> columnMapping = createColumnMapping(headerRow);
            validateRequiredColumns(columnMapping, errors);

            if (!errors.isEmpty()) {
                return new ImportResult(createdApprentis, errors, totalRows, successfulImports);
            }

            // Traiter chaque ligne de données
            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row == null) continue;

                totalRows++;
                try {
                    Apprenti apprenti = processRow(row, columnMapping);
                    Apprenti created = apprentiService.createApprenti(apprenti);
                    createdApprentis.add(created);
                    successfulImports++;
                } catch (Exception e) {
                    errors.add("Ligne " + (i + 1) + ": " + e.getMessage());
                }
            }
        }

        return new ImportResult(createdApprentis, errors, totalRows, successfulImports);
    }

    private Map<String, Integer> createColumnMapping(Row headerRow) {
        Map<String, Integer> mapping = new HashMap<>();

        for (int i = 0; i < headerRow.getLastCellNum(); i++) {
            Cell cell = headerRow.getCell(i);
            if (cell != null) {
                String header = cell.getStringCellValue().trim().toLowerCase();
                mapping.put(header, i);
            }
        }

        return mapping;
    }

    private void validateRequiredColumns(Map<String, Integer> columnMapping, List<String> errors) {
        List<String> requiredColumns = List.of("nom", "prenom", "email", "entreprise", "maitre_apprentissage", "tuteur_enseignant");

        for (String required : requiredColumns) {
            if (!columnMapping.containsKey(required)) {
                errors.add("Colonne obligatoire manquante: " + required);
            }
        }
    }

    private Apprenti processRow(Row row, Map<String, Integer> columnMapping) throws Exception {
        Apprenti apprenti = new Apprenti();

        // Champs obligatoires
        apprenti.setNom(getCellStringValue(row, columnMapping.get("nom")));
        apprenti.setPrenom(getCellStringValue(row, columnMapping.get("prenom")));
        apprenti.setEmail(getCellStringValue(row, columnMapping.get("email")));

        // Vérifier si l'email existe déjà
        if (emailAlreadyExists(apprenti.getEmail())) {
            throw new Exception("Un apprenti avec cet email existe déjà: " + apprenti.getEmail());
        }

        // Champs optionnels
        setOptionalFields(apprenti, row, columnMapping);

        // Relations obligatoires
        setRequiredRelations(apprenti, row, columnMapping);

        // Valeurs par défaut
        apprenti.setArchive(false);
        apprenti.setDateCreation(Instant.now());
        apprenti.setDateModification(Instant.now());

        return apprenti;
    }

    private boolean emailAlreadyExists(String email) {
        return apprentiService.getAllApprentis().stream()
                .anyMatch(a -> a.getEmail().equals(email));
    }

    private void setOptionalFields(Apprenti apprenti, Row row, Map<String, Integer> columnMapping) {
        apprenti.setTelephone(getCellStringValue(row, columnMapping.get("telephone")));
        apprenti.setProgramme(getCellStringValue(row, columnMapping.get("programme")));
        apprenti.setMajeure(getCellStringValue(row, columnMapping.get("majeure")));
        apprenti.setMissionMotsCles(getCellStringValue(row, columnMapping.get("mission_mots_cles")));
        apprenti.setMissionMetierCible(getCellStringValue(row, columnMapping.get("mission_metier_cible")));
        apprenti.setMissionCommentaires(getCellStringValue(row, columnMapping.get("mission_commentaires")));
        apprenti.setRemarquesGenerales(getCellStringValue(row, columnMapping.get("remarques_generales")));

        // Niveau avec valeur par défaut
        String niveau = getCellStringValue(row, columnMapping.get("niveau"));
        apprenti.setNiveau(niveau != null && !niveau.isEmpty() ? niveau : "I1");

        // Année académique avec valeur par défaut
        String annee = getCellStringValue(row, columnMapping.get("annee_academique"));
        apprenti.setAnneeAcademique(annee != null && !annee.isEmpty() ? annee :
            anneeAcademiqueService.getAnneeAcademiqueEnCours());
    }

    private void setRequiredRelations(Apprenti apprenti, Row row, Map<String, Integer> columnMapping) throws Exception {
        // Entreprise
        String entrepriseNom = getCellStringValue(row, columnMapping.get("entreprise"));
        Entreprise entreprise = findEntreprise(entrepriseNom);
        apprenti.setEntreprise(entreprise);

        // Maître d'apprentissage
        String maitreNom = getCellStringValue(row, columnMapping.get("maitre_apprentissage"));
        MaitreApprentissage maitre = findMaitreApprentissage(maitreNom);
        apprenti.setMaitreApprentissage(maitre);

        // Tuteur enseignant
        String tuteurNom = getCellStringValue(row, columnMapping.get("tuteur_enseignant"));
        Utilisateur tuteur = findTuteurEnseignant(tuteurNom);
        apprenti.setTuteurEnseignant(tuteur);
    }

    private Entreprise findEntreprise(String nom) throws Exception {
        if (nom == null || nom.trim().isEmpty()) {
            throw new Exception("Nom d'entreprise manquant");
        }

        return entrepriseService.getAllEntreprises().stream()
                .filter(e -> e.getRaisonSociale().equalsIgnoreCase(nom.trim()))
                .findFirst()
                .orElseThrow(() -> new Exception("Entreprise non trouvée: '" + nom + "'. Vérifiez que l'entreprise existe dans le système."));
    }

    private MaitreApprentissage findMaitreApprentissage(String nom) throws Exception {
        if (nom == null || nom.trim().isEmpty()) {
            throw new Exception("Nom du maître d'apprentissage manquant");
        }

        return maitreApprentissageService.getAllMaitresApprentissage().stream()
                .filter(m -> matchesFullName(m.getNom(), m.getPrenom(), nom.trim()))
                .findFirst()
                .orElseThrow(() -> new Exception("Maître d'apprentissage non trouvé: '" + nom + "'. Vérifiez que le maître d'apprentissage existe dans le système."));
    }

    private Utilisateur findTuteurEnseignant(String nom) throws Exception {
        if (nom == null || nom.trim().isEmpty()) {
            throw new Exception("Nom du tuteur enseignant manquant");
        }

        return utilisateurService.getAllUtilisateurs().stream()
                .filter(u -> matchesFullName(u.getNom(), u.getPrenom(), nom.trim()))
                .findFirst()
                .orElseThrow(() -> new Exception("Tuteur enseignant non trouvé: '" + nom + "'. Vérifiez que le tuteur enseignant existe dans le système."));
    }

    private boolean matchesFullName(String nom, String prenom, String searchName) {
        String fullName1 = (nom + " " + prenom).trim();
        String fullName2 = (prenom + " " + nom).trim();
        return fullName1.equalsIgnoreCase(searchName) || fullName2.equalsIgnoreCase(searchName);
    }

    private String getCellStringValue(Row row, Integer columnIndex) {
        if (columnIndex == null) return null;

        Cell cell = row.getCell(columnIndex);
        if (cell == null) return null;

        switch (cell.getCellType()) {
            case STRING:
                return cell.getStringCellValue().trim();
            case NUMERIC:
                if (DateUtil.isCellDateFormatted(cell)) {
                    return cell.getDateCellValue().toString();
                } else {
                    return String.valueOf((long) cell.getNumericCellValue());
                }
            case BOOLEAN:
                return String.valueOf(cell.getBooleanCellValue());
            case FORMULA:
                return cell.getCellFormula();
            default:
                return null;
        }
    }

    @Getter
    public static class ImportResult {
        private final List<String> errors;
        private final int totalRows;
        private final int successfulImports;

        public ImportResult(List<Apprenti> createdApprentis, List<String> errors, int totalRows, int successfulImports) {
            this.errors = errors;
            this.totalRows = totalRows;
            this.successfulImports = successfulImports;
        }

        public boolean hasErrors() { return !errors.isEmpty(); }
    }
}
