package com.rafael.securityjwt.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Entidad JPA que representa un usuario en la base de datos.
 *
 * Anotaciones de Lombok:
 * - @Data: genera getters, setters, toString, equals y hashCode automaticamente.
 * - @Builder: permite crear instancias con el patron Builder (User.builder().username("x").build()).
 * - @NoArgsConstructor / @AllArgsConstructor: constructores vacio y con todos los campos (requeridos por JPA y Builder).
 *
 * Anotaciones de JPA:
 * - @Entity: marca esta clase como una tabla en la BD.
 * - @Table(name = "users"): nombre de la tabla. Usamos "users" porque "user" es palabra reservada en H2.
 */
@Entity
@Table(name = "users")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class User {

    /** Clave primaria auto-generada por la base de datos */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Nombre de usuario unico - se usa como identificador para login */
    @Column(nullable = false, unique = true)
    private String username;

    /** Password hasheado con BCrypt - NUNCA se almacena en texto plano */
    @Column(nullable = false)
    private String password;

    /** Rol del usuario (ADMIN o USER). Se usa para control de acceso a endpoints */
    @Column(nullable = false)
    private String role;
}
