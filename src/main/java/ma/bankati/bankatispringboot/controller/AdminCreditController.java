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
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/admin/credits")
@RequiredArgsConstructor
@Slf4j
@PreAuthorize("hasRole('ADMIN')")
public class AdminCreditController {

    private final CreditService creditService;

    @GetMapping({"/", "/list"})
    public String listAllCredits(@RequestParam(value = "status", required = false) String status,
                                 @AuthenticationPrincipal User user,
                                 Model model) {
        log.debug("Liste des crédits pour admin: {}, filtre: {}", user.getUsername(), status);

        List<CreditRequest> credits;
        if (status != null && !status.isEmpty()) {
            try {
                CreditStatus creditStatus = CreditStatus.valueOf(status.toUpperCase());
                credits = creditService.findByStatus(creditStatus);
                model.addAttribute("statusFilter", status.toLowerCase());
            } catch (IllegalArgumentException e) {
                credits = creditService.findAll();
            }
        } else {
            credits = creditService.findAll();
        }

        model.addAttribute("credits", credits);
        model.addAttribute("user", user);
        // AppName et currentPage sont ajoutés automatiquement par GlobalControllerAdvice

        return "admin/credit/list";
    }

    @GetMapping("/pending")
    public String listPendingCredits(@AuthenticationPrincipal User user, Model model) {
        log.debug("Liste des crédits en attente pour admin: {}", user.getUsername());

        List<CreditRequest> pendingCredits = creditService.findByStatus(CreditStatus.PENDING);

        model.addAttribute("credits", pendingCredits);
        model.addAttribute("statusFilter", "pending");
        model.addAttribute("user", user);
        // AppName et currentPage sont ajoutés automatiquement par GlobalControllerAdvice

        return "admin/credit/list";
    }

    @GetMapping("/details")
    public String creditDetails(@RequestParam("id") Long id,
                                @AuthenticationPrincipal User user,
                                Model model,
                                RedirectAttributes redirectAttributes) {
        log.debug("Détails crédit ID: {} pour admin: {}", id, user.getUsername());

        Optional<CreditRequest> creditOpt = creditService.findById(id);
        if (creditOpt.isEmpty()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Demande de crédit introuvable");
            return "redirect:/admin/credits/list";
        }

        model.addAttribute("creditRequest", creditOpt.get());
        model.addAttribute("user", user);
        // AppName et currentPage sont ajoutés automatiquement par GlobalControllerAdvice

        return "admin/credit/details";
    }

    @PostMapping("/process")
    public String processCreditRequest(@RequestParam("id") Long id,
                                       @RequestParam("decision") String decision,
                                       @RequestParam(value = "reason", required = false) String reason,
                                       @AuthenticationPrincipal User user,
                                       RedirectAttributes redirectAttributes) {
        log.debug("Traitement crédit ID: {} par admin: {}, décision: {}", id, user.getUsername(), decision);

        try {
            if (!creditService.canProcessCreditRequest(id)) {
                redirectAttributes.addFlashAttribute("errorMessage", "Cette demande ne peut plus être traitée");
                return "redirect:/admin/credits/list";
            }

            if ("approve".equals(decision)) {
                creditService.approveCreditRequest(id, reason);
                redirectAttributes.addFlashAttribute("successMessage",
                        "Demande de crédit #" + id + " approuvée avec succès");
                log.info("Crédit approuvé: ID={}, Admin={}", id, user.getUsername());

            } else if ("reject".equals(decision)) {
                creditService.rejectCreditRequest(id, reason);
                redirectAttributes.addFlashAttribute("successMessage",
                        "Demande de crédit #" + id + " rejetée");
                log.info("Crédit rejeté: ID={}, Admin={}", id, user.getUsername());

            } else {
                redirectAttributes.addFlashAttribute("errorMessage", "Décision invalide");
            }

        } catch (Exception e) {
            log.error("Erreur lors du traitement du crédit", e);
            redirectAttributes.addFlashAttribute("errorMessage", "Erreur lors du traitement: " + e.getMessage());
        }

        return "redirect:/admin/credits/list";
    }
}