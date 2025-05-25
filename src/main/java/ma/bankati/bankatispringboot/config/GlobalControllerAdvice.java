package ma.bankati.bankatispringboot.config;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

/**
 * Advice global pour ajouter des attributs communs à tous les modèles
 */
@ControllerAdvice
public class GlobalControllerAdvice {

    /**
     * Ajoute automatiquement le nom de l'application à tous les modèles
     */
    @ModelAttribute("AppName")
    public String appName() {
        return "Bankati";
    }

    /**
     * Détermine automatiquement la page courante basée sur l'URL
     */
    @ModelAttribute("currentPage")
    public String currentPage(HttpServletRequest request) {
        String requestURI = request.getRequestURI();

        // Déterminer la page courante basée sur l'URL
        if (requestURI.equals("/bankati/") || requestURI.equals("/bankati/home") ||
                requestURI.equals("/bankati/admin/home") || requestURI.equals("/bankati/client/home")) {
            return "home";
        } else if (requestURI.contains("/users")) {
            return "users";
        } else if (requestURI.contains("/admin/credits")) {
            return "admin-credits";
        } else if (requestURI.contains("/client/credit/new")) {
            return "new-credit";
        } else if (requestURI.contains("/client/credit")) {
            return "client-credits";
        } else if (requestURI.contains("/login")) {
            return "login";
        }

        return ""; // Page par défaut
    }

    /**
     * Ajoute l'URL de retour pour le convertisseur de devises
     */
    @ModelAttribute("returnUrl")
    public String returnUrl(HttpServletRequest request) {
        return request.getRequestURI();
    }
}