package ma.bankati.bankatispringboot.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ma.bankati.bankatispringboot.entity.User;
import ma.bankati.bankatispringboot.entity.enums.ERole;
import ma.bankati.bankatispringboot.repository.UserRepository;
import ma.bankati.bankatispringboot.service.UserService;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Implémentation du service utilisateur
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        log.debug("Tentative de connexion pour l'utilisateur: {}", username);

        return userRepository.findByUsername(username)
                .orElseThrow(() -> {
                    log.warn("Utilisateur non trouvé: {}", username);
                    return new UsernameNotFoundException("Utilisateur non trouvé: " + username);
                });
    }

    @Override
    @Transactional(readOnly = true)
    public List<User> findAll() {
        log.debug("Récupération de tous les utilisateurs");
        return userRepository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<User> findById(Long id) {
        log.debug("Recherche utilisateur par ID: {}", id);
        return userRepository.findById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<User> findByUsername(String username) {
        log.debug("Recherche utilisateur par username: {}", username);
        return userRepository.findByUsername(username);
    }

    @Override
    public User save(User user) {
        log.debug("Sauvegarde utilisateur: {}", user.getUsername());

        // Validation
        validateUser(user);

        // Encodage du mot de passe pour les nouveaux utilisateurs
        if (user.getId() == null && user.getPassword() != null) {
            user.setPassword(passwordEncoder.encode(user.getPassword()));
        }

        // Définir la date de création pour les nouveaux utilisateurs
        if (user.getId() == null && user.getCreationDate() == null) {
            user.setCreationDate(LocalDate.now());
        }

        User savedUser = userRepository.save(user);
        log.info("Utilisateur sauvegardé avec succès: ID = {}, Username = {}",
                savedUser.getId(), savedUser.getUsername());

        return savedUser;
    }

    @Override
    public void deleteById(Long id) {
        log.debug("Suppression utilisateur ID: {}", id);

        if (!userRepository.existsById(id)) {
            throw new IllegalArgumentException("Utilisateur non trouvé avec l'ID: " + id);
        }

        userRepository.deleteById(id);
        log.info("Utilisateur supprimé: ID = {}", id);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsByUsername(String username) {
        return userRepository.existsByUsername(username);
    }

    @Override
    @Transactional(readOnly = true)
    public List<User> findByRole(ERole role) {
        log.debug("Recherche utilisateurs par rôle: {}", role);
        return userRepository.findByRole(role);
    }

    @Override
    @Transactional(readOnly = true)
    public List<User> findAllClients() {
        log.debug("Récupération de tous les clients");
        return userRepository.findAllClients();
    }

    @Override
    @Transactional(readOnly = true)
    public List<User> searchUsers(String searchTerm) {
        log.debug("Recherche utilisateurs avec le terme: {}", searchTerm);
        return userRepository.searchUsers(searchTerm);
    }

    @Override
    @Transactional(readOnly = true)
    public long countByRole(ERole role) {
        return userRepository.countByRole(role);
    }

    @Override
    public void validateUser(User user) {
        if (user == null) {
            throw new IllegalArgumentException("L'utilisateur ne peut pas être null");
        }

        if (user.getUsername() == null || user.getUsername().trim().isEmpty()) {
            throw new IllegalArgumentException("Le nom d'utilisateur est obligatoire");
        }

        if (user.getUsername().length() < 3) {
            throw new IllegalArgumentException("Le nom d'utilisateur doit contenir au moins 3 caractères");
        }

        // Vérifier l'unicité du nom d'utilisateur pour les nouveaux utilisateurs
        if (user.getId() == null && existsByUsername(user.getUsername())) {
            throw new IllegalArgumentException("Ce nom d'utilisateur existe déjà");
        }

        if (user.getFirstName() == null || user.getFirstName().trim().isEmpty()) {
            throw new IllegalArgumentException("Le prénom est obligatoire");
        }

        if (user.getLastName() == null || user.getLastName().trim().isEmpty()) {
            throw new IllegalArgumentException("Le nom est obligatoire");
        }

        if (user.getRole() == null) {
            throw new IllegalArgumentException("Le rôle est obligatoire");
        }

        // Validation du mot de passe pour les nouveaux utilisateurs
        if (user.getId() == null && (user.getPassword() == null || user.getPassword().length() < 4)) {
            throw new IllegalArgumentException("Le mot de passe doit contenir au moins 4 caractères");
        }
    }

    @Override
    public void changePassword(Long userId, String newPassword) {
        log.debug("Changement de mot de passe pour l'utilisateur ID: {}", userId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("Utilisateur non trouvé"));

        if (newPassword == null || newPassword.length() < 4) {
            throw new IllegalArgumentException("Le mot de passe doit contenir au moins 4 caractères");
        }

        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);

        log.info("Mot de passe changé pour l'utilisateur: {}", user.getUsername());
    }

    @Override
    public void toggleUserStatus(Long userId) {
        log.debug("Basculement du statut pour l'utilisateur ID: {}", userId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("Utilisateur non trouvé"));

        user.setEnabled(!user.getEnabled());
        userRepository.save(user);

        log.info("Statut basculé pour l'utilisateur: {} - Actif: {}",
                user.getUsername(), user.getEnabled());
    }
}