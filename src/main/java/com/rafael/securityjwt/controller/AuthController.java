package com.rafael.securityjwt.controller;

import com.rafael.securityjwt.dto.AuthResponse;
import com.rafael.securityjwt.dto.LoginRequest;
import com.rafael.securityjwt.dto.RegisterRequest;
import com.rafael.securityjwt.entity.User;
import com.rafael.securityjwt.repository.UserRepository;
import com.rafael.securityjwt.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * Controller de autenticacion. Maneja login y registro de usuarios.
 *
 * Estos endpoints son PUBLICOS (configurado en SecurityConfig con permitAll()).
 * No requieren JWT porque son el punto de entrada para obtener uno.
 *
 * @RestController: combina @Controller + @ResponseBody.
 *   Todas las respuestas se serializan automaticamente a JSON.
 * @RequestMapping("/auth"): prefijo comun para todos los endpoints de esta clase.
 */
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    /** Coordina la autenticacion: valida username/password contra la BD */
    private final AuthenticationManager authenticationManager;

    /** Acceso a la tabla de usuarios */
    private final UserRepository userRepository;

    /** Utilidad para generar tokens JWT */
    private final JwtUtil jwtUtil;

    /** Encoder para hashear passwords con BCrypt */
    private final PasswordEncoder passwordEncoder;

    /**
     * Endpoint de login: POST /auth/login
     *
     * Flujo:
     * 1. Recibe username y password en el body (JSON)
     * 2. AuthenticationManager valida las credenciales contra la BD
     *    - Internamente usa UserDetailsServiceImpl para cargar el usuario
     *    - Usa BCryptPasswordEncoder para comparar el password
     *    - Si las credenciales son incorrectas, lanza BadCredentialsException (401)
     * 3. Si la autenticacion es exitosa, genera un token JWT
     * 4. Retorna el token junto con la info del usuario
     *
     * @param request DTO con username y password
     * @return AuthResponse con token, username y role
     */
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {
        // Paso 1: Validar credenciales. Si falla, Spring lanza 401 automaticamente.
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword())
        );

        // Paso 2: Cargar el usuario de la BD para obtener su rol
        var user = userRepository.findByUsername(request.getUsername()).orElseThrow();

        // Paso 3: Generar token JWT
        String token = jwtUtil.generateToken(user.getUsername(), user.getRole());

        // Paso 4: Retornar respuesta con el token
        return ResponseEntity.ok(new AuthResponse(token, user.getUsername(), user.getRole()));
    }

    /**
     * Endpoint de registro: POST /auth/register
     *
     * Flujo:
     * 1. Verifica que el username no exista
     * 2. Hashea el password con BCrypt
     * 3. Guarda el nuevo usuario con rol USER
     * 4. Genera un token JWT para que pueda usarse inmediatamente
     *
     * @param request DTO con username y password del nuevo usuario
     * @return AuthResponse con token, o error si el usuario ya existe
     */
    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody RegisterRequest request) {
        // Verificar que no exista otro usuario con el mismo username
        if (userRepository.existsByUsername(request.getUsername())) {
            return ResponseEntity.badRequest().body(Map.of("error", "El usuario ya existe"));
        }

        // Crear el usuario con password hasheado y rol USER por defecto
        var user = User.builder()
                .username(request.getUsername())
                .password(passwordEncoder.encode(request.getPassword()))
                .role("USER")
                .build();

        userRepository.save(user);

        // Generar token para que el usuario pueda autenticarse inmediatamente
        String token = jwtUtil.generateToken(user.getUsername(), user.getRole());
        return ResponseEntity.ok(new AuthResponse(token, user.getUsername(), user.getRole()));
    }
}
