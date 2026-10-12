package com.reservas.autenticacion.service;

import com.nimbusds.jose.jwk.source.ImmutableSecret;
import com.reservas.compartido.CuentaResumen;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;

/**
 * Genera el token JWT del login (acuerdo 4: Authorization: Bearer {token}).
 * Duración larga (8 h): los 15 minutos de inactividad de HU-03 los controla el frontend.
 */
@Service
public class TokenService {

    private final JwtEncoder encoder;
    private final Duration duracion;

    public TokenService(@Value("${jwt.secreto}") String secreto,
                        @Value("${jwt.horas:8}") long horas) {
        SecretKey clave = new SecretKeySpec(Base64.getDecoder().decode(secreto), "HmacSHA256");
        this.encoder = new NimbusJwtEncoder(new ImmutableSecret<>(clave));
        this.duracion = Duration.ofHours(horas);
    }

    public String generar(CuentaResumen cuenta) {
        Instant ahora = Instant.now();
        JwtClaimsSet datos = JwtClaimsSet.builder()
                .issuer("agendaya")
                .subject(cuenta.id())                 // UUID del cliente o NIT del proveedor
                .claim("rol", cuenta.rol())           // "CLIENTE" o "PROVEEDOR" (lo usa A3)
                .claim("correo", cuenta.correo())
                .issuedAt(ahora)
                .expiresAt(ahora.plus(duracion))
                .build();
        JwsHeader cabecera = JwsHeader.with(MacAlgorithm.HS256).build();
        return encoder.encode(JwtEncoderParameters.from(cabecera, datos)).getTokenValue();
    }
}