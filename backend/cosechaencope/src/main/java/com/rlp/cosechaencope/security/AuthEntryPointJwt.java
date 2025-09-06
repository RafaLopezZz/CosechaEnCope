package com.rlp.cosechaencope.security;

import java.io.IOException;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;

/**
 * Punto de entrada para manejar accesos no autorizados en la aplicación.
 *
 * Responde con JSON consistente para que el frontend (Angular, fetch, etc.)
 * pueda manejar el error de forma programática.
 */
@Slf4j
@Component
public class AuthEntryPointJwt implements AuthenticationEntryPoint {

    private static final Logger logs = LoggerFactory.getLogger(AuthEntryPointJwt.class);

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void commence(HttpServletRequest request,
            HttpServletResponse response,
            AuthenticationException authException) throws IOException, ServletException {

        // Logueamos el evento como WARN: útil para saber qué endpoints están
        // generando accesos no autorizados sin ensuciar con stacktraces.
        logs.warn("Acceso no autorizado a '{}': {}", request.getRequestURI(),
                authException == null ? "null" : authException.getMessage());

        // Construimos un body JSON sencillo y seguro
        Map<String, Object> body = new HashMap<>();
        body.put("timestamp", Instant.now().toString());
        body.put("status", HttpServletResponse.SC_UNAUTHORIZED);
        body.put("error", "Unauthorized");
        // Mensaje genérico para no filtrar detalles sensibles; podemos añadir mensaje más descriptivo si procede
        body.put("message", "Error: No autorizado");
        body.put("path", request.getRequestURI());

        // Configuramos la respuesta
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json;charset=UTF-8");

        // Si necesitas añadir cabeceras CORS específicas para que el cliente reciba la respuesta
        // (recomendado: configurar CORS globalmente en SecurityConfig / CorsConfigurationSource)
        // response.setHeader("Access-Control-Allow-Origin", "*");
        // response.setHeader("Access-Control-Allow-Credentials", "true");
        // Escribir el JSON en la respuesta
        objectMapper.writeValue(response.getWriter(), body);
    }
}
