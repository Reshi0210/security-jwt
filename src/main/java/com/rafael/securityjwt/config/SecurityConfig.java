package com.rafael.securityjwt.config;

import com.rafael.securityjwt.security.JwtAuthenticationFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * Configuracion central de Spring Security.
 *
 * Aqui se define:
 * - Que rutas son publicas y cuales requieren autenticacion
 * - Que la app es STATELESS (sin sesiones HTTP, solo JWT)
 * - Donde se inserta nuestro filtro JWT en la cadena de seguridad
 * - El PasswordEncoder para hashear passwords
 * - El AuthenticationManager para el proceso de login
 *
 * @Configuration: indica que esta clase contiene @Bean (definiciones de componentes Spring).
 * @EnableWebSecurity: activa la configuracion de seguridad web de Spring Security.
 */
@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    /**
     * Define la cadena de filtros de seguridad (SecurityFilterChain).
     *
     * Cada peticion HTTP pasa por esta cadena antes de llegar al controller.
     * Aqui configuramos las reglas de seguridad de la aplicacion.
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // Desactivar CSRF porque usamos JWT (stateless), no cookies de sesion.
                // CSRF protege contra ataques que explotan cookies, pero con JWT no aplica.
                .csrf(csrf -> csrf.disable())

                // Politica de sesiones STATELESS: Spring Security NO crea sesiones HTTP.
                // Cada peticion debe traer su propio JWT. Esto es fundamental para APIs REST.
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                // Reglas de autorizacion por ruta:
                .authorizeHttpRequests(auth -> auth
                        // Rutas publicas: login, registro y consola H2 no requieren token
                        .requestMatchers("/auth/**", "/h2-console/**").permitAll()
                        // Solo usuarios con rol ADMIN pueden acceder a /admin/**
                        .requestMatchers("/admin/**").hasRole("ADMIN")
                        // Todo lo demas requiere estar autenticado (tener un JWT valido)
                        .anyRequest().authenticated()
                )

                // Desactivar frameOptions para que funcione la consola H2 (usa iframes)
                .headers(headers -> headers.frameOptions(frame -> frame.disable()))

                // Insertar nuestro filtro JWT ANTES del filtro de usuario/password de Spring.
                // Asi, cuando llega una peticion con JWT, ya estara autenticada antes de que
                // Spring intente pedir usuario/password.
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    /**
     * Bean de PasswordEncoder usando BCrypt.
     *
     * BCrypt es el algoritmo recomendado para hashear passwords porque:
     * - Incluye un salt aleatorio automatico (cada hash es diferente)
     * - Es intencionalmente lento (dificulta ataques de fuerza bruta)
     * - El factor de trabajo se puede ajustar
     *
     * Se usa tanto al registrar (para hashear) como al hacer login (para comparar).
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * Bean de AuthenticationManager.
     *
     * Este componente coordina todo el proceso de autenticacion.
     * Internamente usa nuestro UserDetailsService para cargar el usuario
     * y el PasswordEncoder para comparar el password.
     *
     * Lo inyectamos en AuthController para usarlo en el endpoint de login.
     */
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }
}
