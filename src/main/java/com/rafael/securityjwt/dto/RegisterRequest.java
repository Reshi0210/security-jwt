package com.rafael.securityjwt.dto;

import lombok.Getter;
import lombok.Setter;

/**
 * DTO para peticiones de registro de nuevos usuarios.
 *
 * Representa el JSON: { "username": "nuevoUsuario", "password": "miPassword" }
 * El rol no se envia porque siempre se asigna USER por defecto (por seguridad).
 */
@Getter
@Setter
public class RegisterRequest {
    private String username;
    private String password;
}
