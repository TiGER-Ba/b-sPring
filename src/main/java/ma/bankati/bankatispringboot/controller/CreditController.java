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
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jakarta.validation.Valid;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/client/credit")
@RequiredArgsConstructor
@Slf4j
@PreAuthorize("hasRole('USER')")
public class CreditController {

    private final CreditService creditService;

    /**
     * Affiche la liste des demandes de crédit du client connecté
     */
    @GetMapping({"/", "/list"})
    public String listCreditRequests(@AuthenticationPrincipal User user, Model model) {
        log.debug("Affichage des crédits pour le client: {}", user.getUsername());

        try {
            List<CreditRequest> creditRequests = creditService.findByClientId(user.getId());
            log.info("Client {} a {} demande(s) de crédit", user.getUsername(), creditRequests.size());

            model.addAttribute("creditRequests", creditRequests);
            model.addAttribute("user", user);

            // Calculer quelques statistiques pour l'affichage
            long pendingCount = creditRequests.stream()
                    .mapToLong(cr -> cr.getStatus() == CreditStatus.PENDING ? 1 : 0)
                    .sum();
            long approvedCount = creditRequests.stream()
                    .mapToLong(cr -> cr.getStatus() == CreditStatus.APPROVED ? 1 : 0)
                    .sum();
            long rejectedCount = creditRequests.stream()
                    .mapToLong(cr -> cr.getStatus() == CreditStatus.REJECTED ? 1 : 0)
                    .sum();

            model.addAttribute("pendingCount", pendingCount);
            model.addAttribute("approvedCount", approvedCount);
            model.addAttribute("rejectedCount", rejectedCount);

        } catch (Exception e) {
            log.error("Erreur lors de la récupération des crédits pour {}", user.getUsername(), e);
            model.addAttribute("creditRequests", List.of());
            model.addAttribute("user", user);
            model.addAttribute("errorMessage", "Erreur lors du chargement de vos demandes");
        }

        return "client/credit/list";
    }

    /**
     * Affiche le formulaire de nouvelle demande de crédit
     */
    @GetMapping("/new")
    public String newCreditForm(@AuthenticationPrincipal User user, Model model) {
        log.debug("Affichage formulaire nouvelle demande pour: {}", user.getUsername());

        CreditRequest creditRequest = new CreditRequest();
        creditRequest.setClient(user);

        model.addAttribute("creditRequest", creditRequest);
        model.addAttribute("user", user);

        return "client/credit/form";
    }

    /**
     * Traite la soumission d'une nouvelle demande de crédit
     */
    @PostMapping("/save")
    public String saveCreditRequest(@Valid @ModelAttribute CreditRequest creditRequest,
                                    BindingResult bindingResult,
                                    @AuthenticationPrincipal User user,
                                    Model model,
                                    RedirectAttributes redirectAttributes) {
        log.debug("Traitement nouvelle demande pour: {}", user.getUsername());

        // Vérification des erreurs de validation
        if (bindingResult.hasErrors()) {
            log.warn("Erreurs de validation pour la demande de {}: {}", user.getUsername(), bindingResult.getAllErrors());
            model.addAttribute("user", user);
            return "client/credit/form";
        }

        try {
            // S'assurer que le client est défini
            creditRequest.setClient(user);

            // Validations métier
            if (creditRequest.getAmount() == null || creditRequest.getAmount().compareTo(BigDecimal.valueOf(1000)) < 0) {
                model.addAttribute("errorMessage", "Le montant minimum est de 1,000 DH");
                model.addAttribute("user", user);
                return "client/credit/form";
            }

            if (creditRequest.getDuration() == null || creditRequest.getDuration() < 6 || creditRequest.getDuration() > 120) {
                model.addAttribute("errorMessage", "La durée doit être entre 6 et 120 mois");
                model.addAttribute("user", user);
                return "client/credit/form";
            }

            if (creditRequest.getDescription() == null || creditRequest.getDescription().trim().length() < 10) {
                model.addAttribute("errorMessage", "La description doit contenir au moins 10 caractères");
                model.addAttribute("user", user);
                return "client/credit/form";
            }

            // Créer la demande
            CreditRequest saved = creditService.createCreditRequest(creditRequest);

            redirectAttributes.addFlashAttribute("successMessage",
                    "Votre demande de crédit #" + saved.getId() + " a été soumise avec succès ! " +
                            "Vous recevrez une réponse sous 2 à 5 jours ouvrés.");

            log.info("Demande de crédit créée avec succès: ID={}, Client={}, Montant={} DH, Durée={} mois",
                    saved.getId(), user.getUsername(), saved.getAmount(), saved.getDuration());

            return "redirect:/client/credit/list";

        } catch (IllegalArgumentException e) {
            log.warn("Validation échouée pour {}: {}", user.getUsername(), e.getMessage());
            model.addAttribute("errorMessage", e.getMessage());
            model.addAttribute("user", user);
            return "client/credit/form";

        } catch (Exception e) {
            log.error("Erreur lors de la création de la demande pour {}", user.getUsername(), e);
            model.addAttribute("errorMessage", "Erreur inattendue lors de la soumission. Veuillez réessayer.");
            model.addAttribute("user", user);
            return "client/credit/form";
        }
    }

    /**
     * Affiche les détails d'une demande de crédit
     */
    @GetMapping("/details")
    public String creditDetails(@RequestParam("id") Long id,
                                @AuthenticationPrincipal User user,
                                Model model,
                                RedirectAttributes redirectAttributes) {
        log.debug("Affichage détails crédit ID: {} pour: {}", id, user.getUsername());

        try {
            Optional<CreditRequest> creditOpt = creditService.findById(id);

            if (creditOpt.isEmpty()) {
                redirectAttributes.addFlashAttribute("errorMessage", "Demande de crédit introuvable");
                return "redirect:/client/credit/list";
            }

            CreditRequest creditRequest = creditOpt.get();

            // Vérifier que la demande appartient bien au client connecté
            if (!creditRequest.getClient().getId().equals(user.getId())) {
                log.warn("Tentative d'accès non autorisé: Client {} essaie d'accéder au crédit {} du client {}",
                        user.getUsername(), id, creditRequest.getClient().getUsername());
                redirectAttributes.addFlashAttribute("errorMessage", "Accès non autorisé à cette demande");
                return "redirect:/client/credit/list";
            }

            model.addAttribute("creditRequest", creditRequest);
            model.addAttribute("user", user);

            log.debug("Affichage réussi des détails du crédit {} pour {}", id, user.getUsername());

        } catch (Exception e) {
            log.error("Erreur lors de l'affichage des détails du crédit {} pour {}", id, user.getUsername(), e);
            redirectAttributes.addFlashAttribute("errorMessage", "Erreur lors du chargement des détails");
            return "redirect:/client/credit/list";
        }

        return "client/credit/details";
    }

    /**
     * Supprime une demande de crédit (seulement si elle est en attente)
     */
    @GetMapping("/delete")
    public String deleteCreditRequest(@RequestParam("id") Long id,
                                      @AuthenticationPrincipal User user,
                                      RedirectAttributes redirectAttributes) {
        log.debug("Tentative suppression crédit ID: {} par: {}", id, user.getUsername());

        try {
            Optional<CreditRequest> creditOpt = creditService.findById(id);

            if (creditOpt.isEmpty()) {
                redirectAttributes.addFlashAttribute("errorMessage", "Demande de crédit introuvable");
                return "redirect:/client/credit/list";
            }

            CreditRequest creditRequest = creditOpt.get();

            // Vérifier que la demande appartient au client connecté
            if (!creditRequest.getClient().getId().equals(user.getId())) {
                log.warn("Tentative de suppression non autorisée: Client {} essaie de supprimer le crédit {} du client {}",
                        user.getUsername(), id, creditRequest.getClient().getUsername());
                redirectAttributes.addFlashAttribute("errorMessage", "Vous ne pouvez pas supprimer cette demande");
                return "redirect:/client/credit/list";
            }

            // Vérifier que la demande peut être supprimée
            if (!creditService.canDeleteCreditRequest(id)) {
                redirectAttributes.addFlashAttribute("errorMessage",
                        "Seules les demandes en attente peuvent être supprimées. Cette demande a déjà été traitée.");
                return "redirect:/client/credit/list";
            }

            // Supprimer la demande
            creditService.deleteById(id);

            redirectAttributes.addFlashAttribute("successMessage",
                    "Demande de crédit #" + id + " supprimée avec succès");

            log.info("Demande de crédit supprimée avec succès: ID={}, Client={}", id, user.getUsername());

        } catch (Exception e) {
            log.error("Erreur lors de la suppression du crédit {} par {}", id, user.getUsername(), e);
            redirectAttributes.addFlashAttribute("errorMessage",
                    "Erreur lors de la suppression. Veuillez réessayer.");
        }

        return "redirect:/client/credit/list";
    }
}