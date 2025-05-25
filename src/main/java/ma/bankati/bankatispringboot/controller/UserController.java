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

    /**
     * Affiche la liste des utilisateurs avec formulaire d'ajout
     */
    @GetMapping({"/", ""})
    public String listUsers(@AuthenticationPrincipal User currentUser, Model model) {
        log.debug("Liste des utilisateurs pour admin: {}", currentUser.getUsername());

        try {
            List<User> users = userService.findAll();
            User editUser = new User();

            model.addAttribute("users", users);
            model.addAttribute("user", editUser);
            model.addAttribute("currentUser", currentUser);
            model.addAttribute("editMode", false);

            // Statistiques pour l'affichage
            long totalUsers = users.size();
            long totalClients = users.stream().mapToLong(u -> u.getRole() == ERole.USER ? 1 : 0).sum();
            long totalAdmins = users.stream().mapToLong(u -> u.getRole() == ERole.ADMIN ? 1 : 0).sum();
            long activeUsers = users.stream().mapToLong(u -> u.getEnabled() ? 1 : 0).sum();

            model.addAttribute("totalUsers", totalUsers);
            model.addAttribute("totalClients", totalClients);
            model.addAttribute("totalAdmins", totalAdmins);
            model.addAttribute("activeUsers", activeUsers);

            log.info("Admin {} consulte {} utilisateurs (Clients: {}, Admins: {}, Actifs: {})",
                    currentUser.getUsername(), totalUsers, totalClients, totalAdmins, activeUsers);

        } catch (Exception e) {
            log.error("Erreur lors de la récupération des utilisateurs pour admin {}", currentUser.getUsername(), e);
            model.addAttribute("users", List.of());
            model.addAttribute("user", new User());
            model.addAttribute("currentUser", currentUser);
            model.addAttribute("errorMessage", "Erreur lors du chargement des utilisateurs");
        }

        return "admin/users";
    }

    /**
     * Prépare l'édition d'un utilisateur existant
     */
    @GetMapping("/edit")
    public String editUser(@RequestParam("id") Long id,
                           @AuthenticationPrincipal User currentUser,
                           Model model,
                           RedirectAttributes redirectAttributes) {
        log.debug("Édition utilisateur ID: {} par admin: {}", id, currentUser.getUsername());

        try {
            Optional<User> userOpt = userService.findById(id);

            if (userOpt.isEmpty()) {
                log.warn("Utilisateur ID {} introuvable pour édition par admin {}", id, currentUser.getUsername());
                redirectAttributes.addFlashAttribute("errorMessage", "Utilisateur introuvable");
                return "redirect:/users";
            }

            List<User> users = userService.findAll();
            User userToEdit = userOpt.get();

            model.addAttribute("users", users);
            model.addAttribute("user", userToEdit);
            model.addAttribute("currentUser", currentUser);
            model.addAttribute("editMode", true);

            // Statistiques
            long totalUsers = users.size();
            long totalClients = users.stream().mapToLong(u -> u.getRole() == ERole.USER ? 1 : 0).sum();
            long totalAdmins = users.stream().mapToLong(u -> u.getRole() == ERole.ADMIN ? 1 : 0).sum();
            long activeUsers = users.stream().mapToLong(u -> u.getEnabled() ? 1 : 0).sum();

            model.addAttribute("totalUsers", totalUsers);
            model.addAttribute("totalClients", totalClients);
            model.addAttribute("totalAdmins", totalAdmins);
            model.addAttribute("activeUsers", activeUsers);

            log.debug("Préparation édition utilisateur {} par admin {}", userToEdit.getUsername(), currentUser.getUsername());

        } catch (Exception e) {
            log.error("Erreur lors de la préparation d'édition de l'utilisateur {} par admin {}", id, currentUser.getUsername(), e);
            redirectAttributes.addFlashAttribute("errorMessage", "Erreur lors du chargement de l'utilisateur");
            return "redirect:/users";
        }

        return "admin/users";
    }

    /**
     * Sauvegarde un utilisateur (création ou modification)
     */
    @PostMapping("/save")
    public String saveUser(@Valid @ModelAttribute("user") User user,
                           BindingResult bindingResult,
                           @AuthenticationPrincipal User currentUser,
                           Model model,
                           RedirectAttributes redirectAttributes) {
        log.debug("Sauvegarde utilisateur: {} par admin: {}", user.getUsername(), currentUser.getUsername());

        // Vérifier les erreurs de validation
        if (bindingResult.hasErrors()) {
            log.warn("Erreurs de validation pour l'utilisateur {} par admin {}: {}",
                    user.getUsername(), currentUser.getUsername(), bindingResult.getAllErrors());

            List<User> users = userService.findAll();
            model.addAttribute("users", users);
            model.addAttribute("currentUser", currentUser);
            model.addAttribute("editMode", user.getId() != null);

            // Recalculer les statistiques pour l'affichage en cas d'erreur
            long totalUsers = users.size();
            long totalClients = users.stream().mapToLong(u -> u.getRole() == ERole.USER ? 1 : 0).sum();
            long totalAdmins = users.stream().mapToLong(u -> u.getRole() == ERole.ADMIN ? 1 : 0).sum();
            long activeUsers = users.stream().mapToLong(u -> u.getEnabled() ? 1 : 0).sum();

            model.addAttribute("totalUsers", totalUsers);
            model.addAttribute("totalClients", totalClients);
            model.addAttribute("totalAdmins", totalAdmins);
            model.addAttribute("activeUsers", activeUsers);

            return "admin/users";
        }

        try {
            // Validation spécifique pour la création
            if (user.getId() == null && userService.existsByUsername(user.getUsername())) {
                model.addAttribute("errorMessage", "Ce nom d'utilisateur existe déjà");
                List<User> users = userService.findAll();
                model.addAttribute("users", users);
                model.addAttribute("currentUser", currentUser);
                model.addAttribute("editMode", false);
                return "admin/users";
            }

            // Pour les modifications, conserver le mot de passe existant si vide
            if (user.getId() != null && (user.getPassword() == null || user.getPassword().trim().isEmpty())) {
                Optional<User> existingUserOpt = userService.findById(user.getId());
                if (existingUserOpt.isPresent()) {
                    user.setPassword(existingUserOpt.get().getPassword());
                }
            }

            // Validation du mot de passe pour les nouveaux utilisateurs
            if (user.getId() == null && (user.getPassword() == null || user.getPassword().trim().length() < 4)) {
                model.addAttribute("errorMessage", "Le mot de passe doit contenir au moins 4 caractères");
                List<User> users = userService.findAll();
                model.addAttribute("users", users);
                model.addAttribute("currentUser", currentUser);
                model.addAttribute("editMode", false);
                return "admin/users";
            }

            // Empêcher un admin de se rétrograder
            if (user.getId() != null && user.getId().equals(currentUser.getId()) && user.getRole() != ERole.ADMIN) {
                model.addAttribute("errorMessage", "Vous ne pouvez pas modifier votre propre rôle d'administrateur");
                List<User> users = userService.findAll();
                model.addAttribute("users", users);
                model.addAttribute("currentUser", currentUser);
                model.addAttribute("editMode", true);
                return "admin/users";
            }

            // Sauvegarder l'utilisateur
            User savedUser = userService.save(user);
            String action = user.getId() == null ? "créé" : "modifié";

            redirectAttributes.addFlashAttribute("successMessage",
                    "Utilisateur '" + savedUser.getUsername() + "' " + action + " avec succès");

            log.info("Utilisateur {} avec succès: ID={}, Username={}, Role={}, Admin={}",
                    action, savedUser.getId(), savedUser.getUsername(), savedUser.getRole(), currentUser.getUsername());

        } catch (IllegalArgumentException e) {
            log.warn("Validation échouée lors de la sauvegarde par admin {}: {}", currentUser.getUsername(), e.getMessage());
            model.addAttribute("errorMessage", e.getMessage());
            List<User> users = userService.findAll();
            model.addAttribute("users", users);
            model.addAttribute("currentUser", currentUser);
            model.addAttribute("editMode", user.getId() != null);
            return "admin/users";

        } catch (Exception e) {
            log.error("Erreur lors de la sauvegarde de l'utilisateur {} par admin {}", user.getUsername(), currentUser.getUsername(), e);
            redirectAttributes.addFlashAttribute("errorMessage", "Erreur lors de la sauvegarde : " + e.getMessage());
        }

        return "redirect:/users";
    }

    /**
     * Supprime un utilisateur
     */
    @GetMapping("/delete")
    public String deleteUser(@RequestParam("id") Long id,
                             @AuthenticationPrincipal User currentUser,
                             RedirectAttributes redirectAttributes) {
        log.debug("Suppression utilisateur ID: {} par admin: {}", id, currentUser.getUsername());

        try {
            // Empêcher l'auto-suppression
            if (id.equals(currentUser.getId())) {
                log.warn("Tentative d'auto-suppression par admin {}", currentUser.getUsername());
                redirectAttributes.addFlashAttribute("errorMessage",
                        "Vous ne pouvez pas supprimer votre propre compte");
                return "redirect:/users";
            }

            Optional<User> userOpt = userService.findById(id);
            if (userOpt.isEmpty()) {
                log.warn("Utilisateur ID {} introuvable pour suppression par admin {}", id, currentUser.getUsername());
                redirectAttributes.addFlashAttribute("errorMessage", "Utilisateur introuvable");
                return "redirect:/users";
            }

            User userToDelete = userOpt.get();
            String deletedUsername = userToDelete.getUsername();
            String deletedRole = userToDelete.getRole().name();

            // Supprimer l'utilisateur
            userService.deleteById(id);

            redirectAttributes.addFlashAttribute("successMessage",
                    "Utilisateur '" + deletedUsername + "' (" + deletedRole + ") supprimé avec succès");

            log.info("Utilisateur supprimé avec succès: ID={}, Username={}, Role={}, Admin={}",
                    id, deletedUsername, deletedRole, currentUser.getUsername());

        } catch (Exception e) {
            log.error("Erreur lors de la suppression de l'utilisateur {} par admin {}", id, currentUser.getUsername(), e);
            redirectAttributes.addFlashAttribute("errorMessage",
                    "Erreur lors de la suppression. L'utilisateur pourrait avoir des demandes de crédit associées.");
        }

        return "redirect:/users";
    }

    /**
     * Recherche d'utilisateurs
     */
    @GetMapping("/search")
    public String searchUsers(@RequestParam("q") String searchTerm,
                              @AuthenticationPrincipal User currentUser,
                              Model model) {
        log.debug("Recherche d'utilisateurs par admin {}: '{}'", currentUser.getUsername(), searchTerm);

        try {
            if (searchTerm == null || searchTerm.trim().isEmpty()) {
                return "redirect:/users";
            }

            List<User> users = userService.searchUsers(searchTerm.trim());

            model.addAttribute("users", users);
            model.addAttribute("user", new User());
            model.addAttribute("currentUser", currentUser);
            model.addAttribute("searchTerm", searchTerm);
            model.addAttribute("editMode", false);

            log.info("Recherche '{}' par admin {} : {} résultat(s)", searchTerm, currentUser.getUsername(), users.size());

        } catch (Exception e) {
            log.error("Erreur lors de la recherche d'utilisateurs par admin {}", currentUser.getUsername(), e);
            model.addAttribute("users", List.of());
            model.addAttribute("user", new User());
            model.addAttribute("currentUser", currentUser);
            model.addAttribute("errorMessage", "Erreur lors de la recherche");
        }

        return "admin/users";
    }

    /**
     * Bascule le statut d'un utilisateur (actif/inactif)
     */
    @PostMapping("/toggle-status")
    public String toggleUserStatus(@RequestParam("id") Long id,
                                   @AuthenticationPrincipal User currentUser,
                                   RedirectAttributes redirectAttributes) {
        log.debug("Basculement du statut de l'utilisateur ID: {} par admin: {}", id, currentUser.getUsername());

        try {
            // Empêcher de désactiver son propre compte
            if (id.equals(currentUser.getId())) {
                redirectAttributes.addFlashAttribute("errorMessage",
                        "Vous ne pouvez pas désactiver votre propre compte");
                return "redirect:/users";
            }

            userService.toggleUserStatus(id);
            redirectAttributes.addFlashAttribute("successMessage", "Statut de l'utilisateur modifié avec succès");

            log.info("Statut basculé pour l'utilisateur ID {} par admin {}", id, currentUser.getUsername());

        } catch (Exception e) {
            log.error("Erreur lors du basculement de statut pour l'utilisateur {} par admin {}", id, currentUser.getUsername(), e);
            redirectAttributes.addFlashAttribute("errorMessage", "Erreur lors de la modification du statut");
        }

        return "redirect:/users";
    }
}