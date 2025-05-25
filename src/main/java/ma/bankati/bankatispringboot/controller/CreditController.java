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

/**
 * Contrôleur pour la gestion des crédits côté client
 */
@Controller
@RequestMapping("/client/credit")
@RequiredArgsConstructor
@Slf4j
@PreAuthorize("hasRole('USER')")
public class CreditController {

    private final CreditService creditService;

    /**
     * Liste des demandes de crédit du client
     */
    @GetMapping({"/", "/list"})
    public String listCreditRequests(@AuthenticationPrincipal User user, Model model) {
        log.debug("Affichage des crédits pour le client: {}", user.getUsername());

        List<CreditRequest> creditRequests = creditService.findByClientId(user.getId());

        model.addAttribute("creditRequests", creditRequests);
        model.addAttribute("user", user);
        model.addAttribute("AppName", "Bankati");

        return "client/credit/list";
    }

    /**
     * Formulaire pour une nouvelle demande de crédit
     */
    @GetMapping("/new")
    public String newCreditForm(@AuthenticationPrincipal User user, Model model) {
        log.debug("Affichage formulaire nouvelle demande pour: {}", user.getUsername());

        CreditRequest creditRequest = new CreditRequest();
        creditRequest.setClient(user);

        model.addAttribute("creditRequest", creditRequest);
        model.addAttribute("user", user);
        model.addAttribute("AppName", "Bankati");

        return "client/credit/form";
    }

    /**
     * Traitement du formulaire de nouvelle demande
     */
    @PostMapping("/save")
    public String saveCreditRequest(@Valid @ModelAttribute CreditRequest creditRequest,
                                    BindingResult bindingResult,
                                    @AuthenticationPrincipal User user,
                                    Model model,
                                    RedirectAttributes redirectAttributes) {

        log.debug("Traitement nouvelle demande pour: {}", user.getUsername());

        // Validation des erreurs de binding
        if (bindingResult.hasErrors()) {
            model.addAttribute("user", user);
            model.addAttribute("AppName", "Bankati");
            return "client/credit/form";
        }

        try {
            // Définir le client
            creditRequest.setClient(user);

            // Validation métier supplémentaire
            if (creditRequest.getAmount().compareTo(BigDecimal.valueOf(1000)) < 0) {
                model.addAttribute("errorMessage", "Le montant minimum est de 1,000 DH");
                model.addAttribute("user", user);
                model.addAttribute("AppName", "Bankati");
                return "client/credit/form";
            }

            if (creditRequest.getDuration() < 6 || creditRequest.getDuration() > 120) {
                model.addAttribute("errorMessage", "La durée doit être entre 6 et 120 mois");
                model.addAttribute("user", user);
                model.addAttribute("AppName", "Bankati");
                return "client/credit/form";
            }

            // Créer la demande
            CreditRequest saved = creditService.createCreditRequest(creditRequest);

            redirectAttributes.addFlashAttribute("successMessage",
                    "Votre demande de crédit #" + saved.getId() + " a été soumise avec succès");

            log.info("Demande de crédit créée: ID={}, Client={}, Montant={}",
                    saved.getId(), user.getUsername(), saved.getAmount());

            return "redirect:/client/credit/list";

        } catch (Exception e) {
            log.error("Erreur lors de la création de la demande", e);
            model.addAttribute("errorMessage", "Erreur lors de la soumission: " + e.getMessage());
            model.addAttribute("user", user);
            model.addAttribute("AppName", "Bankati");
            return "client/credit/form";
        }
    }

    /**
     * Détails d'une demande de crédit
     */
    @GetMapping("/details")
    public String creditDetails(@RequestParam("id") Long id,
                                @AuthenticationPrincipal User user,
                                Model model,
                                RedirectAttributes redirectAttributes) {

        log.debug("Affichage détails crédit ID: {} pour: {}", id, user.getUsername());

        Optional<CreditRequest> creditOpt = creditService.findById(id);

        if (creditOpt.isEmpty()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Demande de crédit introuvable");
            return "redirect:/client/credit/list";
        }

        CreditRequest creditRequest = creditOpt.get();

        // Vérifier que la demande appartient bien au client connecté
        if (!creditRequest.getClient().getId().equals(user.getId())) {
            redirectAttributes.addFlashAttribute("errorMessage", "Accès non autorisé à cette demande");
            return "redirect:/client/credit/list";
        }

        model.addAttribute("creditRequest", creditRequest);
        model.addAttribute("user", user);
        model.addAttribute("AppName", "Bankati");

        return "client/credit/details";
    }

    /**
     * Suppression d'une demande de crédit (seulement si en attente)
     */
    @GetMapping("/delete")
    public String deleteCreditRequest(@RequestParam("id") Long id,
                                      @AuthenticationPrincipal User user,
                                      RedirectAttributes redirectAttributes) {

        log.debug("Suppression crédit ID: {} par: {}", id, user.getUsername());

        try {
            Optional<CreditRequest> creditOpt = creditService.findById(id);

            if (creditOpt.isEmpty()) {
                redirectAttributes.addFlashAttribute("errorMessage", "Demande de crédit introuvable");
                return "redirect:/client/credit/list";
            }

            CreditRequest creditRequest = creditOpt.get();

            // Vérifier que la demande appartient au client
            if (!creditRequest.getClient().getId().equals(user.getId())) {
                redirectAttributes.addFlashAttribute("errorMessage", "Vous ne pouvez pas supprimer cette demande");
                return "redirect:/client/credit/list";
            }

            // Vérifier que la demande peut être supprimée
            if (!creditService.canDeleteCreditRequest(id)) {
                redirectAttributes.addFlashAttribute("errorMessage", "Seules les demandes en attente peuvent être supprimées");
                return "redirect:/client/credit/list";
            }

            creditService.deleteById(id);

            redirectAttributes.addFlashAttribute("successMessage", "Demande de crédit supprimée avec succès");

            log.info("Demande de crédit supprimée: ID={}, Client={}", id, user.getUsername());

        } catch (Exception e) {
            log.error("Erreur lors de la suppression", e);
            redirectAttributes.addFlashAttribute("errorMessage", "Erreur lors de la suppression: " + e.getMessage());
        }

        return "redirect:/client/credit/list";
    }
}