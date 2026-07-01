package com.rafael.securityjwt.controller;

import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * Controller con endpoints protegidos de ejemplo.
 *
 * Estos endpoints requieren un JWT valido en el header Authorization.
 * Spring inyecta automaticamente el objeto Authentication con la info
 * del usuario autenticado (extraida del token por JwtAuthenticationFilter).
 */
@RestController
public class HelloController {

    /**
     * GET /hello - Accesible por cualquier usuario autenticado (USER o ADMIN).
     *
     * El parametro Authentication es inyectado por Spring Security.
     * Contiene el username y los roles del usuario que hizo la peticion.
     * Esta informacion fue establecida por JwtAuthenticationFilter al validar el token.
     */
    @GetMapping("/hello")
    public Map<String, String> hello(Authentication authentication) {
        return Map.of(
                "message", "Hola " + authentication.getName() + "!",
                "role", authentication.getAuthorities().toString()
        );
    }

    /**
     * GET /admin/dashboard - Solo accesible por usuarios con rol ADMIN.
     *
     * En SecurityConfig configuramos: requestMatchers("/admin/**").hasRole("ADMIN")
     * Si un usuario con rol USER intenta acceder, recibe 403 Forbidden.
     */
    @GetMapping("/admin/dashboard")
    public Map<String, String> adminDashboard(Authentication authentication) {
        return Map.of(
                "message", "Panel de administracion",
                "admin", authentication.getName()
        );
    }
}
