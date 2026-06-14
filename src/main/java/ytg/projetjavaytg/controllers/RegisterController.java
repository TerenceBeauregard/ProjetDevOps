package ytg.projetjavaytg.controllers;

/*
 * Contrôleur d'inscription désactivé
 * La fonctionnalité d'inscription a été retirée du projet
 */

/*
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import ytg.projetjavaytg.DTO.RegisterForm;
import ytg.projetjavaytg.Services.RegisterService;

@Controller
public class RegisterController {
    private final RegisterService registerService;

    public RegisterController(RegisterService registerService) {
        this.registerService = registerService;
    }

    @GetMapping("/register")
    public String showRegisterForm(Model model) {
        model.addAttribute("registerForm", new RegisterForm());
        return "register";
    }

    @PostMapping("/register")
    public String register(@ModelAttribute RegisterForm form, Model model) {
        try {
            registerService.register(form);
            return "redirect:/login?registered=true";
        } catch (IllegalArgumentException e) {
            model.addAttribute("error", e.getMessage());
            model.addAttribute("registerForm", form);
            return "register";
        }
    }
}
*/
