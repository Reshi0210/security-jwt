package com.rafael.securityjwt.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * Filtro de autenticacion JWT que se ejecuta en CADA peticion HTTP.
 *
 * Este es el corazon de la seguridad JWT. Su trabajo:
 * 1. Interceptar cada request HTTP antes de que llegue al controller
 * 2. Buscar el header "Authorization: Bearer <token>"
 * 3. Si hay token, validarlo y establecer la autenticacion en el SecurityContext
 * 4. Si no hay token, dejar pasar la peticion (Spring Security decidira si la ruta es publica o no)
 *
 * Extiende OncePerRequestFilter para garantizar que se ejecute exactamente UNA vez por request
 * (algunos filtros podrian ejecutarse multiples veces en forwards/redirects internos).
 */
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final UserDetailsService userDetailsService;

    /**
     * Metodo principal del filtro. Se ejecuta automaticamente en cada peticion HTTP.
     *
     * Flujo:
     * 1. Obtener header "Authorization" del request
     * 2. Si no existe o no empieza con "Bearer ", continuar sin autenticar
     * 3. Extraer el token (quitar "Bearer " del inicio)
     * 4. Validar el token (firma + expiracion)
     * 5. Si es valido, cargar el usuario de la BD y establecer la autenticacion
     * 6. Continuar con la cadena de filtros (filterChain.doFilter)
     *
     * @param request  la peticion HTTP entrante
     * @param response la respuesta HTTP
     * @param filterChain la cadena de filtros de Spring (debemos llamar doFilter para continuar)
     */
    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        // Paso 1: Obtener el header Authorization
        String authHeader = request.getHeader("Authorization");

        // Paso 2: Si no hay header o no es Bearer token, seguir sin autenticar
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        // Paso 3: Extraer el token (quitar "Bearer " = 7 caracteres)
        String token = authHeader.substring(7);

        // Paso 4: Validar el token
        if (jwtUtil.isTokenValid(token)) {
            String username = jwtUtil.extractUsername(token);

            // Paso 5: Solo autenticar si aun no hay autenticacion en el contexto
            // (evita procesar dos veces si hay forwarding interno)
            if (SecurityContextHolder.getContext().getAuthentication() == null) {
                // Cargar usuario completo desde la BD (con sus roles/authorities)
                var userDetails = userDetailsService.loadUserByUsername(username);

                // Crear token de autenticacion de Spring Security (no confundir con JWT)
                // Este objeto le dice a Spring: "este usuario esta autenticado y tiene estos roles"
                var authToken = new UsernamePasswordAuthenticationToken(
                        userDetails, null, userDetails.getAuthorities());
                authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                // Establecer la autenticacion en el SecurityContext
                // A partir de aqui, cualquier parte del codigo puede saber quien esta autenticado
                SecurityContextHolder.getContext().setAuthentication(authToken);
            }
        }

        // Paso 6: SIEMPRE continuar con la cadena de filtros
        // (Spring Security decidira si el usuario tiene permiso para la ruta)
        filterChain.doFilter(request, response);
    }
}
