package com.rafael.securityjwt.dto;

import lombok.Getter;
import lombok.Setter;

/**
 * DTO (Data Transfer Object) para peticiones de login.
 *
 * Representa el JSON que el cliente envia en el body:
 * { "username": "admin", "password": "admin123" }
 *
 * @Getter y @Setter: Lombok genera getters y setters.
 * Spring (Jackson) usa los setters para deserializar el JSON a este objeto.
 */
@Getter
@Setter
public class LoginRequest {
    private String username;
    private String password;
}
