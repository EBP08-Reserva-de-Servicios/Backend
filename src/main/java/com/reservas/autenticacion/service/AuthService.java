package com.reservas.autenticacion.service;

import com.reservas.compartido.CuentaAutenticable;
import com.reservas.compartido.CuentaResumen;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Login (HU-02) y bloqueo (HU-Seg-02).
 * Solo conoce la interfaz CuentaAutenticable: nunca importa ClienteService ni ProveedorService.
 */
@Service
public class AuthService {

    private final Map<String, CuentaAutenticable> cuentasPorRol;

    public AuthService(List<CuentaAutenticable> cuentas) {
        this.cuentasPorRol = cuentas.stream()
                .collect(Collectors.toMap(CuentaAutenticable::rol, Function.identity()));
    }

    public ResultadoLogin login(String correo, String clave, String rol) {

        // 1) Obligatorios ANTES de todo: passwordEncoder.matches(null, ...) lanzaría excepción (500)
        List<String> errores = new ArrayList<>();
        if (vacio(correo)) errores.add("El correo es obligatorio.");
        if (vacio(clave))  errores.add("La contraseña es obligatoria.");
        if (vacio(rol))    errores.add("El rol es obligatorio.");
        if (!errores.isEmpty()) return ResultadoLogin.incompleto(errores);

        // 2) Rol desconocido o correo que no existe en ESE rol: mismo error genérico (HU-02)
        CuentaAutenticable cuenta = cuentasPorRol.get(rol);
        if (cuenta == null) return ResultadoLogin.credencialesInvalidas();

        Optional<CuentaResumen> posible = cuenta.buscarPorCorreo(correo);
        if (posible.isEmpty()) return ResultadoLogin.credencialesInvalidas();

        // 3) Bloqueada: se responde sin validar la clave
        Optional<LocalDateTime> hasta = cuenta.bloqueadaHasta(correo);
        if (hasta.isPresent()) return ResultadoLogin.bloqueada(minutosHasta(hasta.get()));

        // 4) Validar la clave
        if (cuenta.validarCredenciales(correo, clave)) {
            cuenta.registrarIntentoExitoso(correo);
            return ResultadoLogin.exitoso(posible.get());
        }
        cuenta.registrarIntentoFallido(correo);
        return ResultadoLogin.credencialesInvalidas();
    }

    /** Minutos que faltan, redondeados hacia arriba (59 s → 1 minuto). Nunca menos de 1. */
    private static long minutosHasta(LocalDateTime hasta) {
        long segundos = Duration.between(LocalDateTime.now(), hasta).getSeconds();
        return Math.max(1, (segundos + 59) / 60);
    }

    private static boolean vacio(String valor) {
        return valor == null || valor.isBlank();
    }
}