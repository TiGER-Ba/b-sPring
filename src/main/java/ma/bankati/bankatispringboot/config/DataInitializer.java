// ===== ma/bankati/bankatispringboot/config/DataInitializer.java =====
package ma.bankati.bankatispringboot.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ma.bankati.bankatispringboot.entity.CreditRequest;
import ma.bankati.bankatispringboot.entity.User;
import ma.bankati.bankatispringboot.entity.enums.CreditStatus;
import ma.bankati.bankatispringboot.entity.enums.ERole;
import ma.bankati.bankatispringboot.repository.CreditRequestRepository;
import ma.bankati.bankatispringboot.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

/**
 * Initialise les données de base lors du démarrage de l'application
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final CreditRequestRepository creditRequestRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) throws Exception {
        log.info("🚀 Initialisation des données de l'application...");

        initializeUsers();
        initializeCreditRequests();

        log.info("✅ Initialisation des données terminée avec succès!");
    }

    private void initializeUsers() {
        // Créer l'administrateur par défaut
        if (!userRepository.existsByUsername("admin")) {
            User admin = User.builder()
                    .firstName("Admin")
                    .lastName("Bankati")
                    .username("admin")
                    .password(passwordEncoder.encode("admin"))
                    .role(ERole.ADMIN)
                    .creationDate(LocalDate.now())
                    .enabled(true)
                    .accountNonExpired(true)
                    .accountNonLocked(true)
                    .credentialsNonExpired(true)
                    .build();

            userRepository.save(admin);
            log.info("👤 Utilisateur admin créé - Login: admin / Mot de passe: admin");
        }

        // Créer un client de test
        if (!userRepository.existsByUsername("user")) {
            User user = User.builder()
                    .firstName("John")
                    .lastName("Doe")
                    .username("user")
                    .password(passwordEncoder.encode("user"))
                    .role(ERole.USER)
                    .creationDate(LocalDate.now())
                    .enabled(true)
                    .accountNonExpired(true)
                    .accountNonLocked(true)
                    .credentialsNonExpired(true)
                    .build();

            userRepository.save(user);
            log.info("👤 Client de test créé - Login: user / Mot de passe: user");
        }

        // Créer d'autres clients de test
        createTestUserIfNotExists("marie", "Marie", "Martin");
        createTestUserIfNotExists("ahmed", "Ahmed", "Benali");
        createTestUserIfNotExists("sarah", "Sarah", "Alami");
    }

    private void createTestUserIfNotExists(String username, String firstName, String lastName) {
        if (!userRepository.existsByUsername(username)) {
            User user = User.builder()
                    .firstName(firstName)
                    .lastName(lastName)
                    .username(username)
                    .password(passwordEncoder.encode("test123"))
                    .role(ERole.USER)
                    .creationDate(LocalDate.now())
                    .enabled(true)
                    .accountNonExpired(true)
                    .accountNonLocked(true)
                    .credentialsNonExpired(true)
                    .build();

            userRepository.save(user);
            log.info("👤 Client {} créé - Login: {} / Mot de passe: test123", firstName, username);
        }
    }

    private void initializeCreditRequests() {
        // Créer quelques demandes de crédit de test si aucune n'existe
        if (creditRequestRepository.count() == 0) {
            createTestCreditRequests();
        }
    }

    private void createTestCreditRequests() {
        Optional<User> userOpt = userRepository.findByUsername("user");
        Optional<User> marieOpt = userRepository.findByUsername("marie");
        Optional<User> ahmedOpt = userRepository.findByUsername("ahmed");
        Optional<User> sarahOpt = userRepository.findByUsername("sarah");

        if (userOpt.isPresent()) {
            CreditRequest credit1 = CreditRequest.builder()
                    .client(userOpt.get())
                    .amount(BigDecimal.valueOf(15000.00))
                    .duration(24)
                    .description("Achat d'une voiture familiale pour les trajets quotidiens")
                    .requestDate(LocalDate.now().minusDays(5))
                    .status(CreditStatus.PENDING)
                    .createdAt(LocalDate.now().minusDays(5))
                    .build();

            creditRequestRepository.save(credit1);
            log.info("💳 Demande de crédit créée pour {}: {} DH", userOpt.get().getUsername(), credit1.getAmount());
        }

        if (marieOpt.isPresent()) {
            CreditRequest credit2 = CreditRequest.builder()
                    .client(marieOpt.get())
                    .amount(BigDecimal.valueOf(50000.00))
                    .duration(60)
                    .description("Rénovation complète de ma maison principale")
                    .requestDate(LocalDate.now().minusDays(10))
                    .status(CreditStatus.APPROVED)
                    .processedDate(LocalDate.now().minusDays(2))
                    .adminComment("Dossier complet et solvabilité confirmée")
                    .createdAt(LocalDate.now().minusDays(10))
                    .build();

            creditRequestRepository.save(credit2);
            log.info("💳 Demande de crédit créée pour {}: {} DH (Approuvée)", marieOpt.get().getUsername(), credit2.getAmount());
        }

        if (ahmedOpt.isPresent()) {
            CreditRequest credit3 = CreditRequest.builder()
                    .client(ahmedOpt.get())
                    .amount(BigDecimal.valueOf(8000.00))
                    .duration(12)
                    .description("Financement d'études supérieures en informatique")
                    .requestDate(LocalDate.now().minusDays(3))
                    .status(CreditStatus.PENDING)
                    .createdAt(LocalDate.now().minusDays(3))
                    .build();

            creditRequestRepository.save(credit3);
            log.info("💳 Demande de crédit créée pour {}: {} DH", ahmedOpt.get().getUsername(), credit3.getAmount());
        }

        if (sarahOpt.isPresent()) {
            CreditRequest credit4 = CreditRequest.builder()
                    .client(sarahOpt.get())
                    .amount(BigDecimal.valueOf(25000.00))
                    .duration(36)
                    .description("Création d'une petite entreprise de e-commerce")
                    .requestDate(LocalDate.now().minusDays(7))
                    .status(CreditStatus.REJECTED)
                    .processedDate(LocalDate.now().minusDays(1))
                    .adminComment("Revenus insuffisants par rapport au montant demandé")
                    .createdAt(LocalDate.now().minusDays(7))
                    .build();

            creditRequestRepository.save(credit4);
            log.info("💳 Demande de crédit créée pour {}: {} DH (Rejetée)", sarahOpt.get().getUsername(), credit4.getAmount());
        }
    }
}