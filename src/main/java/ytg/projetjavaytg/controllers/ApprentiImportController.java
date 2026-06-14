package ytg.projetjavaytg.controllers;

import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import ytg.projetjavaytg.services.ExcelImportService;
import ytg.projetjavaytg.utils.SecurityUtils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

@Controller
@RequestMapping("/apprentis")
public class ApprentiImportController {

    private final ExcelImportService excelImportService;

    public ApprentiImportController(ExcelImportService excelImportService) {
        this.excelImportService = excelImportService;
    }

    @GetMapping("/import")
    public String importForm(Model model) {
        model.addAttribute("username", SecurityUtils.getCurrentUserPrenom());
        return "apprentice/import";
    }

    @PostMapping("/import")
    public String importExcel(@RequestParam("file") MultipartFile file,
                             RedirectAttributes redirectAttributes) {

        if (file.isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "Veuillez sélectionner un fichier");
            return "redirect:/apprentis/import";
        }

        if (!file.getOriginalFilename().endsWith(".xlsx")) {
            redirectAttributes.addFlashAttribute("error", "Le fichier doit être au format Excel (.xlsx)");
            return "redirect:/apprentis/import";
        }

        try {
            ExcelImportService.ImportResult result = excelImportService.importApprentisFromExcel(file);

            if (result.getSuccessfulImports() > 0) {
                String successMsg = result.getSuccessfulImports() + " apprenti(s) importé(s) avec succès";
                if (result.hasErrors()) {
                    successMsg += " (" + result.getErrors().size() + " erreur(s) ignorée(s))";
                }
                redirectAttributes.addFlashAttribute("success", successMsg);
            }

            if (result.hasErrors()) {
                StringBuilder errorMsg = new StringBuilder();
                if (result.getSuccessfulImports() == 0) {
                    errorMsg.append("Aucun apprenti n'a pu être importé. ");
                }
                errorMsg.append("Erreurs détectées :");
                for (String error : result.getErrors()) {
                    errorMsg.append("<br>• ").append(error);
                }
                redirectAttributes.addFlashAttribute("error", errorMsg.toString());
            }

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Erreur lors de l'import du fichier : " + e.getMessage());
        }

        return "redirect:/dashboard";
    }

    @GetMapping("/template")
    public ResponseEntity<byte[]> downloadTemplate() throws IOException {
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Apprentis");

            // Créer les en-têtes
            Row headerRow = sheet.createRow(0);
            String[] headers = {
                "nom", "prenom", "email", "telephone", "programme",
                "majeure", "niveau", "annee_academique", "entreprise",
                "maitre_apprentissage", "tuteur_enseignant", "mission_mots_cles",
                "mission_metier_cible", "mission_commentaires", "remarques_generales"
            };

            for (int i = 0; i < headers.length; i++) {
                headerRow.createCell(i).setCellValue(headers[i]);
            }

            // Ajouter une ligne d'exemple
            Row exampleRow = sheet.createRow(1);
            exampleRow.createCell(0).setCellValue("Dupont");
            exampleRow.createCell(1).setCellValue("Jean");
            exampleRow.createCell(2).setCellValue("jean.dupont@example.com");
            exampleRow.createCell(3).setCellValue("0123456789");
            exampleRow.createCell(4).setCellValue("Informatique");
            exampleRow.createCell(5).setCellValue("Développement Web");
            exampleRow.createCell(6).setCellValue("I1");
            exampleRow.createCell(7).setCellValue("2025-2026");
            exampleRow.createCell(8).setCellValue("Nom de l'entreprise");
            exampleRow.createCell(9).setCellValue("Nom Prénom du maître");
            exampleRow.createCell(10).setCellValue("Nom Prénom du tuteur");
            exampleRow.createCell(11).setCellValue("Java, Spring, Web");
            exampleRow.createCell(12).setCellValue("Développeur Full Stack");
            exampleRow.createCell(13).setCellValue("Mission de développement d'une application web");
            exampleRow.createCell(14).setCellValue("Apprenti motivé et sérieux");

            // Auto-redimensionner les colonnes
            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
            }

            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            workbook.write(outputStream);

            HttpHeaders headers2 = new HttpHeaders();
            headers2.setContentType(MediaType.APPLICATION_OCTET_STREAM);
            headers2.setContentDispositionFormData("attachment", "modele_apprentis.xlsx");

            return ResponseEntity.ok()
                    .headers(headers2)
                    .body(outputStream.toByteArray());
        }
    }

    @GetMapping("/exemple")
    public ResponseEntity<byte[]> downloadExample() throws IOException {
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Apprentis_Exemple");

            // Créer les en-têtes
            Row headerRow = sheet.createRow(0);
            String[] headers = {
                "nom", "prenom", "email", "telephone", "programme",
                "majeure", "niveau", "annee_academique", "entreprise",
                "maitre_apprentissage", "tuteur_enseignant", "mission_mots_cles",
                "mission_metier_cible", "mission_commentaires", "remarques_generales"
            };

            for (int i = 0; i < headers.length; i++) {
                headerRow.createCell(i).setCellValue(headers[i]);
            }

            // Ajouter des exemples d'apprentis
            Object[][] exemples = {
                {"Martin", "Pierre", "pierre.martin@student.com", "0123456789", "Informatique",
                 "Développement Web", "I1", "2025-2026", "TechCorp SARL",
                 "Dupond Jean", "Professeur Smith", "Java, Spring, Angular",
                 "Développeur Full Stack", "Développement d'une application de gestion", "Étudiant motivé"},

                {"Durand", "Marie", "marie.durand@student.com", "0187654321", "Informatique",
                 "Cybersécurité", "I2", "2025-2026", "SecureIT Ltd",
                 "Martin Claire", "Professeur Johnson", "Python, Security, Linux",
                 "Analyste Sécurité", "Audit de sécurité et mise en place de mesures", "Excellents résultats"},

                {"Bernard", "Lucas", "lucas.bernard@student.com", "0156789123", "Informatique",
                 "Intelligence Artificielle", "I1", "2025-2026", "AI Solutions SAS",
                 "Rousseau Paul", "Professeur Garcia", "Machine Learning, Python, TensorFlow",
                 "Data Scientist", "Développement d'algorithmes de reconnaissance", "Très technique"},

                {"Petit", "Emma", "emma.petit@student.com", "0134567890", "Informatique",
                 "Développement Mobile", "I2", "2025-2026", "MobileFirst Inc",
                 "Moreau Sophie", "Professeur Brown", "React Native, iOS, Android",
                 "Développeur Mobile", "Application mobile de e-commerce", "Créative et autonome"}
            };

            for (int i = 0; i < exemples.length; i++) {
                Row row = sheet.createRow(i + 1);
                Object[] exemple = exemples[i];
                for (int j = 0; j < exemple.length; j++) {
                    row.createCell(j).setCellValue(exemple[j].toString());
                }
            }

            // Auto-redimensionner les colonnes
            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
            }

            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            workbook.write(outputStream);

            HttpHeaders headers2 = new HttpHeaders();
            headers2.setContentType(MediaType.APPLICATION_OCTET_STREAM);
            headers2.setContentDispositionFormData("attachment", "exemple_apprentis.xlsx");

            return ResponseEntity.ok()
                    .headers(headers2)
                    .body(outputStream.toByteArray());
        }
    }
}
