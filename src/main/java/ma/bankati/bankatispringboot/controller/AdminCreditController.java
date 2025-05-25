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

    /**
     * Liste toutes les demandes de crédit avec filtrage optionnel par statut
     */
    @GetMapping({"/", "/list"})
    public String listAllCredits(@RequestParam(value = "status", required = false) String status,
                                 @AuthenticationPrincipal User user,
                                 Model model) {
        log.debug("Liste des crédits pour admin: {}, filtre: {}", user.getUsername(), status);

        try {
            List<CreditRequest> credits;

            if (status != null && !status.isEmpty()) {
                try {
                    CreditStatus creditStatus = CreditStatus.valueOf(status.toUpperCase());
                    credits = creditService.findByStatus(creditStatus);
                    model.addAttribute("statusFilter", status.toLowerCase());
                    log.info("Admin {} consulte {} crédits avec statut {}", user.getUsername(), credits.size(), status);
                } catch (IllegalArgumentException e) {
                    log.warn("Statut invalide demandé: {}", status);
                    credits = creditService.findAll();
                    model.addAttribute("statusFilter", "");
                }
            } else {
                credits = creditService.findAll();
                model.addAttribute("statusFilter", "");
            }

            model.addAttribute("credits", credits);
            model.addAttribute("user", user);

            // Calculer les statistiques pour l'affichage
            long totalCredits = credits.size();
            long pendingCount = credits.stream().mapToLong(c -> c.getStatus() == CreditStatus.PENDING ? 1 : 0).sum();
            long approvedCount = credits.stream().mapToLong(c -> c.getStatus() == CreditStatus.APPROVED ? 1 : 0).sum();
            long rejectedCount = credits.stream().mapToLong(c -> c.getStatus() == CreditStatus.REJECTED ? 1 : 0).sum();

            model.addAttribute("totalCredits", totalCredits);
            model.addAttribute("pendingCount", pendingCount);
            model.addAttribute("approvedCount", approvedCount);
            model.addAttribute("rejectedCount", rejectedCount);

            log.debug("Statistiques - Total: {}, En attente: {}, Approuvés: {}, Rejetés: {}",
                    totalCredits, pendingCount, approvedCount, rejectedCount);

        } catch (Exception e) {
            log.error("Erreur lors de la récupération des crédits pour admin {}", user.getUsername(), e);
            model.addAttribute("credits", List.of());
            model.addAttribute("user", user);
            model.addAttribute("errorMessage", "Erreur lors du chargement des demandes");
        }

        return "admin/credit/list";
    }

    /**
     * Liste spécifiquement les demandes en attente
     */
    @GetMapping("/pending")
    public String listPendingCredits(@AuthenticationPrincipal User user, Model model) {
        log.debug("Liste des crédits en attente pour admin: {}", user.getUsername());

        try {
            List<CreditRequest> pendingCredits = creditService.findByStatus(CreditStatus.PENDING);
            log.info("Admin {} consulte {} crédits en attente", user.getUsername(), pendingCredits.size());

            model.addAttribute("credits", pendingCredits);
            model.addAttribute("statusFilter", "pending");
            model.addAttribute("user", user);

            // Statistiques spécifiques aux demandes en attente
            model.addAttribute("totalCredits", pendingCredits.size());
            model.addAttribute("pendingCount", pendingCredits.size());
            model.addAttribute("approvedCount", 0L);
            model.addAttribute("rejectedCount", 0L);

        } catch (Exception e) {
            log.error("Erreur lors de la récupération des crédits en attente pour admin {}", user.getUsername(), e);
            model.addAttribute("credits", List.of());
            model.addAttribute("user", user);
            model.addAttribute("errorMessage", "Erreur lors du chargement des demandes en attente");
        }

        return "admin/credit/list";
    }

    /**
     * Affiche les détails d'une demande de crédit
     */
    @GetMapping("/details")
    public String creditDetails(@RequestParam("id") Long id,
                                @AuthenticationPrincipal User user,
                                Model model,
                                RedirectAttributes redirectAttributes) {
        log.debug("Détails crédit ID: {} pour admin: {}", id, user.getUsername());

        try {
            Optional<CreditRequest> creditOpt = creditService.findById(id);

            if (creditOpt.isEmpty()) {
                log.warn("Crédit ID {} introuvable pour admin {}", id, user.getUsername());
                redirectAttributes.addFlashAttribute("errorMessage", "Demande de crédit introuvable");
                return "redirect:/admin/credits/list";
            }

            CreditRequest creditRequest = creditOpt.get();
            model.addAttribute("creditRequest", creditRequest);
            model.addAttribute("user", user);

            log.debug("Affichage réussi des détails du crédit {} pour admin {}", id, user.getUsername());

        } catch (Exception e) {
            log.error("Erreur lors de l'affichage des détails du crédit {} pour admin {}", id, user.getUsername(), e);
            redirectAttributes.addFlashAttribute("errorMessage", "Erreur lors du chargement des détails");
            return "redirect:/admin/credits/list";
        }

        return "admin/credit/details";
    }

    /**
     * Traite une demande de crédit (approbation ou rejet)
     */
    @PostMapping("/process")
    public String processCreditRequest(@RequestParam("id") Long id,
                                       @RequestParam("decision") String decision,
                                       @RequestParam(value = "reason", required = false) String reason,
                                       @AuthenticationPrincipal User user,
                                       RedirectAttributes redirectAttributes) {
        log.debug("Traitement crédit ID: {} par admin: {}, décision: {}", id, user.getUsername(), decision);

        try {
            // Vérifier que la demande existe et peut être traitée
            if (!creditService.canProcessCreditRequest(id)) {
                log.warn("Tentative de traitement d'un crédit non traitable: ID={}, Admin={}", id, user.getUsername());
                redirectAttributes.addFlashAttribute("errorMessage",
                        "Cette demande ne peut plus être traitée (déjà traitée ou inexistante)");
                return "redirect:/admin/credits/list";
            }

            // Traiter selon la décision
            if ("approve".equals(decision)) {
                creditService.approveCreditRequest(id, reason);
                redirectAttributes.addFlashAttribute("successMessage",
                        "Demande de crédit #" + id + " approuvée avec succès ! Le montant a été crédité.");
                log.info("Crédit approuvé: ID={}, Admin={}, Commentaire={}", id, user.getUsername(), reason);

            } else if ("reject".equals(decision)) {
                if (reason == null || reason.trim().isEmpty()) {
                    redirectAttributes.addFlashAttribute("errorMessage",
                            "Un motif de refus est obligatoire");
                    return "redirect:/admin/credits/details?id=" + id;
                }

                creditService.rejectCreditRequest(id, reason);
                redirectAttributes.addFlashAttribute("successMessage",
                        "Demande de crédit #" + id + " rejetée. Le client a été notifié.");
                log.info("Crédit rejeté: ID={}, Admin={}, Motif={}", id, user.getUsername(), reason);

            } else {
                log.warn("Décision invalide reçue: {}", decision);
                redirectAttributes.addFlashAttribute("errorMessage", "Décision invalide");
                return "redirect:/admin/credits/details?id=" + id;
            }

        } catch (IllegalArgumentException e) {
            log.warn("Erreur de validation lors du traitement du crédit {}: {}", id, e.getMessage());
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/admin/credits/details?id=" + id;

        } catch (IllegalStateException e) {
            log.warn("État invalide pour le traitement du crédit {}: {}", id, e.getMessage());
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/admin/credits/list";

        } catch (Exception e) {
            log.error("Erreur inattendue lors du traitement du crédit {} par admin {}", id, user.getUsername(), e);
            redirectAttributes.addFlashAttribute("errorMessage",
                    "Erreur inattendue lors du traitement. Veuillez réessayer.");
            return "redirect:/admin/credits/details?id=" + id;
        }

        return "redirect:/admin/credits/list";
    }

    /**
     * Recherche de demandes par terme
     */
    @GetMapping("/search")
    public String searchCredits(@RequestParam("q") String searchTerm,
                                @AuthenticationPrincipal User user,
                                Model model) {
        log.debug("Recherche de crédits par admin {}: '{}'", user.getUsername(), searchTerm);

        try {
            if (searchTerm == null || searchTerm.trim().isEmpty()) {
                return "redirect:/admin/credits/list";
            }

            List<CreditRequest> credits = creditService.searchByDescription(searchTerm.trim());

            model.addAttribute("credits", credits);
            model.addAttribute("user", user);
            model.addAttribute("searchTerm", searchTerm);
            model.addAttribute("statusFilter", "search");

            log.info("Recherche '{}' par admin {} : {} résultat(s)", searchTerm, user.getUsername(), credits.size());

        } catch (Exception e) {
            log.error("Erreur lors de la recherche par admin {}", user.getUsername(), e);
            model.addAttribute("credits", List.of());
            model.addAttribute("user", user);
            model.addAttribute("errorMessage", "Erreur lors de la recherche");
        }

        return "admin/credit/list";
    }
}