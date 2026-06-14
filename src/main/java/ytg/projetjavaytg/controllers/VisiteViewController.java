package ytg.projetjavaytg.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import ytg.projetjavaytg.models.Apprenti;
import ytg.projetjavaytg.models.Visite;
import ytg.projetjavaytg.services.ApprentiService;
import ytg.projetjavaytg.services.VisiteService;
import ytg.projetjavaytg.utils.SecurityUtils;

import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.TextStyle;
import java.util.*;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/visites")
public class VisiteViewController {

    private final VisiteService visiteService;
    private final ApprentiService apprentiService;

    public VisiteViewController(VisiteService visiteService, ApprentiService apprentiService) {
        this.visiteService = visiteService;
        this.apprentiService = apprentiService;
    }

    @GetMapping
    public String monthlyView(@RequestParam(value = "month", required = false, defaultValue = "0") int monthOffset,
                              Model model) {
        // Calcul du mois affiché en fonction d'un offset (0 = mois courant)
        YearMonth base = YearMonth.now().plusMonths(monthOffset);
        LocalDate firstOfMonth = base.atDay(1);

        // Construire la date de début du calendrier (lundi de la première semaine affichée)
        LocalDate start = firstOfMonth;
        while (start.getDayOfWeek().getValue() != 1) { // 1 = Monday
            start = start.minusDays(1);
        }

        // Construire la date de fin (dimanche de la dernière semaine affichée)
        LocalDate end = base.atEndOfMonth();
        while (end.getDayOfWeek().getValue() != 7) { // 7 = Sunday
            end = end.plusDays(1);
        }

        // Récupérer toutes les visites et grouper par date
        List<Visite> allVisites = visiteService.getAllVisites();
        Map<LocalDate, List<Visite>> visitesByDate = allVisites.stream()
                .filter(v -> v.getDateVisite() != null)
                .collect(Collectors.groupingBy(Visite::getDateVisite));

        List<List<DayView>> calendar = new ArrayList<>();
        LocalDate cursor = start;
        while (!cursor.isAfter(end)) {
            List<DayView> week = new ArrayList<>();
            for (int i = 0; i < 7; i++) {
                DayView dv = new DayView(cursor, cursor.getDayOfMonth(),
                        cursor.getMonthValue() != base.getMonthValue(),
                        visitesByDate.getOrDefault(cursor, Collections.emptyList()));
                week.add(dv);
                cursor = cursor.plusDays(1);
            }
            calendar.add(week);
        }

        // Préparer label du mois
        String monthLabel = base.getMonth().getDisplayName(TextStyle.FULL, Locale.FRENCH) + " " + base.getYear();

        model.addAttribute("username", SecurityUtils.getCurrentUserPrenom());
        model.addAttribute("calendar", calendar);
        model.addAttribute("currentMonthLabel", monthLabel);
        model.addAttribute("apprentis", apprentiService.getAllApprentis());
        model.addAttribute("month", monthOffset);

        return "visite/monthly";
    }

    @PostMapping("/create")
    public String createVisite(@RequestParam Long apprentiId,
                               @RequestParam String dateVisite,
                               @RequestParam(required = false) String format,
                               @RequestParam(required = false) String commentaires,
                               @RequestParam(value = "month", required = false) Integer monthOffset,
                               RedirectAttributes redirectAttributes) {
        try {
            Optional<Apprenti> apprentiOpt = apprentiService.getApprentiById(apprentiId);
            if (apprentiOpt.isEmpty()) {
                redirectAttributes.addFlashAttribute("error", "Apprenti introuvable");
                return "redirect:/visites" + (monthOffset != null ? "?month=" + monthOffset : "");
            }
            Visite v = new Visite();
            v.setApprenti(apprentiOpt.get());
            v.setDateVisite(LocalDate.parse(dateVisite));
            v.setFormat(format);
            v.setCommentaires(commentaires);
            visiteService.createVisite(v);
            redirectAttributes.addFlashAttribute("success", "Visite créée");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Erreur lors de la création : " + e.getMessage());
        }
        return "redirect:/visites" + (monthOffset != null ? "?month=" + monthOffset : "");
    }

    private record DayView(LocalDate date, int day, boolean otherMonth, List<Visite> visites) {
    }
}
