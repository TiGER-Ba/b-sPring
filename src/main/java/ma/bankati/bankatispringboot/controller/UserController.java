package ma.bankati.bankatispringboot.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ma.bankati.bankatispringboot.entity.User;
import ma.bankati.bankatispringboot.entity.enums.ERole;
import ma.bankati.bankatispringboot.service.UserService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jakarta.validation.Valid;
import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/users")
@RequiredArgsConstructor
@Slf4j
@PreAuthorize("hasRole('ADMIN')")
public class UserController {

    private final UserService userService;

    @GetMapping({"/", ""})
    public String listUsers(@AuthenticationPrincipal User currentUser, Model model) {
        log.debug("Liste des utilisateurs pour admin: {}", currentUser.getUsername());

        List<User> users = userService.findAll();
        User editUser = new User();

        model.addAttribute("users", users);
        model.addAttribute("user", editUser);
        model.addAttribute("currentUser", currentUser);
        // AppName et currentPage sont ajoutés automatiquement par GlobalControllerAdvice

        return "admin/users";
    }

    @GetMapping("/edit")
    public String editUser(@RequestParam("id") Long id,
                           @AuthenticationPrincipal User currentUser,
                           Model model,
                           RedirectAttributes redirectAttributes) {
        log.debug("Édition utilisateur ID: {} par admin: {}", id, currentUser.getUsername());

        Optional<User> userOpt = userService.findById(id);
        if (userOpt.isEmpty()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Utilisateur introuvable");
            return "redirect:/users";
        }

        List<User> users = userService.findAll();

        model.addAttribute("users", users);
        model.addAttribute("user", userOpt.get());
        model.addAttribute("currentUser", currentUser);
        model.addAttribute("editMode", true);
        // AppName et currentPage sont ajoutés automatiquement par GlobalControllerAdvice

        return "admin/users";
    }

    @PostMapping("/save")
    public String saveUser(@Valid @ModelAttribute("user") User user,
                           BindingResult bindingResult,
                           @AuthenticationPrincipal User currentUser,
                           Model model,
                           RedirectAttributes redirectAttributes) {
        log.debug("Sauvegarde utilisateur: {} par admin: {}", user.getUsername(), currentUser.getUsername());

        if (bindingResult.hasErrors()) {
            List<User> users = userService.findAll();
            model.addAttribute("users", users);
            model.addAttribute("currentUser", currentUser);
            // AppName et currentPage sont ajoutés automatiquement par GlobalControllerAdvice
            return "admin/users";
        }

        try {
            if (user.getId() == null && userService.existsByUsername(user.getUsername())) {
                model.addAttribute("errorMessage", "Ce nom d'utilisateur existe déjà");
                List<User> users = userService.findAll();
                model.addAttribute("users", users);
                model.addAttribute("currentUser", currentUser);
                // AppName et currentPage sont ajoutés automatiquement par GlobalControllerAdvice
                return "admin/users";
            }

            if (user.getId() != null && (user.getPassword() == null || user.getPassword().trim().isEmpty())) {
                Optional<User> existingUserOpt = userService.findById(user.getId());
                if (existingUserOpt.isPresent()) {
                    user.setPassword(existingUserOpt.get().getPassword());
                }
            }

            User savedUser = userService.save(user);
            String action = user.getId() == null ? "créé" : "modifié";
            redirectAttributes.addFlashAttribute("successMessage",
                    "Utilisateur " + savedUser.getUsername() + " " + action + " avec succès");

            log.info("Utilisateur {}: ID={}, Username={}, Admin={}",
                    action, savedUser.getId(), savedUser.getUsername(), currentUser.getUsername());

        } catch (Exception e) {
            log.error("Erreur lors de la sauvegarde", e);
            redirectAttributes.addFlashAttribute("errorMessage", "Erreur: " + e.getMessage());
        }

        return "redirect:/users";
    }

    @GetMapping("/delete")
    public String deleteUser(@RequestParam("id") Long id,
                             @AuthenticationPrincipal User currentUser,
                             RedirectAttributes redirectAttributes) {
        log.debug("Suppression utilisateur ID: {} par admin: {}", id, currentUser.getUsername());

        try {
            if (id.equals(currentUser.getId())) {
                redirectAttributes.addFlashAttribute("errorMessage", "Vous ne pouvez pas supprimer votre propre compte");
                return "redirect:/users";
            }

            Optional<User> userOpt = userService.findById(id);
            if (userOpt.isEmpty()) {
                redirectAttributes.addFlashAttribute("errorMessage", "Utilisateur introuvable");
                return "redirect:/users";
            }

            String deletedUsername = userOpt.get().getUsername();
            userService.deleteById(id);

            redirectAttributes.addFlashAttribute("successMessage",
                    "Utilisateur " + deletedUsername + " supprimé avec succès");

            log.info("Utilisateur supprimé: ID={}, Username={}, Admin={}",
                    id, deletedUsername, currentUser.getUsername());

        } catch (Exception e) {
            log.error("Erreur lors de la suppression", e);
            redirectAttributes.addFlashAttribute("errorMessage", "Erreur lors de la suppression: " + e.getMessage());
        }

        return "redirect:/users";
    }
}