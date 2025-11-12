package com.rlp.cosechaencope.security;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import com.rlp.cosechaencope.service.UserDetailsServiceImpl;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.UnsupportedJwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;

/**
 * Filtro de autenticación JWT que valida tokens en peticiones HTTP.
 *
 * Este filtro se ejecuta una vez por petición y realiza:
 * <ul>
 * <li>Exclusión de recursos estáticos para optimización</li>
 * <li>Extracción y validación del JWT desde el header Authorization</li>
 * <li>Carga de detalles del usuario autenticado</li>
 * <li>Establecimiento del contexto de seguridad de Spring</li>
 * </ul>
 *
 * @author rafalopezzz
 * @version 2.0
 */
@Slf4j
@Component
public class AuthTokenFilter extends OncePerRequestFilter {

    @Autowired
    private JwtUtils jwtUtils;

    @Autowired
    private UserDetailsServiceImpl userDetailsService;

    /**
     * CRÍTICO: Determina si el filtro debe saltarse para esta petición. Esto
     * mejora el rendimiento evitando procesamiento innecesario en archivos
     * estáticos.
     *
     * ACTUALIZADO: Incluye patrones de archivos compilados de Angular (chunk-*,
     * main-*, etc.)
     */
    @Override
    protected boolean shouldNotFilter(@NonNull HttpServletRequest request) throws ServletException {
        String path = request.getRequestURI();

        // 1. Archivos estáticos por extensión (MÁXIMA PRIORIDAD)
        if (path.matches(".*\\.(js|css|svg|png|jpg|jpeg|gif|ico|woff|woff2|ttf|eot|map|json|webp|txt|html)$")) {
            return true;
        }

        // 2. Angular chunks con hash hexadecimal (main.abc123.js, styles.def456.css)
        // Patrón: /app/nombre.hash.extensión donde hash es hexadecimal
        if (path.matches(".*/app/[a-zA-Z0-9]+\\.[a-f0-9]{10,}\\.(js|css)$")) {
            return true;
        }

        // 3. Angular index.html explícitamente
        if (path.equals("/app/") || path.equals("/app/index.html")) {
            return true;
        }

        // 4. Recursos públicos por prefijo
        return path.startsWith("/css/")
                || path.startsWith("/js/")
                || path.startsWith("/images/")
                || path.startsWith("/gal/")
                || path.startsWith("/iconos/")
                || path.startsWith("/fonts/")
                || path.startsWith("/webjars/")
                || path.startsWith("/.well-known/")
                || path.equals("/favicon.ico")
                || path.equals("/favicon.svg")
                || // AÑADIR
                path.equals("/manifest.json")
                || path.equals("/error")
                || path.startsWith("/actuator/")
                || path.startsWith("/v3/api-docs")
                || path.startsWith("/swagger-ui");
    }

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain)
            throws ServletException, IOException {

        String path = request.getRequestURI();
        String method = request.getMethod();

        // Log de nivel TRACE para evitar ruido en logs de producción
        if (log.isTraceEnabled()) {
            log.trace("Procesando petición: {} {}", method, path);
        }

        try {
            String jwt = parseJwt(request);

            if (jwt != null) {
                // Validar token
                if (jwtUtils.validateJwtToken(jwt)) {
                    String username = jwtUtils.getUserNameFromJwtToken(jwt);

                    // Log de nivel DEBUG solo para autenticaciones exitosas
                    log.debug("Autenticando usuario desde JWT: {}", username);

                    // Cargar detalles del usuario
                    UserDetails userDetails = userDetailsService.loadUserByUsername(username);

                    // Crear objeto de autenticación
                    UsernamePasswordAuthenticationToken authentication
                            = new UsernamePasswordAuthenticationToken(
                                    userDetails,
                                    null,
                                    userDetails.getAuthorities()
                            );

                    authentication.setDetails(
                            new WebAuthenticationDetailsSource().buildDetails(request)
                    );

                    // Establecer en contexto de seguridad
                    SecurityContextHolder.getContext().setAuthentication(authentication);

                    // Log de éxito solo en DEBUG
                    if (log.isDebugEnabled()) {
                        log.debug("Usuario {} autenticado con roles: {}",
                                username, userDetails.getAuthorities());
                    }
                } else {
                    // Token presente pero inválido - ALERTA DE SEGURIDAD
                    log.warn("Intento de acceso con JWT inválido desde IP: {} a ruta: {}",
                            getClientIP(request), path);
                }
            } else {
                // Sin token - petición anónima (normal para rutas públicas)
                if (log.isTraceEnabled()) {
                    log.trace("Petición sin JWT a: {}", path);
                }
            }

        } catch (ExpiredJwtException e) {
            // Token expirado - INFO nivel, no ERROR
            log.info("Token JWT expirado desde IP: {} - Usuario: {}",
                    getClientIP(request), e.getClaims().getSubject());
            SecurityContextHolder.clearContext();

        } catch (MalformedJwtException e) {
            // Token malformado - ALERTA DE SEGURIDAD
            log.warn("JWT malformado desde IP: {} a ruta: {} - Posible ataque",
                    getClientIP(request), path);
            SecurityContextHolder.clearContext();

        } catch (UnsupportedJwtException | IllegalArgumentException e) {
            // Token no soportado o inválido
            log.warn("Problema con JWT ({}) desde IP: {} - Mensaje: {}",
                    e.getClass().getSimpleName(), getClientIP(request), e.getMessage());
            SecurityContextHolder.clearContext();

        } catch (Exception ex) {
            // Error inesperado - nivel ERROR para investigación
            log.error("Error crítico procesando JWT desde IP: {} a ruta: {} - Error: {}",
                    getClientIP(request), path, ex.getMessage(), ex);
            SecurityContextHolder.clearContext();
        }

        // Continuar con la cadena de filtros
        filterChain.doFilter(request, response);
    }

    /**
     * Extrae el token JWT del header Authorization.
     *
     * @param request La petición HTTP
     * @return El token JWT sin el prefijo "Bearer ", o null si no existe
     */
    private String parseJwt(HttpServletRequest request) {
        String headerAuth = request.getHeader("Authorization");

        if (StringUtils.hasText(headerAuth) && headerAuth.startsWith("Bearer ")) {
            return headerAuth.substring(7).trim();
        }

        return null;
    }

    /**
     * Obtiene la IP real del cliente, considerando proxies y balanceadores de
     * carga.
     *
     * @param request La petición HTTP
     * @return La dirección IP del cliente
     */
    private String getClientIP(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");

        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("X-Real-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }

        // Si hay múltiples IPs (proxy chain), tomar la primera
        if (ip != null && ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }

        return ip;
    }
}
