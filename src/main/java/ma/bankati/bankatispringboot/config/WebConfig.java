package ma.bankati.bankatispringboot.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Configuration Web MVC
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    /**
     * Configuration des ressources statiques
     */
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // Configuration pour les ressources statiques
        registry.addResourceHandler("/assets/**")
                .addResourceLocations("classpath:/static/assets/")
                .setCachePeriod(31536000); // 1 an en production

        registry.addResourceHandler("/css/**")
                .addResourceLocations("classpath:/static/css/");

        registry.addResourceHandler("/js/**")
                .addResourceLocations("classpath:/static/js/");

        registry.addResourceHandler("/img/**", "/images/**")
                .addResourceLocations("classpath:/static/img/");
    }

    /**
     * Configuration des contrôleurs de vue simples
     */
    @Override
    public void addViewControllers(ViewControllerRegistry registry) {
        // Redirection de la racine vers /home
        registry.addRedirectViewController("/", "/home");

        // Vue de connexion
        registry.addViewController("/login").setViewName("auth/login");
    }
}