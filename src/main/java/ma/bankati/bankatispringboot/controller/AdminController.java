package ma.bankati.bankatispringboot.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ma.bankati.bankatispringboot.entity.User;
import ma.bankati.bankatispringboot.entity.enums.CreditStatus;
import ma.bankati.bankatispringboot.entity.enums.ERole;
import ma.bankati.bankatispringboot.service.CreditService;
import ma.bankati.bankatispringboot.service.UserService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.math.BigDecimal;

@Controller
@RequestMapping("/admin")
@RequiredArgsConstructor
@Slf4j
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    private final UserService userService;
    private final CreditService creditService;

    @GetMapping({"/", "/home"})
    public String adminHome(@AuthenticationPrincipal User user, Model model) {
        log.debug("Accès dashboard admin pour: {}", user.getUsername());

        try {
            // Statistiques des utilisateurs
            long totalUsers = userService.countByRole(ERole.USER);
            long totalAdmins = userService.countByRole(ERole.ADMIN);

            // Statistiques des crédits
            long pendingCredits = creditService.countByStatus(CreditStatus.PENDING);
            long approvedCredits = creditService.countByStatus(CreditStatus.APPROVED);
            long rejectedCredits = creditService.countByStatus(CreditStatus.REJECTED);
            long totalCredits = pendingCredits + approvedCredits + rejectedCredits;

            // Montants
            BigDecimal totalApprovedAmount = creditService.getTotalApprovedAmount();
            if (totalApprovedAmount == null) {
                totalApprovedAmount = BigDecimal.ZERO;
            }

            // Ajouter au modèle
            model.addAttribute("user", user);
            model.addAttribute("totalUsers", totalUsers);
            model.addAttribute("totalAdmins", totalAdmins);
            model.addAttribute("totalCredits", totalCredits);
            model.addAttribute("pendingCredits", pendingCredits);
            model.addAttribute("approvedCredits", approvedCredits);
            model.addAttribute("rejectedCredits", rejectedCredits);
            model.addAttribute("totalApprovedAmount", totalApprovedAmount);

            // Formatage du montant total
            String formattedAmount = String.format("%.2f DH", totalApprovedAmount);
            model.addAttribute("formattedTotalAmount", formattedAmount);

            log.info("Dashboard stats - Users: {}, Pending Credits: {}, Total Amount: {}",
                    totalUsers, pendingCredits, formattedAmount);

        } catch (Exception e) {
            log.error("Erreur lors du chargement des statistiques du dashboard", e);
            // Valeurs par défaut en cas d'erreur
            model.addAttribute("user", user);
            model.addAttribute("totalUsers", 0L);
            model.addAttribute("totalAdmins", 0L);
            model.addAttribute("totalCredits", 0L);
            model.addAttribute("pendingCredits", 0L);
            model.addAttribute("approvedCredits", 0L);
            model.addAttribute("rejectedCredits", 0L);
            model.addAttribute("totalApprovedAmount", BigDecimal.ZERO);
            model.addAttribute("formattedTotalAmount", "0.00 DH");
        }

        return "admin/home";
    }
}