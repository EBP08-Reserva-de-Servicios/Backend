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
import java.util.Optional;

@Service
public class ClienteService implements ConsultaClientes, CuentaAutenticable {

    private static final int MAX_INTENTOS = 3;
    private static final int MINUTOS_BLOQUEO = 15;

    private final ClienteRepository clienteRepository;
    private final PasswordEncoder passwordEncoder;

    public ClienteService(ClienteRepository clienteRepository, PasswordEncoder passwordEncoder) {
        this.clienteRepository = clienteRepository;
        this.passwordEncoder = passwordEncoder;
    }

    // --- NUEVO: Método que necesita el Controller para verificar duplicados ---
    public Optional<Cliente> findByCorreo(String correo) {
        return clienteRepository.findByCorreo(correo);
    }

    // --- Contrato de ConsultaClientes ---
    @Override
    public Optional<ClienteResumen> obtenerCliente(String clienteId) {
        return clienteRepository.findById(clienteId)
                .map(c -> new ClienteResumen(c.getApellidos(), c.getId(), c.getNombres()));
    }

    // --- Contrato de CuentaAutenticable ---
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

    // --- Lógica de Registro (HU-01 y HU-Seg-01) ---
    public Cliente registrarCliente(Cliente cliente, String clavePlana) {
        cliente.setClave(passwordEncoder.encode(clavePlana));
        return clienteRepository.save(cliente);
    }
}