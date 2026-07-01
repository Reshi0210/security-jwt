package com.rafael.securityjwt.dto;

import lombok.Data;

/**
 * DTO (Data Transfer Object) para peticiones de login.
 *
 * Representa el JSON que el cliente envia en el body:
 * { "username": "admin", "password": "admin123" }
 *
 * @Data de Lombok genera getters, setters y toString automaticamente.
 * Spring (Jackson) usa los setters para deserializar el JSON a este objeto.
 */
@Data
public class LoginRequest {
    private String username;
    private String password;
}
