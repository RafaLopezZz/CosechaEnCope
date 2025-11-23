package com.rlp.cosechaencope.controller;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.http.CacheControl;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;

/**
 * Controlador para manejar las rutas de la Single Page Application (SPA) de
 * Angular.
 *
 * Este controlador sirve el index.html de Angular para todas las rutas bajo
 * /app/** que no correspondan a archivos estáticos, permitiendo que el router
 * de Angular maneje la navegación del lado del cliente.
 *
 * @author rafalopezzz
 * @since 1.0.0
 */
@Slf4j
@Controller
@RequestMapping("/app")
public class AngularSpaController {

    private static final String ANGULAR_INDEX_PATH = "static/app/index.html";

    /**
     * Maneja todas las rutas de Angular SPA que no corresponden a archivos
     * estáticos. Sirve el index.html de Angular para permitir el routing del
     * lado del cliente.
     *
     * @param request La petición HTTP actual
     * @return ResponseEntity con el contenido del index.html de Angular
     */
    @GetMapping(value = {
        "",
        "/",
        "/auth",
        "/auth/**",
        "/login",
        "/login/**",
        "/registro",
        "/registro/**",
        "/articulos",
        "/articulos/**",
        "/categorias",
        "/categorias/**",
        "/productor",
        "/productor/**",
        "/cliente",
        "/cliente/**",
        "/pedidos",
        "/pedidos/**",
        "/carrito",
        "/carrito/**",
        "/checkout",
        "/checkout/**",
        "/error"
    })
    public ResponseEntity<String> handleAngularRoutes(HttpServletRequest request) {
        String path = request.getRequestURI();

        // Redirección real para /app -> /app/ para evitar problemas con base href
        if (path.endsWith("/app")) {
            return ResponseEntity.status(org.springframework.http.HttpStatus.FOUND)
                    .header("Location", path + "/")
                    .build();
        }

        log.debug("[SPA] Sirviendo index.html para ruta: {}", path);
        try {
            Resource resource = new ClassPathResource(ANGULAR_INDEX_PATH);
            if (!resource.exists()) {
                log.error("[SPA] No se encontró index.html en {}", ANGULAR_INDEX_PATH);
                return ResponseEntity.notFound().build();
            }
            String content = resource.getContentAsString(StandardCharsets.UTF_8);
            return ResponseEntity.ok()
                    .cacheControl(CacheControl.noCache())
                    .contentType(MediaType.TEXT_HTML)
                    .body(content);
        } catch (IOException e) {
            log.error("[SPA] Error leyendo index.html: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }
}
