package com.rafael.securityjwt.config;

import com.rafael.securityjwt.entity.User;
import com.rafael.securityjwt.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * Inicializador de datos que se ejecuta al arrancar la aplicacion.
 *
 * Implementa CommandLineRunner: Spring ejecuta automaticamente el metodo run()
 * despues de que el contexto de la aplicacion se haya inicializado completamente.
 *
 * Esto es util para:
 * - Crear datos iniciales (seed data)
 * - Crear un usuario administrador por defecto
 * - Cargar configuracion inicial
 *
 * @Slf4j: Lombok genera automaticamente un logger (log.info, log.error, etc.)
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    /**
     * Se ejecuta automaticamente al iniciar la aplicacion.
     *
     * Crea el usuario admin si no existe. El password se hashea con BCrypt
     * antes de guardarse (NUNCA se guarda en texto plano).
     *
     * Como usamos H2 en memoria con ddl-auto=create-drop, la BD se reinicia
     * cada vez que se levanta la app, asi que el admin se crea cada vez.
     */
    @Override
    public void run(String... args) {
        if (!userRepository.existsByUsername("admin")) {
            var admin = User.builder()
                    .username("admin")
                    .password(passwordEncoder.encode("admin123"))
                    .role("ADMIN")
                    .build();
            userRepository.save(admin);
            log.info("Usuario admin creado: admin / admin123");
        }
    }
}
