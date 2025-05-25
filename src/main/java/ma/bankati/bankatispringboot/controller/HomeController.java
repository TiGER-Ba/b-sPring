package ma.bankati.bankatispringboot.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ma.bankati.bankatispringboot.dto.MoneyDataDto;
import ma.bankati.bankatispringboot.entity.User;
import ma.bankati.bankatispringboot.entity.enums.Devise;
import ma.bankati.bankatispringboot.entity.enums.ERole;
import ma.bankati.bankatispringboot.service.MoneyService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jakarta.servlet.http.HttpSession;

/**
 * Contrôleur principal pour les pages d'accueil
 */
@Controller
@RequiredArgsConstructor
@Slf4j
public class HomeController {

    private final MoneyService moneyService;

    /**
     * Page d'accueil - redirige selon le rôle
     */
    @GetMapping({"/", "/home"})
    public String home(@AuthenticationPrincipal User user, Model model, HttpSession session) {

        log.debug("Accès page d'accueil pour l'utilisateur: {}", user.getUsername());

        // Récupérer les données de conversion depuis la session
        MoneyDataDto convertedAmount = (MoneyDataDto) session.getAttribute("convertedAmount");
        String selectedCurrency = (String) session.getAttribute("selectedCurrency");
        MoneyDataDto defaultAmount = (MoneyDataDto) session.getAttribute("defaultAmount");

        // Si pas de conversion en session, utiliser la devise par défaut
        if (convertedAmount == null) {
            convertedAmount = moneyService.getDefaultBalance();
            selectedCurrency = "default";
            defaultAmount = convertedAmount;
        }

        // Ajouter les attributs au modèle
        model.addAttribute("result", convertedAmount);
        model.addAttribute("selectedCurrency", selectedCurrency);
        model.addAttribute("defaultResult", defaultAmount);
        model.addAttribute("user", user);
        model.addAttribute("AppName", "Bankati");

        // Redirection selon le rôle
        if (user.getRole() == ERole.ADMIN) {
            return "admin/home";
        } else {
            return "client/home";
        }
    }

    /**
     * Conversion de devise
     */
    @PostMapping("/convertCurrency")
    public String convertCurrency(@RequestParam("currency") String currency,
                                  @RequestParam(value = "returnUrl", required = false) String returnUrl,
                                  HttpSession session,
                                  RedirectAttributes redirectAttributes) {

        log.debug("Conversion de devise demandée: {}", currency);

        try {
            Devise devise = switch (currency) {
                case "dollar" -> Devise.USD;
                case "pound" -> Devise.GBP;
                case "euro" -> Devise.EUR;
                default -> Devise.DH;
            };

            // Effectuer la conversion
            MoneyDataDto convertedAmount = moneyService.convertBalance(devise);
            MoneyDataDto defaultAmount = moneyService.getDefaultBalance();

            // Stocker en session
            session.setAttribute("convertedAmount", convertedAmount);
            session.setAttribute("selectedCurrency", currency);
            session.setAttribute("defaultAmount", defaultAmount);

            log.info("Conversion effectuée: {} -> {}", defaultAmount, convertedAmount);

        } catch (Exception e) {
            log.error("Erreur lors de la conversion de devise", e);
            redirectAttributes.addFlashAttribute("errorMessage", "Erreur lors de la conversion de devise");
        }

        // Redirection vers la page d'origine ou /home par défaut
        if (returnUrl != null && !returnUrl.isEmpty()) {
            return "redirect:" + returnUrl;
        }
        return "redirect:/home";
    }
}