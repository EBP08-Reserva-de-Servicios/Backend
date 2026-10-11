package com.reservas.clientes.service;

import com.reservas.clientes.api.ClienteResumen;
import com.reservas.clientes.api.ConsultaClientes;
import com.reservas.clientes.model.Cliente;
import com.reservas.clientes.repository.ClienteRepository;
import com.reservas.compartido.CuentaAutenticable;
import com.reservas.compartido.CuentaResumen;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class ClienteService implements ConsultaClientes, CuentaAutenticable {

    private static final int MAX_INTENTOS = 3;
    private static final int MINUTOS_BLOQUEO = 15;

    private static final String REGEX_CORREO = "^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$";
    private static final String REGEX_CELULAR = "^3\\d{9}$";

    private final ClienteRepository clienteRepository;
    private final PasswordEncoder passwordEncoder;

    public ClienteService(ClienteRepository clienteRepository, PasswordEncoder passwordEncoder) {
        this.clienteRepository = clienteRepository;
        this.passwordEncoder = passwordEncoder;
    }

    // ---------------------------------------------------------------
    // Registro (HU-01 y HU-Seg-01)
    // ---------------------------------------------------------------

    /**
     * Valida los datos, verifica duplicados, cifra la clave y guarda el cliente.
     * Lanza RegistroClienteException (INVALIDO o DUPLICADO) con la lista de mensajes.
     */
    public Cliente registrarCliente(Cliente cliente, String clavePlana) {

        // 1) Reglas de formato y obligatorios
        List<String> errores = new ArrayList<>();

        if (vacio(cliente.getNombres()))         errores.add("El nombre es obligatorio.");
        if (vacio(cliente.getApellidos()))       errores.add("El apellido es obligatorio.");
        if (vacio(cliente.getTipoDocumento()))   errores.add("El tipo de documento es obligatorio.");
        if (vacio(cliente.getNumeroDocumento())) errores.add("El número de documento es obligatorio.");

        if (vacio(cliente.getTelefono())) {
            errores.add("El celular es obligatorio.");
        } else if (!cliente.getTelefono().matches(REGEX_CELULAR)) {
            errores.add("El celular debe tener 10 dígitos y empezar por 3.");
        }

        if (vacio(cliente.getCorreo())) {
            errores.add("El correo es obligatorio.");
        } else if (!cliente.getCorreo().matches(REGEX_CORREO)) {
            errores.add("El correo electrónico no es válido.");
        }

        if (vacio(clavePlana)) {
            errores.add("La contraseña es obligatoria.");
        } else {
            // Las 5 reglas SOLO si hay clave (evita NullPointerException)
            if (clavePlana.length() < 8)                 errores.add("La contraseña debe tener al menos 8 caracteres.");
            if (!clavePlana.matches(".*[A-Z].*"))        errores.add("La contraseña debe incluir al menos una letra mayúscula.");
            if (!clavePlana.matches(".*[a-z].*"))        errores.add("La contraseña debe incluir al menos una letra minúscula.");
            if (!clavePlana.matches(".*\\d.*"))          errores.add("La contraseña debe incluir al menos un número.");
            if (!clavePlana.matches(".*[^A-Za-z0-9].*")) errores.add("La contraseña debe incluir al menos un carácter especial.");
        }

        if (!errores.isEmpty()) {
            throw new RegistroClienteException(RegistroClienteException.Tipo.INVALIDO, errores);
        }

        // 2) Duplicados (solo si los datos ya son válidos)
        List<String> duplicados = new ArrayList<>();
        if (clienteRepository.existsByCorreo(cliente.getCorreo())) {
            duplicados.add("Este correo ya está registrado.");
        }
        if (clienteRepository.existsByNumeroDocumento(cliente.getNumeroDocumento())) {
            duplicados.add("Este número de documento ya está registrado.");
        }
        if (!duplicados.isEmpty()) {
            throw new RegistroClienteException(RegistroClienteException.Tipo.DUPLICADO, duplicados);
        }

        // 3) Guardar con la clave cifrada (HU-Seg-01)
        cliente.setClave(passwordEncoder.encode(clavePlana));
        return clienteRepository.save(cliente);
    }

    private static boolean vacio(String valor) {
        return valor == null || valor.isBlank();
    }

    // ---------------------------------------------------------------
    // Contrato ConsultaClientes (lo usa Oscar en O3)
    // ---------------------------------------------------------------

    @Override
    public Optional<ClienteResumen> obtenerCliente(String clienteId) {
        return clienteRepository.findById(clienteId)
                .map(c -> new ClienteResumen(c.getApellidos(), c.getId(), c.getNombres()));
    }

    // ---------------------------------------------------------------
    // Contrato CuentaAutenticable (lo usa el login en A2)
    // ---------------------------------------------------------------

    @Override
    public String rol() {
        return "CLIENTE";
    }

    @Override
    public Optional<CuentaResumen> buscarPorCorreo(String correo) {
        return clienteRepository.findByCorreo(correo)
                .map(c -> new CuentaResumen(c.getId(), c.getNombres(), c.getCorreo(), rol()));
    }

    @Override
    public boolean validarCredenciales(String correo, String clave) {
        return clienteRepository.findByCorreo(correo)
                .map(c -> passwordEncoder.matches(clave, c.getClave()))
                .orElse(false);
    }

    @Override
    public boolean estaBloqueada(String correo) {
        return clienteRepository.findByCorreo(correo)
                .map(c -> c.getBloqueadoHasta() != null && c.getBloqueadoHasta().isAfter(LocalDateTime.now()))
                .orElse(false);
    }

    @Override
    public void registrarIntentoFallido(String correo) {
        clienteRepository.findByCorreo(correo).ifPresent(c -> {
            int intentos = c.getIntentosFallidos() + 1;
            c.setIntentosFallidos(intentos);
            if (intentos >= MAX_INTENTOS) {
                c.setBloqueadoHasta(LocalDateTime.now().plusMinutes(MINUTOS_BLOQUEO));
            }
            clienteRepository.save(c);
        });
    }

    @Override
    public void registrarIntentoExitoso(String correo) {
        clienteRepository.findByCorreo(correo).ifPresent(c -> {
            c.setIntentosFallidos(0);
            c.setBloqueadoHasta(null);
            clienteRepository.save(c);
        });
    }
}