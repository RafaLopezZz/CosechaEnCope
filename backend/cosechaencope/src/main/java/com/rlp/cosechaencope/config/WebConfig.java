package com.rlp.cosechaencope.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.lang.NonNull;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Configuración web para manejar recursos estáticos y rutas híbridas Permite
 * servir tanto Thymeleaf templates como Angular SPA
 *
 * @author rafalopezzz
 * @since 1.0.0
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    /**
     * Configurar manejadores de recursos estáticos
     */
    @Override
    public void addResourceHandlers(@NonNull ResourceHandlerRegistry registry) {

        // Recursos estáticos para Thymeleaf (landing pages)
        registry.addResourceHandler("/css/**")
                .addResourceLocations("classpath:/static/thymeleaf/css/")
                .setCachePeriod(3600); // Cache por 1 hora

        registry.addResourceHandler("/js/**")
                .addResourceLocations("classpath:/static/thymeleaf/js/")
                .setCachePeriod(3600);

        registry.addResourceHandler("/images/**")
                .addResourceLocations("classpath:/static/thymeleaf/images/")
                .setCachePeriod(86400); // Cache por 24 horas para imágenes

        registry.addResourceHandler("/fonts/**")
                .addResourceLocations("classpath:/static/thymeleaf/fonts/")
                .setCachePeriod(604800); // Cache por 1 semana para fuentes

        // Recursos para Angular SPA
        // CRÍTICO: Angular 20 compila a static/app/ directamente, NO a static/app/browser/
        String angularLocation = "classpath:/static/app/";

        // Archivos de Angular (JS, CSS, etc.)
        registry.addResourceHandler(
                "/app/*.js",
                "/app/*.css",
                "/app/*.ico",
                "/app/*.svg",
                "/app/*.webp",
                "/app/*.png",
                "/app/*.jpg",
                "/app/*.jpeg",
                "/app/*.gif",
                "/app/*.woff",
                "/app/*.woff2",
                "/app/*.ttf",
                "/app/*.eot",
                "/app/*.map",
                "/app/*.txt",
                "/app/chunk-*.js",
                "/app/main-*.js",
                "/app/polyfills-*.js",
                "/app/scripts-*.js",
                "/app/styles-*.css")
                .addResourceLocations(angularLocation)
                .setCachePeriod(3600);

        // Assets de Angular (separado para path explícito)
        registry.addResourceHandler("/app/assets/**", "/app/media/**", "/app/images/**", "/app/gal/**", "/app/iconos/**")
                .addResourceLocations(angularLocation + "assets/", angularLocation + "media/", angularLocation + "images/", angularLocation + "gal/", angularLocation + "iconos/")
                .setCachePeriod(3600);

        // Favicon y otros recursos raíz de Angular
        registry.addResourceHandler("/favicon.ico")
                .addResourceLocations(angularLocation) // Usa la misma location de Angular
                .setCachePeriod(86400);

        registry.addResourceHandler("/manifest.json")
                .addResourceLocations(angularLocation) // Usa la misma location de Angular
                .setCachePeriod(86400);
    }

    /**
     * Configurar controladores de vista para redirecciones y SPAs
     */
    @Override
    public void addViewControllers(@NonNull ViewControllerRegistry registry) {
        // Redirecciones de rutas antiguas de Angular a nueva estructura /app
        registry.addRedirectViewController("/login", "/app/auth");
        registry.addRedirectViewController("/registro", "/app/auth");
        registry.addRedirectViewController("/articulos", "/app/articulos");
        registry.addRedirectViewController("/categorias/{categoria}", "/app/categorias/{categoria}");
    }
}
