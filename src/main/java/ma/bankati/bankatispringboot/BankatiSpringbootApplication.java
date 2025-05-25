package ma.bankati.bankatispringboot;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/**
 * Classe principale de l'application Bankati Spring Boot
 */
@SpringBootApplication
@EnableJpaRepositories
@EnableTransactionManagement
public class BankatiSpringbootApplication {

    public static void main(String[] args) {
        SpringApplication.run(BankatiSpringbootApplication.class, args);
        System.out.println("🏦 Application Bankati démarrée avec succès!");
        System.out.println("📍 URL: http://localhost:8080/bankati");
    }
}