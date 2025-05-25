package ma.bankati.bankatispringboot.service;

import ma.bankati.bankatispringboot.entity.User;
import ma.bankati.bankatispringboot.entity.enums.ERole;
import org.springframework.security.core.userdetails.UserDetailsService;

import java.util.List;
import java.util.Optional;

/**
 * Interface du service utilisateur
 * Étend UserDetailsService pour l'intégration avec Spring Security
 */
public interface UserService extends UserDetailsService {

    /**
     * Trouve tous les utilisateurs
     */
    List<User> findAll();

    /**
     * Trouve un utilisateur par son ID
     */
    Optional<User> findById(Long id);

    /**
     * Trouve un utilisateur par son nom d'utilisateur
     */
    Optional<User> findByUsername(String username);

    /**
     * Sauvegarde un utilisateur (création ou mise à jour)
     */
    User save(User user);

    /**
     * Supprime un utilisateur par son ID
     */
    void deleteById(Long id);

    /**
     * Vérifie si un nom d'utilisateur existe
     */
    boolean existsByUsername(String username);

    /**
     * Trouve tous les utilisateurs par rôle
     */
    List<User> findByRole(ERole role);

    /**
     * Trouve tous les clients
     */
    List<User> findAllClients();

    /**
     * Recherche des utilisateurs par terme
     */
    List<User> searchUsers(String searchTerm);

    /**
     * Compte les utilisateurs par rôle
     */
    long countByRole(ERole role);

    /**
     * Valide les données d'un utilisateur
     */
    void validateUser(User user);

    /**
     * Change le mot de passe d'un utilisateur
     */
    void changePassword(Long userId, String newPassword);

    /**
     * Active/désactive un utilisateur
     */
    void toggleUserStatus(Long userId);
}