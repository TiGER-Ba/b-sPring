package ma.bankati.bankatispringboot.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jakarta.servlet.http.HttpServletRequest;

/**
 * Gestionnaire global des exceptions pour l'application Bankati
 */
@ControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    /**
     * Gestion des exceptions métier de l'application
     */
    @ExceptionHandler(BankatiException.class)
    public String handleBankatiException(BankatiException e,
                                         RedirectAttributes redirectAttributes,
                                         HttpServletRequest request) {
        log.error("Erreur métier Bankati: {}", e.getMessage(), e);

        redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());

        // Rediriger vers la page précédente ou la page d'accueil
        String referer = request.getHeader("Referer");
        if (referer != null && !referer.isEmpty()) {
            return "redirect:" + referer;
        }

        return "redirect:/home";
    }

    /**
     * Gestion des erreurs d'accès refusé
     */
    @ExceptionHandler(AccessDeniedException.class)
    public String handleAccessDenied(AccessDeniedException e, Model model) {
        log.warn("Accès refusé: {}", e.getMessage());

        model.addAttribute("errorMessage", "Accès refusé : vous n'avez pas les permissions nécessaires");
        model.addAttribute("AppName", "Bankati");

        return "error/403";
    }

    /**
     * Gestion des erreurs d'arguments invalides
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public String handleIllegalArgument(IllegalArgumentException e,
                                        RedirectAttributes redirectAttributes,
                                        HttpServletRequest request) {
        log.error("Argument invalide: {}", e.getMessage(), e);

        redirectAttributes.addFlashAttribute("errorMessage", "Données invalides : " + e.getMessage());

        // Rediriger vers la page précédente ou la page d'accueil
        String referer = request.getHeader("Referer");
        if (referer != null && !referer.isEmpty()) {
            return "redirect:" + referer;
        }

        return "redirect:/home";
    }

    /**
     * Gestion des autres exceptions non gérées
     */
    @ExceptionHandler(Exception.class)
    public String handleGeneralException(Exception e,
                                         RedirectAttributes redirectAttributes,
                                         HttpServletRequest request) {
        log.error("Erreur inattendue: {}", e.getMessage(), e);

        redirectAttributes.addFlashAttribute("errorMessage",
                "Une erreur inattendue s'est produite. Veuillez réessayer ou contacter le support.");

        // Rediriger vers la page précédente ou la page d'accueil
        String referer = request.getHeader("Referer");
        if (referer != null && !referer.isEmpty()) {
            return "redirect:" + referer;
        }

        return "redirect:/home";
    }
}