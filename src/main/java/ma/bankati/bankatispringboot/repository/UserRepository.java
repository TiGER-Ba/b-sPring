package ma.bankati.bankatispringboot.repository;

import ma.bankati.bankatispringboot.entity.User;
import ma.bankati.bankatispringboot.entity.enums.ERole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Repository pour l'entité User
 * Spring Data JPA génère automatiquement l'implémentation
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    /**
     * Trouve un utilisateur par son nom d'utilisateur
     * Utilisé pour l'authentification Spring Security
     */
    Optional<User> findByUsername(String username);

    /**
     * Trouve un utilisateur par nom d'utilisateur et mot de passe
     * (Méthode héritée de votre ancien DAO)
     */
    Optional<User> findByUsernameAndPassword(String username, String password);

    /**
     * Vérifie si un nom d'utilisateur existe déjà
     */
    boolean existsByUsername(String username);

    /**
     * Trouve tous les utilisateurs par rôle
     */
    List<User> findByRole(ERole role);

    /**
     * Trouve tous les utilisateurs actifs
     */
    List<User> findByEnabledTrue();

    /**
     * Trouve tous les utilisateurs créés après une certaine date
     */
    List<User> findByCreationDateAfter(LocalDate date);

    /**
     * Compte le nombre d'utilisateurs par rôle
     */
    @Query("SELECT COUNT(u) FROM User u WHERE u.role = :role")
    long countByRole(@Param("role") ERole role);

    /**
     * Trouve les utilisateurs par nom ou prénom (recherche insensible à la casse)
     */
    @Query("SELECT u FROM User u WHERE " +
            "LOWER(u.firstName) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
            "LOWER(u.lastName) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
            "LOWER(u.username) LIKE LOWER(CONCAT('%', :search, '%'))")
    List<User> searchUsers(@Param("search") String search);

    /**
     * Trouve tous les clients (utilisateurs avec rôle USER)
     */
    @Query("SELECT u FROM User u WHERE u.role = 'USER' ORDER BY u.firstName, u.lastName")
    List<User> findAllClients();
}