package ma.bankati.bankatispringboot.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ma.bankati.bankatispringboot.entity.User;
import ma.bankati.bankatispringboot.service.CreditService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * Contrôleur pour les pages client
 */
@Controller
@RequestMapping("/client")
@RequiredArgsConstructor
@Slf4j
@PreAuthorize("hasRole('USER')")
public class ClientController {

    private final CreditService creditService;

    /**
     * Page d'accueil client
     */
    @GetMapping({"/", "/home"})
    public String clientHome(@AuthenticationPrincipal User user, Model model) {

        log.debug("Accès dashboard client pour: {}", user.getUsername());

        // Ajouter des statistiques du client
        long myCredits = creditService.findByClientId(user.getId()).size();

        model.addAttribute("user", user);
        model.addAttribute("AppName", "Bankati");
        model.addAttribute("myCredits", myCredits);

        return "client/home";
    }
}