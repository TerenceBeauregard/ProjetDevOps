package ytg.projetjavaytg.controllers;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import ytg.projetjavaytg.models.Apprenti;
import ytg.projetjavaytg.security.CustomUserDetailsService;
import ytg.projetjavaytg.services.ExcelImportService;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ApprentiImportController.class)
class ApprentiImportControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ExcelImportService excelImportService;

    @MockBean
    private CustomUserDetailsService customUserDetailsService;

    @Test
    @WithMockUser
    void importForm_retourne200() throws Exception {
        mockMvc.perform(get("/apprentis/import"))
                .andExpect(status().isOk())
                .andExpect(view().name("apprentice/import"));
    }

    @Test
    @WithMockUser
    void importExcel_redirigeAvecErreurQuandFichierVide() throws Exception {
        MockMultipartFile file = new MockMultipartFile("file", "vide.xlsx",
                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", new byte[0]);

        mockMvc.perform(multipart("/apprentis/import").file(file).with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/apprentis/import"))
                .andExpect(flash().attribute("error", "Veuillez sélectionner un fichier"));
    }

    @Test
    @WithMockUser
    void importExcel_redirigeAvecErreurQuandFormatInvalide() throws Exception {
        MockMultipartFile file = new MockMultipartFile("file", "fichier.txt",
                "text/plain", "contenu".getBytes());

        mockMvc.perform(multipart("/apprentis/import").file(file).with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/apprentis/import"))
                .andExpect(flash().attribute("error", "Le fichier doit être au format Excel (.xlsx)"));
    }

    @Test
    @WithMockUser
    void importExcel_redirigeAvecSuccesQuandImportComplet() throws Exception {
        MockMultipartFile file = new MockMultipartFile("file", "apprentis.xlsx",
                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", "contenu".getBytes());

        ExcelImportService.ImportResult result = new ExcelImportService.ImportResult(List.of(new Apprenti()), List.of(), 1, 1);
        when(excelImportService.importApprentisFromExcel(any())).thenReturn(result);

        mockMvc.perform(multipart("/apprentis/import").file(file).with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/dashboard"))
                .andExpect(flash().attribute("success", "1 apprenti(s) importé(s) avec succès"));
    }

    @Test
    @WithMockUser
    void importExcel_redirigeAvecSuccesEtErreursQuandImportPartiel() throws Exception {
        MockMultipartFile file = new MockMultipartFile("file", "apprentis.xlsx",
                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", "contenu".getBytes());

        ExcelImportService.ImportResult result = new ExcelImportService.ImportResult(
                List.of(new Apprenti()), List.of("Ligne 3 : email déjà utilisé"), 2, 1);
        when(excelImportService.importApprentisFromExcel(any())).thenReturn(result);

        mockMvc.perform(multipart("/apprentis/import").file(file).with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/dashboard"))
                .andExpect(flash().attribute("success", "1 apprenti(s) importé(s) avec succès (1 erreur(s) ignorée(s))"))
                .andExpect(flash().attribute("error", "Erreurs détectées :<br>• Ligne 3 : email déjà utilisé"));
    }

    @Test
    @WithMockUser
    void importExcel_redirigeAvecErreurQuandAucunImport() throws Exception {
        MockMultipartFile file = new MockMultipartFile("file", "apprentis.xlsx",
                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", "contenu".getBytes());

        ExcelImportService.ImportResult result = new ExcelImportService.ImportResult(
                List.of(), List.of("Ligne 2 : entreprise non trouvée"), 1, 0);
        when(excelImportService.importApprentisFromExcel(any())).thenReturn(result);

        mockMvc.perform(multipart("/apprentis/import").file(file).with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/dashboard"))
                .andExpect(flash().attribute("error",
                        "Aucun apprenti n'a pu être importé. Erreurs détectées :<br>• Ligne 2 : entreprise non trouvée"));
    }

    @Test
    @WithMockUser
    void importExcel_redirigeAvecErreurQuandExceptionPendantImport() throws Exception {
        MockMultipartFile file = new MockMultipartFile("file", "apprentis.xlsx",
                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", "contenu".getBytes());

        when(excelImportService.importApprentisFromExcel(any())).thenThrow(new RuntimeException("Fichier corrompu"));

        mockMvc.perform(multipart("/apprentis/import").file(file).with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/dashboard"))
                .andExpect(flash().attribute("error", "Erreur lors de l'import du fichier : Fichier corrompu"));
    }

    @Test
    @WithMockUser
    void downloadTemplate_retourneFichierExcel() throws Exception {
        mockMvc.perform(get("/apprentis/template"))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Disposition", org.hamcrest.Matchers.containsString("modele_apprentis.xlsx")));
    }

    @Test
    @WithMockUser
    void downloadExemple_retourneFichierExcel() throws Exception {
        mockMvc.perform(get("/apprentis/exemple"))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Disposition", org.hamcrest.Matchers.containsString("exemple_apprentis.xlsx")));
    }

    @Test
    void importForm_retourne401SansAuthentification() throws Exception {
        mockMvc.perform(get("/apprentis/import"))
                .andExpect(status().isUnauthorized());
    }
}
