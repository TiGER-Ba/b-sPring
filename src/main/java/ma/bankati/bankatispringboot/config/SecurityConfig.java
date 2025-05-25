package ma.bankati.bankatispringboot.config;

import lombok.RequiredArgsConstructor;
import ma.bankati.bankatispringboot.service.UserService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

/**
 * Configuration Spring Security pour l'application Bankati
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
@RequiredArgsConstructor
public class SecurityConfig {

    private final UserService userService;

    /**
     * Configuration de la chaîne de filtres de sécurité
     */
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                // Configuration CSRF
                .csrf(csrf -> csrf.disable()) // Désactivé pour simplifier, à activer en production

                // Configuration des autorisations
                .authorizeHttpRequests(auth -> auth
                        // Ressources publiques
                        .requestMatchers("/css/**", "/js/**", "/img/**", "/images/**",
                                "/static/**", "/assets/**", "/favicon.ico").permitAll()

                        // Page de connexion accessible à tous
                        .requestMatchers("/login", "/logout").permitAll()

                        // Pages d'administration réservées aux ADMIN
                        .requestMatchers("/admin/**", "/users/**").hasRole("ADMIN")

                        // Pages client réservées aux USER
                        .requestMatchers("/client/**").hasRole("USER")

                        // Page d'accueil accessible aux utilisateurs connectés
                        .requestMatchers("/", "/home", "/convertCurrency").authenticated()

                        // Toutes les autres requêtes nécessitent une authentification
                        .anyRequest().authenticated()
                )

                // Configuration du formulaire de connexion
                .formLogin(form -> form
                        .loginPage("/login")
                        .loginProcessingUrl("/login")
                        .usernameParameter("lg")  // Correspond à votre ancien formulaire
                        .passwordParameter("pss") // Correspond à votre ancien formulaire
                        .successHandler(authenticationSuccessHandler())
                        .failureUrl("/login?error=true")
                        .permitAll()
                )

                // Configuration de la déconnexion
                .logout(logout -> logout
                        .logoutRequestMatcher(new AntPathRequestMatcher("/logout"))
                        .logoutSuccessUrl("/login?logout=true")
                        .deleteCookies("JSESSIONID")
                        .invalidateHttpSession(true)
                        .clearAuthentication(true)
                        .permitAll()
                )

                // Configuration de la gestion des sessions
                .sessionManagement(session -> session
                        .maximumSessions(1)  // Une seule session par utilisateur
                        .maxSessionsPreventsLogin(false)
                        .and()
                        .sessionFixation().migrateSession()
                )

                // Configuration pour Remember Me (optionnel)
                .rememberMe(remember -> remember
                        .key("bankati-remember-me")
                        .tokenValiditySeconds(86400) // 24 heures
                        .userDetailsService(userService)
                );

        return http.build();
    }

    /**
     * Gestionnaire de succès d'authentification personnalisé
     * Redirige selon le rôle de l'utilisateur
     */
    @Bean
    public AuthenticationSuccessHandler authenticationSuccessHandler() {
        return (request, response, authentication) -> {
            String redirectUrl = "/home"; // URL par défaut

            // Redirection basée sur le rôle
            if (authentication.getAuthorities().stream()
                    .anyMatch(grantedAuthority -> grantedAuthority.getAuthority().equals("ROLE_ADMIN"))) {
                redirectUrl = "/admin/home";
            } else if (authentication.getAuthorities().stream()
                    .anyMatch(grantedAuthority -> grantedAuthority.getAuthority().equals("ROLE_USER"))) {
                redirectUrl = "/client/home";
            }

            response.sendRedirect(request.getContextPath() + redirectUrl);
        };
    }

    /**
     * Encodeur de mot de passe BCrypt
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * Fournisseur d'authentification DAO
     */
    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userService);
        authProvider.setPasswordEncoder(passwordEncoder());
        return authProvider;
    }

    /**
     * Gestionnaire d'authentification
     */
    @Bean
    public AuthenticationManager authenticationManager(
            AuthenticationConfiguration authConfig) throws Exception {
        return authConfig.getAuthenticationManager();
    }
}