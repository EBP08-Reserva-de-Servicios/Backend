package com.reservas.proveedores.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Mantiene el calendario con 4 semanas de bloques hacia adelante (HU-32, observación de Arelis).
 * - Al arrancar: en el plan gratis de Render el servicio duerme y arranca de nuevo al despertar,
 *   así que esto garantiza la extensión aunque a la 1:00 a. m. el servidor esté dormido.
 * - Cada día a la 1:00 a. m. (hora de Colombia), si el servidor está despierto.
 * Un error aquí no debe tumbar la aplicación: se registra en el log y se sigue.
 */
@Component
public class ExtensionDisponibilidadTarea {

    private static final Logger log = LoggerFactory.getLogger(ExtensionDisponibilidadTarea.class);

    private final HorarioService horarioService;

    public ExtensionDisponibilidadTarea(HorarioService horarioService) {
        this.horarioService = horarioService;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void alArrancar() {
        ejecutar("al arrancar");
    }

    @Scheduled(cron = "0 0 1 * * *", zone = "America/Bogota")
    public void cadaDia() {
        ejecutar("diaria");
    }

    private void ejecutar(String motivo) {
        try {
            int bloques = horarioService.extenderDisponibilidad();
            log.info("Extensión de disponibilidad ({}): {} bloques generados", motivo, bloques);
        } catch (RuntimeException e) {
            log.error("Extensión de disponibilidad ({}) falló: {}", motivo, e.getMessage(), e);
        }
    }
}
