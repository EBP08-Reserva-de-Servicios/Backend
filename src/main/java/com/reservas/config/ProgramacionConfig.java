package com.reservas.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

/** Enciende las tareas programadas (@Scheduled) de la aplicación. */
@Configuration
@EnableScheduling
public class ProgramacionConfig {
}
