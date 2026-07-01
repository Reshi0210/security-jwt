package com.rafael.securityjwt.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * DTO para respuestas de autenticacion (login y registro).
 *
 * Se serializa a JSON y se envia al cliente:
 * { "token": "eyJhbGciOi...", "username": "admin", "role": "ADMIN" }
 *
 * El cliente debe guardar el token y enviarlo en el header Authorization
 * de las peticiones posteriores: "Authorization: Bearer eyJhbGciOi..."
 *
 * @AllArgsConstructor: Lombok genera un constructor con todos los campos.
 */
@Getter
@AllArgsConstructor
public class AuthResponse {
    /** Token JWT que el cliente debe usar para autenticarse */
    private String token;

    /** Username del usuario autenticado */
    private String username;

    /** Rol del usuario (ADMIN o USER) */
    private String role;
}
