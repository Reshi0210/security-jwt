package com.rafael.securityjwt.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

/**
 * Utilidad para generar y validar tokens JWT.
 *
 * Un JWT (JSON Web Token) es un string firmado digitalmente que contiene
 * informacion del usuario. El servidor lo genera al hacer login y el cliente
 * lo envia en cada peticion posterior en el header "Authorization".
 *
 * La clave secreta (jwt.secret) se usa para firmar y verificar los tokens.
 * Si alguien modifica el token, la firma no coincidira y sera rechazado.
 *
 * @Component: hace que Spring cree una unica instancia y la inyecte donde se necesite.
 */
@Component
public class JwtUtil {

    /** Clave secreta para firmar los tokens. Se genera a partir del string en application.properties */
    private final SecretKey key;

    /** Tiempo de expiracion del token en milisegundos (por defecto 24h = 86400000ms) */
    private final long expiration;

    /**
     * Constructor que recibe la configuracion desde application.properties.
     * @Value inyecta el valor de la propiedad indicada.
     * Keys.hmacShaKeyFor() convierte el string en una clave criptografica HMAC-SHA.
     */
    public JwtUtil(@Value("${jwt.secret}") String secret,
                   @Value("${jwt.expiration}") long expiration) {
        this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.expiration = expiration;
    }

    /**
     * Genera un nuevo token JWT para un usuario autenticado.
     *
     * El token contiene:
     * - subject: el username (identificador principal)
     * - claim "role": el rol del usuario
     * - issuedAt: fecha de emision
     * - expiration: fecha de expiracion (momento actual + duracion configurada)
     * - signWith: firma digital con nuestra clave secreta
     *
     * @param username nombre del usuario
     * @param role rol del usuario (ADMIN, USER, etc.)
     * @return token JWT como string (ej: "eyJhbGciOi...")
     */
    public String generateToken(String username, String role) {
        return Jwts.builder()
                .subject(username)
                .claim("role", role)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + expiration))
                .signWith(key)
                .compact();
    }

    /**
     * Extrae el username (subject) del token JWT.
     * Se usa en el filtro para saber que usuario esta haciendo la peticion.
     */
    public String extractUsername(String token) {
        return extractClaims(token).getSubject();
    }

    /**
     * Verifica si un token es valido.
     * Un token es invalido si: la firma no coincide, esta expirado, o esta malformado.
     * El metodo parseSignedClaims() lanza una excepcion en cualquiera de esos casos.
     */
    public boolean isTokenValid(String token) {
        try {
            extractClaims(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Parsea el token JWT y extrae los Claims (datos del payload).
     * Este metodo verifica la firma automaticamente. Si el token fue alterado,
     * lanza SignatureException. Si expiro, lanza ExpiredJwtException.
     */
    private Claims extractClaims(String token) {
        return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}
