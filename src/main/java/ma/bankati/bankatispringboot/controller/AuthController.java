package ma.bankati.bankatispringboot.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * Contrôleur pour l'authentification
 */
@Controller
@RequiredArgsConstructor
@Slf4j
public class AuthController {

    /**
     * Page de connexion
     */
    @GetMapping("/login")
    public String loginPage(@RequestParam(value = "error", required = false) String error,
                            @RequestParam(value = "logout", required = false) String logout,
                            Model model) {

        // Rediriger si déjà connecté
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated() && !auth.getName().equals("anonymousUser")) {
            return "redirect:/home";
        }

        if (error != null) {
            model.addAttribute("globalMessage", "Nom d'utilisateur ou mot de passe incorrect.");
        }

        if (logout != null) {
            model.addAttribute("globalMessage", "Vous avez été déconnecté avec succès.");
        }

        // AppName et currentPage sont ajoutés automatiquement par GlobalControllerAdvice
        // Pas besoin de les ajouter manuellement

        return "auth/login";
    }
}