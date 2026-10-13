package com.reservas.proveedores.api;

import java.util.List;

/**
 * Pregunta "hacia atrás" de proveedores a agendamiento (inversión de dependencias):
 * proveedores declara la interfaz y agendamiento la implementa, así proveedores nunca importa agendamiento.
 */
public interface GeneradorDisponibilidad {

    /**
     * Borra los bloques DISPONIBLES futuros del profesional y los vuelve a crear desde su horario,
     * para cada oferta que atiende. Los bloques ya reservados no se tocan.
     * Devuelve cuántos bloques nuevos se crearon.
     */
    int regenerar(String profesionalId, List<Long> servicioProveedorIds, List<FranjaHorarioResumen> franjas);
}
