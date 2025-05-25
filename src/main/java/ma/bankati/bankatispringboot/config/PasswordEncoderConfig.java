package ma.bankati.bankatispringboot.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * Configuration séparée pour le PasswordEncoder
 * Cela évite les dépendances circulaires avec SecurityConfig
 */
@Configuration
public class PasswordEncoderConfig {

    /**
     * Bean PasswordEncoder utilisant BCrypt
     * Séparé de SecurityConfig pour éviter les dépendances circulaires
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}