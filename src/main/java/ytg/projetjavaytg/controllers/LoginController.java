package ytg.projetjavaytg.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class LoginController {

    @GetMapping("/login")
    public String login(@RequestParam(value = "error", required = false) String error,
                       @RequestParam(value = "logout", required = false) String logout,
                       @RequestParam(value = "registered", required = false) String registered,
                       Model model) {
        if (error != null) {
            model.addAttribute("error", "Identifiants incorrects");
        }
        if (logout != null) {
            model.addAttribute("message", "Déconnexion réussie");
        }
        if (registered != null) {
            model.addAttribute("success", "Inscription réussie ! Vous pouvez maintenant vous connecter.");
        }
        return "login";
    }

    @GetMapping("/")
    public String home() {
        return "redirect:/dashboard";
    }
}

