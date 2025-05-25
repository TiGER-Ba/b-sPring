package ma.bankati.bankatispringboot.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ma.bankati.bankatispringboot.entity.CreditRequest;
import ma.bankati.bankatispringboot.entity.User;
import ma.bankati.bankatispringboot.entity.enums.CreditStatus;
import ma.bankati.bankatispringboot.service.CreditService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.math.BigDecimal;
import java.util.List;

@Controller
@RequestMapping("/client")
@RequiredArgsConstructor
@Slf4j
@PreAuthorize("hasRole('USER')")
public class ClientController {

    private final CreditService creditService;

    @GetMapping({"/", "/home"})
    public String clientHome(@AuthenticationPrincipal User user, Model model) {
        log.debug("Accès dashboard client pour: {}", user.getUsername());

        try {
            // Récupérer toutes les demandes du client
            List<CreditRequest> userCredits = creditService.findByClientId(user.getId());

            // Calculer les statistiques
            long totalRequests = userCredits.size();
            long pendingRequests = userCredits.stream()
                    .mapToLong(cr -> cr.getStatus() == CreditStatus.PENDING ? 1 : 0)
                    .sum();
            long approvedRequests = userCredits.stream()
                    .mapToLong(cr -> cr.getStatus() == CreditStatus.APPROVED ? 1 : 0)
                    .sum();
            long rejectedRequests = userCredits.stream()
                    .mapToLong(cr -> cr.getStatus() == CreditStatus.REJECTED ? 1 : 0)
                    .sum();

            // Calculer le montant total des crédits approuvés
            BigDecimal totalApprovedAmount = userCredits.stream()
                    .filter(cr -> cr.getStatus() == CreditStatus.APPROVED)
                    .map(CreditRequest::getAmount)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            // Ajouter au modèle
            model.addAttribute("user", user);
            model.addAttribute("myCredits", totalRequests);
            model.addAttribute("pendingCredits", pendingRequests);
            model.addAttribute("approvedCredits", approvedRequests);
            model.addAttribute("rejectedCredits", rejectedRequests);
            model.addAttribute("totalApprovedAmount", totalApprovedAmount);

            // Formatage du montant
            String formattedAmount = String.format("%.2f DH", totalApprovedAmount);
            model.addAttribute("formattedTotalAmount", formattedAmount);

            // Dernière demande
            if (!userCredits.isEmpty()) {
                CreditRequest lastRequest = userCredits.get(0); // Les demandes sont triées par date décroissante
                model.addAttribute("lastRequest", lastRequest);
                model.addAttribute("hasRequests", true);
            } else {
                model.addAttribute("hasRequests", false);
            }

            log.info("Client {} - Demandes: {}, En attente: {}, Montant total: {}",
                    user.getUsername(), totalRequests, pendingRequests, formattedAmount);

        } catch (Exception e) {
            log.error("Erreur lors du chargement des statistiques client", e);
            // Valeurs par défaut en cas d'erreur
            model.addAttribute("user", user);
            model.addAttribute("myCredits", 0L);
            model.addAttribute("pendingCredits", 0L);
            model.addAttribute("approvedCredits", 0L);
            model.addAttribute("rejectedCredits", 0L);
            model.addAttribute("totalApprovedAmount", BigDecimal.ZERO);
            model.addAttribute("formattedTotalAmount", "0.00 DH");
            model.addAttribute("hasRequests", false);
        }

        return "client/home";
    }
}