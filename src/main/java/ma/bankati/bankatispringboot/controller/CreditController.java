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

    @GetMapping({"/", "/list"})
    public String listCreditRequests(@AuthenticationPrincipal User user, Model model) {
        log.debug("Affichage des crédits pour le client: {}", user.getUsername());

        List<CreditRequest> creditRequests = creditService.findByClientId(user.getId());

        model.addAttribute("creditRequests", creditRequests);
        model.addAttribute("user", user);
        // AppName et currentPage sont ajoutés automatiquement par GlobalControllerAdvice

        return "client/credit/list";
    }

    @GetMapping("/new")
    public String newCreditForm(@AuthenticationPrincipal User user, Model model) {
        log.debug("Affichage formulaire nouvelle demande pour: {}", user.getUsername());

        CreditRequest creditRequest = new CreditRequest();
        creditRequest.setClient(user);

        model.addAttribute("creditRequest", creditRequest);
        model.addAttribute("user", user);
        // AppName et currentPage sont ajoutés automatiquement par GlobalControllerAdvice

        return "client/credit/form";
    }

    @PostMapping("/save")
    public String saveCreditRequest(@Valid @ModelAttribute CreditRequest creditRequest,
                                    BindingResult bindingResult,
                                    @AuthenticationPrincipal User user,
                                    Model model,
                                    RedirectAttributes redirectAttributes) {
        log.debug("Traitement nouvelle demande pour: {}", user.getUsername());

        if (bindingResult.hasErrors()) {
            model.addAttribute("user", user);
            // AppName et currentPage sont ajoutés automatiquement par GlobalControllerAdvice
            return "client/credit/form";
        }

        try {
            creditRequest.setClient(user);

            if (creditRequest.getAmount().compareTo(BigDecimal.valueOf(1000)) < 0) {
                model.addAttribute("errorMessage", "Le montant minimum est de 1,000 DH");
                model.addAttribute("user", user);
                // AppName et currentPage sont ajoutés automatiquement par GlobalControllerAdvice
                return "client/credit/form";
            }

            if (creditRequest.getDuration() < 6 || creditRequest.getDuration() > 120) {
                model.addAttribute("errorMessage", "La durée doit être entre 6 et 120 mois");
                model.addAttribute("user", user);
                // AppName et currentPage sont ajoutés automatiquement par GlobalControllerAdvice
                return "client/credit/form";
            }

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
            // AppName et currentPage sont ajoutés automatiquement par GlobalControllerAdvice
            return "client/credit/form";
        }
    }

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
        if (!creditRequest.getClient().getId().equals(user.getId())) {
            redirectAttributes.addFlashAttribute("errorMessage", "Accès non autorisé à cette demande");
            return "redirect:/client/credit/list";
        }

        model.addAttribute("creditRequest", creditRequest);
        model.addAttribute("user", user);
        // AppName et currentPage sont ajoutés automatiquement par GlobalControllerAdvice

        return "client/credit/details";
    }

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
            if (!creditRequest.getClient().getId().equals(user.getId())) {
                redirectAttributes.addFlashAttribute("errorMessage", "Vous ne pouvez pas supprimer cette demande");
                return "redirect:/client/credit/list";
            }

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